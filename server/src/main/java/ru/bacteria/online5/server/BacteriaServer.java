package ru.bacteria.online5.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BacteriaServer {
    private static final int DEFAULT_PORT = 5055;
    private static final int DEFAULT_HTTP_PORT = 5056;
    private static final int WORLD_TILES = 100;
    private static final int WORLD_TILE_SIZE = 100;
    private static final int WORLD_SIZE = WORLD_TILES * WORLD_TILE_SIZE;
    private static final int WORLD_SEED = 50_505;
    private static final long PLAYER_TIMEOUT_MS = 15_000L;
    private static final long CONNECTION_DUMP_MS = 5_000L;
    private static final long STATE_TICK_MS = 250L;
    private static final int DAILY_REWARD = 150;
    private static final int DNA_PICKUP_TARGET = 2;
    private static final int DNA_NORMAL_VALUE = 1;
    private static final int DNA_ULTRA_VALUE = 45;
    private static final int DNA_ULTRA_CHANCE_PERCENT = 8;
    private static final float DNA_HEAL_PER_POINT = 2.5f;
    private static final int BIG_DNA_VALUE = 500;
    private static final int BIG_DNA_TICK_BASE_VALUE = 1;
    private static final float BIG_DNA_ZONE_RADIUS = 280f;
    private static final float BIG_DNA_EAT_RADIUS = 78f;
    private static final long BOSS_SPAWN_INTERVAL_MS = 30L * 60L * 1000L;
    private static final int BOSS_START_DNA = 2_000;
    private static final int BOSS_DEFEAT_DNA = 100;
    private static final float BOSS_DAMAGE_DNA_HALF = 0.50f;
    private static final float BOSS_DAMAGE_REDUCTION = 0.10f;
    private static final int GENE_MAX_LEVEL = 10;
    private static final int PASS_SEASON = 1;
    private static final int PRO_PASS_COST = 50_000;
    private static final int PASS_SKIN_1F51 = 3;
    private static final int PASS_SKIN_1P25 = 4;
    private static final int PASS_SKIN_1P50 = 5;
    private static final int CASE_SKIN_START = 6;
    private static final int CASE_SKIN_COUNT = 50;
    private static final int TOTAL_SKINS = CASE_SKIN_START + CASE_SKIN_COUNT;
    private static final int INVENTORY_ITEM_CASE = 0;
    private static final int INVENTORY_ITEM_SKIN = 1;
    private static final int[] CASE_COSTS = {2_000, 5_000, 10_000};
    private static final int MARKET_MAX_SHOWCASE = 6;
    private static final ZoneId WORLD_ZONE = ZoneId.of("Europe/Moscow");
    private static final File ACCOUNT_FILE = new File("server-data/accounts.tsv");
    private static final File MARKET_FILE = new File("server-data/market.tsv");
    private static final File DONATION_FILE = new File("server-data/donations.tsv");
    private static final String DEFAULT_GAME_HTTP_URL = "https://bakteria5.ru";
    private static final String DEFAULT_PUBLIC_HTTP_URL = "https://pay.bakteria5.ru";
    private static final DonationPack[] DONATION_PACKS = {
            new DonationPack("coins_2000", "2 000 gene coins", "50.00", 2_000),
            new DonationPack("coins_5000", "5 000 gene coins", "90.00", 5_000),
            new DonationPack("coins_10000", "10 000 gene coins", "175.00", 10_000),
            new DonationPack("coins_50000", "50 000 gene coins", "875.00", 50_000),
            new DonationPack("coins_100000", "100 000 gene coins", "1750.00", 100_000)
    };

    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ClientSession> connections = new ConcurrentHashMap<>();
    private final Map<String, Account> accountsById = new ConcurrentHashMap<>();
    private final Map<String, String> accountIdByName = new ConcurrentHashMap<>();
    private final Map<String, DonationOrder> donationOrders = new ConcurrentHashMap<>();
    private final ArrayList<MarketListing> marketListings = new ArrayList<>();
    private final Map<Integer, Integer> marketLastSalePrices = new ConcurrentHashMap<>();
    private final ArrayList<ChatEntry> chatHistory = new ArrayList<>();
    private final ArrayList<DnaPickup> dnaPickups = new ArrayList<>();
    private final Random random = new Random();
    private final ScheduledExecutorService tickLoop = Executors.newSingleThreadScheduledExecutor();
    private volatile long lastConnectionDump;
    private volatile String latestNewsText = "Добро пожаловать в Бактерии Онлайн 5";
    private volatile String latestNewsDate = Instant.now().toString().substring(0, 10);
    private volatile int latestVersionCode = 8;
    private volatile String latestVersionName = "0.1.9";
    private volatile String latestApkUrl = DEFAULT_GAME_HTTP_URL + "/app-debug.apk";
    private volatile int currentWorldSeed = WORLD_SEED;
    private volatile LocalDate lastWorldSeedResetDate = initialSeedResetDate();
    private volatile boolean bossActive;
    private volatile long nextBossSpawnAtMs;
    private volatile int bossDna = BOSS_START_DNA;
    private volatile float bossX;
    private volatile float bossY;
    private volatile int bossSpawnId;
    private int nextPickupId = 1;
    private int nextMarketListingId = 1;
    private int nextChatSeq = 1;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        new BacteriaServer().start(port);
    }

    private void start(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            loadAccounts();
            loadMarket();
            loadDonations();
            resetWorldPickups();
            resetBossTimer(System.currentTimeMillis());
            tickLoop.scheduleAtFixedRate(this::broadcastState, 0L, STATE_TICK_MS, TimeUnit.MILLISECONDS);
            tickLoop.scheduleAtFixedRate(this::printConnectionTable, CONNECTION_DUMP_MS, CONNECTION_DUMP_MS, TimeUnit.MILLISECONDS);
            tickLoop.scheduleAtFixedRate(this::resetWorldSeedAtNoon, 5L, 30L, TimeUnit.SECONDS);
            tickLoop.scheduleAtFixedRate(this::settlePendingDonations, 5L, 15L, TimeUnit.SECONDS);
            Thread httpThread = new Thread(() -> startHttp(DEFAULT_HTTP_PORT), "bacteria-http");
            httpThread.setDaemon(true);
            httpThread.start();
            System.out.println("Bacteria Online 5 server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(() -> handleClient(socket), "bacteria-client");
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    private void startHttp(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Bacteria update/news HTTP started on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(() -> handleHttp(socket), "bacteria-http-client");
                thread.setDaemon(true);
                thread.start();
            }
        } catch (IOException error) {
            System.out.println(ts() + " HTTP server stopped: " + error.getMessage());
        }
    }

    private void handleHttp(Socket socket) {
        try (Socket closeable = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(closeable.getInputStream(), StandardCharsets.UTF_8))) {
            String request = reader.readLine();
            if (request == null || request.isEmpty()) {
                return;
            }
            String[] parts = request.split(" ");
            String path = parts.length > 1 ? parts[1] : "/";
            if (path.startsWith("/news?text=")) {
                latestNewsText = sanitizeChat(URLDecoder.decode(path.substring("/news?text=".length()), StandardCharsets.UTF_8));
                latestNewsDate = Instant.now().toString().substring(0, 10);
                writeHttpText(closeable, 200, "application/json", "{\"ok\":true,\"date\":\"" + escape(latestNewsDate) + "\"}");
            } else if (path.startsWith("/news.json")) {
                writeHttpText(closeable, 200, "application/json", "{\"text\":\"" + escape(latestNewsText) + "\",\"date\":\"" + escape(latestNewsDate) + "\"}");
            } else if (path.startsWith("/update.json")) {
                writeHttpText(closeable, 200, "application/json", buildVersionMessage());
            } else if (path.startsWith("/donate?")) {
                handleDonateStart(closeable, path);
            } else if (path.startsWith("/donate/return?")) {
                handleDonateReturn(closeable, path);
            } else if (path.startsWith("/donate/status?")) {
                handleDonateStatus(closeable, path);
            } else if (path.startsWith("/app-debug.apk")) {
                writeApk(closeable);
            } else {
                writeHttpText(closeable, 404, "text/plain", "not found");
            }
        } catch (IOException ignored) {
        }
    }

    private void writeHttpText(Socket socket, int code, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        writer.write("HTTP/1.1 " + code + " OK\r\n");
        writer.write("Content-Type: " + contentType + "; charset=utf-8\r\n");
        writer.write("Content-Length: " + bytes.length + "\r\n");
        writer.write("Connection: close\r\n\r\n");
        writer.flush();
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
    }

    private void writeApk(Socket socket) throws IOException {
        File apk = new File("app-debug.apk");
        if (!apk.isFile()) {
            apk = new File("app/build/outputs/apk/debug/app-debug.apk");
        }
        if (!apk.isFile()) {
            writeHttpText(socket, 404, "text/plain", "apk not found");
            return;
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        writer.write("HTTP/1.1 200 OK\r\n");
        writer.write("Content-Type: application/vnd.android.package-archive\r\n");
        writer.write("Content-Length: " + apk.length() + "\r\n");
        writer.write("Connection: close\r\n\r\n");
        writer.flush();
        try (FileInputStream input = new FileInputStream(apk)) {
            byte[] buffer = new byte[16_384];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                socket.getOutputStream().write(buffer, 0, read);
            }
            socket.getOutputStream().flush();
        }
    }

    private void handleDonateStart(Socket socket, String path) throws IOException {
        Map<String, String> query = parseQuery(path);
        String accountId = sanitizeToken(query.get("accountId"), 80);
        int packIndex = clampInt(parseInt(query.getOrDefault("pack", "0"), 0), 0, DONATION_PACKS.length - 1);
        Account account = accountsById.get(accountId);
        if (account == null) {
            writeHttpText(socket, 400, "text/html", donatePage("Account not found", "Open the game, log in, then try donation again."));
            return;
        }
        if (!isYooKassaConfigured()) {
            writeHttpText(socket, 503, "text/html", donatePage("YooKassa is not configured",
                    "Set YOOKASSA_SHOP_ID and YOOKASSA_SECRET_KEY on the server, then restart it."));
            return;
        }
        DonationPack pack = DONATION_PACKS[packIndex];
        String orderId = UUID.randomUUID().toString();
        String returnUrl = publicHttpUrl() + "/donate/return?orderId=" + url(orderId);
        try {
            YooKassaPayment payment = createYooKassaPayment(orderId, account, pack, returnUrl);
            DonationOrder order = new DonationOrder(orderId, payment.id, account.id, pack.id, pack.coins, pack.rub, false);
            donationOrders.put(orderId, order);
            saveDonations();
            writeHttpRedirect(socket, payment.confirmationUrl);
        } catch (Exception error) {
            System.out.println(ts() + " DONATE create failed: " + error.getMessage());
            writeHttpText(socket, 502, "text/html", donatePage("Payment creation failed", escapeHtml(error.getMessage())));
        }
    }

    private void handleDonateReturn(Socket socket, String path) throws IOException {
        String orderId = sanitizeToken(parseQuery(path).get("orderId"), 80);
        DonationOrder order = donationOrders.get(orderId);
        if (order == null) {
            writeHttpText(socket, 404, "text/html", donatePage("Order not found", "Return to the game and create a new donation."));
            return;
        }
        try {
            String status = getYooKassaPaymentStatus(order.paymentId);
            boolean credited = "succeeded".equals(status) && creditDonation(order);
            if (credited || order.credited) {
                writeHttpText(socket, 200, "text/html", donatePage("Donation credited",
                        "+" + order.coins + " gene coins. You can return to the game."));
            } else {
                writeHttpText(socket, 200, "text/html", donatePage("Payment status: " + escapeHtml(status),
                        "If you have just paid, wait a few seconds and refresh this page."));
            }
        } catch (Exception error) {
            System.out.println(ts() + " DONATE return failed: " + error.getMessage());
            writeHttpText(socket, 502, "text/html", donatePage("Payment check failed", escapeHtml(error.getMessage())));
        }
    }

    private void handleDonateStatus(Socket socket, String path) throws IOException {
        String orderId = sanitizeToken(parseQuery(path).get("orderId"), 80);
        DonationOrder order = donationOrders.get(orderId);
        if (order == null) {
            writeHttpText(socket, 404, "application/json", "{\"ok\":false,\"message\":\"order not found\"}");
            return;
        }
        writeHttpText(socket, 200, "application/json", "{\"ok\":true,\"credited\":" + order.credited
                + ",\"coins\":" + order.coins + "}");
    }

    private boolean creditDonation(DonationOrder order) {
        synchronized (this) {
            if (order.credited) {
                return false;
            }
            Account account = accountsById.get(order.accountId);
            if (account == null) {
                return false;
            }
            account.currency += order.coins;
            order.credited = true;
            saveAccounts();
            saveDonations();
            ClientSession session = sessions.get(account.id);
            if (session != null) {
                session.send(accountMessage("profileSaved", account));
                session.send("{\"type\":\"accountResult\",\"ok\":true,\"action\":\"donate\",\"message\":\"donation credited +" + order.coins + "\"}");
            }
            return true;
        }
    }

    private void settlePendingDonations() {
        if (!isYooKassaConfigured()) {
            return;
        }
        for (DonationOrder order : donationOrders.values()) {
            if (order.credited) {
                continue;
            }
            try {
                if ("succeeded".equals(getYooKassaPaymentStatus(order.paymentId))) {
                    creditDonation(order);
                }
            } catch (Exception error) {
                System.out.println(ts() + " DONATE status check failed for " + order.orderId + ": " + error.getMessage());
            }
        }
    }

    private YooKassaPayment createYooKassaPayment(String orderId, Account account, DonationPack pack, String returnUrl) throws Exception {
        String body = "{"
                + "\"amount\":{\"value\":\"" + pack.rub + "\",\"currency\":\"RUB\"},"
                + "\"capture\":true,"
                + "\"confirmation\":{\"type\":\"redirect\",\"return_url\":\"" + escape(returnUrl) + "\"},"
                + "\"description\":\"Bacteria Online 5 donation " + escape(pack.title) + "\","
                + "\"metadata\":{\"order_id\":\"" + escape(orderId) + "\",\"account_id\":\"" + escape(account.id) + "\",\"pack\":\"" + escape(pack.id) + "\"}"
                + "}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.yookassa.ru/v3/payments"))
                .header("Authorization", "Basic " + yooKassaBasicAuth())
                .header("Idempotence-Key", orderId)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("YooKassa HTTP " + response.statusCode() + ": " + response.body());
        }
        String paymentId = readString(response.body(), "id", "");
        String confirmationUrl = readString(response.body(), "confirmation_url", "");
        if (paymentId.isEmpty() || confirmationUrl.isEmpty()) {
            throw new IOException("YooKassa response has no payment id or confirmation_url");
        }
        return new YooKassaPayment(paymentId, confirmationUrl);
    }

    private String getYooKassaPaymentStatus(String paymentId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.yookassa.ru/v3/payments/" + url(paymentId)))
                .header("Authorization", "Basic " + yooKassaBasicAuth())
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("YooKassa HTTP " + response.statusCode() + ": " + response.body());
        }
        return readString(response.body(), "status", "unknown");
    }

    private static boolean isYooKassaConfigured() {
        return !env("YOOKASSA_SHOP_ID").isEmpty() && !env("YOOKASSA_SECRET_KEY").isEmpty();
    }

    private static String yooKassaBasicAuth() {
        String credentials = env("YOOKASSA_SHOP_ID") + ":" + env("YOOKASSA_SECRET_KEY");
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static String publicHttpUrl() {
        String value = env("PUBLIC_HTTP_URL");
        if (value.isEmpty()) {
            return DEFAULT_PUBLIC_HTTP_URL;
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value.trim();
    }

    private static Map<String, String> parseQuery(String path) {
        Map<String, String> result = new HashMap<>();
        int queryStart = path.indexOf('?');
        if (queryStart < 0 || queryStart + 1 >= path.length()) {
            return result;
        }
        String[] pairs = path.substring(queryStart + 1).split("&");
        for (String pair : pairs) {
            int equals = pair.indexOf('=');
            String key = equals >= 0 ? pair.substring(0, equals) : pair;
            String value = equals >= 0 ? pair.substring(equals + 1) : "";
            result.put(urlDecode(key), urlDecode(value));
        }
        return result;
    }

    private static String url(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private void writeHttpRedirect(Socket socket, String location) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        writer.write("HTTP/1.1 302 Found\r\n");
        writer.write("Location: " + location + "\r\n");
        writer.write("Content-Length: 0\r\n");
        writer.write("Connection: close\r\n\r\n");
        writer.flush();
    }

    private static String donatePage(String title, String message) {
        return "<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>Bacteria Online 5 donation</title><style>body{font-family:sans-serif;background:#161717;color:#fff;padding:32px}"
                + ".card{max-width:560px;margin:auto;background:#202222;border-radius:18px;padding:28px}p{color:#cfcfcf}</style></head>"
                + "<body><div class=\"card\"><h1>" + title + "</h1><p>" + message + "</p></div></body></html>";
    }

    private static String escapeHtml(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private void handleClient(Socket socket) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Player player = new Player(id, "player-" + id, spawn(), spawn(), 32.5f);
        ClientSession session = null;

        try {
            session = new ClientSession(socket, player, id);
            connections.put(id, session);
            sessions.put(id, session);
            session.send("{\"type\":\"welcome\",\"id\":\"" + id + "\",\"world\":" + WORLD_SIZE
                    + ",\"tiles\":" + WORLD_TILES
                    + ",\"tileSize\":" + WORLD_TILE_SIZE
                    + ",\"seed\":" + currentWorldSeed
                    + ",\"newsText\":\"" + escape(latestNewsText)
                    + "\",\"newsDate\":\"" + escape(latestNewsDate)
                    + "\",\"versionCode\":" + latestVersionCode
                    + ",\"versionName\":\"" + escape(latestVersionName)
                    + "\",\"apkUrl\":\"" + escape(latestApkUrl) + "\"}");
            log(session, "CONNECTED", "active=" + connections.size());
            printConnectionTable();

            String line;
            while ((line = session.reader.readLine()) != null) {
                handleMessage(session, line.trim());
            }
        } catch (IOException error) {
            if (session == null) {
                System.out.println(ts() + " [unknown] DISCONNECTED before session: " + error.getMessage());
            } else {
                log(session, "DISCONNECTED", error.getMessage());
            }
        } finally {
            if (session != null) {
                connections.remove(session.connectionId, session);
                sessions.remove(id, session);
                sessions.remove(session.player.id, session);
                syncAccountFromPlayer(session);
                session.close();
                log(session, "REMOVED", "active=" + connections.size());
                printConnectionTable();
            }
        }
    }

    private void handleMessage(ClientSession session, String message) {
        if (message.isEmpty()) {
            return;
        }

        session.player.lastSeen = System.currentTimeMillis();
        String type = readString(message, "type", "");
        if ("join".equals(type)) {
            String name = readString(message, "name", session.player.name);
            session.player.name = sanitizeName(name);
            session.lastAction = "join name=" + session.player.name;
            log(session, "JOIN", "name=" + session.player.name);
            session.send("{\"type\":\"joined\",\"id\":\"" + session.player.id + "\",\"name\":\"" + escape(session.player.name) + "\"}");
        } else if ("auth".equals(type)) {
            String name = sanitizeName(readString(message, "name", session.player.name));
            String password = readString(message, "password", "");
            Account account = getOrCreateAccount(name, password);
            ensureAccountSeason(account);
            applyPassRewards(account);
            saveAccounts();
            session.account = account;
            sessions.remove(session.player.id, session);
            session.player.id = account.id;
            sessions.put(session.player.id, session);
            session.player.name = account.name;
            session.player.level = account.level;
            session.player.dna = 0;
            session.lastAction = "auth account=" + account.id;
            log(session, "AUTH", account.name + " id=" + account.id);
            session.send(accountMessage("profile", account));
            session.send(buildMarketMessage());
        } else if ("daily".equals(type)) {
            Account account = session.account != null ? session.account : getOrCreateAccount(session.player.name, "");
            session.account = account;
            long day = currentDay();
            if (account.lastDailyDay != day) {
                account.lastDailyDay = day;
                account.currency += DAILY_REWARD;
                saveAccounts();
                session.send("{\"type\":\"daily\",\"ok\":true,\"reward\":" + DAILY_REWARD + ",\"currency\":" + account.currency + "}");
                session.send(accountMessage("profileSaved", account));
            } else {
                session.send("{\"type\":\"daily\",\"ok\":false,\"reason\":\"claimed\",\"currency\":" + account.currency + "}");
                session.send(accountMessage("profileSaved", account));
            }
        } else if ("profileUpdate".equals(type)) {
            Account account = session.account != null ? session.account : getOrCreateAccount(session.player.name, "");
            session.account = account;
            account.levelSeed = readInt(message, "levelSeed", currentWorldSeed);
            session.player.level = account.level;
            session.lastAction = "profile refresh server-owned";
            saveAccounts();
            log(session, "PROFILE", "server-owned currency=" + account.currency + " level=" + account.level);
            session.send(accountMessage("profileSaved", account));
        } else if ("move".equals(type)) {
            float x = readFloat(message, "x", session.player.x);
            float y = readFloat(message, "y", session.player.y);
            session.player.x = clamp(x, 0f, WORLD_SIZE);
            session.player.y = clamp(y, 0f, WORLD_SIZE);
            session.player.dna = Math.max(0, readInt(message, "dna", session.player.dna));
            float reportedHp = clamp(readFloat(message, "hp", session.player.hp), 0f, 100f);
            session.player.hp = Math.min(session.player.hp, reportedHp);
            if (session.account != null) {
                session.player.level = session.account.level;
            }
            session.moveMessages++;
            session.lastAction = "move x=" + number(session.player.x) + " y=" + number(session.player.y);
            if (session.moveMessages % 10 == 1) {
                log(session, "MOVE", "x=" + number(session.player.x) + " y=" + number(session.player.y));
            }
        } else if ("eat".equals(type)) {
            String pickupId = readString(message, "pickupId", "");
            handleEatPickup(session, pickupId);
        } else if ("marketList".equals(type)) {
            session.send(buildMarketMessage());
        } else if ("marketSell".equals(type)) {
            handleMarketSell(session, readInt(message, "skin", -1), readInt(message, "price", 0));
        } else if ("marketBuy".equals(type)) {
            handleMarketBuy(session, readInt(message, "listingId", -1));
        } else if ("accountAction".equals(type)) {
            handleAccountAction(session, readString(message, "action", ""), message);
        } else if ("bigDnaTick".equals(type)) {
            handleBigDnaTick(session, readString(message, "pickupId", ""));
        } else if ("ping".equals(type)) {
            session.lastAction = "ping";
            log(session, "PING", "pong");
            session.send("{\"type\":\"pong\",\"time\":\"" + Instant.now() + "\"}");
        } else if ("chat".equals(type)) {
            String text = readString(message, "text", "");
            if (isBossCommand(text)) {
                handleBossCommand(session);
            } else {
                String cleaned = sanitizeChat(text);
                if (!cleaned.isEmpty()) {
                    session.lastAction = "chat " + cleaned;
                    log(session, "CHAT", cleaned);
                    broadcastChat(session, cleaned);
                }
            }
        } else if ("bossCommand".equals(type)) {
            handleBossCommand(session);
        } else if ("bossDamage".equals(type)) {
            int spawnId = readInt(message, "spawnId", -1);
            int sourceDna = Math.max(0, readInt(message, "dna", session.player.dna));
            session.player.dna = sourceDna;
            handleBossDamage(session, spawnId, sourceDna);
        } else if ("bossDefeated".equals(type)) {
            int spawnId = readInt(message, "spawnId", -1);
            int dna = readInt(message, "dna", BOSS_DEFEAT_DNA - 1);
            handleBossDefeated(session, spawnId, dna);
        } else if ("hit".equals(type)) {
            String targetId = readString(message, "targetId", "");
            ClientSession target = sessions.get(targetId);
            if (target != null && target != session) {
                if (distanceSq(session.player.x, session.player.y, target.player.x, target.player.y) > 260f * 260f) {
                    return;
                }
                float damage = calculateDamage(session.player, target.player);
                target.player.hp -= damage;
                if (target.player.hp <= 0f) {
                    int stolen = Math.max(0, Math.round(target.player.dna * 0.70f));
                    session.player.dna += stolen;
                    if (session.account != null) {
                        session.account.dna += stolen;
                    }
                    target.player.dna = 0;
                    target.player.hp = 100f;
                    target.player.x = spawn();
                    target.player.y = spawn();
                    syncAccountFromPlayer(session);
                    syncAccountFromPlayer(target);
                    session.send("{\"type\":\"kill\",\"targetId\":\"" + escape(target.player.id) + "\",\"dna\":" + session.player.dna + "}");
                    target.send("{\"type\":\"death\",\"killerId\":\"" + escape(session.player.id) + "\",\"killerName\":\"" + escape(session.player.name) + "\"}");
                }
                session.lastAction = "hit target=" + targetId + " damage=" + number(damage);
                log(session, "HIT", session.lastAction);
            }
        } else if ("news".equals(type)) {
            String text = sanitizeChat(readString(message, "text", ""));
            if (!text.isEmpty()) {
                latestNewsText = text;
                latestNewsDate = Instant.now().toString().substring(0, 10);
            }
            session.lastAction = "news updated";
            log(session, "NEWS", latestNewsText + " date=" + latestNewsDate);
            session.send("{\"type\":\"news\",\"text\":\"" + escape(latestNewsText) + "\",\"date\":\"" + escape(latestNewsDate) + "\"}");
        } else if ("version".equals(type)) {
            latestVersionCode = Math.max(latestVersionCode, readInt(message, "versionCode", latestVersionCode));
            latestVersionName = sanitizeName(readString(message, "versionName", latestVersionName));
            latestApkUrl = readString(message, "apkUrl", latestApkUrl);
            session.lastAction = "version updated";
            log(session, "VERSION", latestVersionName + " code=" + latestVersionCode);
            session.send(buildVersionMessage());
        } else if ("action".equals(type)) {
            String name = readString(message, "name", "unknown");
            session.lastAction = "action " + sanitizeName(name);
            log(session, "ACTION", sanitizeName(name));
        } else {
            session.lastAction = "unknown type=" + type;
            log(session, "UNKNOWN", message);
        }
    }

    private void broadcastState() {
        long now = System.currentTimeMillis();
        updateBossTimer(now);
        Collection<ClientSession> activeSessions = new ArrayList<>(connections.values());
        String state = buildState(activeSessions, now);

        for (ClientSession session : activeSessions) {
            if (now - session.player.lastSeen > PLAYER_TIMEOUT_MS) {
                log(session, "TIMEOUT", "lastSeenMsAgo=" + (now - session.player.lastSeen));
                session.close();
                connections.remove(session.connectionId, session);
                sessions.remove(session.player.id, session);
                continue;
            }
            session.send(state);
        }
    }

    private String buildState(Collection<ClientSession> activeSessions, long now) {
        ArrayList<ClientSession> sortedSessions = new ArrayList<>(activeSessions);
        sortedSessions.sort((first, second) -> {
            int dna = Integer.compare(second.player.dna, first.player.dna);
            if (dna != 0) {
                return dna;
            }
            int level = Integer.compare(second.player.level, first.player.level);
            if (level != 0) {
                return level;
            }
            return first.player.name.compareToIgnoreCase(second.player.name);
        });
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\":\"state\",\"newsText\":\"").append(escape(latestNewsText))
                .append("\",\"newsDate\":\"").append(escape(latestNewsDate))
                .append("\",\"seed\":").append(currentWorldSeed)
                .append(",\"versionCode\":").append(latestVersionCode)
                .append(",\"versionName\":\"").append(escape(latestVersionName))
                .append("\",\"apkUrl\":\"").append(escape(latestApkUrl))
                .append("\",\"players\":[");
        boolean first = true;
        for (ClientSession session : sortedSessions) {
            Player player = session.player;
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append("{\"id\":\"").append(player.id)
                    .append("\",\"name\":\"").append(escape(player.name))
                    .append("\",\"x\":").append(number(player.x))
                    .append(",\"y\":").append(number(player.y))
                    .append(",\"r\":").append(number(player.radius))
                    .append(",\"hp\":").append(number(player.hp))
                    .append(",\"dna\":").append(player.dna)
                    .append(",\"level\":").append(player.level)
                    .append('}');
        }
        builder.append("],\"leaderboard\":[");
        appendAccountLeaderboard(builder);
        builder.append("],\"dna\":[");
        synchronized (dnaPickups) {
            first = true;
            for (DnaPickup pickup : dnaPickups) {
                if (!pickup.active) {
                    continue;
                }
                if (!first) {
                    builder.append(',');
                }
                first = false;
                appendPickupJson(builder, pickup);
            }
        }
        builder.append(']');
        appendChatHistory(builder);
        builder.append(",\"bigDna\":null");
        appendBossState(builder, now);
        builder.append('}');
        return builder.toString();
    }

    private void appendAccountLeaderboard(StringBuilder builder) {
        ArrayList<Account> leaderboard = new ArrayList<>(accountsById.values());
        leaderboard.sort((first, second) -> {
            int byLevel = Integer.compare(second.level, first.level);
            if (byLevel != 0) {
                return byLevel;
            }
            int byDna = Integer.compare(second.dna, first.dna);
            if (byDna != 0) {
                return byDna;
            }
            return first.name.compareToIgnoreCase(second.name);
        });
        int limit = Math.min(100, leaderboard.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                builder.append(',');
            }
            Account account = leaderboard.get(i);
            builder.append("{\"id\":\"").append(escape(account.id))
                    .append("\",\"name\":\"").append(escape(account.name))
                    .append("\",\"totalDna\":").append(Math.max(0, account.dna))
                    .append(",\"level\":").append(account.level)
                    .append('}');
        }
    }

    private void appendChatHistory(StringBuilder builder) {
        synchronized (chatHistory) {
            builder.append(",\"chatSeq\":").append(Math.max(0, nextChatSeq - 1)).append(",\"chat\":[");
            boolean first = true;
            for (ChatEntry entry : chatHistory) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                appendChatJson(builder, entry);
            }
            builder.append(']');
        }
    }

    private void appendChatJson(StringBuilder builder, ChatEntry entry) {
        builder.append('{');
        appendChatFields(builder, entry);
        builder.append('}');
    }

    private void appendChatFields(StringBuilder builder, ChatEntry entry) {
        builder.append("\"seq\":").append(entry.seq)
                .append(",\"id\":\"").append(escape(entry.id))
                .append("\",\"name\":\"").append(escape(entry.name))
                .append("\",\"text\":\"").append(escape(entry.text))
                .append("\"");
    }

    private void appendBossState(StringBuilder builder, long now) {
        synchronized (this) {
            long cooldown = bossActive ? 0L : Math.max(0L, nextBossSpawnAtMs - now);
            builder.append(",\"bossActive\":").append(bossActive)
                    .append(",\"bossSpawnId\":").append(bossSpawnId)
                    .append(",\"bossDna\":").append(bossDna)
                    .append(",\"bossX\":").append(number(bossX))
                    .append(",\"bossY\":").append(number(bossY))
                    .append(",\"bossCooldownMs\":").append(cooldown);
        }
    }

    private String buildVersionMessage() {
        return "{\"type\":\"version\",\"versionCode\":" + latestVersionCode
                + ",\"versionName\":\"" + escape(latestVersionName)
                + "\",\"apkUrl\":\"" + escape(latestApkUrl) + "\"}";
    }

    private void appendPickupJson(StringBuilder builder, DnaPickup pickup) {
        builder.append("{\"id\":\"").append(escape(pickup.id))
                .append("\",\"x\":").append(number(pickup.x))
                .append(",\"y\":").append(number(pickup.y))
                .append(",\"value\":").append(pickup.value)
                .append(",\"radius\":").append(number(pickup.radius))
                .append(",\"zone\":").append(number(pickup.zoneRadius))
                .append(",\"kind\":\"").append(escape(pickup.kind))
                .append("\"}");
    }

    private synchronized void resetBossTimer(long now) {
        bossActive = false;
        bossDna = BOSS_START_DNA;
        nextBossSpawnAtMs = now + BOSS_SPAWN_INTERVAL_MS;
    }

    private synchronized void updateBossTimer(long now) {
        if (nextBossSpawnAtMs <= 0L) {
            nextBossSpawnAtMs = now + BOSS_SPAWN_INTERVAL_MS;
        }
        if (!bossActive && now >= nextBossSpawnAtMs) {
            spawnServerBoss(now, "timer");
        }
    }

    private synchronized void spawnServerBoss(long now, String source) {
        if (bossActive) {
            return;
        }
        bossActive = true;
        bossDna = BOSS_START_DNA;
        bossX = spawn();
        bossY = spawn();
        bossSpawnId++;
        nextBossSpawnAtMs = 0L;
        System.out.println(ts() + " BOSS SPAWN source=" + source + " id=" + bossSpawnId
                + " pos=(" + number(bossX) + ", " + number(bossY) + ")");
    }

    private synchronized void finishServerBoss(long now, String source) {
        if (!bossActive) {
            return;
        }
        bossActive = false;
        bossDna = BOSS_DEFEAT_DNA - 1;
        nextBossSpawnAtMs = now + BOSS_SPAWN_INTERVAL_MS;
        System.out.println(ts() + " BOSS DEFEATED source=" + source + " nextMs=" + nextBossSpawnAtMs);
    }

    private void handleBossCommand(ClientSession session) {
        boolean alreadyActive;
        synchronized (this) {
            alreadyActive = bossActive;
            if (!bossActive) {
                spawnServerBoss(System.currentTimeMillis(), "command:" + session.player.name);
            }
        }
        session.lastAction = alreadyActive ? "boss command active" : "boss command spawned";
        log(session, "BOSS", session.lastAction);
        session.send("{\"type\":\"boss\",\"message\":\""
                + (alreadyActive ? "босс уже на карте" : "босс призван на сервере")
                + "\"}");
    }

    private void handleBossDefeated(ClientSession session, int spawnId, int dna) {
        synchronized (this) {
            if (!bossActive || (spawnId > 0 && spawnId != bossSpawnId)) {
                session.send("{\"type\":\"boss\",\"message\":\"сервер не подтвердил убийство босса\"}");
                return;
            }
            if (bossDna >= BOSS_DEFEAT_DNA) {
                session.send("{\"type\":\"boss\",\"message\":\"сервер не подтвердил убийство босса\"}");
                return;
            }
            finishServerBoss(System.currentTimeMillis(), session.player.name);
        }
        session.lastAction = "boss defeated";
        log(session, "BOSS", "defeated by " + session.player.name + " spawnId=" + spawnId);
        session.send("{\"type\":\"boss\",\"message\":\"босс убит, таймер запущен сервером\"}");
    }

    private void handleBossDamage(ClientSession session, int spawnId, int sourceDna) {
        int damage = calculateBossDamageFromDna(sourceDna);
        if (damage == 0) {
            return;
        }
        synchronized (this) {
            if (!bossActive || (spawnId > 0 && spawnId != bossSpawnId)) {
                return;
            }
            bossDna = Math.max(0, bossDna - damage);
            session.lastAction = "boss damage -" + damage + " dna=" + sourceDna + " hp=" + bossDna;
            log(session, "BOSS_DAMAGE", session.lastAction);
            if (bossDna < BOSS_DEFEAT_DNA) {
                finishServerBoss(System.currentTimeMillis(), session.player.name);
            }
        }
    }

    private static int calculateBossDamageFromDna(int dna) {
        if (dna <= 0) {
            return 0;
        }
        return Math.max(1, Math.round(dna * BOSS_DAMAGE_DNA_HALF * (1f - BOSS_DAMAGE_REDUCTION)));
    }

    private void handleEatPickup(ClientSession session, String pickupId) {
        if (pickupId.isEmpty()) {
            return;
        }

        synchronized (dnaPickups) {
            for (int i = 0; i < dnaPickups.size(); i++) {
                DnaPickup pickup = dnaPickups.get(i);
                if (!pickup.active || !pickup.id.equals(pickupId)) {
                    continue;
                }
                float eatRadius = "big".equals(pickup.kind) ? BIG_DNA_EAT_RADIUS : pickup.radius + session.player.radius + 18f;
                if (distanceSq(session.player.x, session.player.y, pickup.x, pickup.y) > eatRadius * eatRadius) {
                    return;
                }
                int gain = pickup.value;
                dnaPickups.remove(i);
                spawnDnaPickupLocked();
                grantDna(session, gain, pickup.kind);
                return;
            }
        }
    }

    private void handleBigDnaTick(ClientSession session, String requestedId) {
        long now = System.currentTimeMillis();
        if (now < session.player.nextBigDnaTickMs) {
            return;
        }

        DnaPickup activeBig = null;
        synchronized (dnaPickups) {
            for (DnaPickup pickup : dnaPickups) {
                if (!pickup.active || !"big".equals(pickup.kind)) {
                    continue;
                }
                if (!requestedId.isEmpty() && !requestedId.equals(pickup.id)) {
                    continue;
                }
                if (distanceSq(session.player.x, session.player.y, pickup.x, pickup.y) <= pickup.zoneRadius * pickup.zoneRadius) {
                    activeBig = pickup;
                    break;
                }
            }
        }

        if (activeBig == null) {
            session.player.bigDnaZonePickupId = "";
            session.player.bigDnaZoneEnteredMs = 0L;
            return;
        }

        if (!activeBig.id.equals(session.player.bigDnaZonePickupId)) {
            session.player.bigDnaZonePickupId = activeBig.id;
            session.player.bigDnaZoneEnteredMs = now;
        }
        long minutesInZone = Math.max(0L, (now - session.player.bigDnaZoneEnteredMs) / 60_000L);
        int gain = BIG_DNA_TICK_BASE_VALUE + (int) Math.min(999L, minutesInZone);
        session.player.nextBigDnaTickMs = now + 1000L;
        grantDna(session, gain, "bigDnaZone");
    }

    private void grantDna(ClientSession session, int amount, String source) {
        int gain = Math.max(0, amount);
        DayOfWeek day = LocalDate.now(WORLD_ZONE).getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            gain *= 2;
        }
        if (gain == 0) {
            return;
        }
        if (session.account != null) {
            session.account.dna += gain;
            addAccountExperience(session.account, gain);
            session.player.level = session.account.level;
            saveAccounts();
        }
        session.player.hp = clamp(session.player.hp + gain * DNA_HEAL_PER_POINT, 0f, 100f);
        session.lastAction = "eat " + source + " +" + gain;
        session.send("{\"type\":\"eatResult\",\"source\":\"" + escape(source)
                + "\",\"gain\":" + gain
                + ",\"currency\":" + (session.account != null ? session.account.currency : 0)
                + "}");
        if (session.account != null) {
            session.send(accountMessage("profileSaved", session.account));
        }
    }

    private void resetWorldPickups() {
        synchronized (dnaPickups) {
            dnaPickups.clear();
            nextPickupId = 1;
            for (int i = 0; i < DNA_PICKUP_TARGET; i++) {
                spawnDnaPickupLocked();
            }
        }
    }

    private void spawnDnaPickupLocked() {
        dnaPickups.add(new DnaPickup("big-" + currentWorldSeed + "-" + nextPickupId++, spawn(), spawn(),
                BIG_DNA_VALUE, "big", 72f, BIG_DNA_ZONE_RADIUS));
    }

    private Account getOrCreateAccount(String name, String password) {
        String safeName = sanitizeName(name);
        String key = safeName.toLowerCase(Locale.ROOT);
        String accountId = accountIdByName.get(key);
        if (accountId != null) {
            Account existing = accountsById.get(accountId);
            if (existing != null) {
                return existing;
            }
        }

        Account account = new Account();
        account.id = UUID.randomUUID().toString().substring(0, 8);
        account.name = safeName;
        account.password = password == null ? "" : password.replace('\t', ' ').trim();
        account.level = 1;
        account.dna = 0;
        account.levelSeed = currentWorldSeed;
        account.servers = "official,community";
        account.bkPass = "free";
        account.genes = "0,0,0,0";
        account.skins = "1,0,0,0,0,0";
        account.freeRewards = "";
        account.proRewards = "";
        account.proPassLevel = 1;
        account.passSeason = 1;
        account.showcase = "";
        account.inventory = "1_0";
        account.friends = "";
        account.friendRequests = "";
        accountsById.put(account.id, account);
        accountIdByName.put(key, account.id);
        saveAccounts();
        return account;
    }

    private String accountMessage(String type, Account account) {
        return "{\"type\":\"" + type + "\",\"id\":\"" + escape(account.id)
                + "\",\"name\":\"" + escape(account.name)
                + "\",\"level\":" + account.level
                + ",\"dna\":" + Math.max(0, account.dna)
                + ",\"currency\":" + account.currency
                + ",\"levelSeed\":" + account.levelSeed
                + ",\"servers\":\"" + escape(account.servers)
                + "\",\"bkPass\":\"" + escape(account.bkPass)
                + "\",\"xp\":" + account.xp
                + ",\"genes\":\"" + escape(account.genes)
                + "\",\"skins\":\"" + escape(account.skins)
                + "\",\"selectedSkin\":" + account.selectedSkin
                + ",\"freeRewards\":\"" + escape(account.freeRewards)
                + "\",\"proRewards\":\"" + escape(account.proRewards)
                + "\",\"proPassLevel\":" + account.proPassLevel
                + ",\"passSeason\":" + account.passSeason
                + ",\"showcase\":\"" + escape(account.showcase)
                + "\",\"inventory\":\"" + escape(account.inventory)
                + "\",\"friends\":\"" + escape(account.friends)
                + "\",\"friendRequests\":\"" + escape(account.friendRequests) + "\"}";
    }

    private float calculateDamage(Player attacker, Player target) {
        float dnaGap = Math.max(0, attacker.dna - target.dna);
        return clamp(1.5f + dnaGap * 0.015f + attacker.level * 0.10f, 1.5f, 15f);
    }

    private void syncAccountFromPlayer(ClientSession session) {
        if (session.account == null) {
            return;
        }
        session.account.levelSeed = currentWorldSeed;
        saveAccounts();
    }

    private void handleAccountAction(ClientSession session, String action, String message) {
        Account account = session.account != null ? session.account : getOrCreateAccount(session.player.name, "");
        session.account = account;
        ensureAccountSeason(account);
        boolean ok = false;
        String result = action.isEmpty() ? "empty action" : action;

        if ("localDnaEat".equals(action)) {
            int amount = clampInt(readInt(message, "amount", 0), 0, 10_000);
            account.dna += amount;
            addAccountExperience(account, amount);
            session.player.hp = clamp(session.player.hp + amount * DNA_HEAL_PER_POINT, 0f, 100f);
            ok = amount > 0;
            result = ok ? "progress synced" : "empty gain";
        } else if ("buyProPass".equals(action)) {
            if ("pro".equals(account.bkPass)) {
                ok = true;
                result = "pro already active";
            } else if (account.currency >= PRO_PASS_COST) {
                account.currency -= PRO_PASS_COST;
                account.bkPass = "pro";
                account.proPassLevel = 1;
                account.proRewards = "";
                applyPassRewards(account);
                ok = true;
                result = "pro pass bought";
            } else {
                result = "not enough currency";
            }
        } else if ("upgradeGene".equals(action)) {
            int gene = clampInt(readInt(message, "gene", -1), -1, 3);
            if (gene >= 0) {
                int level = geneLevel(account, gene);
                int cost = (level + 1) * 50;
                if (level >= GENE_MAX_LEVEL) {
                    result = "gene max";
                } else if (account.currency >= cost) {
                    account.currency -= cost;
                    setGeneLevel(account, gene, level + 1);
                    ok = true;
                    result = "gene upgraded";
                } else {
                    result = "not enough currency";
                }
            }
        } else if ("buySkin".equals(action)) {
            int skin = clampInt(readInt(message, "skin", -1), -1, TOTAL_SKINS - 1);
            int cost = skinCost(skin);
            if (skin <= 0) {
                result = "skin unavailable";
            } else if (isSkinOwned(account, skin)) {
                account.selectedSkin = skin;
                ok = true;
                result = "skin equipped";
            } else if (account.currency >= cost) {
                account.currency -= cost;
                markSkinOwned(account, skin);
                addInventorySkin(account, skin);
                account.selectedSkin = skin;
                ok = true;
                result = "skin bought";
            } else {
                result = "not enough currency";
            }
        } else if ("equipSkin".equals(action)) {
            int skin = clampInt(readInt(message, "skin", -1), -1, TOTAL_SKINS - 1);
            if (skin >= 0 && isSkinOwned(account, skin)) {
                account.selectedSkin = skin;
                ok = true;
                result = "skin equipped";
            } else {
                result = "skin not owned";
            }
        } else if ("toggleShowcase".equals(action)) {
            int skin = clampInt(readInt(message, "skin", -1), -1, TOTAL_SKINS - 1);
            if (skin >= 0 && isSkinOwned(account, skin)) {
                boolean enabled = !isShowcaseEnabled(account, skin);
                if (enabled && showcaseCount(account) >= MARKET_MAX_SHOWCASE) {
                    result = "showcase full";
                } else {
                    setShowcaseEnabled(account, skin, enabled);
                    ok = true;
                    result = enabled ? "showcase enabled" : "showcase disabled";
                }
            } else {
                result = "skin not owned";
            }
        } else if ("buyCase".equals(action)) {
            int tier = clampInt(readInt(message, "tier", -1), -1, CASE_COSTS.length - 1);
            if (tier >= 0) {
                int cost = CASE_COSTS[tier];
                if (account.currency >= cost) {
                    account.currency -= cost;
                    addInventoryItem(account, INVENTORY_ITEM_CASE, tier);
                    ok = true;
                    result = "case bought";
                } else {
                    result = "not enough currency";
                }
            }
        } else if ("openCase".equals(action)) {
            int index = readInt(message, "index", -1);
            int tier = inventoryCaseTier(account, index);
            if (tier >= 0) {
                int skin = rollCaseSkin(tier);
                replaceInventoryItem(account, index, INVENTORY_ITEM_SKIN, skin);
                markSkinOwned(account, skin);
                ok = true;
                result = "case opened";
                session.send("{\"type\":\"caseOpened\",\"ok\":true,\"index\":" + index
                        + ",\"tier\":" + tier
                        + ",\"skin\":" + skin + "}");
            } else {
                result = "case not found";
            }
        } else if ("friendRequest".equals(action)) {
            String targetId = sanitizeStateString(readString(message, "target", ""), "");
            Account target = accountsById.get(targetId);
            if (target != null && !target.id.equals(account.id)) {
                target.friendRequests = addToken(target.friendRequests, account.id, 30);
                saveAccounts();
                ClientSession targetSession = sessions.get(target.id);
                if (targetSession != null) {
                    targetSession.send(accountMessage("profileSaved", target));
                }
                ok = true;
                result = "friend request sent";
            } else {
                result = "player not found";
            }
        } else if ("friendAccept".equals(action) || "friendDecline".equals(action)) {
            String friendId = sanitizeStateString(readString(message, "target", ""), "");
            if (containsToken(account.friendRequests, friendId)) {
                account.friendRequests = removeToken(account.friendRequests, friendId);
                if ("friendAccept".equals(action)) {
                    account.friends = addToken(account.friends, friendId, 40);
                    Account friend = accountsById.get(friendId);
                    if (friend != null) {
                        friend.friends = addToken(friend.friends, account.id, 40);
                    }
                }
                ok = true;
                result = "friend request updated";
            } else {
                result = "request not found";
            }
        }

        session.player.level = account.level;
        saveAccounts();
        session.lastAction = "account action " + action + " ok=" + ok;
        log(session, "ACCOUNT", session.lastAction);
        session.send("{\"type\":\"accountResult\",\"ok\":" + ok
                + ",\"action\":\"" + escape(action)
                + "\",\"message\":\"" + escape(result) + "\"}");
        session.send(accountMessage("profileSaved", account));
    }

    private void addAccountExperience(Account account, int amount) {
        int gain = Math.max(0, amount);
        if (gain == 0) {
            return;
        }
        ensureAccountSeason(account);
        account.currency += gain;
        account.xp += gain;
        while (account.xp >= xpForLevel(account.level)) {
            account.xp -= xpForLevel(account.level);
            account.level++;
            if ("pro".equals(account.bkPass)) {
                account.proPassLevel = clampInt(account.proPassLevel + 1, 1, 50);
            }
            account.currency += account.level * 50;
        }
        applyPassRewards(account);
    }

    private void ensureAccountSeason(Account account) {
        if (account.passSeason == PASS_SEASON) {
            return;
        }
        account.passSeason = PASS_SEASON;
        account.bkPass = "free";
        account.proPassLevel = 1;
        account.proRewards = "";
    }

    private void applyPassRewards(Account account) {
        int freeLevel = Math.min(50, account.level);
        for (int level = 1; level <= freeLevel; level++) {
            if (!boolAt(account.freeRewards, level)) {
                account.freeRewards = setBoolAt(account.freeRewards, level, true, 52);
                account.currency += 50;
            }
        }
        if (account.level >= 50 && !boolAt(account.freeRewards, 50)) {
            account.freeRewards = setBoolAt(account.freeRewards, 50, true, 52);
            grantUniqueSkin(account, PASS_SKIN_1F51);
        }
        if (!"pro".equals(account.bkPass)) {
            return;
        }
        int proLevel = clampInt(account.proPassLevel, 1, 50);
        for (int level = 1; level <= proLevel; level++) {
            if (!boolAt(account.proRewards, level)) {
                account.proRewards = setBoolAt(account.proRewards, level, true, 51);
                account.currency += 100;
            }
        }
        if (boolAt(account.proRewards, 25)) {
            grantUniqueSkin(account, PASS_SKIN_1P25);
        }
        if (boolAt(account.proRewards, 50)) {
            grantUniqueSkin(account, PASS_SKIN_1P50);
        }
    }

    private void grantUniqueSkin(Account account, int skin) {
        if (!isSkinOwned(account, skin)) {
            markSkinOwned(account, skin);
            addInventorySkin(account, skin);
        }
    }

    private int xpForLevel(int level) {
        return Math.max(50, level * 50);
    }

    private int geneLevel(Account account, int gene) {
        String[] parts = account.genes == null ? new String[0] : account.genes.split(",");
        return gene >= 0 && gene < parts.length ? clampInt(parseInt(parts[gene], 0), 0, GENE_MAX_LEVEL) : 0;
    }

    private void setGeneLevel(Account account, int gene, int level) {
        int count = Math.max(4, gene + 1);
        String[] parts = account.genes == null ? new String[0] : account.genes.split(",");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                builder.append(',');
            }
            int value = i < parts.length ? parseInt(parts[i], 0) : 0;
            builder.append(i == gene ? clampInt(level, 0, GENE_MAX_LEVEL) : clampInt(value, 0, GENE_MAX_LEVEL));
        }
        account.genes = builder.toString();
    }

    private void handleMarketSell(ClientSession session, int skinIndex, int price) {
        Account account = session.account != null ? session.account : getOrCreateAccount(session.player.name, "");
        session.account = account;
        int safeSkin = Math.max(0, skinIndex);
        int safePrice = clampInt(price, 100, 999_999);
        if (safeSkin <= 0) {
            session.send("{\"type\":\"marketResult\",\"ok\":false,\"message\":\"default skin cannot be listed\"}");
            return;
        }
        if (!removeInventorySkin(account, safeSkin)) {
            session.send("{\"type\":\"marketResult\",\"ok\":false,\"message\":\"skin not in inventory\"}");
            return;
        }
        MarketListing listing;
        synchronized (marketListings) {
            listing = new MarketListing(nextMarketListingId++, safeSkin, safePrice, account.id, account.name);
            marketListings.add(0, listing);
            while (marketListings.size() > 250) {
                marketListings.remove(marketListings.size() - 1);
            }
        }
        saveAccounts();
        saveMarket();
        session.send(accountMessage("profileSaved", account));
        session.send("{\"type\":\"marketResult\",\"ok\":true,\"message\":\"listed\",\"listingId\":" + listing.id + "}");
        broadcastMarketState();
    }

    private void handleMarketBuy(ClientSession session, int listingId) {
        Account buyer = session.account != null ? session.account : getOrCreateAccount(session.player.name, "");
        session.account = buyer;
        MarketListing listing = null;
        synchronized (marketListings) {
            for (int i = 0; i < marketListings.size(); i++) {
                MarketListing candidate = marketListings.get(i);
                if (candidate.id == listingId) {
                    listing = candidate;
                    break;
                }
            }
            if (listing == null) {
                session.send("{\"type\":\"marketResult\",\"ok\":false,\"message\":\"listing not found\"}");
                return;
            }
            if (buyer.id.equals(listing.ownerId)) {
                session.send("{\"type\":\"marketResult\",\"ok\":false,\"message\":\"own listing\"}");
                return;
            }
            if (buyer.currency < listing.price) {
                session.send("{\"type\":\"marketResult\",\"ok\":false,\"message\":\"not enough currency\"}");
                return;
            }
            marketListings.remove(listing);
        }

        buyer.currency -= listing.price;
        addInventorySkin(buyer, listing.skinIndex);
        markSkinOwned(buyer, listing.skinIndex);

        Account seller = accountsById.get(listing.ownerId);
        if (seller != null) {
            seller.currency += listing.price;
        }
        marketLastSalePrices.put(listing.skinIndex, listing.price);
        saveAccounts();
        saveMarket();

        session.send(accountMessage("profileSaved", buyer));
        session.send("{\"type\":\"marketResult\",\"ok\":true,\"message\":\"bought\",\"skin\":" + listing.skinIndex + ",\"price\":" + listing.price + "}");
        if (seller != null) {
            ClientSession sellerSession = sessions.get(seller.id);
            if (sellerSession != null) {
                sellerSession.send(accountMessage("profileSaved", seller));
                sellerSession.send("{\"type\":\"marketResult\",\"ok\":true,\"message\":\"sold\",\"skin\":" + listing.skinIndex + ",\"price\":" + listing.price + "}");
            }
        }
        broadcastMarketState();
    }

    private void broadcastMarketState() {
        String message = buildMarketMessage();
        for (ClientSession session : sessions.values()) {
            session.send(message);
        }
    }

    private String buildMarketMessage() {
        StringBuilder builder = new StringBuilder("{\"type\":\"market\",\"listings\":[");
        synchronized (marketListings) {
            for (int i = 0; i < marketListings.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                MarketListing listing = marketListings.get(i);
                builder.append("{\"id\":").append(listing.id)
                        .append(",\"skin\":").append(listing.skinIndex)
                        .append(",\"price\":").append(listing.price)
                        .append(",\"ownerId\":\"").append(escape(listing.ownerId))
                        .append("\",\"owner\":\"").append(escape(listing.ownerName))
                        .append("\"}");
            }
        }
        builder.append("],\"lastSales\":[");
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : marketLastSalePrices.entrySet()) {
            if (index++ > 0) {
                builder.append(',');
            }
            builder.append("{\"skin\":").append(entry.getKey())
                    .append(",\"price\":").append(entry.getValue())
                    .append("}");
        }
        builder.append("]}");
        return builder.toString();
    }

    private boolean removeInventorySkin(Account account, int skinIndex) {
        int safeSkin = Math.max(0, skinIndex);
        String[] items = account.inventory == null || account.inventory.trim().isEmpty()
                ? new String[0] : account.inventory.split(";");
        String target = "1_" + safeSkin;
        StringBuilder builder = new StringBuilder();
        boolean removed = false;
        for (String raw : items) {
            String item = raw.trim();
            if (item.isEmpty()) {
                continue;
            }
            if (!removed && target.equals(item)) {
                removed = true;
                continue;
            }
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(item);
        }
        if (removed) {
            account.inventory = builder.toString();
            if (!hasInventorySkin(account, safeSkin)) {
                setSkinOwned(account, safeSkin, false);
            }
            return true;
        }
        if (isSkinOwned(account, safeSkin)) {
            setSkinOwned(account, safeSkin, false);
            return true;
        }
        return false;
    }

    private void addInventorySkin(Account account, int skinIndex) {
        String item = "1_" + Math.max(0, skinIndex);
        if (account.inventory == null || account.inventory.trim().isEmpty()) {
            account.inventory = item;
            return;
        }
        account.inventory = account.inventory + ";" + item;
    }

    private void markSkinOwned(Account account, int skinIndex) {
        setSkinOwned(account, skinIndex, true);
    }

    private void setSkinOwned(Account account, int skinIndex, boolean ownedValue) {
        int safeSkin = Math.max(0, skinIndex);
        String[] parts = account.skins == null || account.skins.trim().isEmpty() ? new String[0] : account.skins.split(",");
        int count = Math.max(parts.length, safeSkin + 1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            boolean owned = i == safeSkin ? ownedValue : false;
            if (i != safeSkin && i < parts.length) {
                String value = parts[i].trim();
                owned = "1".equals(value) || "true".equals(value);
            }
            if (i > 0) {
                builder.append(',');
            }
            builder.append(owned ? '1' : '0');
        }
        account.skins = builder.toString();
    }

    private boolean isSkinOwned(Account account, int skinIndex) {
        String[] parts = account.skins == null || account.skins.trim().isEmpty() ? new String[0] : account.skins.split(",");
        if (skinIndex < 0 || skinIndex >= parts.length) {
            return false;
        }
        String value = parts[skinIndex].trim();
        return "1".equals(value) || "true".equals(value);
    }

    private boolean hasInventorySkin(Account account, int skinIndex) {
        String[] items = account.inventory == null || account.inventory.trim().isEmpty()
                ? new String[0] : account.inventory.split(";");
        String target = "1_" + Math.max(0, skinIndex);
        for (String raw : items) {
            if (target.equals(raw.trim())) {
                return true;
            }
        }
        return false;
    }

    private void addInventoryItem(Account account, int type, int value) {
        String item = Math.max(0, type) + "_" + Math.max(0, value);
        if (account.inventory == null || account.inventory.trim().isEmpty()) {
            account.inventory = item;
            return;
        }
        account.inventory = account.inventory + ";" + item;
    }

    private int inventoryCaseTier(Account account, int index) {
        String[] items = account.inventory == null || account.inventory.trim().isEmpty()
                ? new String[0] : account.inventory.split(";");
        if (index < 0 || index >= items.length) {
            return -1;
        }
        String[] fields = items[index].split("_", -1);
        if (fields.length != 2 || parseInt(fields[0], -1) != INVENTORY_ITEM_CASE) {
            return -1;
        }
        return clampInt(parseInt(fields[1], -1), 0, CASE_COSTS.length - 1);
    }

    private void replaceInventoryItem(Account account, int index, int type, int value) {
        String[] items = account.inventory == null || account.inventory.trim().isEmpty()
                ? new String[0] : account.inventory.split(";");
        if (index < 0 || index >= items.length) {
            return;
        }
        items[index] = Math.max(0, type) + "_" + Math.max(0, value);
        account.inventory = String.join(";", items);
    }

    private boolean boolAt(String value, int index) {
        String[] parts = value == null || value.trim().isEmpty() ? new String[0] : value.split(",");
        return index >= 0 && index < parts.length && ("1".equals(parts[index].trim()) || "true".equals(parts[index].trim()));
    }

    private String setBoolAt(String value, int index, boolean enabled, int minSize) {
        String[] parts = value == null || value.trim().isEmpty() ? new String[0] : value.split(",");
        int count = Math.max(minSize, index + 1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            boolean current = i < parts.length && ("1".equals(parts[i].trim()) || "true".equals(parts[i].trim()));
            if (i == index) {
                current = enabled;
            }
            if (i > 0) {
                builder.append(',');
            }
            builder.append(current ? '1' : '0');
        }
        return builder.toString();
    }

    private boolean isShowcaseEnabled(Account account, int skin) {
        return boolAt(account.showcase, Math.max(0, skin));
    }

    private void setShowcaseEnabled(Account account, int skin, boolean enabled) {
        account.showcase = setBoolAt(account.showcase, Math.max(0, skin), enabled, TOTAL_SKINS);
    }

    private int showcaseCount(Account account) {
        int count = 0;
        String[] parts = account.showcase == null || account.showcase.trim().isEmpty() ? new String[0] : account.showcase.split(",");
        for (String part : parts) {
            if ("1".equals(part.trim()) || "true".equals(part.trim())) {
                count++;
            }
        }
        return count;
    }

    private int skinCost(int skin) {
        if (skin <= 0 || skin >= TOTAL_SKINS) {
            return Integer.MAX_VALUE;
        }
        if (skin == 1) {
            return 850;
        }
        if (skin == 2) {
            return 1_400;
        }
        if (skin == PASS_SKIN_1F51 || skin == PASS_SKIN_1P25 || skin == PASS_SKIN_1P50) {
            return 50_000;
        }
        if (skin >= CASE_SKIN_START) {
            return 1_200 + (skin - CASE_SKIN_START) * 260;
        }
        return Integer.MAX_VALUE;
    }

    private int rollCaseSkin(int tier) {
        int safeTier = clampInt(tier, 0, CASE_COSTS.length - 1);
        int total = 0;
        for (int i = 0; i < CASE_SKIN_COUNT; i++) {
            int weight = CASE_SKIN_COUNT - i + safeTier * 3;
            total += Math.max(1, weight);
        }
        int roll = random.nextInt(Math.max(1, total));
        for (int i = 0; i < CASE_SKIN_COUNT; i++) {
            int weight = Math.max(1, CASE_SKIN_COUNT - i + safeTier * 3);
            roll -= weight;
            if (roll < 0) {
                return CASE_SKIN_START + i;
            }
        }
        return CASE_SKIN_START;
    }

    private boolean containsToken(String list, String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        String[] parts = list == null || list.trim().isEmpty() ? new String[0] : list.split(";");
        for (String part : parts) {
            if (token.equals(part.trim())) {
                return true;
            }
        }
        return false;
    }

    private String addToken(String list, String token, int maxItems) {
        String safeToken = token == null ? "" : token.trim();
        if (safeToken.isEmpty() || containsToken(list, safeToken)) {
            return list == null ? "" : list;
        }
        String current = list == null || list.trim().isEmpty() ? "" : list.trim();
        if (current.isEmpty()) {
            return safeToken;
        }
        String[] parts = current.split(";");
        StringBuilder builder = new StringBuilder(safeToken);
        int copied = 0;
        for (String part : parts) {
            String value = part.trim();
            if (value.isEmpty() || value.equals(safeToken)) {
                continue;
            }
            if (++copied >= Math.max(1, maxItems)) {
                break;
            }
            builder.append(';').append(value);
        }
        return builder.toString();
    }

    private String removeToken(String list, String token) {
        String[] parts = list == null || list.trim().isEmpty() ? new String[0] : list.split(";");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String value = part.trim();
            if (value.isEmpty() || value.equals(token)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private void broadcastChat(ClientSession sender, String text) {
        ChatEntry entry = rememberChat(sender, text);
        StringBuilder builder = new StringBuilder("{\"type\":\"chat\",");
        appendChatFields(builder, entry);
        builder.append('}');
        String message = builder.toString();
        for (ClientSession session : connections.values()) {
            session.send(message);
        }
    }

    private ChatEntry rememberChat(ClientSession sender, String text) {
        synchronized (chatHistory) {
            ChatEntry entry = new ChatEntry(nextChatSeq++,
                    sender.player.id,
                    sender.player.name,
                    text);
            chatHistory.add(entry);
            while (chatHistory.size() > 30) {
                chatHistory.remove(0);
            }
            return entry;
        }
    }

    private void resetWorldSeedAtNoon() {
        LocalDate today = LocalDate.now(WORLD_ZONE);
        LocalDateTime noon = today.atTime(12, 0);
        if (LocalDateTime.now(WORLD_ZONE).isBefore(noon) || today.equals(lastWorldSeedResetDate)) {
            return;
        }
        currentWorldSeed = Math.abs(random.nextInt());
        lastWorldSeedResetDate = today;
        resetWorldPickups();
        resetBossTimer(System.currentTimeMillis());
        for (Account account : accountsById.values()) {
            account.levelSeed = currentWorldSeed;
        }
        saveAccounts();
        for (ClientSession session : new ArrayList<>(connections.values())) {
            session.send("{\"type\":\"kick\",\"reason\":\"seed_reset\",\"seed\":" + currentWorldSeed + "}");
            session.close();
            connections.remove(session.connectionId, session);
            sessions.remove(session.player.id, session);
        }
        System.out.println(ts() + " WORLD SEED RESET seed=" + currentWorldSeed + " players kicked");
    }

    private static long currentDay() {
        return System.currentTimeMillis() / 86_400_000L;
    }

    private static LocalDate initialSeedResetDate() {
        LocalDate today = LocalDate.now(WORLD_ZONE);
        LocalDateTime now = LocalDateTime.now(WORLD_ZONE);
        return now.isBefore(today.atTime(12, 0)) ? today.minusDays(1) : today;
    }

    private static float distanceSq(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return dx * dx + dy * dy;
    }

    private float spawn() {
        return 300f + random.nextFloat() * (WORLD_SIZE - 600f);
    }

    private static String sanitizeChat(String text) {
        String cleaned = text == null ? "" : text.trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        return cleaned.length() <= 96 ? cleaned : cleaned.substring(0, 96);
    }

    private static boolean isBossCommand(String text) {
        String command = sanitizeChat(text).toLowerCase(Locale.ROOT).replace("ё", "е").trim();
        return command.equals("/boss")
                || command.equals("/boss spawn")
                || command.equals("/spawnboss")
                || command.equals("/босс")
                || command.equals("/призвать босса")
                || command.equals("/призватьбосса");
    }

    private static String sanitizePass(String value) {
        String cleaned = value == null ? "free" : value.trim().toLowerCase(Locale.ROOT);
        return "pro".equals(cleaned) ? "pro" : "free";
    }

    private static String sanitizeStateString(String value, String fallback) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            return fallback == null ? "" : fallback;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cleaned.length() && builder.length() < 512; i++) {
            char c = cleaned.charAt(i);
            if ((c >= '0' && c <= '9') || c == ',' || c == ';' || c == '-' || c == '_') {
                builder.append(c);
            }
        }
        return builder.length() == 0 ? (fallback == null ? "" : fallback) : builder.toString();
    }

    private static String sanitizeToken(String value, int maxLength) {
        String cleaned = value == null ? "" : value.trim();
        StringBuilder builder = new StringBuilder();
        int limit = Math.max(1, maxLength);
        for (int i = 0; i < cleaned.length() && builder.length() < limit; i++) {
            char c = cleaned.charAt(i);
            if ((c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || c == '-' || c == '_' || c == '.') {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private synchronized void loadAccounts() {
        if (!ACCOUNT_FILE.isFile()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ACCOUNT_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", -1);
                if (parts.length < 10) {
                    continue;
                }
                Account account = new Account();
                account.id = parts[0];
                account.name = parts[1];
                account.password = parts[2];
                account.level = parseInt(parts[3], 1);
                account.dna = Math.max(0, parseInt(parts[4], 0));
                account.levelSeed = parseInt(parts[5], currentWorldSeed);
                account.servers = parts[6];
                account.bkPass = parts[7];
                account.lastDailyDay = parseLong(parts[8], -1L);
                account.currency = parseInt(parts[9], 0);
                account.xp = parts.length > 10 ? parseInt(parts[10], 0) : 0;
                account.genes = parts.length > 11 ? sanitizeStateString(parts[11], "0,0,0,0") : "0,0,0,0";
                account.skins = parts.length > 12 ? sanitizeStateString(parts[12], "1,0,0,0,0,0") : "1,0,0,0,0,0";
                account.selectedSkin = parts.length > 13 ? parseInt(parts[13], 0) : 0;
                account.freeRewards = parts.length > 14 ? sanitizeStateString(parts[14], "") : "";
                account.proRewards = parts.length > 15 ? sanitizeStateString(parts[15], "") : "";
                account.proPassLevel = parts.length > 16 ? Math.max(1, parseInt(parts[16], 1)) : 1;
                account.passSeason = parts.length > 17 ? Math.max(1, parseInt(parts[17], 1)) : 1;
                account.showcase = parts.length > 18 ? sanitizeStateString(parts[18], "") : "";
                account.inventory = parts.length > 19 ? sanitizeStateString(parts[19], "1_0") : "1_0";
                account.friends = parts.length > 20 ? sanitizeStateString(parts[20], "") : "";
                account.friendRequests = parts.length > 21 ? sanitizeStateString(parts[21], "") : "";
                accountsById.put(account.id, account);
                accountIdByName.put(account.name.toLowerCase(Locale.ROOT), account.id);
            }
        } catch (IOException error) {
            System.out.println(ts() + " account load failed: " + error.getMessage());
        }
    }

    private synchronized void saveAccounts() {
        File dir = ACCOUNT_FILE.getParentFile();
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ACCOUNT_FILE), StandardCharsets.UTF_8))) {
            for (Account account : accountsById.values()) {
                writer.write(account.id + "\t"
                        + account.name + "\t"
                        + account.password + "\t"
                        + account.level + "\t"
                        + account.dna + "\t"
                        + account.levelSeed + "\t"
                        + account.servers + "\t"
                        + account.bkPass + "\t"
                        + account.lastDailyDay + "\t"
                        + account.currency + "\t"
                        + account.xp + "\t"
                        + account.genes + "\t"
                        + account.skins + "\t"
                        + account.selectedSkin + "\t"
                        + account.freeRewards + "\t"
                        + account.proRewards + "\t"
                        + account.proPassLevel + "\t"
                        + account.passSeason + "\t"
                        + account.showcase + "\t"
                        + account.inventory + "\t"
                        + account.friends + "\t"
                        + account.friendRequests);
                writer.newLine();
            }
        } catch (IOException error) {
            System.out.println(ts() + " account save failed: " + error.getMessage());
        }
    }

    private synchronized void loadMarket() {
        if (!MARKET_FILE.isFile()) {
            return;
        }
        int maxId = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(MARKET_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", -1);
                if (parts.length == 0) {
                    continue;
                }
                if ("L".equals(parts[0]) && parts.length >= 6) {
                    int id = parseInt(parts[1], 0);
                    int skin = Math.max(0, parseInt(parts[2], 0));
                    int price = Math.max(100, parseInt(parts[3], 100));
                    String ownerId = parts[4];
                    String ownerName = sanitizeName(parts[5]);
                    marketListings.add(new MarketListing(id, skin, price, ownerId, ownerName));
                    maxId = Math.max(maxId, id);
                } else if ("S".equals(parts[0]) && parts.length >= 3) {
                    marketLastSalePrices.put(Math.max(0, parseInt(parts[1], 0)), Math.max(100, parseInt(parts[2], 100)));
                }
            }
            nextMarketListingId = Math.max(nextMarketListingId, maxId + 1);
        } catch (IOException error) {
            System.out.println(ts() + " market load failed: " + error.getMessage());
        }
    }

    private synchronized void saveMarket() {
        File dir = MARKET_FILE.getParentFile();
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(MARKET_FILE), StandardCharsets.UTF_8))) {
            synchronized (marketListings) {
                for (MarketListing listing : marketListings) {
                    writer.write("L\t" + listing.id + "\t"
                            + listing.skinIndex + "\t"
                            + listing.price + "\t"
                            + listing.ownerId + "\t"
                            + listing.ownerName);
                    writer.newLine();
                }
            }
            for (Map.Entry<Integer, Integer> entry : marketLastSalePrices.entrySet()) {
                writer.write("S\t" + entry.getKey() + "\t" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException error) {
            System.out.println(ts() + " market save failed: " + error.getMessage());
        }
    }

    private synchronized void loadDonations() {
        if (!DONATION_FILE.isFile()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(DONATION_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", -1);
                if (parts.length < 7) {
                    continue;
                }
                DonationOrder order = new DonationOrder(
                        sanitizeToken(parts[0], 80),
                        parts[1],
                        sanitizeToken(parts[2], 80),
                        sanitizeToken(parts[3], 80),
                        Math.max(0, parseInt(parts[4], 0)),
                        parts[5],
                        "1".equals(parts[6])
                );
                if (!order.orderId.isEmpty()) {
                    donationOrders.put(order.orderId, order);
                }
            }
        } catch (IOException error) {
            System.out.println(ts() + " donation load failed: " + error.getMessage());
        }
    }

    private synchronized void saveDonations() {
        File dir = DONATION_FILE.getParentFile();
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DONATION_FILE), StandardCharsets.UTF_8))) {
            for (DonationOrder order : donationOrders.values()) {
                writer.write(order.orderId + "\t"
                        + order.paymentId + "\t"
                        + order.accountId + "\t"
                        + order.packId + "\t"
                        + order.coins + "\t"
                        + order.rub + "\t"
                        + (order.credited ? "1" : "0"));
                writer.newLine();
            }
        } catch (IOException error) {
            System.out.println(ts() + " donation save failed: " + error.getMessage());
        }
    }

    private void printConnectionTable() {
        long now = System.currentTimeMillis();
        if (now - lastConnectionDump < 500L) {
            return;
        }
        lastConnectionDump = now;
        ArrayList<ClientSession> snapshot = new ArrayList<>(connections.values());
        snapshot.sort(Comparator.comparing(session -> session.player.id));

        System.out.println(ts() + " active connections: " + snapshot.size());
        if (snapshot.isEmpty()) {
            return;
        }
        for (ClientSession session : snapshot) {
            Player player = session.player;
            String line = String.format(
                    Locale.US,
                    "  - id=%s name=%s remote=%s pos=(%.1f, %.1f) moves=%d last=%s",
                    player.id,
                    player.name,
                    session.remoteAddress,
                    player.x,
                    player.y,
                    session.moveMessages,
                    session.lastAction
            );
            System.out.println(line);
        }
    }

    private static void log(ClientSession session, String action, String details) {
        System.out.println(ts() + " [" + session.player.id + " " + session.remoteAddress + "] " + action + " " + details);
    }

    private static String ts() {
        return Instant.now().toString();
    }

    private static String sanitizeName(String name) {
        String cleaned = name == null ? "" : name.trim();
        if (cleaned.isEmpty()) {
            return "USERNAME";
        }
        return cleaned.length() <= 16 ? cleaned : cleaned.substring(0, 16);
    }

    private static String readString(String json, String key, String fallback) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return fallback;
        }
        int colon = json.indexOf(':', keyIndex + pattern.length());
        int firstQuote = json.indexOf('"', colon + 1);
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (colon < 0 || firstQuote < 0 || secondQuote < 0) {
            return fallback;
        }
        return json.substring(firstQuote + 1, secondQuote);
    }

    private static float readFloat(String json, String key, float fallback) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return fallback;
        }
        int colon = json.indexOf(':', keyIndex + pattern.length());
        if (colon < 0) {
            return fallback;
        }
        int end = colon + 1;
        while (end < json.length() && " -0123456789.".indexOf(json.charAt(end)) >= 0) {
            end++;
        }
        try {
            return Float.parseFloat(json.substring(colon + 1, end).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int readInt(String json, String key, int fallback) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return fallback;
        }
        int colon = json.indexOf(':', keyIndex + pattern.length());
        if (colon < 0) {
            return fallback;
        }
        int end = colon + 1;
        while (end < json.length() && " -0123456789".indexOf(json.charAt(end)) >= 0) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(colon + 1, end).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String number(float value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class Player {
        private String id;
        private String name;
        private float x;
        private float y;
        private final float radius;
        private float hp = 100f;
        private int level = 1;
        private int dna = 0;
        private long lastSeen = System.currentTimeMillis();
        private long nextBigDnaTickMs;
        private String bigDnaZonePickupId = "";
        private long bigDnaZoneEnteredMs;

        private Player(String id, String name, float x, float y, float radius) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    private static final class DnaPickup {
        private final String id;
        private final float x;
        private final float y;
        private final int value;
        private final String kind;
        private final float radius;
        private final float zoneRadius;
        private boolean active = true;

        private DnaPickup(String id, float x, float y, int value, String kind, float radius, float zoneRadius) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.value = value;
            this.kind = kind;
            this.radius = radius;
            this.zoneRadius = zoneRadius;
        }
    }

    private static final class MarketListing {
        private final int id;
        private final int skinIndex;
        private final int price;
        private final String ownerId;
        private final String ownerName;

        private MarketListing(int id, int skinIndex, int price, String ownerId, String ownerName) {
            this.id = Math.max(1, id);
            this.skinIndex = Math.max(0, skinIndex);
            this.price = Math.max(100, price);
            this.ownerId = ownerId == null ? "" : ownerId;
            this.ownerName = ownerName == null || ownerName.trim().isEmpty() ? "player" : ownerName.trim();
        }
    }

    private static final class DonationPack {
        private final String id;
        private final String title;
        private final String rub;
        private final int coins;

        private DonationPack(String id, String title, String rub, int coins) {
            this.id = id;
            this.title = title;
            this.rub = rub;
            this.coins = coins;
        }
    }

    private static final class DonationOrder {
        private final String orderId;
        private final String paymentId;
        private final String accountId;
        private final String packId;
        private final int coins;
        private final String rub;
        private boolean credited;

        private DonationOrder(String orderId, String paymentId, String accountId, String packId, int coins, String rub, boolean credited) {
            this.orderId = orderId == null ? "" : orderId;
            this.paymentId = paymentId == null ? "" : paymentId;
            this.accountId = accountId == null ? "" : accountId;
            this.packId = packId == null ? "" : packId;
            this.coins = Math.max(0, coins);
            this.rub = rub == null ? "0.00" : rub;
            this.credited = credited;
        }
    }

    private static final class YooKassaPayment {
        private final String id;
        private final String confirmationUrl;

        private YooKassaPayment(String id, String confirmationUrl) {
            this.id = id == null ? "" : id;
            this.confirmationUrl = confirmationUrl == null ? "" : confirmationUrl;
        }
    }

    private static final class ChatEntry {
        private final int seq;
        private final String id;
        private final String name;
        private final String text;

        private ChatEntry(int seq, String id, String name, String text) {
            this.seq = Math.max(1, seq);
            this.id = id == null ? "" : id;
            this.name = name == null || name.trim().isEmpty() ? "player" : name.trim();
            this.text = text == null ? "" : text;
        }
    }

    private static final class ClientSession {
        private final String connectionId;
        private final Socket socket;
        private final Player player;
        private final BufferedReader reader;
        private final BufferedWriter writer;
        private final String remoteAddress;
        private Account account;
        private volatile String lastAction = "connected";
        private volatile int moveMessages;

        private ClientSession(Socket socket, Player player, String connectionId) throws IOException {
            this.connectionId = connectionId;
            this.socket = socket;
            this.player = player;
            this.remoteAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        private synchronized void send(String message) {
            try {
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch (IOException ignored) {
                close();
            }
        }

        private void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static final class Account {
        private String id;
        private String name;
        private String password;
        private int level;
        private int dna;
        private int levelSeed;
        private String servers;
        private String bkPass;
        private long lastDailyDay = -1L;
        private int currency;
        private int xp;
        private String genes = "0,0,0,0";
        private String skins = "1,0,0,0,0,0";
        private int selectedSkin;
        private String freeRewards = "";
        private String proRewards = "";
        private int proPassLevel = 1;
        private int passSeason = 1;
        private String showcase = "";
        private String inventory = "1_0";
        private String friends = "";
        private String friendRequests = "";
    }
}
