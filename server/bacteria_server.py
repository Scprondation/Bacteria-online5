#!/usr/bin/env python3
"""TCP server for Bacteria Online 5.

The Android client sends one JSON object per line to TCP port 5055 and expects
one JSON object per line in response.  This implementation intentionally uses
only the Python standard library, so it can be started on a clean VPS:

    python3 bacteria_server.py --host 0.0.0.0 --port 5055

Persistent game data is stored atomically in ``server-data/state.json``.  Do
not expose the TCP port without a firewall and TLS/VPN in production.
"""

from __future__ import annotations

import argparse
import asyncio
import base64
import hashlib
import hmac
import html
import json
import logging
import os
import secrets
import signal
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
from collections import deque
from dataclasses import asdict, dataclass, field
from pathlib import Path
from random import Random
from typing import Any


WORLD_SIZE = 10_000
WORLD_TILES = 100
WORLD_TILE_SIZE = 100
WORLD_SEED = 50_505
STATE_INTERVAL_SECONDS = 0.25
PLAYER_TIMEOUT_SECONDS = 20
MAX_MESSAGE_BYTES = 16 * 1024
MAX_NAME_LENGTH = 16
MAX_CHAT_LENGTH = 180
DAILY_REWARD = 150
GENE_MAX_LEVEL = 10
GENE_COUNT = 4
CASE_COSTS = (2_000, 5_000, 10_000, 50_000, 100_000)
TOTAL_SKINS = 56
MAX_MARKET_LISTINGS = 250
RENAME_COST = 100_000
MAX_AVATAR_BYTES = 120_000
DONATION_PACKS = (
    {"id": "coins_2000", "title": "2 000 gene coins", "rub": "50.00", "coins": 2_000},
    {"id": "coins_5000", "title": "5 000 gene coins", "rub": "90.00", "coins": 5_000},
    {"id": "coins_10000", "title": "10 000 gene coins", "rub": "175.00", "coins": 10_000},
    {"id": "coins_50000", "title": "50 000 gene coins", "rub": "875.00", "coins": 50_000},
    {"id": "coins_100000", "title": "100 000 gene coins", "rub": "1750.00", "coins": 100_000},
)


def now_ms() -> int:
    return time.time_ns() // 1_000_000


def clamp(value: float, minimum: float, maximum: float) -> float:
    return max(minimum, min(maximum, value))


def integer(value: Any, default: int = 0) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def number(value: Any, default: float = 0.0) -> float:
    try:
        return float(value)
    except (TypeError, ValueError):
        return default


def clean_text(value: Any, limit: int, fallback: str = "") -> str:
    if not isinstance(value, str):
        return fallback
    # Control characters break line-delimited protocols and logs.
    value = "".join(char for char in value.strip() if char >= " " and char != "\x7f")
    return value[:limit] or fallback


def password_digest(password: str, salt: str) -> str:
    """Return a slow, salted verifier without storing the raw password."""
    return hashlib.scrypt(password.encode("utf-8"), salt=bytes.fromhex(salt), n=2**14, r=8, p=1).hex()


@dataclass
class Account:
    id: str
    name: str
    password_salt: str
    password_hash: str
    level: int = 1
    dna: int = 0
    currency: int = 0
    xp: int = 0
    level_seed: int = WORLD_SEED
    genes: list[int] = field(default_factory=lambda: [0] * GENE_COUNT)
    skins: list[int] = field(default_factory=lambda: [1, 0, 0, 0, 0, 0])
    selected_skin: int = 0
    bk_pass: str = "free"
    pass_season: int = 1
    pro_pass_level: int = 1
    free_rewards: str = ""
    pro_rewards: str = ""
    showcase: list[int] = field(default_factory=lambda: [0] * TOTAL_SKINS)
    inventory: list[dict[str, int]] = field(default_factory=lambda: [{"kind": 1, "value": 0}])
    friends: list[str] = field(default_factory=list)
    friend_requests: list[str] = field(default_factory=list)
    last_daily_day: str = ""
    avatar: str = ""

    @classmethod
    def from_dict(cls, value: dict[str, Any]) -> "Account":
        account = cls(
            id=clean_text(value.get("id"), 64, secrets.token_hex(4)),
            name=clean_text(value.get("name"), MAX_NAME_LENGTH, "player"),
            password_salt=clean_text(value.get("password_salt"), 64),
            password_hash=clean_text(value.get("password_hash"), 256),
        )
        for key in ("level", "dna", "currency", "xp", "level_seed", "selected_skin"):
            setattr(account, key, max(0, integer(value.get(key), getattr(account, key))))
        account.bk_pass = "pro" if value.get("bk_pass") == "pro" else "free"
        account.pass_season = max(1, integer(value.get("pass_season"), 1))
        account.pro_pass_level = int(clamp(integer(value.get("pro_pass_level"), 1), 1, 50))
        account.free_rewards = clean_text(value.get("free_rewards"), 128)
        account.pro_rewards = clean_text(value.get("pro_rewards"), 128)
        account.genes = [int(clamp(integer(item), 0, GENE_MAX_LEVEL)) for item in value.get("genes", account.genes)[:GENE_COUNT]]
        account.genes += [0] * (GENE_COUNT - len(account.genes))
        account.skins = [1 if integer(item) else 0 for item in value.get("skins", account.skins)[:TOTAL_SKINS]]
        account.skins += [0] * (max(6, TOTAL_SKINS) - len(account.skins))
        account.skins[0] = 1
        account.showcase = [1 if integer(item) else 0 for item in value.get("showcase", account.showcase)[:TOTAL_SKINS]]
        account.showcase += [0] * (TOTAL_SKINS - len(account.showcase))
        inventory = value.get("inventory", [])
        account.inventory = [
            {"kind": integer(item.get("kind")), "value": max(0, integer(item.get("value")))}
            for item in inventory if isinstance(item, dict)
        ][:100]
        account.friends = [clean_text(item, 64) for item in value.get("friends", []) if clean_text(item, 64)][:40]
        account.friend_requests = [clean_text(item, 64) for item in value.get("friend_requests", []) if clean_text(item, 64)][:30]
        account.last_daily_day = clean_text(value.get("last_daily_day"), 16)
        account.avatar = clean_text(value.get("avatar"), MAX_AVATAR_BYTES * 2)
        return account


