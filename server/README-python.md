# Python server

`bacteria_server.py` is a dependency-free replacement for the TCP game
server.  It accepts newline-delimited UTF-8 JSON on port `5055`, which is the
protocol already used by the Android client.

Authentication uses one form: if the login is new, the server creates the
account with the supplied password; if it already exists, the password must
match.  Completed account actions are written to disk before the next client
packet is processed, and the server profile is always returned on login.

Run it from the project root:

```powershell
python .\server\bacteria_server.py
```

The game data is written every five seconds to `server-data\state.json`.
Pass explicit network settings when deploying:

```bash
python3 server/bacteria_server.py --host 0.0.0.0 --port 5055 --http-port 5056
```

## Migrating the Java server

Before the first Python start, copy `accounts.tsv`, `market.tsv`, and
`donations.tsv` from the old server into `server-data/`.  When `state.json`
does not exist, the Python server imports all three files and writes a new
`state.json`; it does not modify the TSV files.  Take a backup first.  The
legacy plaintext passwords are converted to salted password hashes.

## Donations

The `/donate`, `/donate/return`, and `/donate/status` HTTP endpoints use
YooKassa.  Set these environment variables in the systemd environment file:

```text
YOOKASSA_SHOP_ID=your_shop_id
YOOKASSA_SECRET_KEY=your_secret_key
PUBLIC_HTTP_URL=https://pay.bakteria5.ru
```

Pending orders imported from `donations.tsv` are checked every 15 seconds and
credited once only after YooKassa reports a successful payment.

The HTTP port has read-only `GET /health`, `GET /news.json`, and `GET
/update.json` endpoints.  Permit both ports in the firewall only as required;
place a TLS proxy in front of public HTTP traffic.