@dataclass
class Player:
    id: str
    name: str
    x: float
    y: float
    dna: int = 0
    level: int = 1
    hp: float = 100.0
    skin: int = 0
    avatar: str = ""
    last_seen: int = field(default_factory=now_ms)


@dataclass
class Session:
    connection_id: str
    reader: asyncio.StreamReader
    writer: asyncio.StreamWriter
    player: Player
    account: Account | None = None
    write_lock: asyncio.Lock = field(default_factory=asyncio.Lock)


class GameServer:
    def __init__(self, data_file: Path) -> None:
        self.data_file = data_file
        self.lock = asyncio.Lock()
        self.accounts_by_id: dict[str, Account] = {}
        self.account_id_by_name: dict[str, str] = {}
        self.sessions: dict[str, Session] = {}
        self.market: list[dict[str, Any]] = []
        self.market_last_sale_prices: dict[str, int] = {}
        self.next_market_id = 1
        self.donations: dict[str, dict[str, Any]] = {}
        self.chat: deque[dict[str, Any]] = deque(maxlen=40)
        self.chat_sequence = 0
        self.random = Random()
        self.news_text = "Добро пожаловать в Бактерии Онлайн 5"
        self.news_date = time.strftime("%Y-%m-%d")
        self.version_code = 8
        self.version_name = "0.1.9"
        self.apk_url = "https://bakteria5.ru/app-debug.apk"
        self.seed = WORLD_SEED
        self.pickups = self._make_pickups()
        self.boss_active = False
        self.boss_spawn_id = 0
        self.boss_dna = 2_000
        self.boss_x = 5_000.0
        self.boss_y = 5_000.0
        self.next_boss_at = now_ms() + 30 * 60 * 1000
        self._dirty = False

    def _make_pickups(self) -> list[dict[str, Any]]:
        return [
            {"id": f"big-{self.seed}-{index}", "x": self.random.uniform(500, 9500), "y": self.random.uniform(500, 9500),
             "value": 500, "radius": 72.0, "zone": 280.0, "kind": "big"}
            for index in range(1, 3)
        ]

    def load(self) -> None:
        if not self.data_file.exists():
            self.import_legacy_tsv()
            if self._dirty:
                self.save()
            return
        try:
            payload = json.loads(self.data_file.read_text(encoding="utf-8"))
            accounts = payload.get("accounts", [])
            for raw in accounts:
                if not isinstance(raw, dict):
                    continue
                account = Account.from_dict(raw)
                self.accounts_by_id[account.id] = account
                self.account_id_by_name[account.name.casefold()] = account.id
            self.market = [item for item in payload.get("market", []) if isinstance(item, dict)][-MAX_MARKET_LISTINGS:]
            self.market_last_sale_prices = {str(key): max(100, integer(price, 100)) for key, price in payload.get("market_last_sale_prices", {}).items()}
            self.next_market_id = max(1, integer(payload.get("next_market_id"), 1))
            self.donations = {str(key): value for key, value in payload.get("donations", {}).items() if isinstance(value, dict)}
            logging.info("Loaded %d accounts, %d market entries and %d donations", len(self.accounts_by_id), len(self.market), len(self.donations))
        except (OSError, json.JSONDecodeError, ValueError) as error:
            logging.error("Cannot load %s: %s", self.data_file, error)

    def save(self) -> None:
        if not self._dirty:
            return
        self.data_file.parent.mkdir(parents=True, exist_ok=True)
        payload = {"schema": 2, "accounts": [asdict(account) for account in self.accounts_by_id.values()],
                   "market": self.market, "market_last_sale_prices": self.market_last_sale_prices,
                   "next_market_id": self.next_market_id, "donations": self.donations}
        temporary = self.data_file.with_suffix(".tmp")
        temporary.write_text(json.dumps(payload, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
        temporary.replace(self.data_file)
        self._dirty = False

    def import_legacy_tsv(self) -> None:
        """One-time non-destructive import of the Java server data files."""
        directory = self.data_file.parent
        accounts_file, market_file, donations_file = (directory / "accounts.tsv", directory / "market.tsv", directory / "donations.tsv")
        if not any(path.is_file() for path in (accounts_file, market_file, donations_file)):
            return
        if accounts_file.is_file():
            for line in accounts_file.read_text(encoding="utf-8").splitlines():
                parts = line.split("\t")
                if len(parts) < 10:
                    continue
                name = clean_text(parts[1], MAX_NAME_LENGTH, "player")
                salt = secrets.token_hex(16)
                account = Account(id=clean_text(parts[0], 64, secrets.token_hex(4)), name=name, password_salt=salt,
                                  password_hash=password_digest(parts[2], salt), level=max(1, integer(parts[3], 1)),
                                  dna=max(0, integer(parts[4])), level_seed=integer(parts[5], WORLD_SEED),
                                  bk_pass="pro" if len(parts) > 7 and parts[7] == "pro" else "free",
                                  last_daily_day=parts[8] if len(parts) > 8 else "", currency=max(0, integer(parts[9])),
                                  xp=max(0, integer(parts[10])) if len(parts) > 10 else 0)
                account.genes = self._legacy_int_list(parts[11] if len(parts) > 11 else "", GENE_COUNT, GENE_MAX_LEVEL)
                account.skins = self._legacy_int_list(parts[12] if len(parts) > 12 else "", TOTAL_SKINS, 1)
                account.skins[0] = 1
                account.selected_skin = int(clamp(integer(parts[13] if len(parts) > 13 else 0), 0, TOTAL_SKINS - 1))
                account.free_rewards = clean_text(parts[14] if len(parts) > 14 else "", 128)
                account.pro_rewards = clean_text(parts[15] if len(parts) > 15 else "", 128)
                account.pro_pass_level = int(clamp(integer(parts[16] if len(parts) > 16 else 1), 1, 50))
                account.pass_season = max(1, integer(parts[17] if len(parts) > 17 else 1))
                account.showcase = self._legacy_int_list(parts[18] if len(parts) > 18 else "", TOTAL_SKINS, 1)
                account.inventory = self._legacy_inventory(parts[19] if len(parts) > 19 else "1_0")
                account.friends = self._legacy_tokens(parts[20] if len(parts) > 20 else "", 40)
                account.friend_requests = self._legacy_tokens(parts[21] if len(parts) > 21 else "", 30)
                self.accounts_by_id[account.id] = account
                self.account_id_by_name[name.casefold()] = account.id
        if market_file.is_file():
            for line in market_file.read_text(encoding="utf-8").splitlines():
                parts = line.split("\t")
                if len(parts) >= 6 and parts[0] == "L":
                    listing_id = max(1, integer(parts[1], 1))
                    self.market.append({"id": listing_id, "skin": max(0, integer(parts[2])), "price": max(100, integer(parts[3], 100)),
                                        "ownerId": clean_text(parts[4], 64), "ownerName": clean_text(parts[5], MAX_NAME_LENGTH, "player")})
                    self.next_market_id = max(self.next_market_id, listing_id + 1)
                elif len(parts) >= 3 and parts[0] == "S":
                    self.market_last_sale_prices[str(max(0, integer(parts[1])))] = max(100, integer(parts[2], 100))
        if donations_file.is_file():
            for line in donations_file.read_text(encoding="utf-8").splitlines():
                parts = line.split("\t")
                if len(parts) >= 7 and clean_text(parts[0], 80):
                    self.donations[parts[0]] = {"orderId": parts[0], "paymentId": parts[1], "accountId": parts[2], "packId": parts[3],
                                                "coins": max(0, integer(parts[4])), "rub": parts[5], "credited": parts[6] == "1"}
        self.market = self.market[:MAX_MARKET_LISTINGS]
        self._dirty = True
        logging.info("Imported Java TSV: %d accounts, %d market entries, %d donations", len(self.accounts_by_id), len(self.market), len(self.donations))

    @staticmethod
    def _legacy_int_list(value: str, size: int, maximum: int) -> list[int]:
        result = [int(clamp(integer(part), 0, maximum)) for part in value.split(",")[:size] if part != ""]
        return (result + [0] * size)[:size]

    @staticmethod
    def _legacy_tokens(value: str, limit: int) -> list[str]:
        return [clean_text(item, 64) for item in value.split(",") if clean_text(item, 64)][:limit]

    @staticmethod
    def _legacy_inventory(value: str) -> list[dict[str, int]]:
        result: list[dict[str, int]] = []
        for item in value.split(";"):
            kind, separator, raw_value = item.partition("_")
            if separator:
                result.append({"kind": max(0, integer(kind)), "value": max(0, integer(raw_value))})
        return result[:100] or [{"kind": 1, "value": 0}]

    async def send(self, session: Session, message: dict[str, Any]) -> bool:
        if session.writer.is_closing():
            return False
        try:
            encoded = json.dumps(message, ensure_ascii=False, separators=(",", ":")) + "\n"
            async with session.write_lock:
                session.writer.write(encoded.encode("utf-8"))
                await session.writer.drain()
            return True
        except (ConnectionError, OSError):
            return False

    def profile_message(self, kind: str, account: Account) -> dict[str, Any]:
        # Strings retain the format expected by the existing Java client.
        return {"type": kind, "id": account.id, "name": account.name, "level": account.level,
                "dna": account.dna, "currency": account.currency, "levelSeed": account.level_seed,
                "servers": "official,community", "bkPass": account.bk_pass, "xp": account.xp,
                "genes": ",".join(map(str, account.genes)), "skins": ",".join(map(str, account.skins)),
                "selectedSkin": account.selected_skin, "freeRewards": account.free_rewards, "proRewards": account.pro_rewards,
                "proPassLevel": account.pro_pass_level, "passSeason": account.pass_season,
                "showcase": ",".join(map(str, account.showcase)),
                # Android's inventory parser uses ';' between items.
                "inventory": ";".join(f"{item['kind']}_{item['value']}" for item in account.inventory),
                "friends": ",".join(account.friends), "friendRequests": ",".join(account.friend_requests), "avatar": account.avatar}

    def market_message(self) -> dict[str, Any]:
        listings = [{**listing, "owner": listing.get("owner", listing.get("ownerName", "player"))} for listing in self.market]
        last_sales = [{"skin": integer(skin), "price": price} for skin, price in self.market_last_sale_prices.items()]
        return {"type": "market", "listings": listings, "lastSales": last_sales}

    async def broadcast(self, message: dict[str, Any]) -> None:
        await asyncio.gather(*(self.send(session, message) for session in list(self.sessions.values())), return_exceptions=True)

    async def save_loop(self) -> None:
        while True:
            await asyncio.sleep(5)
            async with self.lock:
                self.save()

    async def state_loop(self) -> None:
        while True:
            await asyncio.sleep(STATE_INTERVAL_SECONDS)
            async with self.lock:
                current = now_ms()
                if not self.boss_active and current >= self.next_boss_at:
                    self.boss_active = True
                    self.boss_spawn_id += 1
                    self.boss_dna = 2_000
                    self.boss_x, self.boss_y = self.random.uniform(1000, 9000), self.random.uniform(1000, 9000)
                timed_out = [key for key, session in self.sessions.items() if current - session.player.last_seen > PLAYER_TIMEOUT_SECONDS * 1000]
                for key in timed_out:
                    session = self.sessions.pop(key)
                    session.writer.close()
                state = self.state_message(current)
            await self.broadcast(state)

    def state_message(self, current: int) -> dict[str, Any]:
        players = sorted((session.player for session in self.sessions.values()), key=lambda player: (-player.dna, player.name.casefold()))
        # The game leaderboard is a live room list. Persistent accounts must
        # never appear here while offline.
        leaderboard = sorted(players, key=lambda player: (-player.level, -player.dna, player.name.casefold()))[:100]
        menu_leaderboard = sorted(self.accounts_by_id.values(), key=lambda account: (-account.level, account.name.casefold()))[:100]
        return {"type": "state", "newsText": self.news_text, "newsDate": self.news_date, "seed": self.seed,
                "versionCode": self.version_code, "versionName": self.version_name, "apkUrl": self.apk_url,
                "players": [{"id": p.id, "name": p.name, "x": round(p.x, 2), "y": round(p.y, 2), "r": 32.5,
                             "hp": round(p.hp, 2), "dna": p.dna, "level": p.level, "skin": p.skin, "avatar": p.avatar} for p in players],
                # Keep the historic totalDna key for the Android parser, but
                # its value is only DNA collected in the current live session.
                "leaderboard": [{"id": p.id, "name": p.name, "totalDna": p.dna, "level": p.level} for p in leaderboard],
                "menuLeaderboard": [{"id": a.id, "name": a.name, "level": a.level} for a in menu_leaderboard],
                "dna": self.pickups, "bigDna": None, "chatSeq": self.chat_sequence, "chat": list(self.chat),
                "bossActive": self.boss_active, "bossSpawnId": self.boss_spawn_id, "bossDna": self.boss_dna,
                "bossX": self.boss_x, "bossY": self.boss_y,
                "bossCooldownMs": 0 if self.boss_active else max(0, self.next_boss_at - current)}

    def authenticate(self, name: Any, password: Any) -> tuple[Account | None, str | None]:
        safe_name = clean_text(name, MAX_NAME_LENGTH, "player")
        password = password if isinstance(password, str) else ""
        key = safe_name.casefold()
        account_id = self.account_id_by_name.get(key)
        if account_id:
            account = self.accounts_by_id[account_id]
            # Old/no-password accounts remain usable once, then get a verifier.
            if account.password_hash and not hmac.compare_digest(account.password_hash, password_digest(password, account.password_salt)):
                return None, "wrong password"
            if not account.password_hash:
                account.password_salt = secrets.token_hex(16)
                account.password_hash = password_digest(password, account.password_salt)
                self._dirty = True
            return account, None
        salt = secrets.token_hex(16)
        account = Account(id=secrets.token_hex(4), name=safe_name, password_salt=salt, password_hash=password_digest(password, salt))
        self.accounts_by_id[account.id] = account
        self.account_id_by_name[key] = account.id
        self._dirty = True
        return account, None

    def grant_xp(self, account: Account, amount: int) -> None:
        if amount <= 0:
            return
        account.dna += amount
        account.currency += amount
        account.xp += amount
        while account.xp >= max(50, account.level * 50):
            account.xp -= max(50, account.level * 50)
            account.level += 1
            account.currency += account.level * 50

    async def handle(self, session: Session, message: dict[str, Any]) -> None:
        kind = message.get("type")
        if not isinstance(kind, str):
            await self.send(session, {"type": "error", "message": "field type is required"})
            return
        session.player.last_seen = now_ms()
        if kind == "auth":
            account, error = self.authenticate(message.get("name"), message.get("password"))
            if error:
                await self.send(session, {"type": "authError", "message": error})
                return
            assert account
            session.account = account
            self.sessions.pop(session.connection_id, None)
            self.sessions[account.id] = session
            session.player.id, session.player.name = account.id, account.name
            session.player.level = account.level
            session.player.skin = account.selected_skin
            session.player.avatar = account.avatar
            await self.send(session, self.profile_message("profile", account))
            await self.send(session, self.market_message())
        elif kind == "join":
            session.player.name = clean_text(message.get("name"), MAX_NAME_LENGTH, session.player.name)
            await self.send(session, {"type": "joined", "id": session.player.id, "name": session.player.name})
        elif kind == "move":
            session.player.x = clamp(number(message.get("x"), session.player.x), 0, WORLD_SIZE)
            session.player.y = clamp(number(message.get("y"), session.player.y), 0, WORLD_SIZE)
            session.player.dna = max(0, integer(message.get("dna"), session.player.dna))
            session.player.hp = min(session.player.hp, clamp(number(message.get("hp"), session.player.hp), 0, 100))
        elif kind == "ping":
            await self.send(session, {"type": "pong", "time": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())})
        elif kind == "chat":
            text = clean_text(message.get("text"), MAX_CHAT_LENGTH)
            if text:
                self.chat_sequence += 1
                chat = {"type": "chat", "seq": self.chat_sequence, "id": session.player.id, "name": session.player.name, "text": text}
                self.chat.append({key: value for key, value in chat.items() if key != "type"})
                await self.broadcast(chat)
        elif kind == "daily":
            account = self.require_account(session)
            if not account:
                return
            today = time.strftime("%Y-%m-%d")
            ok = account.last_daily_day != today
            if ok:
                account.last_daily_day, account.currency = today, account.currency + DAILY_REWARD
                self._dirty = True
            await self.send(session, {"type": "daily", "ok": ok, "reward": DAILY_REWARD if ok else 0, "currency": account.currency,
                                      **({} if ok else {"reason": "claimed"})})
            await self.send(session, self.profile_message("profileSaved", account))
        elif kind == "profileUpdate":
            account = self.require_account(session)
            if account:
                account.level_seed = integer(message.get("levelSeed"), self.seed)
                self._dirty = True
                await self.send(session, self.profile_message("profileSaved", account))
        elif kind == "profileGet":
            account = self.require_account(session)
            if account:
                # This is deliberately a fresh server-side snapshot, rather
                # than data echoed back from the device cache.
                await self.send(session, self.profile_message("profile", account))
            else:
                await self.send(session, {"type": "error", "message": "authenticate first"})
        elif kind == "accountAction":
            await self.account_action(session, message)
        elif kind == "marketList":
            await self.send(session, self.market_message())
        elif kind == "marketSell":
            await self.market_sell(session, message)
        elif kind == "marketBuy":
            await self.market_buy(session, message)
        elif kind == "eat":
            await self.eat(session, message)
        elif kind == "bigDnaTick":
            # The client sends this while it is inside a large DNA zone.  The
            # authoritative reward is granted only by ``eat`` after a distance
            # check, so this packet merely keeps the session alive.
            pass
        elif kind in ("bossDamage", "bossDefeated", "bossCommand"):
            await self.boss(session, message)
        elif kind == "hit":
            await self.hit(session, message)
        elif kind == "news":
            self.news_text = clean_text(message.get("text"), MAX_CHAT_LENGTH, self.news_text)
            self.news_date = time.strftime("%Y-%m-%d")
            await self.send(session, {"type": "news", "text": self.news_text, "date": self.news_date})
        else:
            await self.send(session, {"type": "error", "message": f"unsupported type: {kind}"})

    def require_account(self, session: Session) -> Account | None:
        return session.account

    async def account_action(self, session: Session, message: dict[str, Any]) -> None:
        account = self.require_account(session)
        if not account:
            await self.send(session, {"type": "accountResult", "ok": False, "message": "authenticate first"})
            return
        action = clean_text(message.get("action"), 32)
        ok, result = False, "unknown action"
        if action == "localDnaEat":
            amount = int(clamp(integer(message.get("amount")), 0, 10_000))
            if amount:
                self.grant_xp(account, amount); ok, result = True, "progress synced"
            else: result = "empty gain"
        elif action == "buyProPass":
            cost = 50_000
            if account.bk_pass == "pro":
                ok, result = True, "pro already active"
            elif account.currency < cost:
                result = "not enough currency"
            else:
                account.currency -= cost
                account.bk_pass = "pro"
                account.pro_pass_level = 1
                ok, result = True, "pro pass bought"
        elif action == "rename":
            new_name = clean_text(message.get("name"), MAX_NAME_LENGTH)
            key = new_name.casefold()
            if not new_name:
                result = "name is empty"
            elif key in self.account_id_by_name and self.account_id_by_name[key] != account.id:
                result = "name already used"
            elif account.currency < RENAME_COST:
                result = "not enough currency"
            else:
                self.account_id_by_name.pop(account.name.casefold(), None)
                self.account_id_by_name[key] = account.id
                account.name = new_name; account.currency -= RENAME_COST
                session.player.name = new_name
                ok, result = True, "name changed"
        elif action == "setAvatar":
            avatar = message.get("avatar")
            if not isinstance(avatar, str) or len(avatar) > MAX_AVATAR_BYTES * 2:
                result = "invalid avatar"
            elif avatar and not avatar.startswith("data:image/"):
                result = "invalid avatar"
            else:
                account.avatar = avatar
                ok, result = True, "avatar saved"
        elif action == "upgradeGene":
            gene = integer(message.get("gene"), -1)
            if 0 <= gene < GENE_COUNT:
                cost = (account.genes[gene] + 1) * 50
                if account.genes[gene] >= GENE_MAX_LEVEL: result = "gene max"
                elif account.currency < cost: result = "not enough currency"
                else:
                    account.currency -= cost; account.genes[gene] += 1; ok, result = True, "gene upgraded"
        elif action in ("buySkin", "equipSkin"):
            skin = integer(message.get("skin"), -1)
            if not 0 <= skin < TOTAL_SKINS: result = "skin unavailable"
            elif skin < len(account.skins) and account.skins[skin]: account.selected_skin, ok, result = skin, True, "skin equipped"
            elif action == "equipSkin": result = "skin not owned"
            else:
                cost = 500 + skin * 250
                if account.currency < cost: result = "not enough currency"
                else:
                    account.currency -= cost
                    account.skins += [0] * (skin + 1 - len(account.skins)); account.skins[skin] = 1
                    account.inventory.append({"kind": 1, "value": skin}); account.selected_skin = skin; ok, result = True, "skin bought"
        elif action == "buyCase":
            tier = integer(message.get("tier"), -1)
            if not 0 <= tier < len(CASE_COSTS): result = "case unavailable"
            elif account.currency < CASE_COSTS[tier]: result = "not enough currency"
            else:
                account.currency -= CASE_COSTS[tier]; account.inventory.append({"kind": 0, "value": tier}); ok, result = True, "case bought"
        elif action == "openCase":
            index = integer(message.get("index"), -1)
            if not 0 <= index < len(account.inventory) or account.inventory[index]["kind"] != 0: result = "case not found"
            else:
                tier = account.inventory[index]["value"]
                skin = self.random.randrange(1, TOTAL_SKINS)
                account.inventory[index] = {"kind": 1, "value": skin}
                account.skins += [0] * (skin + 1 - len(account.skins)); account.skins[skin] = 1
                ok, result = True, "case opened"
                await self.send(session, {"type": "caseOpened", "ok": True, "index": index, "tier": tier, "skin": skin})
        elif action == "toggleShowcase":
            skin = integer(message.get("skin"), -1)
            if not 0 <= skin < TOTAL_SKINS or skin >= len(account.skins) or not account.skins[skin]:
                result = "skin not owned"
            else:
                account.showcase[skin] = 0 if account.showcase[skin] else 1
                ok, result = True, "showcase updated"
        elif action == "friendRequest":
            target = self.accounts_by_id.get(clean_text(message.get("target"), 64))
            if target and target.id != account.id and account.id not in target.friend_requests:
                target.friend_requests.append(account.id); target.friend_requests = target.friend_requests[-30:]
                ok, result = True, "friend request sent"
            else: result = "player not found"
        elif action in ("friendAccept", "friendDecline"):
            target_id = clean_text(message.get("target"), 64)
            if target_id not in account.friend_requests: result = "request not found"
            else:
                account.friend_requests.remove(target_id)
                if action == "friendAccept":
                    account.friends = list(dict.fromkeys((account.friends + [target_id])))[-40:]
                    target = self.accounts_by_id.get(target_id)
                    if target: target.friends = list(dict.fromkeys((target.friends + [account.id])))[-40:]
                ok, result = True, "friend request updated"
        self._dirty |= ok
        session.player.name = account.name
        session.player.skin = account.selected_skin
        session.player.avatar = account.avatar
        await self.send(session, {"type": "accountResult", "ok": ok, "action": action, "message": result})
        await self.send(session, self.profile_message("profileSaved", account))

    async def market_sell(self, session: Session, message: dict[str, Any]) -> None:
        account = self.require_account(session)
        skin, price = integer(message.get("skin"), -1), int(clamp(integer(message.get("price")), 100, 999_999))
        item = next((item for item in (account.inventory if account else []) if item["kind"] == 1 and item["value"] == skin), None)
        if not account or skin <= 0 or not item:
            await self.send(session, {"type": "marketResult", "ok": False, "message": "skin not in inventory"}); return
        account.inventory.remove(item)
        listing = {"id": self.next_market_id, "skin": skin, "price": price, "ownerId": account.id, "ownerName": account.name}
        self.next_market_id += 1; self.market.insert(0, listing); del self.market[MAX_MARKET_LISTINGS:]; self._dirty = True
        await self.send(session, self.profile_message("profileSaved", account))
        await self.send(session, {"type": "marketResult", "ok": True, "message": "listed", "listingId": listing["id"]})
        await self.broadcast(self.market_message())

    async def market_buy(self, session: Session, message: dict[str, Any]) -> None:
        account = self.require_account(session); listing_id = integer(message.get("listingId"), -1)
        listing = next((item for item in self.market if integer(item.get("id")) == listing_id), None)
        if not account or not listing or listing["ownerId"] == account.id or account.currency < integer(listing.get("price")):
            await self.send(session, {"type": "marketResult", "ok": False, "message": "listing unavailable"}); return
        self.market.remove(listing); account.currency -= integer(listing["price"]); account.inventory.append({"kind": 1, "value": integer(listing["skin"])})
        skin = integer(listing["skin"])
        account.skins += [0] * max(0, skin + 1 - len(account.skins))
        account.skins[skin] = 1
        self.market_last_sale_prices[str(skin)] = integer(listing["price"])
        seller = self.accounts_by_id.get(str(listing["ownerId"]))
        if seller: seller.currency += integer(listing["price"])
        self._dirty = True
        await self.send(session, self.profile_message("profileSaved", account))
        await self.send(session, {"type": "marketResult", "ok": True, "message": "bought", "skin": listing["skin"], "price": listing["price"]})
        await self.broadcast(self.market_message())

    async def eat(self, session: Session, message: dict[str, Any]) -> None:
        pickup_id = clean_text(message.get("pickupId"), 64)
        pickup = next((item for item in self.pickups if item["id"] == pickup_id), None)
        if not pickup or (session.player.x - pickup["x"]) ** 2 + (session.player.y - pickup["y"]) ** 2 > 78 ** 2:
            return
        self.pickups.remove(pickup); self.pickups.extend(self._make_pickups()[:1])
        gain = integer(pickup["value"]); session.player.dna += gain
        if session.account:
            self.grant_xp(session.account, gain)
            self._dirty = True
        await self.send(session, {"type": "eatResult", "gain": gain, "currency": session.account.currency if session.account else 0})

    async def boss(self, session: Session, message: dict[str, Any]) -> None:
        kind = message["type"]
        if kind == "bossCommand":
            self.boss_active, self.boss_spawn_id, self.boss_dna = True, self.boss_spawn_id + 1, 2_000
        elif self.boss_active and integer(message.get("spawnId"), -1) == self.boss_spawn_id:
            self.boss_dna = min(self.boss_dna, max(0, integer(message.get("dna"), self.boss_dna)))
            if kind == "bossDefeated" or self.boss_dna <= 100:
                self.boss_active, self.next_boss_at = False, now_ms() + 30 * 60 * 1000
        await self.broadcast({"type": "boss", "message": "boss updated"})

    async def hit(self, session: Session, message: dict[str, Any]) -> None:
        target = self.sessions.get(clean_text(message.get("targetId"), 64))
        if not target or target is session: return
        if (session.player.x - target.player.x) ** 2 + (session.player.y - target.player.y) ** 2 > 260 ** 2: return
        damage = clamp(1.5 + max(0, session.player.dna - target.player.dna) * .015 + session.player.level * .1, 1.5, 15)
        target.player.hp -= damage
        if target.player.hp <= 0:
            stolen = round(target.player.dna * .7); session.player.dna += stolen; target.player.dna, target.player.hp = 0, 100
            await self.send(session, {"type": "kill", "targetId": target.player.id, "dna": session.player.dna})
            await self.send(target, {"type": "death", "killerId": session.player.id, "killerName": session.player.name})

    @staticmethod
    def yookassa_configured() -> bool:
        return bool(os.getenv("YOOKASSA_SHOP_ID", "").strip() and os.getenv("YOOKASSA_SECRET_KEY", "").strip())

    @staticmethod
    def public_http_url() -> str:
        return os.getenv("PUBLIC_HTTP_URL", "https://pay.bakteria5.ru").strip().rstrip("/")

    @staticmethod
    def _yookassa_request(url: str, method: str, body: dict[str, Any] | None = None, idempotence_key: str = "") -> dict[str, Any]:
        credentials = f"{os.environ['YOOKASSA_SHOP_ID'].strip()}:{os.environ['YOOKASSA_SECRET_KEY'].strip()}".encode("utf-8")
        headers = {"Authorization": "Basic " + base64.b64encode(credentials).decode("ascii"), "Accept": "application/json"}
        data = None
        if body is not None:
            data = json.dumps(body, ensure_ascii=False).encode("utf-8")
            headers["Content-Type"] = "application/json"
        if idempotence_key:
            headers["Idempotence-Key"] = idempotence_key
        request = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(request, timeout=20) as response:
                return json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            details = error.read().decode("utf-8", "replace")[:300]
            raise RuntimeError(f"YooKassa HTTP {error.code}: {details}") from error

    async def create_donation(self, account_id: str, pack_index: int) -> tuple[str, str]:
        async with self.lock:
            account = self.accounts_by_id.get(account_id)
            if not account:
                raise LookupError("Account not found")
            account_name = account.name
        if not self.yookassa_configured():
            raise ValueError("YooKassa is not configured")
        pack = DONATION_PACKS[int(clamp(pack_index, 0, len(DONATION_PACKS) - 1))]
        order_id = str(uuid.uuid4())
        return_url = self.public_http_url() + "/donate/return?orderId=" + urllib.parse.quote(order_id, safe="")
        payload = {"amount": {"value": pack["rub"], "currency": "RUB"}, "capture": True,
                   "confirmation": {"type": "redirect", "return_url": return_url},
                   "description": "Bacteria Online 5 donation " + pack["title"],
                   "metadata": {"order_id": order_id, "account_id": account_id, "account_name": account_name, "pack": pack["id"]}}
        response = await asyncio.to_thread(self._yookassa_request, "https://api.yookassa.ru/v3/payments", "POST", payload, order_id)
        payment_id = clean_text(response.get("id"), 128)
        confirmation = response.get("confirmation") if isinstance(response.get("confirmation"), dict) else {}
        confirmation_url = clean_text(confirmation.get("confirmation_url"), 2_000)
        if not payment_id or not confirmation_url:
            raise RuntimeError("YooKassa response has no payment id or confirmation URL")
        async with self.lock:
            self.donations[order_id] = {"orderId": order_id, "paymentId": payment_id, "accountId": account_id, "packId": pack["id"],
                                        "coins": pack["coins"], "rub": pack["rub"], "credited": False}
            self._dirty = True
            self.save()
        return order_id, confirmation_url

    async def donation_status(self, order_id: str, check_payment: bool = False) -> dict[str, Any] | None:
        async with self.lock:
            order = self.donations.get(order_id)
            if not order:
                return None
            snapshot = dict(order)
        payment_status = "unknown"
        if check_payment and not snapshot.get("credited"):
            response = await asyncio.to_thread(self._yookassa_request,
                                               "https://api.yookassa.ru/v3/payments/" + urllib.parse.quote(str(snapshot["paymentId"]), safe=""), "GET")
            payment_status = clean_text(response.get("status"), 40, "unknown")
            if payment_status == "succeeded":
                async with self.lock:
                    order = self.donations.get(order_id)
                    account = self.accounts_by_id.get(str(order.get("accountId"))) if order else None
                    if order and account and not order.get("credited"):
                        account.currency += max(0, integer(order.get("coins")))
                        order["credited"] = True
                        self._dirty = True
                        self.save()
                        session = self.sessions.get(account.id)
                        if session:
                            await self.send(session, self.profile_message("profileSaved", account))
                            await self.send(session, {"type": "accountResult", "ok": True, "action": "donate",
                                                      "message": f"donation credited +{order['coins']}"})
                    snapshot = dict(order) if order else snapshot
        snapshot["paymentStatus"] = payment_status
        return snapshot

    async def settle_donations_loop(self) -> None:
        while True:
            await asyncio.sleep(15)
            if not self.yookassa_configured():
                continue
            async with self.lock:
                order_ids = [order_id for order_id, order in self.donations.items() if not order.get("credited")]
            for order_id in order_ids:
                try:
                    await self.donation_status(order_id, check_payment=True)
                except (OSError, RuntimeError, ValueError) as error:
                    logging.warning("Donation %s was not checked: %s", order_id, error)

    async def client(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter) -> None:
        connection_id = secrets.token_hex(4)
        player = Player(connection_id, f"player-{connection_id}", self.random.uniform(500, 9500), self.random.uniform(500, 9500))
        session = Session(connection_id, reader, writer, player)
        async with self.lock:
            self.sessions[connection_id] = session
        peer = writer.get_extra_info("peername")
        logging.info("Client %s connected from %s", connection_id, peer)
        await self.send(session, {"type": "welcome", "id": connection_id, "world": WORLD_SIZE, "tiles": WORLD_TILES,
                                  "tileSize": WORLD_TILE_SIZE, "seed": self.seed, "newsText": self.news_text,
                                  "newsDate": self.news_date, "versionCode": self.version_code, "versionName": self.version_name,
                                  "apkUrl": self.apk_url})
        try:
            while not reader.at_eof():
                raw = await reader.readline()
                if not raw: break
                if len(raw) > MAX_MESSAGE_BYTES:
                    await self.send(session, {"type": "error", "message": "message too large"}); break
                try:
                    message = json.loads(raw.decode("utf-8"))
                except (UnicodeDecodeError, json.JSONDecodeError):
                    await self.send(session, {"type": "error", "message": "invalid JSON"}); continue
                if not isinstance(message, dict):
                    await self.send(session, {"type": "error", "message": "JSON object expected"}); continue
                async with self.lock:
                    await self.handle(session, message)
                    # Account mutations are committed before acknowledging the
                    # next client packet, so a restart cannot lose a completed
                    # purchase, reward, or profile change.
                    self.save()
        except (ConnectionError, asyncio.IncompleteReadError):
            pass
        finally:
            async with self.lock:
                self.sessions.pop(connection_id, None)
                if session.player.id != connection_id: self.sessions.pop(session.player.id, None)
            writer.close()
            await writer.wait_closed()
            logging.info("Client %s disconnected", connection_id)


def donation_page(title: str, message: str) -> str:
    return ("<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
            "<title>Bacteria Online 5 donation</title><style>body{font-family:sans-serif;background:#161717;color:#fff;padding:32px}.card{max-width:560px;margin:auto;background:#202222;border-radius:18px;padding:28px}p{color:#cfcfcf}</style>"
            f"</head><body><div class=\"card\"><h1>{html.escape(title)}</h1><p>{html.escape(message)}</p></div></body></html>")


async def write_http(writer: asyncio.StreamWriter, status: str, content_type: str, body: str | bytes = b"", location: str = "") -> None:
    encoded = body.encode("utf-8") if isinstance(body, str) else body
    headers = [f"HTTP/1.1 {status}", f"Content-Type: {content_type}", f"Content-Length: {len(encoded)}", "Connection: close"]
    if location:
        headers.append("Location: " + location.replace("\r", "").replace("\n", ""))
    writer.write(("\r\n".join(headers) + "\r\n\r\n").encode("utf-8") + encoded)
    await writer.drain()


async def http_client(reader: asyncio.StreamReader, writer: asyncio.StreamWriter, game: GameServer) -> None:
    """HTTP endpoints for monitoring, updates, and YooKassa donation redirects."""
    try:
        request = (await asyncio.wait_for(reader.readline(), timeout=3)).decode("ascii", "replace").split()
        target = request[1] if len(request) >= 2 else "/"
        parsed = urllib.parse.urlsplit(target)
        path, query = parsed.path, urllib.parse.parse_qs(parsed.query)
        if path == "/health":
            await write_http(writer, "200 OK", "application/json; charset=utf-8", json.dumps({"ok": True, "players": len(game.sessions)}))
        elif path == "/news.json":
            await write_http(writer, "200 OK", "application/json; charset=utf-8", json.dumps({"text": game.news_text, "date": game.news_date}, ensure_ascii=False))
        elif path == "/update.json":
            await write_http(writer, "200 OK", "application/json; charset=utf-8", json.dumps({"versionCode": game.version_code, "versionName": game.version_name, "apkUrl": game.apk_url}))
        elif path == "/donate":
            account_id = clean_text((query.get("accountId") or [""])[0], 80)
            pack = integer((query.get("pack") or ["0"])[0])
            try:
                _, confirmation_url = await game.create_donation(account_id, pack)
                await write_http(writer, "302 Found", "text/html; charset=utf-8", location=confirmation_url)
            except LookupError:
                await write_http(writer, "400 Bad Request", "text/html; charset=utf-8", donation_page("Account not found", "Open the game, log in, then try donation again."))
            except ValueError:
                await write_http(writer, "503 Service Unavailable", "text/html; charset=utf-8", donation_page("YooKassa is not configured", "Set YOOKASSA_SHOP_ID and YOOKASSA_SECRET_KEY, then restart the server."))
            except (OSError, RuntimeError) as error:
                logging.error("Donation creation failed: %s", error)
                await write_http(writer, "502 Bad Gateway", "text/html; charset=utf-8", donation_page("Payment creation failed", str(error)))
        elif path == "/donate/return":
            order_id = clean_text((query.get("orderId") or [""])[0], 80)
            try:
                order = await game.donation_status(order_id, check_payment=True)
                if not order:
                    await write_http(writer, "404 Not Found", "text/html; charset=utf-8", donation_page("Order not found", "Return to the game and create a new donation."))
                elif order.get("credited"):
                    await write_http(writer, "200 OK", "text/html; charset=utf-8", donation_page("Donation credited", f"+{order['coins']} gene coins. You can return to the game."))
                else:
                    await write_http(writer, "200 OK", "text/html; charset=utf-8", donation_page("Payment is pending", "Wait a few seconds and refresh this page."))
            except (OSError, RuntimeError) as error:
                logging.error("Donation status check failed: %s", error)
                await write_http(writer, "502 Bad Gateway", "text/html; charset=utf-8", donation_page("Payment check failed", str(error)))
        elif path == "/donate/status":
            order_id = clean_text((query.get("orderId") or [""])[0], 80)
            order = await game.donation_status(order_id)
            status = "200 OK" if order else "404 Not Found"
            body = {"ok": bool(order), "credited": bool(order and order.get("credited")), "coins": integer(order.get("coins")) if order else 0}
            await write_http(writer, status, "application/json; charset=utf-8", json.dumps(body))
        else:
            await write_http(writer, "404 Not Found", "application/json; charset=utf-8", json.dumps({"ok": False, "message": "not found"}))
    except (OSError, asyncio.TimeoutError):
        pass
    finally:
        writer.close()
        await writer.wait_closed()


async def main() -> None:
    parser = argparse.ArgumentParser(description="Bacteria Online 5 JSON/TCP server")
    parser.add_argument("--host", default=os.getenv("BACTERIA_HOST", "0.0.0.0"))
    parser.add_argument("--port", type=int, default=int(os.getenv("BACTERIA_PORT", "5055")))
    parser.add_argument("--http-port", type=int, default=int(os.getenv("BACTERIA_HTTP_PORT", "5056")))
    parser.add_argument("--data", type=Path, default=Path(os.getenv("BACTERIA_DATA", "server-data/state.json")))
    args = parser.parse_args()
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    game = GameServer(args.data); game.load()
    tcp = await asyncio.start_server(game.client, args.host, args.port, limit=MAX_MESSAGE_BYTES + 1)
    http = await asyncio.start_server(lambda r, w: http_client(r, w, game), args.host, args.http_port)
    logging.info("Game TCP server: %s:%d; HTTP API: %s:%d", args.host, args.port, args.host, args.http_port)
    async with tcp, http:
        await asyncio.gather(tcp.serve_forever(), http.serve_forever(), game.state_loop(), game.save_loop(), game.settle_donations_loop())


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass
