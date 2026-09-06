package ru.bacteria.online5;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Build;
import android.text.InputType;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BacteriaMenuView extends View {

    private static final int COLOR_BACKGROUND = Color.rgb(22, 23, 23);
    private static final int COLOR_PANEL = Color.rgb(26, 27, 27);
    private static final int COLOR_PANEL_DARK = Color.rgb(18, 19, 19);
    private static final int COLOR_PANEL_SOFT = Color.rgb(31, 32, 32);
    private static final int COLOR_TEXT = Color.WHITE;
    private static final int COLOR_MUTED = Color.rgb(140, 140, 140);
    private static final int COLOR_GREEN = Color.rgb(128, 216, 90);
    private static final int COLOR_GREEN_DARK = Color.rgb(95, 180, 67);
    private static final int COLOR_FIELD = Color.rgb(14, 15, 15);
    private static final int DAILY_REWARD = 150;
    private static final float MIN_SAFE_TOP_DP = 18f;
    private static final String PREFS_NAME = "bacteria_online_menu";
    private static final String GAME_SERVER_HOST = "31.207.76.76";
    private static final String GAME_SERVER_HTTP_URL = "http://31.207.76.76:5056";
    private static final int GAME_SERVER_PORT = 5055;
    private static final long DAY_MS = 86_400_000L;
    private static final int WORLD_SEED = 50_505;
    private static final int WORLD_TILES = 100;
    private static final float WORLD_TILE_SIZE = 100f;
    private static final float WORLD_SIZE = WORLD_TILES * WORLD_TILE_SIZE;
    private static final float PLAYER_WIDTH = 65f;
    private static final float PLAYER_HEIGHT = 150f;
    private static final float PLAYER_MIN_WIDTH = 50f;
    private static final float PLAYER_MIN_HEIGHT = 125f;
    private static final float PLAYER_COLLISION_RADIUS = 0.44f;
    private static final float DNA_HEAL_PER_POINT = 2.5f;
    private static final int PLAYER_MESH_SEGMENTS = 36;
    private static final float PLAYER_MESH_DENT_DEPTH = 12f;
    private static final float PLAYER_MESH_BULGE_DEPTH = 6f;
    private static final float[][] PLAYER_BODY_FITS = {
            {PLAYER_WIDTH, PLAYER_HEIGHT},
            {58f, PLAYER_HEIGHT},
            {PLAYER_WIDTH, 138f},
            {54f, 136f},
            {PLAYER_MIN_WIDTH, 132f},
            {58f, PLAYER_MIN_HEIGHT},
            {PLAYER_MIN_WIDTH, PLAYER_MIN_HEIGHT}
    };
    private static final int COLOR_PLAYER = Color.rgb(169, 255, 103);
    private static final int COLOR_BLOCK = Color.rgb(131, 213, 92);
    private static final int COLOR_BLACK = Color.rgb(0, 0, 0);
    private static final int COLOR_WORLD_BORDER = Color.rgb(204, 90, 90);
    private static final int COLOR_WORLD_BACKGROUND = Color.rgb(28, 28, 28);
    private static final int COLOR_GRID = Color.rgb(34, 35, 35);
    private static final int GENE_MAX_LEVEL = 10;
    private static final int GENE_SPIKES = 3;
    private static final int POWER_SPEED = 0;
    private static final int POWER_DNA = 1;
    private static final int POWER_TELEPORT = 2;
    private static final long POWER_DURATION_MS = 5_000L;
    private static final long BOSS_SPAWN_INTERVAL_MS = 30L * 60L * 1000L;
    private static final int BOSS_START_DNA = 2_000;
    private static final int BOSS_DEFEAT_DNA = 100;
    private static final float BOSS_DAMAGE_DNA_HALF = 0.50f;
    private static final float BOSS_DAMAGE_REDUCTION = 0.10f;
    private static final float BOSS_WIDTH = 260f;
    private static final float BOSS_HEIGHT = 360f;
    private static final float BOSS_CONTACT_RADIUS = 190f;
    private static final float BOSS_SPEED = 74f;
    private static final float SERVER_BIG_DNA_ZONE_RADIUS = 280f;
    private static final float SERVER_BIG_DNA_EAT_RADIUS = 78f;
    private static final int MAX_BOSS_BORDER_INSET_TILES = 5;
    private static final int PASS_SEASON = 1;
    private static final int PASS_SKIN_1F51 = 3;
    private static final int PASS_SKIN_1P25 = 4;
    private static final int PASS_SKIN_1P50 = 5;
    private static final int CASE_SKIN_START = 6;
    private static final int CASE_SKIN_COUNT = 50;
    private static final int TOTAL_SKINS = CASE_SKIN_START + CASE_SKIN_COUNT;
    private static final int INVENTORY_ITEM_CASE = 0;
    private static final int INVENTORY_ITEM_SKIN = 1;
    private static final int CASE_TIER_2K = 0;
    private static final int CASE_TIER_5K = 1;
    private static final int CASE_TIER_10K = 2;
    private static final int CASE_TIER_50K = 3;
    private static final int CASE_TIER_100K = 4;
    private static final int[] CASE_COSTS = {2_000, 5_000, 10_000, 50_000, 100_000};
    private static final int[] DONATION_COINS = {2_000, 5_000, 10_000, 50_000, 100_000};
    private static final String[] DONATION_RUB = {"50", "90", "175", "875", "1750"};
    private static final String[] DONATION_SALE = {"", "-28%", "-30%", "-30%", "-30%"};
    private static final int CASE_REEL_RESULT_SLOT = 26;
    private static final int CASE_REEL_SLOT_COUNT = 38;
    private static final int MARKET_MAX_SHOWCASE = 6;
    private static final int[] SKIN_COSTS = buildSkinCosts();
    private static final int[] SKIN_COLORS = buildSkinColors();
    private static final String[] MEDAL_CODES = {"1f50", "1p10", "1p25", "1p50"};
    private static final int[] MUSIC_TRACKS = {
            R.raw.music_quad_bi_tron,
            R.raw.music_jesters_imaginary_friend,
            R.raw.music_work_in_progress,
            R.raw.music_subway_01,
            R.raw.music_solitude
    };
    private static final String[] MUSIC_TITLES = {
            "Quad-Bi Tron",
            "a Jester's Imaginary Friend",
            "Work In Progress Hours...",
            "Subway -01",
            "SOLITUDE"
    };
    private static final String[] MUSIC_AUTHORS = {
            "kubernikus18",
            "RaineEera",
            "RaineEera",
            "Canys",
            "alteregoFL"
    };
    private static final String[] MUSIC_URLS = {
            "https://www.newgrounds.com/audio/listen/1569022",
            "https://www.newgrounds.com/audio/listen/1568966",
            "https://www.newgrounds.com/audio/listen/1568963",
            "https://www.newgrounds.com/audio/listen/1568946",
            "https://www.newgrounds.com/audio/listen/1568859"
    };
    private static final String[] MUSIC_CREDITS = {
            "Quad-Bi Tron — kubernikus18 — https://www.newgrounds.com/audio/listen/1569022",
            "a Jester's Imaginary Friend — RaineEera — https://www.newgrounds.com/audio/listen/1568966",
            "Work In Progress Hours... — RaineEera — https://www.newgrounds.com/audio/listen/1568963",
            "Subway -01 — Canys — https://www.newgrounds.com/audio/listen/1568946",
            "SOLITUDE — alteregoFL — https://www.newgrounds.com/audio/listen/1568859"
    };
    private static final int PRO_PASS_COST = 50_000;
    private static final int SOUND_DNA_EAT = 0;
    private static final int SOUND_ENEMY_KILL = 1;
    private static final int TILE_EMPTY = 0;
    private static final int TILE_SOFT = 1;
    private static final int TILE_DENSE = 2;
    private static final int TILE_WALL = 3;
    private static final int TILE_DNA = 4;
    private static final int LOCAL_DNA_TARGET = 1200;
    private static final int[][][] GENERATION_BLOCKS = {
            {
                    {1, 1, 1, 0},
                    {0, 2, 1, 0},
                    {0, 1, 1, 0},
                    {0, 0, 0, 0}
            },
            {
                    {3, 1, 1, 3},
                    {1, 0, 0, 1},
                    {1, 0, 2, 1},
                    {3, 1, 1, 3}
            },
            {
                    {0, 2, 2, 0},
                    {2, 1, 1, 2},
                    {0, 1, 1, 0},
                    {0, 0, 0, 0}
            },
            {
                    {1, 0, 1, 0},
                    {1, 1, 1, 0},
                    {0, 1, 2, 1},
                    {0, 0, 1, 0}
            },
            {
                    {0, 0, 3, 0},
                    {1, 1, 3, 1},
                    {0, 2, 1, 1},
                    {0, 0, 0, 1}
            }
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF avatarRect = new RectF();
    private final Path path = new Path();
    private final Path meshPath = new Path();
    private final Matrix shaderMatrix = new Matrix();
    private final Typeface titleFont = Typeface.create("sans-serif-black", Typeface.BOLD);
    private final Typeface boldFont = Typeface.create("sans-serif-condensed", Typeface.BOLD);
    private final Typeface baseFont = Typeface.create("sans-serif", Typeface.NORMAL);
    private final SharedPreferences prefs;
    private final Bitmap dnaBitmap;
    private final Bitmap powerSpeedBitmap;
    private final Bitmap powerDnaBitmap;
    private final Bitmap powerTeleportBitmap;
    private final Bitmap[] medalBitmaps;
    private final Bitmap[] caseBitmaps;
    private final Bitmap[] skinBitmaps;
    private final Bitmap tabMainBitmap;
    private final Bitmap tabRewardsBitmap;
    private final Bitmap tabProfileBitmap;
    private final Bitmap tabEditorBitmap;
    private final Bitmap tabShopBitmap;
    private final Bitmap tabLeaderboardBitmap;
    private final Bitmap tabMusicBitmap;
    private final Bitmap tabSettingsBitmap;
    private final ConcurrentLinkedQueue<String> serverInbox = new ConcurrentLinkedQueue<>();
    private final long animationStartMs = System.currentTimeMillis();
    private long menuTransitionStartMs = System.currentTimeMillis();

    private final List<RectF> tabRects = new ArrayList<>();
    private final List<RectF> serverRects = new ArrayList<>();
    private final List<RectF> geneUpgradeRects = new ArrayList<>();
    private final List<RectF> skinRects = new ArrayList<>();
    private final List<Integer> skinRectIndices = new ArrayList<>();
    private final List<RectF> friendProfileRects = new ArrayList<>();
    private final List<Integer> friendProfileIndices = new ArrayList<>();
    private final List<RectF> friendRequestAcceptRects = new ArrayList<>();
    private final List<RectF> friendRequestDeclineRects = new ArrayList<>();
    private final List<RectF> musicOpenRects = new ArrayList<>();
    private final List<Integer> friendRequestIndices = new ArrayList<>();
    private final List<String> friendIds = new ArrayList<>();
    private final List<String> friendRequestIds = new ArrayList<>();
    private final List<String> chatMessages = new ArrayList<>();
    private final List<CellOpponent> opponents = new ArrayList<>();
    private final List<PowerUpPickup> powerUps = new ArrayList<>();
    private final List<BossDnaOrb> bossDnaOrbs = new ArrayList<>();
    private final List<GameParticle> gameParticles = new ArrayList<>();
    private final List<ServerDnaPickup> serverDnaPickups = new ArrayList<>();
    private final List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
    private final List<LeaderboardEntry> serverLeaderboardEntries = new ArrayList<>();
    private final List<LeaderboardEntry> menuLeaderboardEntries = new ArrayList<>();
    private final List<InventoryItem> inventoryItems = new ArrayList<>();
    private final List<RectF> inventoryItemRects = new ArrayList<>();
    private final List<Integer> inventoryItemIndices = new ArrayList<>();
    private final List<RectF> caseShopRects = new ArrayList<>();
    private final List<Integer> caseShopTiers = new ArrayList<>();
    private final List<RectF> donationRects = new ArrayList<>();
    private final List<Integer> donationPacks = new ArrayList<>();
    private final List<MarketListing> marketListings = new ArrayList<>();
    private final List<RectF> marketBuyRects = new ArrayList<>();
    private final List<Integer> marketBuyIndices = new ArrayList<>();
    private final List<RectF> marketSkinRects = new ArrayList<>();
    private final List<Integer> marketSkinIndices = new ArrayList<>();
    private final List<RectF> marketOrderBuyRects = new ArrayList<>();
    private final List<Integer> marketOrderBuyIndices = new ArrayList<>();
    private final ConcurrentLinkedQueue<String> serverOutbox = new ConcurrentLinkedQueue<>();
    private final int[][] worldTiles = new int[WORLD_TILES][WORLD_TILES];
    private final Random worldRandom = new Random(50_505L);
    private final Random effectsRandom = new Random();

    private Screen screen = Screen.AUTH;
    private FocusField focusField = FocusField.NONE;
    private String composingInput = "";
    private int selectedServer = 0;
    private int coins = 0;
    private boolean dailyClaimed = false;
    private boolean musicEnabled = true;
    private boolean soundsEnabled = true;
    private int musicVolume = 80;
    private int effectsVolume = 80;
    private int activeSettingsSlider = 0;
    private int controlMode = 0;
    private int graphicsQuality = 1;
    private int playerLevel = 1;
    private int selectedSkin = 0;
    private boolean proPassOwned = false;
    private int passSeason = PASS_SEASON;
    private int proPassLevel = 1;
    private int shopMode = 0;
    private int selectedInventorySkin = -1;
    private int selectedInventoryItemIndex = -1;
    private int selectedMarketSkin = -1;
    private int modalListingPrice = 0;
    private boolean caseOpeningActive = false;
    private long caseOpeningStartMs = 0L;
    private int caseOpeningInventoryIndex = -1;
    private int caseOpeningTier = CASE_TIER_2K;
    private int caseOpeningResultSkin = 0;
    private int[] caseOpeningReel = new int[0];
    private String username = "";
    private String email = "";
    private String password = "";
    private boolean sessionSaved = false;
    private String statusMessage = "";
    private String chatDraft = "";
    private int lastServerChatSeq = 0;
    private String friendDraft = "";
    private String latestNewsText = "текст новости, который я напишу на сервере";
    private String latestNewsDate = "20.05.2026";
    private String accountId = "";
    private String avatarData = "";
    private Bitmap avatarBitmap;
    private boolean deathOverlayActive = false;
    private String deathKillerName = "";
    private String deathKillerProfileId = "";
    private int deathKillerColor = COLOR_GREEN;
    private boolean viewingExternalProfile = false;
    private String externalProfileName = "";
    private String externalProfileId = "";
    private int externalProfileColor = COLOR_GREEN;
    private volatile Socket gameSocket;
    private volatile BufferedWriter gameWriter;
    private volatile boolean gameServerConnecting = false;
    private volatile boolean gameServerConnected = false;
    private volatile boolean gameServerWanted = false;
    private volatile boolean pendingDailyClaim = false;
    private boolean waitingForAuthentication = false;
    private volatile boolean serverSenderRunning = false;
    private boolean worldGenerated = false;
    private int worldSeed = WORLD_SEED;
    private float playerX = WORLD_SIZE * 0.5f;
    private float playerY = WORLD_SIZE * 0.5f;
    private float playerRadius = PLAYER_HEIGHT * 0.5f;
    private float playerAngleDeg = 0f;
    private float playerBodyWidth = PLAYER_WIDTH;
    private float playerBodyHeight = PLAYER_HEIGHT;
    private float playerBodyTargetWidth = PLAYER_WIDTH;
    private float playerBodyTargetHeight = PLAYER_HEIGHT;
    private float playerDentWorldX = 0f;
    private float playerDentWorldY = 0f;
    private float playerDentTargetWorldX = 0f;
    private float playerDentTargetWorldY = 0f;
    private float playerDentStrength = 0f;
    private float playerDentTargetStrength = 0f;
    private float playerMeshWave = 0f;
    private final float[] playerMeshOffsetX = new float[PLAYER_MESH_SEGMENTS];
    private final float[] playerMeshOffsetY = new float[PLAYER_MESH_SEGMENTS];
    private final float[] playerMeshTargetOffsetX = new float[PLAYER_MESH_SEGMENTS];
    private final float[] playerMeshTargetOffsetY = new float[PLAYER_MESH_SEGMENTS];
    private final float[] playerMeshPointX = new float[PLAYER_MESH_SEGMENTS];
    private final float[] playerMeshPointY = new float[PLAYER_MESH_SEGMENTS];
    private int playerXp = 0;
    private int dnaEaten = 0;
    private int totalDnaEarned = 0;
    private float playerHp = 100f;
    private long playerHitCooldownMs = 0L;
    private long speedBoostUntilMs = 0L;
    private long dnaBoostUntilMs = 0L;
    private final WorldBoss boss = new WorldBoss();
    private long nextBossSpawnMs = 0L;
    private int serverBossSpawnId = 0;
    private int worldInsetLeftTiles = 0;
    private int worldInsetTopTiles = 0;
    private int worldInsetRightTiles = 0;
    private int worldInsetBottomTiles = 0;
    private float moveX = 0f;
    private float moveY = 0f;
    private float aimX = 0f;
    private float aimY = 0f;
    private float leftJoyBaseX;
    private float leftJoyBaseY;
    private float leftJoyKnobX;
    private float leftJoyKnobY;
    private float rightJoyBaseX;
    private float rightJoyBaseY;
    private float rightJoyKnobX;
    private float rightJoyKnobY;
    private int leftPointerId = -1;
    private int rightPointerId = -1;
    private long lastGameFrameMs = 0L;
    private long lastServerMoveMs = 0L;
    private long lastServerLeaderboardMs = 0L;
    private long lastMarketRequestMs = 0L;
    private long lastBigDnaTickMs = 0L;
    private ServerDnaPickup serverBigDna;

    private final int[] geneLevels = {0, 0, 0, 0};
    private final boolean[] skinOwned = new boolean[TOTAL_SKINS];
    private final boolean[] skinShowcase = new boolean[TOTAL_SKINS];
    private final boolean[] freePassRewardsClaimed = new boolean[52];
    private final boolean[] proPassRewardsClaimed = new boolean[51];
    private final String[] geneNames = {"скорость", "масса", "иммунитет", "шипы"};
    private final String[] geneDescriptions = {
            "быстрее передвигается по карте",
            "увеличивает размер и силу",
            "снижает урон и ускоряет лечение",
            "отражают часть урона"
    };
    private final String[] skinNames = buildSkinNames();
    private final int[] skinLastSalePrices = buildDefaultSalePrices();

    private final RectF authPrimaryRect = new RectF();
    private final RectF authSwitchRect = new RectF();
    private final RectF authUserRect = new RectF();
    private final RectF authEmailRect = new RectF();
    private final RectF authPasswordRect = new RectF();
    private final RectF playRect = new RectF();
    private final RectF rewardClaimRect = new RectF();
    private final RectF avatarButtonRect = new RectF();
    private final RectF musicRect = new RectF();
    private final RectF soundsRect = new RectF();
    private final RectF musicVolumeSliderRect = new RectF();
    private final RectF effectsVolumeSliderRect = new RectF();
    private final RectF controlRect = new RectF();
    private final RectF graphicsRect = new RectF();
    private final RectF loadServerRect = new RectF();
    private final RectF saveServerRect = new RectF();
    private final RectF logoutRect = new RectF();
    private final RectF chatInputRect = new RectF();
    private final RectF friendInputRect = new RectF();
    private final RectF friendAddRect = new RectF();
    private final RectF friendsTabRect = new RectF();
    private final RectF friendRequestsTabRect = new RectF();
    private final RectF medalsViewportRect = new RectF();
    private final RectF friendsViewportRect = new RectF();
    private final RectF buyProPassRect = new RectF();
    private final RectF battlePassOpenRect = new RectF();
    private final RectF battlePassBackRect = new RectF();
    private final RectF shopInventoryTabRect = new RectF();
    private final RectF shopDnaTabRect = new RectF();
    private final RectF shopMarketTabRect = new RectF();
    private final RectF caseOpenRect = new RectF();
    private final RectF shopViewportRect = new RectF();
    private final RectF editorInventoryViewportRect = new RectF();
    private final RectF skinModalRect = new RectF();
    private final RectF modalEquipRect = new RectF();
    private final RectF modalMarketRect = new RectF();
    private final RectF modalShowcaseRect = new RectF();
    private final RectF modalPriceMinusRect = new RectF();
    private final RectF modalPricePlusRect = new RectF();
    private final RectF modalCloseRect = new RectF();
    private final RectF marketDetailRect = new RectF();
    private final RectF marketDetailCloseRect = new RectF();
    private final RectF caseOpeningModalRect = new RectF();
    private final RectF caseOpeningClaimRect = new RectF();
    private final RectF externalProfileCloseRect = new RectF();
    private final RectF externalProfileMessageRect = new RectF();
    private final RectF deathMenuRect = new RectF();
    private final RectF deathProfileRect = new RectF();
    private final RectF deathRestartRect = new RectF();
    private final RectF gamePauseRect = new RectF();
    private MediaPlayer musicPlayer;
    private Equalizer musicEqualizer;
    private SoundPool soundPool;
    private int dnaEatSoundId;
    private int enemyKillSoundId;
    private final Random musicRandom = new Random();
    private int currentMusicTrack;
    private int lastMusicTrack = -1;
    private float medalsScrollX = 0f;
    private float friendsScrollY = 0f;
    private int friendPanelTab = 0;
    private float profileDownX = 0f;
    private float profileDownY = 0f;
    private float profileStartMedalsScrollX = 0f;
    private float profileStartFriendsScrollY = 0f;
    private int profileDragMode = 0;
    private boolean profileDragged = false;
    private float shopScrollY = 0f;
    private float shopDownY = 0f;
    private float shopStartScrollY = 0f;
    private boolean shopDragged = false;
    private boolean shopDragActive = false;
    private float editorInventoryScrollY = 0f;
    private float editorInventoryDownY = 0f;
    private float editorInventoryStartScrollY = 0f;
    private boolean editorInventoryDragged = false;
    private boolean editorInventoryDragActive = false;
    private long fpsWindowStartMs = 0L;
    private int fpsFrameCount = 0;
    private int displayedFps = 0;
    private long pingSentAtMs = 0L;
    private long nextPingAtMs = 0L;
    private int displayedPingMs = -1;
    private long bossSpawnFlashUntilMs = 0L;
    private long bossShakeUntilMs = 0L;
    private long deathEffectUntilMs = 0L;
    private long nextLocalDnaReplenishMs = 0L;
    private int localDnaCount = 0;
    private float safeInsetLeft = 0f;
    private float safeInsetTop = 0f;
    private float safeInsetRight = 0f;
    private float safeInsetBottom = 0f;

    public BacteriaMenuView(Context context) {
        super(context);
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        dnaBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dna);
        powerSpeedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.power_speed);
        powerDnaBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.power_dna);
        powerTeleportBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.power_teleport);
        medalBitmaps = new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.medal_1f50),
                BitmapFactory.decodeResource(getResources(), R.drawable.medal_1p10),
                BitmapFactory.decodeResource(getResources(), R.drawable.medal_1p25),
                BitmapFactory.decodeResource(getResources(), R.drawable.medal_1p50)
        };
        caseBitmaps = new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.case_2k),
                BitmapFactory.decodeResource(getResources(), R.drawable.case_5k),
                BitmapFactory.decodeResource(getResources(), R.drawable.case_10k),
                BitmapFactory.decodeResource(getResources(), R.drawable.case_50k),
                BitmapFactory.decodeResource(getResources(), R.drawable.case_100k)
        };
        skinBitmaps = new Bitmap[skinNames.length];
        skinBitmaps[3] = BitmapFactory.decodeResource(getResources(), R.drawable.skin_1f51);
        skinBitmaps[4] = BitmapFactory.decodeResource(getResources(), R.drawable.skin_1p25);
        skinBitmaps[5] = BitmapFactory.decodeResource(getResources(), R.drawable.skin_1p50);
        tabMainBitmap = loadTabIcon(R.drawable.nav_home);
        tabRewardsBitmap = loadTabIcon(R.drawable.nav_calendar);
        tabProfileBitmap = loadTabIcon(R.drawable.nav_profile);
        tabEditorBitmap = loadTabIcon(R.drawable.nav_inventory);
        tabShopBitmap = loadTabIcon(R.drawable.nav_market);
        tabLeaderboardBitmap = loadTabIcon(R.drawable.nav_trophy);
        tabMusicBitmap = loadTabIcon(R.drawable.nav_info);
        tabSettingsBitmap = loadTabIcon(R.drawable.nav_settings);
        setFocusable(true);
        setFocusableInTouchMode(true);
        applyDefaultSafeInsets();
        loadState();
        dnaEaten = 0;
        skinOwned[0] = true;
        ensurePassSeason();
        ensureInventorySeededFromOwnedSkins();
        saveState();
        requestMarketState(false);
        restoreSavedSession();
        initSoundEffects();
        startMusicIfNeeded();
        post(this::requestApplyInsets);
    }

    private Bitmap loadTabIcon(int resourceId) {
        Drawable drawable = getResources().getDrawable(resourceId);
        Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void pauseAudio() {
        pauseMusic();
    }

    public void resumeAudio() {
        startMusicIfNeeded();
    }

    public boolean handleBackPressed() {
        if (screen == Screen.GAME) {
            exitGameToMainMenu();
            return true;
        }
        if (screen != Screen.AUTH && screen != Screen.MAIN) {
            setScreen(Screen.MAIN);
            focusField = FocusField.NONE;
            hideKeyboard();
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        applySafeInsets(insets);
        return insets;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float fullW = getWidth();
        float fullH = getHeight();
        float w = safeContentWidth();
        float h = safeContentHeight();
        float scale = Math.min(w / 1600f, h / 900f);
        drainServerMessages();

        if (screen == Screen.GAME) {
            canvas.drawColor(COLOR_WORLD_BACKGROUND);
            int save = canvas.save();
            canvas.translate(safeInsetLeft, safeInsetTop);
            if (!deathOverlayActive) {
                updateGame();
            }
            drawGame(canvas, w, h, scale);
            canvas.restoreToCount(save);
            postInvalidateOnAnimation();
            return;
        }

        drawBackground(canvas, fullW, fullH, scale);
        int save = canvas.save();
        canvas.translate(safeInsetLeft, safeInsetTop);
        drawHeader(canvas, w, scale);

        if (screen == Screen.AUTH) {
            int contentSave = beginMenuContentAnimation(canvas, w, h, scale);
            drawAuth(canvas, w, h, scale);
            canvas.restoreToCount(contentSave);
            canvas.restoreToCount(save);
            postInvalidateOnAnimation();
            return;
        }

        if (screen != Screen.PASS) {
            drawTabs(canvas, h, scale);
        }
        int contentSave = beginMenuContentAnimation(canvas, w, h, scale);
        drawWallet(canvas, w, scale);
        if (screen == Screen.MAIN) {
            drawMain(canvas, w, h, scale);
        } else if (screen == Screen.PASS) {
            drawBattlePassScreen(canvas, w, h, scale);
        } else if (screen == Screen.REWARDS) {
            drawRewards(canvas, w, h, scale);
        } else if (screen == Screen.PROFILE) {
            drawProfile(canvas, w, h, scale);
        } else if (screen == Screen.SHOP) {
            drawShopV2(canvas, w, h, scale);
        } else if (screen == Screen.LEADERBOARD) {
            drawLeaderboardScreen(canvas, w, h, scale);
        } else if (screen == Screen.EDITOR) {
            drawCharacterEditor(canvas, w, h, scale);
        } else if (screen == Screen.MUSIC) {
            drawMusicLibrary(canvas, w, h, scale);
        } else if (screen == Screen.SETTINGS) {
            drawSettings(canvas, w, h, scale);
        }
        canvas.restoreToCount(contentSave);
        canvas.restoreToCount(save);
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        disconnectGameServer();
        releaseMusicPlayer();
        releaseSoundEffects();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        MotionEvent localEvent = MotionEvent.obtain(event);
        localEvent.offsetLocation(-safeInsetLeft, -safeInsetTop);
        try {
            if (screen == Screen.GAME) {
                return handleGameTouch(localEvent);
            }
            if (screen == Screen.SETTINGS && handleSettingsSliderTouch(localEvent)) {
                return true;
            }
            if (screen == Screen.PROFILE && handleProfileScrollTouch(localEvent)) {
                return true;
            }
            if (screen == Screen.SHOP && handleShopScrollTouch(localEvent)) {
                return true;
            }
            if (screen == Screen.EDITOR && handleEditorInventoryScrollTouch(localEvent)) {
                return true;
            }

            if (localEvent.getAction() != MotionEvent.ACTION_UP) {
                return true;
            }

            float x = localEvent.getX();
            float y = localEvent.getY();
            requestFocus();

            if (screen == Screen.AUTH) {
                handleAuthTouch(x, y);
                return true;
            }

            for (int i = 0; i < tabRects.size(); i++) {
                if (tabRects.get(i).contains(x, y)) {
                    Screen nextScreen = tabScreenAt(i);
                    setScreen(nextScreen);
                    if (screen == Screen.PROFILE) {
                        viewingExternalProfile = false;
                    }
                    statusMessage = "";
                    hideKeyboard();
                    invalidate();
                    return true;
                }
            }

            if (screen == Screen.MAIN) {
                handleMainTouch(x, y);
            } else if (screen == Screen.PASS) {
                handlePassTouch(x, y);
            } else if (screen == Screen.REWARDS) {
                handleRewardsTouch(x, y);
            } else if (screen == Screen.PROFILE) {
                handleProfileTouch(x, y);
            } else if (screen == Screen.SHOP) {
                handleShopTouchV2(x, y);
            } else if (screen == Screen.EDITOR) {
                handleEditorTouch(x, y);
            } else if (screen == Screen.MUSIC) {
                handleMusicTouch(x, y);
            } else if (screen == Screen.SETTINGS) {
                handleSettingsTouch(x, y);
            }
            return true;
        } finally {
            localEvent.recycle();
        }
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return focusField != FocusField.NONE;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI | imeActionForFocusedField();
        int cursor = focusedInputValue().length();
        outAttrs.initialSelStart = cursor;
        outAttrs.initialSelEnd = cursor;
        if (focusField == FocusField.PASSWORD) {
            outAttrs.inputType |= InputType.TYPE_TEXT_VARIATION_PASSWORD;
        } else if (focusField == FocusField.EMAIL) {
            outAttrs.inputType |= InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        } else if (focusField == FocusField.CHAT) {
            outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEND;
            outAttrs.inputType |= InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
        }

        return new BaseInputConnection(this, false) {
            @Override
            public boolean performEditorAction(int actionCode) {
                if (handleImeAction(actionCode)) {
                    return true;
                }
                return super.performEditorAction(actionCode);
            }

            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                commitInput(text == null ? "" : text.toString());
                return true;
            }

            @Override
            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                replaceComposingInput(text == null ? "" : text.toString());
                return true;
            }

            @Override
            public boolean finishComposingText() {
                composingInput = "";
                return true;
            }

            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                backspaceInput();
                return true;
            }

            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    backspaceInput();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        handleImeAction(focusField == FocusField.CHAT ? EditorInfo.IME_ACTION_SEND : EditorInfo.IME_ACTION_DONE);
                    }
                    return true;
                }
                return super.sendKeyEvent(event);
            }
        };
    }

    private void applyDefaultSafeInsets() {
        float density = getResources().getDisplayMetrics().density;
        safeInsetLeft = 0f;
        safeInsetTop = MIN_SAFE_TOP_DP * density;
        safeInsetRight = 0f;
        safeInsetBottom = 0f;
    }

    private void applySafeInsets(WindowInsets insets) {
        if (insets == null) {
            applyDefaultSafeInsets();
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        float left = insets.getSystemWindowInsetLeft();
        float top = insets.getSystemWindowInsetTop();
        float right = insets.getSystemWindowInsetRight();
        float bottom = insets.getSystemWindowInsetBottom();
        if (Build.VERSION.SDK_INT >= 28) {
            DisplayCutout cutout = insets.getDisplayCutout();
            if (cutout != null) {
                left = Math.max(left, cutout.getSafeInsetLeft());
                top = Math.max(top, cutout.getSafeInsetTop());
                right = Math.max(right, cutout.getSafeInsetRight());
                bottom = Math.max(bottom, cutout.getSafeInsetBottom());
            }
        }
        safeInsetLeft = left;
        safeInsetTop = Math.max(top, MIN_SAFE_TOP_DP * density);
        safeInsetRight = right;
        safeInsetBottom = bottom;
        invalidate();
    }

    private float safeContentWidth() {
        return Math.max(1f, getWidth() - safeInsetLeft - safeInsetRight);
    }

    private float safeContentHeight() {
        return Math.max(1f, getHeight() - safeInsetTop - safeInsetBottom);
    }

    private void drawBackground(Canvas canvas, float w, float h, float scale) {
        canvas.drawColor(COLOR_BACKGROUND);
        drawMenuBackgroundBubbles(canvas, w, h, scale);
    }

    private float animationSeconds() {
        return (System.currentTimeMillis() - animationStartMs) / 1000f;
    }

    private void setScreen(Screen next) {
        if (next == null || screen == next) {
            return;
        }
        screen = next;
        menuTransitionStartMs = System.currentTimeMillis();
    }

    private float menuTransitionProgress() {
        return clamp((System.currentTimeMillis() - menuTransitionStartMs) / 620f, 0f, 1f);
    }

    private float menuStaggerProgress(int index, int total) {
        float offset = Math.max(0f, index) * 0.055f;
        float available = Math.max(0.18f, 1f - offset);
        return clamp((menuTransitionProgress() - offset) / available, 0f, 1f);
    }

    private float easeOutCubic(float value) {
        float t = clamp(value, 0f, 1f);
        float inv = 1f - t;
        return 1f - inv * inv * inv;
    }

    private float easeOutBack(float value) {
        float t = clamp(value, 0f, 1f) - 1f;
        return 1f + t * t * ((1.72f + 1f) * t + 1.72f);
    }

    private int beginMenuContentAnimation(Canvas canvas, float w, float h, float scale) {
        float progress = menuTransitionProgress();
        float eased = easeOutBack(progress);
        int alpha = clampInt(Math.round(60f + 195f * easeOutCubic(progress)), 0, 255);
        int save = canvas.saveLayerAlpha(0f, 0f, w, h, alpha);
        float y = (1f - easeOutCubic(progress)) * 34f * scale;
        float x = (1f - easeOutCubic(progress)) * transitionDirection() * 28f * scale;
        float grow = 0.985f + Math.min(1f, eased) * 0.015f;
        canvas.translate(x, y);
        canvas.scale(grow, grow, w * 0.5f, h * 0.55f);
        return save;
    }

    private float transitionDirection() {
        int ordinal = screen == null ? 0 : screen.ordinal();
        return (ordinal % 2 == 0) ? -1f : 1f;
    }

    private void drawMenuBackgroundBubbles(Canvas canvas, float w, float h, float scale) {
        float time = animationSeconds();
        int count = 30;
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        for (int i = 0; i < count; i++) {
            float seed = fract(i * 0.371f + 0.19f);
            float speed = 0.025f + seed * 0.035f;
            float phase = fract(time * speed + seed * 2.7f);
            float drift = (float) Math.sin(time * (0.18f + seed * 0.12f) + i * 0.9f) * 34f * scale;
            float x = w * fract(seed * 9.13f + i * 0.083f) + drift;
            float y = h + 48f * scale - phase * (h + 120f * scale);
            float radius = (9f + fract(seed * 13.1f + i) * 34f) * scale;
            int alpha = clampInt(Math.round(12f + 26f * (1f - phase)), 8, 42);
            int color = i % 4 == 0 ? Color.rgb(128, 216, 90) : Color.rgb(58, 62, 58);
            paint.setColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawCircle(x, y, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1f, 1.2f * scale));
            paint.setColor(Color.argb(alpha + 16, 128, 216, 90));
            canvas.drawCircle(x, y, radius * 0.72f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        paint.setAlpha(255);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawAnimatedGreenRoundRect(Canvas canvas, RectF bounds, float radiusX, float radiusY, float scale) {
        drawAnimatedGreenRoundRect(canvas, bounds, radiusX, radiusY, scale, 1f);
    }

    private void drawAnimatedGreenRoundRect(Canvas canvas, RectF bounds, float radiusX, float radiusY, float scale, float intensity) {
        float time = animationSeconds();
        float pulse = 0.5f + 0.5f * (float) Math.sin(time * 2.2f + bounds.left * 0.013f + bounds.top * 0.017f);
        int fill = blendColor(COLOR_GREEN_DARK, COLOR_GREEN, 0.72f + pulse * 0.28f);
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setColor(fill);
        canvas.drawRoundRect(bounds, radiusX, radiusY, paint);

        paint.setColor(Color.argb((int) (30f * intensity + pulse * 24f * intensity), 255, 255, 255));
        RectF shine = new RectF(bounds.left, bounds.top, bounds.right, bounds.top + bounds.height() * 0.34f);
        canvas.drawRoundRect(shine, radiusX, radiusY, paint);

        int save = canvas.save();
        path.reset();
        path.addRoundRect(bounds, radiusX, radiusY, Path.Direction.CW);
        canvas.clipPath(path);
        int bubbles = clampInt((int) (bounds.width() * bounds.height() / Math.max(1f, 15500f * scale * scale)), 3, 16);
        for (int i = 0; i < bubbles; i++) {
            float seed = fract(bounds.left * 0.031f + bounds.top * 0.047f + i * 0.173f);
            float speed = 0.16f + seed * 0.16f;
            float travel = bounds.height() + 34f * scale;
            float phase = fract(time * speed + seed + i * 0.219f);
            float wobble = (float) Math.sin(time * (1.1f + seed) + i * 1.7f) * 5f * scale;
            float x = bounds.left + bounds.width() * (0.12f + fract(seed * 7.37f + i * 0.19f) * 0.76f) + wobble;
            float y = bounds.bottom + 16f * scale - phase * travel;
            float radius = (3.5f + fract(seed * 11.3f + i) * 8.5f) * scale;
            int alpha = (int) ((34f + 42f * (1f - phase)) * intensity);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(clampInt(alpha, 18, 92), 255, 255, 255));
            canvas.drawCircle(x, y, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1f, 1.3f * scale));
            paint.setColor(Color.argb(clampInt(alpha + 26, 30, 128), 255, 255, 255));
            canvas.drawCircle(x, y, radius, paint);
        }
        canvas.restoreToCount(save);
        paint.setAlpha(255);
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawAnimatedGreenPath(Canvas canvas, Path shape, RectF bounds, float scale) {
        float time = animationSeconds();
        float pulse = 0.5f + 0.5f * (float) Math.sin(time * 2.1f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(blendColor(COLOR_GREEN_DARK, COLOR_GREEN, 0.78f + pulse * 0.22f));
        canvas.drawPath(shape, paint);

        int save = canvas.save();
        canvas.clipPath(shape);
        for (int i = 0; i < 8; i++) {
            float seed = fract(i * 0.217f + bounds.left * 0.03f);
            float phase = fract(time * (0.14f + seed * 0.1f) + seed);
            float x = bounds.left + bounds.width() * (0.18f + fract(seed * 8.1f) * 0.64f);
            float y = bounds.bottom + 18f * scale - phase * (bounds.height() + 36f * scale);
            float r = (3f + seed * 7f) * scale;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb((int) (34f + 48f * (1f - phase)), 255, 255, 255));
            canvas.drawCircle(x, y, r, paint);
        }
        canvas.restoreToCount(save);
        paint.setStyle(Paint.Style.FILL);
    }

    private float fract(float value) {
        return value - (float) Math.floor(value);
    }

    private int blendColor(int from, int to, float amount) {
        float t = clamp(amount, 0f, 1f);
        return Color.rgb(
                clampColor(Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * t)),
                clampColor(Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * t)),
                clampColor(Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t))
        );
    }

    private void drawHeader(Canvas canvas, float w, float scale) {
        float headerH = 100f * scale;
        rect.set(0f, 0f, w, headerH);
        paint.setColor(COLOR_PANEL_SOFT);
        canvas.drawRoundRect(rect, 24f * scale, 24f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setColor(COLOR_TEXT);
        paint.setTextSize(64f * scale);
        String title = headerTitle();
        if (screen == Screen.MAIN || screen == Screen.AUTH) {
            canvas.drawText(title, 78f * scale, 72f * scale, paint);
        } else {
            drawCenteredText(canvas, title, w * 0.32f, 72f * scale, paint);
        }
    }

    private String headerTitle() {
        if (screen == Screen.SHOP) {
            return "рынок";
        }
        if (screen == Screen.LEADERBOARD) {
            return "лидерборд";
        }
        if (screen == Screen.PROFILE) {
            return "профиль";
        }
        if (screen == Screen.EDITOR) {
            return "редактор";
        }
        if (screen == Screen.REWARDS) {
            return "ежедневка";
        }
        if (screen == Screen.PASS) {
            return "БК pass";
        }
        if (screen == Screen.SETTINGS) {
            return "настройки";
        }
        if (screen == Screen.MUSIC) {
            return "музыка";
        }
        return "бактерии онлайн";
    }

    private float menuContentTop(float scale) {
        return 106f * scale;
    }

    private float menuContentBottom(float h, float scale) {
        return Math.max(menuContentTop(scale) + 320f * scale, h - 130f * scale);
    }

    private float menuContentHeight(float h, float scale) {
        return menuContentBottom(h, scale) - menuContentTop(scale);
    }

    private void drawWallet(Canvas canvas, float w, float scale) {
        String coinText = String.valueOf(coins);
        String levelText = String.valueOf(playerLevel());
        String xpText = playerXpInLevel() + "/" + xpForCurrentLevel();

        paint.setTypeface(titleFont);
        paint.setTextSize(36f * scale);
        float coinTextW = paint.measureText(coinText);
        float coinW = Math.max(73f * scale, coinTextW + 38f * scale);

        paint.setTextSize(64f * scale);
        float levelTextW = paint.measureText(levelText);
        paint.setTypeface(boldFont);
        paint.setTextSize(15f * scale);
        float xpTextW = paint.measureText(xpText);
        float xpBarW = Math.max(230f * scale, xpTextW + 44f * scale);
        float levelW = Math.max(300f * scale, levelTextW + xpBarW + 83f * scale);

        float avatarSize = 70f * scale;
        float avatarRight = w - 30f * scale;
        RectF headerAvatar = new RectF(avatarRight - avatarSize, 14f * scale, avatarRight, 14f * scale + avatarSize);
        float levelRight = headerAvatar.left - 18f * scale;
        RectF levelRect = new RectF(levelRight - levelW, 15f * scale, levelRight, 84f * scale);
        RectF coinRect = new RectF(levelRect.left - 18f * scale - coinW, 16f * scale, levelRect.left - 18f * scale, 85f * scale);

        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(coinRect, 25f * scale, 25f * scale, paint);
        paint.setTypeface(titleFont);
        paint.setTextSize(36f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, coinText, coinRect.centerX(), coinRect.centerY() + 12f * scale, paint);

        rect.set(levelRect);
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(rect, 50f * scale, 50f * scale, paint);
        paint.setTypeface(titleFont);
        paint.setTextSize(64f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText(levelText, levelRect.left + 25f * scale, levelRect.centerY() + 21f * scale, paint);

        float xpLeft = levelRect.left + 45f * scale + levelTextW;
        RectF xpBack = new RectF(xpLeft, levelRect.top + 22f * scale, xpLeft + xpBarW, levelRect.top + 47f * scale);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(xpBack, 50f * scale, 50f * scale, paint);
        float xpWidth = xpBack.width() * levelProgress();
        RectF xpFill = new RectF(xpBack.left, xpBack.top, xpBack.left + xpWidth, xpBack.bottom);
        paint.setColor(Color.rgb(196, 204, 210));
        canvas.drawRoundRect(xpFill, 50f * scale, 50f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(15f * scale);
        paint.setColor(Color.argb(210, 255, 255, 255));
        drawCenteredText(canvas, xpText, xpBack.centerX(), xpBack.centerY() + 5f * scale, paint);

        paint.setColor(COLOR_FIELD);
        canvas.drawRoundRect(headerAvatar, 24f * scale, 24f * scale, paint);
        drawRewardMicrobe(canvas, headerAvatar.centerX(), headerAvatar.centerY(), playerBaseColor(), scale * 0.62f);
    }

    private void drawAuth(Canvas canvas, float w, float h, float scale) {
        float panelW = 560f * scale;
        float panelH = 430f * scale;
        float left = (w - panelW) * 0.5f;
        float top = (h - panelH) * 0.55f;

        rect.set(left, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(42f * scale);
        paint.setColor(COLOR_TEXT);
        String title = "вход";
        drawCenteredText(canvas, title, left + panelW * 0.5f, top + 66f * scale, paint);

        paint.setTypeface(baseFont);
        paint.setTextSize(18f * scale);
        paint.setColor(COLOR_MUTED);
        drawCenteredText(canvas, "новый логин создаст аккаунт автоматически", left + panelW * 0.5f, top + 96f * scale, paint);

        float fieldW = 430f * scale;
        float fieldH = 64f * scale;
        float fieldLeft = left + (panelW - fieldW) * 0.5f;
        float fieldTop = top + 132f * scale;
        authUserRect.set(fieldLeft, fieldTop, fieldLeft + fieldW, fieldTop + fieldH);
        drawInput(canvas, authUserRect, "USERNAME", username, focusField == FocusField.USERNAME, scale, false);

        authEmailRect.setEmpty();

        fieldTop += 80f * scale;
        authPasswordRect.set(fieldLeft, fieldTop, fieldLeft + fieldW, fieldTop + fieldH);
        drawInput(canvas, authPasswordRect, "PASSWORD", password, focusField == FocusField.PASSWORD, scale, true);

        authPrimaryRect.set(fieldLeft, fieldTop + 92f * scale, fieldLeft + fieldW, fieldTop + 158f * scale);
        drawAnimatedGreenRoundRect(canvas, authPrimaryRect, 25f * scale, 25f * scale, scale);
        paint.setTypeface(titleFont);
        paint.setTextSize(36f * scale);
        paint.setColor(Color.rgb(14, 16, 14));
        drawCenteredText(canvas, "войти", authPrimaryRect.centerX(), authPrimaryRect.centerY() + 13f * scale, paint);

        authSwitchRect.setEmpty();
    }

    private void drawMain(Canvas canvas, float w, float h, float scale) {
        serverRects.clear();
        drawBattlePassPreview(canvas, scale);

        float panelW = 300f * scale;
        float panelH = 305f * scale;
        float left = w - panelW - 38f * scale;
        float top = h - panelH - 22f * scale;

        rect.set(left, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(40f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, "сервера", left + panelW * 0.5f, top + 38f * scale, paint);

        paint.setColor(Color.rgb(31, 32, 32));
        canvas.drawRect(left, top + 47f * scale, left + panelW, top + 205f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("оффициальные", left + 25f * scale, top + 70f * scale, paint);
        drawServerRow(canvas, left + 25f * scale, top + 95f * scale, panelW - 50f * scale, 60f * scale, "Россия (СПБ)", 0, scale);

        playRect.set(left, top + 205f * scale, left + panelW, top + panelH);
        drawAnimatedGreenRoundRect(canvas, playRect, 0f, 0f, scale);
        rect.set(left, top + 225f * scale, left + panelW, top + panelH);
        drawAnimatedGreenRoundRect(canvas, rect, 25f * scale, 25f * scale, scale);

        paint.setTypeface(titleFont);
        paint.setTextSize(56f * scale);
        paint.setColor(Color.rgb(14, 16, 14));
        drawCenteredText(canvas, "играть", playRect.centerX(), playRect.centerY() + 20f * scale, paint);

        drawStatus(canvas, w, h, scale);
    }

    private void drawBattlePassPreview(Canvas canvas, float scale) {
        float panelW = 600f * scale;
        float panelH = 130f * scale;
        float left = 0f;
        float top = 138f * scale;

        rect.set(left - 24f * scale, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(44f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("БК pass", left + 22f * scale, top + 56f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(18f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("Награды за уровни и Pro-пропуск", left + 24f * scale, top + 88f * scale, paint);

        battlePassOpenRect.set(left + 392f * scale, top + 38f * scale, left + 570f * scale, top + 94f * scale);
        drawButton(canvas, battlePassOpenRect, "открыть", COLOR_FIELD, COLOR_TEXT, scale, 17f);
        buyProPassRect.setEmpty();
    }

    private void drawBattlePassScreen(Canvas canvas, float w, float h, float scale) {
        float left = 0f;
        float top = menuContentTop(scale);
        float panelW = w;
        float panelH = menuContentHeight(h, scale);
        rect.set(left - 24f * scale, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        battlePassBackRect.set(left + panelW - 152f * scale, top + 18f * scale,
                left + panelW - 38f * scale, top + 66f * scale);
        drawButton(canvas, battlePassBackRect, "назад", COLOR_FIELD, COLOR_TEXT, scale, 15f);

        float contentLeft = left + 46f * scale;
        float contentWidth = panelW - 92f * scale;
        paint.setTypeface(titleFont);
        paint.setTextSize(42f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("Бесплатный pass", contentLeft, top + 58f * scale, paint);
        paint.setTypeface(boldFont);
        paint.setTextSize(19f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("Открыт для всех игроков", contentLeft, top + 86f * scale, paint);
        drawPassTrack(canvas, contentLeft, top + 112f * scale, contentWidth, "", false, scale);

        float proTop = top + Math.max(300f * scale, panelH * 0.52f);
        paint.setTypeface(titleFont);
        paint.setTextSize(42f * scale);
        paint.setColor(proPassOwned ? COLOR_TEXT : Color.rgb(135, 135, 135));
        canvas.drawText("Pro pass", contentLeft, proTop + 42f * scale, paint);
        if (proPassOwned) {
            paint.setTypeface(boldFont);
            paint.setTextSize(19f * scale);
            paint.setColor(COLOR_MUTED);
            canvas.drawText("Pro-пропуск активен", contentLeft, proTop + 70f * scale, paint);
            buyProPassRect.setEmpty();
        } else {
            paint.setTypeface(boldFont);
            paint.setTextSize(19f * scale);
            paint.setColor(Color.rgb(130, 130, 130));
            canvas.drawText("Закрыто. Откройте за 50 000 ДНК", contentLeft, proTop + 70f * scale, paint);
            buyProPassRect.set(contentLeft + contentWidth - 186f * scale, proTop + 16f * scale,
                    contentLeft + contentWidth, proTop + 72f * scale);
            drawButton(canvas, buyProPassRect, "купить", COLOR_FIELD, COLOR_TEXT, scale, 17f);
        }
        drawPassTrack(canvas, contentLeft, proTop + 98f * scale, contentWidth, "", true, scale);
        drawStatus(canvas, w, h, scale);
    }

    private void drawPassTrack(Canvas canvas, float left, float top, float width, String label, boolean pro, float scale) {
        boolean trackUnlocked = !pro || proPassOwned;
        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(trackUnlocked ? COLOR_TEXT : Color.rgb(115, 115, 115));
        canvas.drawText(label, left, top - 10f * scale, paint);

        float trackLeft = left + (pro ? 63f : 0f) * scale;
        RectF track = new RectF(trackLeft, top, left + width, top + 96f * scale);
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(track, 21f * scale, 21f * scale, paint);

        int passLevel = pro ? proBattlePassLevel() : battlePassLevel();
        int claimed = passClaimedCount(pro);
        float fill = trackUnlocked ? clamp(passLevel / 50f, 0f, 1f) : 0f;
        rect.set(track.left, track.top, track.left + track.width() * fill, track.bottom);
        if (trackUnlocked && !pro) {
            paint.setColor(COLOR_GREEN);
            canvas.drawRoundRect(rect, 21f * scale, 21f * scale, paint);
        } else {
            paint.setColor(trackUnlocked ? COLOR_GREEN : Color.rgb(43, 43, 43));
            canvas.drawRoundRect(rect, 21f * scale, 21f * scale, paint);
        }

        if (!trackUnlocked) {
            paint.setColor(Color.argb(155, 0, 0, 0));
            canvas.drawRoundRect(track, 21f * scale, 21f * scale, paint);
        }

        int[] marks = pro ? new int[]{10, 25, 50} : new int[]{50};
        for (int mark : marks) {
            float x = track.left + track.width() * (mark / 50f);
            int skinIndex = passRewardSkinIndex(pro, mark);
            if (skinIndex >= 0) {
                RectF skinReward = new RectF(x - 16f * scale, track.centerY() - 16f * scale,
                        x + 16f * scale, track.centerY() + 16f * scale);
                drawSkinSwatch(canvas, skinReward, 12f * scale, skinIndex, trackUnlocked, scale);
            } else {
                paint.setColor(rewardClaimed(pro, mark) ? COLOR_TEXT : Color.rgb(76, 76, 76));
                canvas.drawCircle(x, track.centerY(), 14f * scale, paint);
            }
            paint.setTypeface(boldFont);
            paint.setTextSize(13f * scale);
            paint.setColor(trackUnlocked ? COLOR_PANEL_DARK : Color.rgb(92, 92, 92));
            drawCenteredText(canvas, String.valueOf(mark), x, track.centerY() + 5f * scale, paint);
        }

        paint.setTypeface(boldFont);
        paint.setTextSize(17f * scale);
        paint.setColor(trackUnlocked ? COLOR_MUTED : Color.rgb(100, 100, 100));
        String reward = (trackUnlocked ? "получено " + claimed + "/50 · " : "закрыто · ")
                + (pro ? "100 валюты/ур. · 10/25/50 медали · 25/50 скины" : "50 валюты/ур. · 50 медаль и скин");
        canvas.drawText(reward, track.left + 14f * scale, track.bottom + 28f * scale, paint);
    }

    private int passRewardSkinIndex(boolean pro, int level) {
        if (!pro && level == 50) {
            return PASS_SKIN_1F51;
        }
        if (pro && level == 25) {
            return PASS_SKIN_1P25;
        }
        if (pro && level == 50) {
            return PASS_SKIN_1P50;
        }
        return -1;
    }

    private boolean rewardClaimed(boolean pro, int level) {
        if (pro) {
            return level > 0 && level < proPassRewardsClaimed.length && proPassRewardsClaimed[level];
        }
        return level > 0 && level < freePassRewardsClaimed.length && freePassRewardsClaimed[level];
    }

    private void drawNewsPanel(Canvas canvas, float w, float h, float scale) {
        float panelW = 300f * scale;
        float panelH = 345f * scale;
        float left = w - panelW - 38f * scale;
        float top = 106f * scale;

        rect.set(left, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRect(left, top + 28f * scale, left + panelW, top + 44f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(25f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, "новости", left + panelW * 0.5f, top + 23f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(25f * scale);
        paint.setColor(COLOR_TEXT);
        drawWrappedCenteredText(canvas, latestNewsText, left + 18f * scale, top + 72f * scale, panelW - 36f * scale, 29f * scale, 8, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(25f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, "дата: " + latestNewsDate, left + panelW * 0.5f, top + panelH - 10f * scale, paint);
    }

    private void drawRewards(Canvas canvas, float w, float h, float scale) {
        float left = 0f;
        float top = menuContentTop(scale);
        float panelW = w;
        float panelH = menuContentHeight(h, scale);

        rect.set(left - 24f * scale, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(23f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("валюта тратится на прокачку генов, кейсы и бк pass", left + 42f * scale, top + 52f * scale, paint);

        float gap = 18f * scale;
        float rowLeft = left + 38f * scale;
        float rowRight = left + panelW - 38f * scale;
        float itemW = Math.max(94f * scale, (rowRight - rowLeft - gap * 6f) / 7f);
        float rowTop = top + 94f * scale;
        float itemH = Math.min(220f * scale, Math.max(142f * scale, panelH - 270f * scale));
        for (int i = 0; i < 7; i++) {
            float itemLeft = rowLeft + i * (itemW + gap);
            rect.set(itemLeft, rowTop, itemLeft + itemW, rowTop + itemH);
            paint.setColor(i == 0 && !dailyClaimed ? Color.rgb(36, 42, 34) : COLOR_PANEL_DARK);
            canvas.drawRoundRect(rect, 18f * scale, 18f * scale, paint);

            paint.setTypeface(titleFont);
            paint.setTextSize(28f * scale);
            paint.setColor(i == 0 && !dailyClaimed ? COLOR_GREEN : COLOR_MUTED);
            drawCenteredText(canvas, "день " + (i + 1), rect.centerX(), rowTop + 48f * scale, paint);

            drawRewardMicrobe(canvas, rect.centerX(), rowTop + itemH * 0.52f, i == 0 && !dailyClaimed ? COLOR_GREEN : Color.rgb(90, 90, 90), scale * 1.25f);

            paint.setTypeface(boldFont);
            paint.setTextSize(20f * scale);
            paint.setColor(i == 0 && !dailyClaimed ? COLOR_TEXT : COLOR_MUTED);
            String label = i == 0 ? (dailyClaimed ? "забрано" : "+" + DAILY_REWARD) : "скоро";
            drawCenteredText(canvas, label, rect.centerX(), rowTop + itemH - 24f * scale, paint);
        }

        rewardClaimRect.set(left + 38f * scale, top + panelH - 116f * scale, left + 330f * scale, top + panelH - 44f * scale);
        if (dailyClaimed) {
            paint.setColor(Color.rgb(52, 52, 52));
            canvas.drawRoundRect(rewardClaimRect, 25f * scale, 25f * scale, paint);
        } else {
            drawAnimatedGreenRoundRect(canvas, rewardClaimRect, 25f * scale, 25f * scale, scale);
        }
        paint.setTypeface(titleFont);
        paint.setTextSize(32f * scale);
        paint.setColor(dailyClaimed ? COLOR_MUTED : Color.rgb(14, 16, 14));
        drawCenteredText(canvas, dailyClaimed ? "забрано" : "забрать", rewardClaimRect.centerX(), rewardClaimRect.centerY() + 11f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("серия: 1 день", left + 360f * scale, top + panelH - 74f * scale, paint);
        drawStatus(canvas, w, h, scale);
    }

    private void drawProfile(Canvas canvas, float w, float h, float scale) {
        geneUpgradeRects.clear();
        skinRects.clear();
        float panelW = w;
        float panelH = menuContentHeight(h, scale);
        float left = 0f;
        float top = menuContentTop(scale);
        float leftW = Math.min(412f * scale, panelW * 0.29f);
        float centerW = Math.max(430f * scale, panelW - leftW - 12f * scale);

        rect.set(left - 24f * scale, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        float avatar = Math.min(250f * scale, leftW - 50f * scale);
        avatarRect.set(left + 25f * scale, top + 20f * scale, left + 25f * scale + avatar, top + 20f * scale + avatar);
        if (viewingExternalProfile) {
            paint.setColor(Color.rgb(214, 214, 214));
            canvas.drawRoundRect(avatarRect, 25f * scale, 25f * scale, paint);
            drawRewardMicrobe(canvas, avatarRect.centerX(), avatarRect.centerY(), externalProfileColor, scale * 2.2f);
        } else {
            drawAvatar(canvas, avatarRect, scale);
        }
        avatarButtonRect.setEmpty();

        paint.setTypeface(titleFont);
        String profileName = viewingExternalProfile ? externalProfileName : displayName();
        drawProfileName(canvas, profileName, left + 34f * scale, top + avatar + 64f * scale, leftW - 70f * scale, scale);

        drawProfileMedals(canvas, left + 24f * scale, top + avatar + 82f * scale, leftW - 50f * scale, scale);

        float centerLeft = left + leftW;
        drawProfileGenePanel(canvas, centerLeft, top + 12f * scale, centerW, panelH - 24f * scale, scale);

        friendProfileRects.clear();
        friendProfileIndices.clear();
        friendRequestAcceptRects.clear();
        friendRequestDeclineRects.clear();
        friendRequestIndices.clear();
        friendInputRect.setEmpty();
        friendAddRect.setEmpty();
        friendsTabRect.setEmpty();
        friendRequestsTabRect.setEmpty();
        friendsViewportRect.setEmpty();

        if (viewingExternalProfile) {
            drawExternalProfileIsland(canvas, w, h, scale);
        } else {
            avatarButtonRect.set(left + 25f * scale, top + panelH - 68f * scale, left + leftW - 25f * scale, top + panelH - 18f * scale);
            drawButton(canvas, avatarButtonRect, "загрузить аватар", COLOR_GREEN, COLOR_BACKGROUND, scale, 14f);
        }

        drawStatus(canvas, w, h, scale);
    }

    private void drawExternalProfileIsland(Canvas canvas, float w, float h, float scale) {
        paint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(0f, 0f, w, h, paint);

        float panelW = 720f * scale;
        float panelH = 470f * scale;
        float left = (w - panelW) * 0.5f;
        float top = (h - panelH) * 0.52f;
        rect.set(left, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        RectF extAvatar = new RectF(left + 28f * scale, top + 34f * scale, left + 178f * scale, top + 184f * scale);
        paint.setColor(Color.rgb(214, 214, 214));
        canvas.drawRoundRect(extAvatar, 18f * scale, 18f * scale, paint);
        drawRewardMicrobe(canvas, extAvatar.centerX(), extAvatar.centerY(), externalProfileColor, scale * 1.75f);

        drawProfileName(canvas, externalProfileName, left + 205f * scale, top + 72f * scale, 310f * scale, scale);
        paint.setTypeface(boldFont);
        paint.setTextSize(18f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("профиль игрока", left + 205f * scale, top + 104f * scale, paint);

        externalProfileCloseRect.set(left + panelW - 64f * scale, top + 22f * scale, left + panelW - 22f * scale, top + 64f * scale);
        drawButton(canvas, externalProfileCloseRect, "x", COLOR_FIELD, COLOR_TEXT, scale, 10f);

        int seed = Math.abs((externalProfileId + externalProfileName).hashCode());
        externalProfileMessageRect.set(left + 205f * scale, top + 126f * scale, left + 420f * scale, top + 178f * scale);
        drawButton(canvas, externalProfileMessageRect, "написать", COLOR_GREEN, Color.rgb(14, 16, 14), scale, 14f);

        paint.setTypeface(titleFont);
        paint.setTextSize(20f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("медали", left + 448f * scale, top + 130f * scale, paint);
        for (int i = 0; i < medalCount(); i++) {
            boolean active = ((seed >> (i + 1)) & 1) == 1 || medalActive(i);
            drawMedal(canvas, left + (448f + i * 48f) * scale, top + 144f * scale, i, active, medalLabel(i), scale);
        }

        float geneLeft = left + 30f * scale;
        float geneTop = top + 220f * scale;
        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("прокачка", geneLeft, geneTop - 14f * scale, paint);
        for (int i = 0; i < geneNames.length; i++) {
            int value = (seed / (i + 3)) % (GENE_MAX_LEVEL + 1);
            RectF bar = new RectF(geneLeft, geneTop + i * 38f * scale, geneLeft + 250f * scale, geneTop + i * 38f * scale + 22f * scale);
            paint.setColor(COLOR_FIELD);
            canvas.drawRoundRect(bar, 12f * scale, 12f * scale, paint);
            rect.set(bar.left, bar.top, bar.left + bar.width() * (value / (float) GENE_MAX_LEVEL), bar.bottom);
            paint.setColor(COLOR_GREEN);
            canvas.drawRoundRect(rect, 12f * scale, 12f * scale, paint);
            paint.setTypeface(boldFont);
            paint.setTextSize(14f * scale);
            paint.setColor(COLOR_TEXT);
            canvas.drawText(geneNames[i] + " " + value, bar.right + 12f * scale, bar.top + 17f * scale, paint);
        }

        float showcaseLeft = left + 405f * scale;
        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("витрина", showcaseLeft, geneTop - 14f * scale, paint);
        int shown = 0;
        for (int i = 0; i < skinShowcase.length && shown < MARKET_MAX_SHOWCASE; i++) {
            if (!skinShowcase[i]) {
                continue;
            }
            float sx = showcaseLeft + (shown % 3) * 72f * scale;
            float sy = geneTop + (shown / 3) * 86f * scale;
            RectF skin = new RectF(sx, sy, sx + 52f * scale, sy + 62f * scale);
            drawSkinSwatch(canvas, skin, 20f * scale, i, true, scale);
            paint.setTypeface(boldFont);
            paint.setTextSize(11f * scale);
            paint.setColor(COLOR_TEXT);
            drawCenteredText(canvas, skinNames[i], skin.centerX(), skin.bottom + 16f * scale, paint);
            shown++;
        }
        if (shown == 0) {
            paint.setTypeface(titleFont);
            paint.setTextSize(20f * scale);
            paint.setColor(COLOR_MUTED);
            canvas.drawText("скинов нет", showcaseLeft, geneTop + 42f * scale, paint);
        }

    }

    private void drawProfileGenePanel(Canvas canvas, float left, float top, float width, float height, float scale) {
        rect.set(left, top, left + width, top + height);
        paint.setColor(COLOR_BACKGROUND);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(31f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("гены персонажа", left + 22f * scale, top + 42f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(18f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("валюта: " + coins, left + width - 154f * scale, top + 39f * scale, paint);

        for (int i = 0; i < geneNames.length; i++) {
            drawGeneRow(canvas, left + 18f * scale, top + (62f + i * 86f) * scale, width - 36f * scale, i, scale);
        }
    }

    private void drawProfileName(Canvas canvas, String name, float left, float baseline, float width, float scale) {
        String safeName = name == null || name.trim().isEmpty() ? "USERNAME" : name.trim();
        paint.setTypeface(titleFont);
        float nameSize = fitTextSize(safeName, 33f * scale, width, titleFont);
        paint.setTypeface(titleFont);
        paint.setTextSize(nameSize);
        paint.setColor(COLOR_TEXT);
        canvas.drawText(safeName, left, baseline, paint);
    }

    private void drawProfileMedals(Canvas canvas, float left, float top, float width, float scale) {
        int activeCount = activeMedalCount();
        if (activeCount <= 0) {
            medalsViewportRect.setEmpty();
            medalsScrollX = 0f;
            return;
        }

        medalsViewportRect.set(left, top + 24f * scale, left + width, top + 76f * scale);
        medalsScrollX = clamp(medalsScrollX, 0f, maxMedalsScroll(scale));

        paint.setTypeface(titleFont);
        paint.setTextSize(18f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("медали", left + 2f * scale, top + 18f * scale, paint);

        int save = canvas.save();
        canvas.clipRect(medalsViewportRect);
        float medalLeft = left - medalsScrollX;
        for (int i = 0; i < activeCount; i++) {
            int medalIndex = activeMedalIndexAt(i);
            drawMedal(canvas, medalLeft + i * 56f * scale, medalsViewportRect.top + 1f * scale, medalIndex, true, medalLabel(medalIndex), scale);
        }
        canvas.restoreToCount(save);

        if (maxMedalsScroll(scale) > 0f) {
            paint.setTypeface(boldFont);
            paint.setTextSize(14f * scale);
            paint.setColor(COLOR_MUTED);
            canvas.drawText("‹", medalsViewportRect.left + 2f * scale, medalsViewportRect.centerY() + 5f * scale, paint);
            canvas.drawText("›", medalsViewportRect.right - 10f * scale, medalsViewportRect.centerY() + 5f * scale, paint);
        }
    }

    private void drawMedal(Canvas canvas, float left, float top, int index, boolean active, String label, float scale) {
        float size = 36f * scale;
        rect.set(left + 3f * scale, top, left + 3f * scale + size, top + size);
        Bitmap medal = index >= 0 && index < medalBitmaps.length ? medalBitmaps[index] : null;
        drawTexturedMedalCircle(canvas, rect, medal, active, scale);
        if (!active) {
            paint.setColor(Color.argb(150, 14, 15, 15));
            canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() * 0.5f, paint);
        }
        paint.setTypeface(titleFont);
        paint.setTextSize(10f * scale);
        paint.setColor(active ? COLOR_TEXT : COLOR_MUTED);
        drawCenteredText(canvas, label, rect.centerX(), top + 48f * scale, paint);
    }

    private void drawTexturedMedalCircle(Canvas canvas, RectF bounds, Bitmap texture, boolean active, float scale) {
        float radius = bounds.width() * 0.5f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? COLOR_GREEN : Color.rgb(55, 55, 55));
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, paint);

        if (texture != null) {
            BitmapShader shader = new BitmapShader(texture, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            float coverScale = Math.max(bounds.width() / texture.getWidth(), bounds.height() / texture.getHeight());
            shaderMatrix.reset();
            shaderMatrix.setScale(coverScale, coverScale);
            shaderMatrix.postTranslate(
                    bounds.centerX() - texture.getWidth() * coverScale * 0.5f,
                    bounds.centerY() - texture.getHeight() * coverScale * 0.5f
            );
            shader.setLocalMatrix(shaderMatrix);
            paint.setShader(shader);
            paint.setAlpha(active ? 255 : 96);
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, paint);
            paint.setAlpha(255);
            paint.setShader(null);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * scale);
        paint.setColor(active ? Color.argb(210, 255, 255, 255) : Color.argb(110, 140, 140, 140));
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius - 1f * scale, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawSkinSwatch(Canvas canvas, RectF bounds, float radius, int index, boolean active, float scale) {
        paint.setStyle(Paint.Style.FILL);
        Bitmap texture = active ? skinTexture(index) : null;
        if (texture != null) {
            applyTextureShader(texture, bounds);
            paint.setAlpha(255);
            canvas.drawRoundRect(bounds, radius, radius, paint);
            paint.setAlpha(255);
            paint.setShader(null);
        } else {
            paint.setColor(active && index >= 0 && index < SKIN_COLORS.length ? SKIN_COLORS[index] : Color.rgb(58, 58, 58));
            canvas.drawRoundRect(bounds, radius, radius, paint);
        }
        if (!active) {
            paint.setColor(Color.argb(132, 14, 15, 15));
            canvas.drawRoundRect(bounds, radius, radius, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private Bitmap skinTexture(int index) {
        if (index < 0 || index >= skinBitmaps.length) {
            return null;
        }
        return skinBitmaps[index];
    }

    private Bitmap caseTexture(int tier) {
        if (tier < 0 || tier >= caseBitmaps.length) {
            return null;
        }
        return caseBitmaps[tier];
    }

    private void drawCaseTexture(Canvas canvas, RectF bounds, int tier, float scale) {
        Bitmap texture = caseTexture(tier);
        if (texture != null) {
            paint.setAlpha(255);
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawBitmap(texture, null, bounds, paint);
            paint.setAlpha(255);
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(caseTierColor(tier));
        canvas.drawRoundRect(bounds, 16f * scale, 16f * scale, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f * scale);
        paint.setColor(COLOR_BLACK);
        canvas.drawRoundRect(bounds, 16f * scale, 16f * scale, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private Bitmap selectedSkinTexture() {
        if (selectedSkin < 0 || selectedSkin >= skinOwned.length || !skinOwned[selectedSkin]) {
            return null;
        }
        return skinTexture(selectedSkin);
    }

    private void applyTextureShader(Bitmap texture, RectF bounds) {
        BitmapShader shader = new BitmapShader(texture, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        shaderMatrix.reset();
        shaderMatrix.setScale(bounds.width() / texture.getWidth(), bounds.height() / texture.getHeight());
        shaderMatrix.postTranslate(bounds.left, bounds.top);
        shader.setLocalMatrix(shaderMatrix);
        paint.setShader(shader);
    }

    private int medalCount() {
        return MEDAL_CODES.length;
    }

    private String medalLabel(int index) {
        return MEDAL_CODES[index % MEDAL_CODES.length];
    }

    private boolean medalActive(int index) {
        switch (index) {
            case 0:
                return freePassRewardsClaimed[50];
            case 1:
                return proPassRewardsClaimed[10];
            case 2:
                return proPassRewardsClaimed[25];
            case 3:
                return proPassRewardsClaimed[50];
            default:
                return false;
        }
    }

    private int activeMedalCount() {
        int count = 0;
        for (int i = 0; i < medalCount(); i++) {
            if (medalActive(i)) {
                count++;
            }
        }
        return count;
    }

    private int activeMedalIndexAt(int visibleIndex) {
        int active = 0;
        for (int i = 0; i < medalCount(); i++) {
            if (!medalActive(i)) {
                continue;
            }
            if (active == visibleIndex) {
                return i;
            }
            active++;
        }
        return 0;
    }

    private float maxMedalsScroll(float scale) {
        return Math.max(0f, activeMedalCount() * 56f * scale - medalsViewportRect.width());
    }

    private void drawFriendsPanel(Canvas canvas, float left, float top, float width, float height, float scale) {
        friendProfileRects.clear();
        friendProfileIndices.clear();
        friendRequestAcceptRects.clear();
        friendRequestDeclineRects.clear();
        friendRequestIndices.clear();
        rect.set(left, top, left + width, top + height);
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(rect, 16f * scale, 16f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(28f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("введите id", left + 12f * scale, top + 31f * scale, paint);

        friendInputRect.set(left + 10f * scale, top + 42f * scale, left + width - 58f * scale, top + 76f * scale);
        paint.setColor(COLOR_FIELD);
        canvas.drawRoundRect(friendInputRect, 10f * scale, 10f * scale, paint);
        paint.setTypeface(boldFont);
        paint.setTextSize(14f * scale);
        paint.setColor(friendDraft.isEmpty() ? COLOR_MUTED : COLOR_TEXT);
        canvas.drawText(friendDraft.isEmpty() ? "id друга" : friendDraft, friendInputRect.left + 10f * scale, friendInputRect.top + 22f * scale, paint);

        friendAddRect.set(left + width - 48f * scale, top + 42f * scale, left + width - 10f * scale, top + 76f * scale);
        drawButton(canvas, friendAddRect, "+", COLOR_GREEN, Color.rgb(14, 16, 14), scale, 10f);

        float tabsTop = top + 86f * scale;
        friendsTabRect.set(left + 10f * scale, tabsTop, left + width * 0.5f - 4f * scale, tabsTop + 32f * scale);
        friendRequestsTabRect.set(left + width * 0.5f + 4f * scale, tabsTop, left + width - 10f * scale, tabsTop + 32f * scale);
        drawFriendTab(canvas, friendsTabRect, "друзья", friendPanelTab == 0, scale);
        drawFriendTab(canvas, friendRequestsTabRect, "заявки " + friendRequestIds.size(), friendPanelTab == 1, scale);

        friendsViewportRect.set(left, top + 128f * scale, left + width, top + height - 8f * scale);
        friendsScrollY = clamp(friendsScrollY, 0f, maxFriendsScroll(scale));
        int save = canvas.save();
        canvas.clipRect(friendsViewportRect);
        float rowHeight = 30f * scale;
        float rowTop = friendsViewportRect.top - friendsScrollY;
        if (friendPanelTab == 0) {
            for (int i = 0; i < friendIds.size(); i++) {
                RectF row = new RectF(left, rowTop + i * rowHeight, left + width, rowTop + i * rowHeight + 24f * scale);
                if (row.bottom < friendsViewportRect.top || row.top > friendsViewportRect.bottom) {
                    continue;
                }
                friendProfileRects.add(row);
                friendProfileIndices.add(i);
                paint.setColor(COLOR_PANEL_DARK);
                canvas.drawRoundRect(row, 10f * scale, 10f * scale, paint);
                paint.setTypeface(boldFont);
                paint.setTextSize(14f * scale);
                paint.setColor(COLOR_TEXT);
                canvas.drawText(friendIds.get(i), row.left + 10f * scale, row.top + 17f * scale, paint);
            }
        } else {
            for (int i = 0; i < friendRequestIds.size(); i++) {
                RectF row = new RectF(left, rowTop + i * rowHeight, left + width, rowTop + i * rowHeight + 24f * scale);
                if (row.bottom < friendsViewportRect.top || row.top > friendsViewportRect.bottom) {
                    continue;
                }
                paint.setColor(COLOR_PANEL_DARK);
                canvas.drawRoundRect(row, 10f * scale, 10f * scale, paint);
                paint.setTypeface(boldFont);
                paint.setTextSize(14f * scale);
                paint.setColor(COLOR_TEXT);
                canvas.drawText(friendRequestIds.get(i), row.left + 10f * scale, row.top + 17f * scale, paint);

                RectF accept = new RectF(row.right - 76f * scale, row.top, row.right - 40f * scale, row.bottom);
                RectF decline = new RectF(row.right - 36f * scale, row.top, row.right, row.bottom);
                friendRequestAcceptRects.add(accept);
                friendRequestDeclineRects.add(decline);
                friendRequestIndices.add(i);
                drawButton(canvas, accept, "ok", COLOR_GREEN, Color.rgb(14, 16, 14), scale, 10f);
                drawButton(canvas, decline, "x", Color.rgb(88, 42, 42), COLOR_TEXT, scale, 10f);
            }
        }
        boolean empty = friendPanelTab == 0 ? friendIds.isEmpty() : friendRequestIds.isEmpty();
        if (empty) {
            paint.setTypeface(titleFont);
            paint.setTextSize(friendPanelTab == 0 ? 30f * scale : 26f * scale);
            paint.setColor(COLOR_TEXT);
            drawCenteredText(canvas, friendPanelTab == 0 ? "тут список друзей" : "заявок пока нет", friendsViewportRect.centerX(), friendsViewportRect.centerY() + 10f * scale, paint);
        }
        canvas.restoreToCount(save);
    }

    private void drawFriendTab(Canvas canvas, RectF bounds, String label, boolean active, float scale) {
        paint.setColor(active ? COLOR_GREEN : COLOR_FIELD);
        canvas.drawRoundRect(bounds, 10f * scale, 10f * scale, paint);
        paint.setTypeface(boldFont);
        paint.setTextSize(14f * scale);
        paint.setColor(active ? Color.rgb(14, 16, 14) : COLOR_MUTED);
        drawCenteredText(canvas, label, bounds.centerX(), bounds.centerY() + 5f * scale, paint);
    }

    private float maxFriendsScroll(float scale) {
        int count = friendPanelTab == 0 ? friendIds.size() : friendRequestIds.size();
        float content = count * 30f * scale;
        return Math.max(0f, content - friendsViewportRect.height());
    }

    private void drawShopV2(Canvas canvas, float w, float h, float scale) {
        skinRects.clear();
        skinRectIndices.clear();
        caseShopRects.clear();
        caseShopTiers.clear();
        donationRects.clear();
        donationPacks.clear();
        marketBuyRects.clear();
        marketBuyIndices.clear();
        marketSkinRects.clear();
        marketSkinIndices.clear();
        marketOrderBuyRects.clear();
        marketOrderBuyIndices.clear();
        requestMarketState(false);

        float top = menuContentTop(scale);
        float panelH = menuContentHeight(h, scale);
        float left = 0f;
        rect.set(left - 24f * scale, top, w + 24f * scale, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        float tabTop = top + 14f * scale;
        float tabW = Math.min(220f * scale, (w - 84f * scale) / 3f);
        shopInventoryTabRect.set(left + 28f * scale, tabTop, left + 28f * scale + tabW, tabTop + 52f * scale);
        shopDnaTabRect.set(shopInventoryTabRect.right + 12f * scale, tabTop, shopInventoryTabRect.right + 12f * scale + tabW, tabTop + 52f * scale);
        shopMarketTabRect.set(shopDnaTabRect.right + 12f * scale, tabTop, shopDnaTabRect.right + 12f * scale + tabW, tabTop + 52f * scale);
        drawButton(canvas, shopInventoryTabRect, "кейсы", shopMode == 0 ? COLOR_GREEN : COLOR_FIELD, shopMode == 0 ? COLOR_BACKGROUND : COLOR_TEXT, scale, 16f);
        drawButton(canvas, shopDnaTabRect, "днк", shopMode == 1 ? COLOR_GREEN : COLOR_FIELD, shopMode == 1 ? COLOR_BACKGROUND : COLOR_TEXT, scale, 16f);
        drawButton(canvas, shopMarketTabRect, "рынок", shopMode == 2 ? COLOR_GREEN : COLOR_FIELD, shopMode == 2 ? COLOR_BACKGROUND : COLOR_TEXT, scale, 16f);
        float contentTop = tabTop + 70f * scale;
        if (shopMode == 0) {
            float gap = 12f * scale;
            float cardW = Math.max(130f * scale, (w - 52f * scale - gap * (CASE_COSTS.length - 1)) / CASE_COSTS.length);
            for (int tier = 0; tier < CASE_COSTS.length; tier++) {
                RectF card = new RectF(left + 26f * scale + tier * (cardW + gap), contentTop, left + 26f * scale + tier * (cardW + gap) + cardW, contentTop + 292f * scale);
                drawCaseShopCard(canvas, card, tier, scale);
                caseShopRects.add(new RectF(card)); caseShopTiers.add(tier);
            }
        } else if (shopMode == 1) {
            drawDonationPanel(canvas, new RectF(left + 26f * scale, contentTop, w - 26f * scale, top + panelH - 18f * scale), scale);
        } else {
            shopViewportRect.set(left + 16f * scale, contentTop, w - 16f * scale, top + panelH - 12f * scale);
            shopScrollY = clamp(shopScrollY, 0f, maxShopScroll(scale));
            int save = canvas.save(); canvas.clipRect(shopViewportRect); drawMarketList(canvas, scale); canvas.restoreToCount(save);
        }

        if (maxShopScroll(scale) > 0f) {
            paint.setTypeface(boldFont);
            paint.setTextSize(15f * scale);
            paint.setColor(COLOR_MUTED);
            drawCenteredText(canvas, "листай вверх/вниз", shopViewportRect.centerX(), shopViewportRect.bottom + 17f * scale, paint);
        }

        drawMarketSkinDetailModal(canvas, w, h, scale);
        drawStatus(canvas, w, h, scale);
    }

    private void drawDonationPanel(Canvas canvas, RectF panel, float scale) {
        if (panel.height() < 120f * scale) {
            return;
        }
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(panel, 22f * scale, 22f * scale, paint);
        paint.setTypeface(titleFont);
        paint.setTextSize(26f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("донат: ген-монеты", panel.left + 18f * scale, panel.top + 36f * scale, paint);
        paint.setTypeface(boldFont);
        paint.setTextSize(14f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("оплата через ЮKassa, начисление после возврата", panel.left + 18f * scale, panel.top + 61f * scale, paint);

        float gap = 12f * scale;
        float cardW = Math.max(118f * scale, (panel.width() - 36f * scale - gap * 2f) / 3f);
        float cardH = 84f * scale;
        float firstTop = panel.top + 78f * scale;
        for (int i = 0; i < DONATION_COINS.length; i++) {
            int row = i / 3;
            int col = i % 3;
            int rowCount = row == 0 ? 3 : 2;
            float rowWidth = rowCount * cardW + (rowCount - 1) * gap;
            float rowLeft = panel.left + (panel.width() - rowWidth) * 0.5f;
            RectF card = new RectF(rowLeft + col * (cardW + gap), firstTop + row * (cardH + gap),
                    rowLeft + col * (cardW + gap) + cardW, firstTop + row * (cardH + gap) + cardH);
            donationRects.add(new RectF(card));
            donationPacks.add(i);
            paint.setColor(COLOR_FIELD);
            canvas.drawRoundRect(card, 18f * scale, 18f * scale, paint);
            paint.setColor(Color.argb(42, 255, 255, 255));
            rect.set(card.left + 8f * scale, card.top + 7f * scale, card.right - 8f * scale, card.top + 30f * scale);
            canvas.drawRoundRect(rect, 12f * scale, 12f * scale, paint);
            paint.setColor(COLOR_GREEN);
            canvas.drawCircle(card.left + 28f * scale, card.top + 43f * scale, 18f * scale, paint);
            paint.setColor(Color.argb(90, 255, 255, 255));
            canvas.drawCircle(card.left + 22f * scale, card.top + 37f * scale, 5f * scale, paint);
            paint.setTypeface(boldFont);
            paint.setTextSize(20f * scale);
            paint.setColor(COLOR_TEXT);
            canvas.drawText("+" + (DONATION_COINS[i] / 1000) + "к", card.left + 56f * scale, card.top + 39f * scale, paint);
            paint.setTextSize(16f * scale);
            paint.setColor(COLOR_GREEN);
            canvas.drawText(DONATION_RUB[i] + "₽", card.left + 56f * scale, card.top + 64f * scale, paint);
            if (!DONATION_SALE[i].isEmpty()) {
                RectF sale = new RectF(card.right - 74f * scale, card.top + 9f * scale, card.right - 9f * scale, card.top + 31f * scale);
                paint.setColor(Color.rgb(255, 214, 64));
                canvas.drawRoundRect(sale, 10f * scale, 10f * scale, paint);
                paint.setTextSize(11f * scale);
                paint.setColor(Color.rgb(35, 28, 8));
                drawCenteredText(canvas, "акция " + DONATION_SALE[i], sale.centerX(), sale.centerY() + 4f * scale, paint);
            }
        }
    }

    private void drawCaseShopCard(Canvas canvas, RectF card, int tier, float scale) {
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(card, 22f * scale, 22f * scale, paint);
        if (card.height() > 190f * scale) {
            paint.setTypeface(titleFont);
            paint.setTextSize(54f * scale);
            paint.setColor(COLOR_TEXT);
            drawCenteredText(canvas, "кейс", card.centerX(), card.top + 62f * scale, paint);

            float preview = Math.min(92f * scale, card.width() - 64f * scale);
            RectF pack = new RectF(card.centerX() - preview * 0.5f, card.top + 104f * scale,
                    card.centerX() + preview * 0.5f, card.top + 104f * scale + preview);
            drawCaseTexture(canvas, pack, tier, scale);

            RectF buy = new RectF(card.left + 12f * scale, card.bottom - 52f * scale, card.right - 12f * scale, card.bottom - 10f * scale);
            drawButton(canvas, buy, caseBuyLabel(tier), coins >= CASE_COSTS[tier] ? COLOR_GREEN : Color.rgb(58, 58, 58),
                    coins >= CASE_COSTS[tier] ? Color.rgb(14, 16, 14) : COLOR_MUTED, scale, 18f);
            return;
        }
        float cx = card.left + 58f * scale;
        float cy = card.centerY() - 2f * scale;
        RectF pack = new RectF(cx - 38f * scale, cy - 38f * scale, cx + 38f * scale, cy + 38f * scale);
        drawCaseTexture(canvas, pack, tier, scale);

        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText(caseTierName(tier), card.left + 104f * scale, card.top + 42f * scale, paint);
        paint.setTypeface(boldFont);
        paint.setTextSize(17f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("в инвентарь", card.left + 104f * scale, card.top + 70f * scale, paint);
        drawButton(canvas, new RectF(card.left + 104f * scale, card.top + 82f * scale, card.right - 14f * scale, card.bottom - 14f * scale),
                CASE_COSTS[tier] + "", coins >= CASE_COSTS[tier] ? COLOR_GREEN : Color.rgb(58, 58, 58),
                coins >= CASE_COSTS[tier] ? Color.rgb(14, 16, 14) : COLOR_MUTED, scale, 11f);
    }

    private String caseBuyLabel(int tier) {
        int safeTier = clampInt(tier, 0, CASE_COSTS.length - 1);
        return "купить за " + (CASE_COSTS[safeTier] / 1000) + "к";
    }

    private void drawShopTab(Canvas canvas, RectF bounds, String label, boolean active, float scale) {
        paint.setColor(active ? COLOR_GREEN : COLOR_FIELD);
        canvas.drawRoundRect(bounds, 16f * scale, 16f * scale, paint);
        paint.setTypeface(titleFont);
        paint.setTextSize(22f * scale);
        paint.setColor(active ? Color.rgb(14, 16, 14) : COLOR_MUTED);
        drawCenteredText(canvas, label, bounds.centerX(), bounds.centerY() + 8f * scale, paint);
    }

    private void drawInventoryGrid(Canvas canvas, float scale) {
        float gap = 16f * scale;
        float cardW = 145f * scale;
        float cardH = 128f * scale;
        int columns = inventoryColumns(scale);
        float startX = shopViewportRect.left + 6f * scale;
        float startY = shopViewportRect.top + 6f * scale - shopScrollY;
        for (int i = 0; i < skinNames.length; i++) {
            int row = i / columns;
            int col = i % columns;
            RectF card = new RectF(startX + col * (cardW + gap), startY + row * (cardH + gap),
                    startX + col * (cardW + gap) + cardW, startY + row * (cardH + gap) + cardH);
            skinRects.add(card);
            skinRectIndices.add(i);
            if (card.bottom < shopViewportRect.top || card.top > shopViewportRect.bottom) {
                continue;
            }
            paint.setColor(COLOR_PANEL_DARK);
            canvas.drawRoundRect(card, 18f * scale, 18f * scale, paint);

            rect.set(card.centerX() - 27f * scale, card.top + 12f * scale, card.centerX() + 27f * scale, card.top + 72f * scale);
            drawSkinSwatch(canvas, rect, 22f * scale, i, skinOwned[i] || canPreviewSkin(i), scale);

            paint.setTypeface(titleFont);
            paint.setTextSize(15f * scale);
            paint.setColor(skinOwned[i] ? COLOR_TEXT : COLOR_MUTED);
            drawCenteredText(canvas, skinNames[i], card.centerX(), card.top + 94f * scale, paint);

            paint.setTypeface(boldFont);
            paint.setTextSize(13f * scale);
            paint.setColor(skinOwned[i] ? COLOR_GREEN : COLOR_MUTED);
            drawCenteredText(canvas, inventorySkinLabel(i), card.centerX(), card.top + 115f * scale, paint);

            if (skinOwned[i] && selectedSkin == i) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(4f * scale);
                paint.setColor(COLOR_GREEN);
                canvas.drawRoundRect(card, 18f * scale, 18f * scale, paint);
                paint.setStyle(Paint.Style.FILL);
            }
            if (skinShowcase[i]) {
                paint.setTypeface(boldFont);
                paint.setTextSize(11f * scale);
                paint.setColor(COLOR_TEXT);
                canvas.drawText("витрина", card.left + 8f * scale, card.top + 18f * scale, paint);
            }
        }
    }

    private void drawMarketList(Canvas canvas, float scale) {
        float rowH = 76f * scale;
        float y = shopViewportRect.top + 8f * scale - shopScrollY;
        int visibleIndex = 0;
        boolean[] seen = new boolean[skinNames.length];
        for (int i = 0; i < marketListings.size(); i++) {
            MarketListing listing = marketListings.get(i);
            if (listing.skinIndex < 0 || listing.skinIndex >= seen.length || seen[listing.skinIndex]) {
                continue;
            }
            seen[listing.skinIndex] = true;
            int orderCount = marketOrderCount(listing.skinIndex);
            int minPrice = marketMinPrice(listing.skinIndex);
            RectF row = new RectF(shopViewportRect.left + 6f * scale, y + visibleIndex * rowH,
                    shopViewportRect.right - 6f * scale, y + visibleIndex * rowH + 62f * scale);
            marketSkinRects.add(row);
            marketSkinIndices.add(listing.skinIndex);
            visibleIndex++;
            if (row.bottom < shopViewportRect.top || row.top > shopViewportRect.bottom) {
                continue;
            }
            paint.setColor(COLOR_PANEL_DARK);
            canvas.drawRoundRect(row, 16f * scale, 16f * scale, paint);
            rect.set(row.left + 12f * scale, row.top + 8f * scale, row.left + 58f * scale, row.bottom - 8f * scale);
            drawSkinSwatch(canvas, rect, 16f * scale, listing.skinIndex, true, scale);

            paint.setTypeface(titleFont);
            paint.setTextSize(20f * scale);
            paint.setColor(COLOR_TEXT);
            canvas.drawText(skinNames[listing.skinIndex], row.left + 72f * scale, row.top + 26f * scale, paint);

            paint.setTypeface(boldFont);
            paint.setTextSize(15f * scale);
            paint.setColor(COLOR_MUTED);
            canvas.drawText("заявок: " + orderCount, row.left + 72f * scale, row.top + 49f * scale, paint);
            canvas.drawText("от " + minPrice + "  последняя: " + skinLastSalePrices[listing.skinIndex], row.left + 250f * scale, row.top + 49f * scale, paint);

            drawMarketGraph(canvas, row.left + 520f * scale, row.top + 12f * scale, 190f * scale, 38f * scale, listing.skinIndex, scale);

            RectF open = new RectF(row.right - 158f * scale, row.top + 8f * scale, row.right - 12f * scale, row.bottom - 8f * scale);
            drawButton(canvas, open, "открыть", COLOR_GREEN, Color.rgb(14, 16, 14), scale, 15f);
        }
    }

    private void drawMarketGraph(Canvas canvas, float left, float top, float width, float height, int skinIndex, float scale) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * scale);
        paint.setColor(Color.rgb(54, 57, 57));
        rect.set(left, top, left + width, top + height);
        canvas.drawRoundRect(rect, 8f * scale, 8f * scale, paint);
        path.reset();
        int points = 8;
        for (int i = 0; i < points; i++) {
            float x = left + width * i / (points - 1f);
            float wave = (float) Math.sin((skinIndex + 1) * 0.9f + i * 0.85f);
            float y = top + height * (0.52f - wave * 0.34f);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        paint.setColor(COLOR_GREEN);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawMarketGraph100(Canvas canvas, RectF bounds, int skinIndex, float scale) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * scale);
        paint.setColor(Color.rgb(54, 57, 57));
        canvas.drawRoundRect(bounds, 12f * scale, 12f * scale, paint);

        int basePrice = Math.max(100, skinLastSalePrices[skinIndex]);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int[] sales = new int[100];
        for (int i = 0; i < sales.length; i++) {
            int wave = Math.round((float) Math.sin((skinIndex + 3) * 0.35f + i * 0.22f) * 240f);
            int jitter = Math.floorMod(neighborHash(skinIndex * 31 + i * 17, i * 13), 380) - 190;
            int trend = (i - 50) * Math.max(2, skinIndex % 9);
            int value = Math.max(100, basePrice + wave + jitter + trend);
            sales[i] = value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        float span = Math.max(1f, max - min);
        path.reset();
        for (int i = 0; i < sales.length; i++) {
            float x = bounds.left + bounds.width() * i / (sales.length - 1f);
            float y = bounds.bottom - bounds.height() * ((sales[i] - min) / span);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        paint.setStrokeWidth(3f * scale);
        paint.setColor(COLOR_GREEN);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setTypeface(boldFont);
        paint.setTextSize(12f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("100 продаж", bounds.left + 10f * scale, bounds.top + 18f * scale, paint);
        canvas.drawText(max + "", bounds.right - 60f * scale, bounds.top + 18f * scale, paint);
        canvas.drawText(min + "", bounds.right - 60f * scale, bounds.bottom - 8f * scale, paint);
    }

    private void drawMarketSkinDetailModal(Canvas canvas, float w, float h, float scale) {
        if (selectedMarketSkin < 0 || selectedMarketSkin >= skinNames.length) {
            marketDetailRect.setEmpty();
            return;
        }
        paint.setColor(Color.argb(160, 0, 0, 0));
        canvas.drawRect(0f, 0f, w, h, paint);

        float modalW = 760f * scale;
        float modalH = 540f * scale;
        float left = (w - modalW) * 0.5f;
        float top = (h - modalH) * 0.5f;
        marketDetailRect.set(left, top, left + modalW, top + modalH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(marketDetailRect, 25f * scale, 25f * scale, paint);

        rect.set(left + 30f * scale, top + 34f * scale, left + 112f * scale, top + 130f * scale);
        drawSkinSwatch(canvas, rect, 28f * scale, selectedMarketSkin, true, scale);
        paint.setTypeface(titleFont);
        paint.setTextSize(34f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText(skinNames[selectedMarketSkin], left + 132f * scale, top + 72f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(17f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("заявок: " + marketOrderCount(selectedMarketSkin) + "  последняя: " + skinLastSalePrices[selectedMarketSkin],
                left + 132f * scale, top + 104f * scale, paint);

        marketDetailCloseRect.set(left + modalW - 64f * scale, top + 22f * scale, left + modalW - 22f * scale, top + 64f * scale);
        drawButton(canvas, marketDetailCloseRect, "x", COLOR_FIELD, COLOR_TEXT, scale, 10f);

        RectF graph = new RectF(left + 30f * scale, top + 150f * scale, left + modalW - 30f * scale, top + 300f * scale);
        drawMarketGraph100(canvas, graph, selectedMarketSkin, scale);

        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("заявки на продажу", left + 30f * scale, top + 342f * scale, paint);

        marketOrderBuyRects.clear();
        marketOrderBuyIndices.clear();
        int shown = 0;
        for (int i = 0; i < marketListings.size() && shown < 5; i++) {
            MarketListing listing = marketListings.get(i);
            if (listing.skinIndex != selectedMarketSkin) {
                continue;
            }
            RectF row = new RectF(left + 30f * scale, top + (362f + shown * 34f) * scale,
                    left + modalW - 30f * scale, top + (390f + shown * 34f) * scale);
            paint.setColor(COLOR_PANEL_DARK);
            canvas.drawRoundRect(row, 10f * scale, 10f * scale, paint);
            paint.setTypeface(boldFont);
            paint.setTextSize(15f * scale);
            paint.setColor(COLOR_TEXT);
            canvas.drawText(listing.owner, row.left + 12f * scale, row.top + 19f * scale, paint);
            paint.setColor(COLOR_MUTED);
            canvas.drawText("цена: " + listing.price, row.left + 280f * scale, row.top + 19f * scale, paint);
            RectF buy = new RectF(row.right - 112f * scale, row.top + 2f * scale, row.right - 4f * scale, row.bottom - 2f * scale);
            marketOrderBuyRects.add(buy);
            marketOrderBuyIndices.add(i);
            drawButton(canvas, buy, "купить", coins >= listing.price ? COLOR_GREEN : Color.rgb(58, 58, 58),
                    coins >= listing.price ? Color.rgb(14, 16, 14) : COLOR_MUTED, scale, 10f);
            shown++;
        }
        if (shown == 0) {
            paint.setTypeface(titleFont);
            paint.setTextSize(22f * scale);
            paint.setColor(COLOR_MUTED);
            drawCenteredText(canvas, "заявок пока нет", marketDetailRect.centerX(), top + 430f * scale, paint);
        }
    }

    private void drawSkinActionModal(Canvas canvas, float w, float h, float scale) {
        if (selectedInventorySkin < 0 || selectedInventorySkin >= skinOwned.length || !skinOwned[selectedInventorySkin]) {
            skinModalRect.setEmpty();
            return;
        }
        paint.setColor(Color.argb(156, 0, 0, 0));
        canvas.drawRect(0f, 0f, w, h, paint);

        float modalW = 520f * scale;
        float modalH = 350f * scale;
        float left = (w - modalW) * 0.5f;
        float top = (h - modalH) * 0.5f;
        skinModalRect.set(left, top, left + modalW, top + modalH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(skinModalRect, 25f * scale, 25f * scale, paint);

        rect.set(left + 34f * scale, top + 42f * scale, left + 124f * scale, top + 162f * scale);
        drawSkinSwatch(canvas, rect, 34f * scale, selectedInventorySkin, true, scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f * scale);
        paint.setColor(COLOR_BLACK);
        canvas.drawRoundRect(rect, 34f * scale, 34f * scale, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setTypeface(titleFont);
        paint.setTextSize(32f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText(skinNames[selectedInventorySkin], left + 150f * scale, top + 72f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(18f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("последняя цена: " + skinLastSalePrices[selectedInventorySkin], left + 150f * scale, top + 104f * scale, paint);

        modalPriceMinusRect.set(left + 150f * scale, top + 124f * scale, left + 198f * scale, top + 170f * scale);
        modalPricePlusRect.set(left + 360f * scale, top + 124f * scale, left + 408f * scale, top + 170f * scale);
        drawButton(canvas, modalPriceMinusRect, "-", COLOR_FIELD, COLOR_TEXT, scale, 11f);
        drawButton(canvas, modalPricePlusRect, "+", COLOR_FIELD, COLOR_TEXT, scale, 11f);
        paint.setTypeface(titleFont);
        paint.setTextSize(22f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, modalListingPrice + "", left + 279f * scale, top + 154f * scale, paint);

        modalEquipRect.set(left + 34f * scale, top + 205f * scale, left + 180f * scale, top + 265f * scale);
        modalMarketRect.set(left + 194f * scale, top + 205f * scale, left + 358f * scale, top + 265f * scale);
        modalShowcaseRect.set(left + 372f * scale, top + 205f * scale, left + 486f * scale, top + 265f * scale);
        modalCloseRect.set(left + 34f * scale, top + 286f * scale, left + 486f * scale, top + 328f * scale);
        drawButton(canvas, modalEquipRect, selectedSkin == selectedInventorySkin ? "выбран" : "экип", COLOR_GREEN, Color.rgb(14, 16, 14), scale, 14f);
        drawButton(canvas, modalMarketRect, "выставить", Color.rgb(196, 120, 255), Color.rgb(14, 16, 14), scale, 13f);
        drawButton(canvas, modalShowcaseRect, skinShowcase[selectedInventorySkin] ? "убрать" : "витрина", COLOR_FIELD, COLOR_TEXT, scale, 12f);
        drawButton(canvas, modalCloseRect, "закрыть", COLOR_PANEL_DARK, COLOR_MUTED, scale, 12f);
    }

    private int inventoryColumns(float scale) {
        if (shopViewportRect.width() <= 0f) {
            return 4;
        }
        float cardW = 145f * scale;
        float gap = 16f * scale;
        return Math.max(3, (int) ((shopViewportRect.width() + gap) / (cardW + gap)));
    }

    private float maxShopScroll(float scale) {
        if (shopViewportRect.height() <= 0f) {
            return 0f;
        }
        if (shopMode == 2) {
            float content = marketUniqueSkinCount() * 76f * scale + 18f * scale;
            return Math.max(0f, content - shopViewportRect.height());
        }
        int columns = inventoryColumns(scale);
        int rows = (int) Math.ceil(skinNames.length / (float) columns);
        float content = rows * (128f * scale + 16f * scale) + 10f * scale;
        return Math.max(0f, content - shopViewportRect.height());
    }

    private int marketUniqueSkinCount() {
        boolean[] seen = new boolean[skinNames.length];
        int count = 0;
        for (MarketListing listing : marketListings) {
            if (listing.skinIndex >= 0 && listing.skinIndex < seen.length && !seen[listing.skinIndex]) {
                seen[listing.skinIndex] = true;
                count++;
            }
        }
        return count;
    }

    private int marketOrderCount(int skinIndex) {
        int count = 0;
        for (MarketListing listing : marketListings) {
            if (listing.skinIndex == skinIndex) {
                count++;
            }
        }
        return count;
    }

    private int marketMinPrice(int skinIndex) {
        int min = Integer.MAX_VALUE;
        for (MarketListing listing : marketListings) {
            if (listing.skinIndex == skinIndex) {
                min = Math.min(min, listing.price);
            }
        }
        return min == Integer.MAX_VALUE ? skinLastSalePrices[skinIndex] : min;
    }

    private float maxEditorInventoryScroll(float scale) {
        if (editorInventoryViewportRect.height() <= 0f) {
            return 0f;
        }
        float cardW = 142f * scale;
        float gap = 16f * scale;
        int columns = Math.max(3, (int) ((editorInventoryViewportRect.width() + gap) / (cardW + gap)));
        int rows = (int) Math.ceil(inventoryItems.size() / (float) columns);
        float content = rows * (132f * scale + gap) + 8f * scale;
        return Math.max(0f, content - editorInventoryViewportRect.height());
    }

    private int caseTierColor(int tier) {
        if (tier == CASE_TIER_5K) {
            return Color.rgb(255, 139, 74);
        }
        if (tier == CASE_TIER_10K) {
            return Color.rgb(218, 218, 218);
        }
        return Color.rgb(146, 107, 188);
    }

    private String caseTierName(int tier) {
        if (tier == CASE_TIER_100K) {
            return "кейс 100к";
        }
        if (tier == CASE_TIER_50K) {
            return "кейс 50к";
        }
        if (tier == CASE_TIER_5K) {
            return "кейс 5к";
        }
        if (tier == CASE_TIER_10K) {
            return "кейс 10к";
        }
        return "кейс 2к";
    }

    private boolean canPreviewSkin(int index) {
        return index >= CASE_SKIN_START || isPassSkin(index) || index == 1 || index == 2;
    }

    private String inventorySkinLabel(int index) {
        if (skinOwned[index] && selectedSkin == index) {
            return "выбран";
        }
        if (skinOwned[index]) {
            return "в инвентаре";
        }
        if (isPassSkin(index)) {
            return "bk pass";
        }
        if (index >= CASE_SKIN_START) {
            return "кейс";
        }
        return SKIN_COSTS[index] + "";
    }

    private void drawShop(Canvas canvas, float w, float h, float scale) {
        skinRects.clear();
        float panelW = 900f * scale;
        float panelH = 520f * scale;
        float left = (w - panelW) * 0.5f;
        float top = (h - panelH) * 0.55f;

        rect.set(left, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(44f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("магазин", left + 38f * scale, top + 66f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(22f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("валюта: " + coins, left + 690f * scale, top + 62f * scale, paint);

        float cardW = 188f * scale;
        float cardH = 180f * scale;
        for (int i = 0; i < skinNames.length; i++) {
            float cardLeft = left + 38f * scale + (i % 4) * (cardW + 22f * scale);
            float cardTop = top + 104f * scale + (i / 4) * (cardH + 28f * scale);
            RectF card = new RectF(cardLeft, cardTop, cardLeft + cardW, cardTop + cardH);
            skinRects.add(card);
            paint.setColor(COLOR_PANEL_DARK);
            canvas.drawRoundRect(card, 18f * scale, 18f * scale, paint);

            rect.set(card.left + 58f * scale, card.top + 22f * scale, card.right - 58f * scale, card.top + 100f * scale);
            drawSkinSwatch(canvas, rect, 28f * scale, i, true, scale);

            paint.setTypeface(titleFont);
            paint.setTextSize(19f * scale);
            paint.setColor(COLOR_TEXT);
            drawCenteredText(canvas, skinNames[i], card.centerX(), card.top + 128f * scale, paint);

            String label;
            if (skinOwned[i] && selectedSkin == i) {
                label = "выбран";
            } else if (skinOwned[i]) {
                label = "куплено";
            } else if (i >= 3) {
                label = "pass";
            } else {
                label = SKIN_COSTS[i] + "";
            }
            paint.setTypeface(boldFont);
            paint.setTextSize(18f * scale);
            paint.setColor(skinOwned[i] ? COLOR_GREEN : COLOR_MUTED);
            drawCenteredText(canvas, label, card.centerX(), card.top + 157f * scale, paint);
            if (skinOwned[i] && selectedSkin == i) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(4f * scale);
                paint.setColor(COLOR_GREEN);
                canvas.drawRoundRect(card, 18f * scale, 18f * scale, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }

        drawStatus(canvas, w, h, scale);
    }

    private void drawCharacterEditor(Canvas canvas, float w, float h, float scale) {
        skinRects.clear();
        skinRectIndices.clear();
        inventoryItemRects.clear();
        inventoryItemIndices.clear();
        float panelW = w;
        float panelH = menuContentHeight(h, scale);
        float left = 0f;
        float top = menuContentTop(scale);
        float previewPaneW = Math.min(330f * scale, panelW * 0.28f);

        rect.set(left - 24f * scale, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        float previewX = left + previewPaneW * 0.5f;
        float previewY = top + panelH * 0.45f;
        rect.set(previewX - 42f * scale, previewY - 88f * scale, previewX + 42f * scale, previewY + 88f * scale);
        drawSkinSwatch(canvas, rect, 42f * scale, selectedSkin, true, scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f * scale);
        paint.setColor(COLOR_BLACK);
        canvas.drawRoundRect(rect, 42f * scale, 42f * scale, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setTypeface(boldFont);
        paint.setTextSize(20f * scale);
        paint.setColor(COLOR_MUTED);
        drawCenteredText(canvas, "скин применяется здесь", previewX, previewY + 132f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(28f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("инвентарь", left + previewPaneW + 24f * scale, top + 44f * scale, paint);
        editorInventoryViewportRect.set(left + previewPaneW + 24f * scale, top + 62f * scale, left + panelW - 28f * scale, top + panelH - 28f * scale);
        editorInventoryScrollY = clamp(editorInventoryScrollY, 0f, maxEditorInventoryScroll(scale));
        int save = canvas.save();
        canvas.clipRect(editorInventoryViewportRect);
        drawEditorInventoryGrid(canvas, scale);
        canvas.restoreToCount(save);
        drawSkinActionModal(canvas, w, h, scale);
        drawCaseOpeningModal(canvas, w, h, scale);
        drawStatus(canvas, w, h, scale);
    }

    private void drawEditorInventoryGrid(Canvas canvas, float scale) {
        float cardW = 142f * scale;
        float cardH = 132f * scale;
        float gap = 16f * scale;
        int columns = Math.max(3, (int) ((editorInventoryViewportRect.width() + gap) / (cardW + gap)));
        float startX = editorInventoryViewportRect.left + 4f * scale;
        float startY = editorInventoryViewportRect.top + 4f * scale - editorInventoryScrollY;
        for (int i = 0; i < inventoryItems.size(); i++) {
            int row = i / columns;
            int col = i % columns;
            RectF card = new RectF(startX + col * (cardW + gap), startY + row * (cardH + gap),
                    startX + col * (cardW + gap) + cardW, startY + row * (cardH + gap) + cardH);
            inventoryItemRects.add(card);
            inventoryItemIndices.add(i);
            if (card.bottom < editorInventoryViewportRect.top || card.top > editorInventoryViewportRect.bottom) {
                continue;
            }
            InventoryItem item = inventoryItems.get(i);
            paint.setColor(COLOR_PANEL_DARK);
            canvas.drawRoundRect(card, 18f * scale, 18f * scale, paint);
            if (item.type == INVENTORY_ITEM_CASE) {
                RectF pack = new RectF(card.centerX() - 25f * scale, card.top + 16f * scale,
                        card.centerX() + 25f * scale, card.top + 78f * scale);
                drawCaseTexture(canvas, pack, item.value, scale);
                paint.setTypeface(titleFont);
                paint.setTextSize(15f * scale);
                paint.setColor(COLOR_TEXT);
                drawCenteredText(canvas, caseTierName(item.value), card.centerX(), card.top + 101f * scale, paint);
                paint.setTypeface(boldFont);
                paint.setTextSize(13f * scale);
                paint.setColor(COLOR_GREEN);
                drawCenteredText(canvas, "открыть", card.centerX(), card.top + 121f * scale, paint);
            } else {
                int skinIndex = clampInt(item.value, 0, skinNames.length - 1);
                rect.set(card.centerX() - 26f * scale, card.top + 15f * scale, card.centerX() + 26f * scale, card.top + 76f * scale);
                drawSkinSwatch(canvas, rect, 22f * scale, skinIndex, true, scale);
                if (selectedSkin == skinIndex) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(4f * scale);
                    paint.setColor(COLOR_GREEN);
                    canvas.drawRoundRect(card, 18f * scale, 18f * scale, paint);
                    paint.setStyle(Paint.Style.FILL);
                }
                paint.setTypeface(titleFont);
                paint.setTextSize(15f * scale);
                paint.setColor(COLOR_TEXT);
                drawCenteredText(canvas, skinNames[skinIndex], card.centerX(), card.top + 101f * scale, paint);
                paint.setTypeface(boldFont);
                paint.setTextSize(13f * scale);
                paint.setColor(COLOR_MUTED);
                drawCenteredText(canvas, selectedSkin == skinIndex ? "выбран" : "скин", card.centerX(), card.top + 121f * scale, paint);
            }
        }
        if (inventoryItems.isEmpty()) {
            paint.setTypeface(titleFont);
            paint.setTextSize(28f * scale);
            paint.setColor(COLOR_MUTED);
            drawCenteredText(canvas, "инвентарь пуст", editorInventoryViewportRect.centerX(), editorInventoryViewportRect.centerY(), paint);
        }
    }

    private void drawCaseOpeningModal(Canvas canvas, float w, float h, float scale) {
        if (!caseOpeningActive) {
            caseOpeningModalRect.setEmpty();
            return;
        }
        long now = System.currentTimeMillis();
        float progress = clamp((now - caseOpeningStartMs) / 2400f, 0f, 1f);
        paint.setColor(Color.argb(168, 0, 0, 0));
        canvas.drawRect(0f, 0f, w, h, paint);
        float modalW = 700f * scale;
        float modalH = 380f * scale;
        float left = (w - modalW) * 0.5f;
        float top = (h - modalH) * 0.5f;
        caseOpeningModalRect.set(left, top, left + modalW, top + modalH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(caseOpeningModalRect, 25f * scale, 25f * scale, paint);

        RectF casePreview = new RectF(left + 46f * scale, top + 26f * scale, left + 116f * scale, top + 96f * scale);
        drawCaseTexture(canvas, casePreview, caseOpeningTier, scale);

        paint.setTypeface(titleFont);
        paint.setTextSize(34f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, caseTierName(caseOpeningTier), caseOpeningModalRect.centerX(), top + 58f * scale, paint);

        RectF reel = new RectF(left + 42f * scale, top + 100f * scale, left + modalW - 42f * scale, top + 210f * scale);
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(reel, 18f * scale, 18f * scale, paint);
        int save = canvas.save();
        canvas.clipRect(reel);
        float slotW = 86f * scale;
        float eased = 1f - (float) Math.pow(1f - progress, 3.0);
        float resultCenter = CASE_REEL_RESULT_SLOT * slotW + slotW * 0.5f;
        float finalScroll = Math.max(0f, resultCenter - reel.width() * 0.5f);
        float scrollX = finalScroll * eased;
        int firstSlot = Math.max(0, (int) Math.floor(scrollX / slotW) - 2);
        int lastSlot = Math.min(CASE_REEL_SLOT_COUNT - 1, firstSlot + 12);
        for (int i = firstSlot; i <= lastSlot; i++) {
            int skinIndex = caseOpeningReelSkin(i);
            float sx = reel.left + i * slotW - scrollX;
            RectF skin = new RectF(sx + 17f * scale, reel.top + 16f * scale, sx + 67f * scale, reel.bottom - 16f * scale);
            drawSkinSwatch(canvas, skin, 18f * scale, skinIndex, true, scale);
        }
        canvas.restoreToCount(save);
        paint.setColor(COLOR_GREEN);
        rect.set(reel.centerX() - 3f * scale, reel.top - 6f * scale, reel.centerX() + 3f * scale, reel.bottom + 6f * scale);
        canvas.drawRoundRect(rect, 3f * scale, 3f * scale, paint);

        if (progress >= 1f) {
            paint.setTypeface(titleFont);
            paint.setTextSize(25f * scale);
            paint.setColor(COLOR_TEXT);
            drawCenteredText(canvas, "выпал: " + skinNames[caseOpeningResultSkin], caseOpeningModalRect.centerX(), top + 254f * scale, paint);
            caseOpeningClaimRect.set(left + 190f * scale, top + 292f * scale, left + modalW - 190f * scale, top + 350f * scale);
            drawButton(canvas, caseOpeningClaimRect, "забрать", COLOR_GREEN, Color.rgb(14, 16, 14), scale, 16f);
        } else {
            caseOpeningClaimRect.setEmpty();
            paint.setTypeface(boldFont);
            paint.setTextSize(18f * scale);
            paint.setColor(COLOR_MUTED);
            drawCenteredText(canvas, "прокрутка...", caseOpeningModalRect.centerX(), top + 288f * scale, paint);
        }
    }

    private void drawDeathOverlay(Canvas canvas, float w, float h, float scale) {
        if (!deathOverlayActive) {
            return;
        }
        long now = System.currentTimeMillis();
        float deathProgress = deathEffectUntilMs > now ? 1f - (deathEffectUntilMs - now) / 1000f : 1f;
        paint.setColor(Color.argb(clampInt(Math.round(70f + 120f * clamp(deathProgress, 0f, 1f)), 70, 190), 0, 0, 0));
        canvas.drawRect(0f, 0f, w, h, paint);

        float panelW = 560f * scale;
        float panelH = 430f * scale;
        float left = (w - panelW) * 0.5f;
        float top = (h - panelH) * 0.5f;
        rect.set(left, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(38f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, "вас убил", rect.centerX(), top + 58f * scale, paint);

        RectF killerAvatar = new RectF(left + 206f * scale, top + 86f * scale, left + 354f * scale, top + 234f * scale);
        paint.setColor(Color.rgb(214, 214, 214));
        canvas.drawRoundRect(killerAvatar, 25f * scale, 25f * scale, paint);
        drawRewardMicrobe(canvas, killerAvatar.centerX(), killerAvatar.centerY(), deathKillerColor, scale * 2.1f);

        paint.setTypeface(titleFont);
        paint.setTextSize(34f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, deathKillerName, rect.centerX(), top + 280f * scale, paint);

        deathMenuRect.set(left + 32f * scale, top + 330f * scale, left + 180f * scale, top + 388f * scale);
        deathProfileRect.set(left + 204f * scale, top + 330f * scale, left + 356f * scale, top + 388f * scale);
        deathRestartRect.set(left + 380f * scale, top + 330f * scale, left + 528f * scale, top + 388f * scale);
        drawButton(canvas, deathMenuRect, "в меню", Color.rgb(58, 58, 58), COLOR_TEXT, scale, 18f);
        drawButton(canvas, deathProfileRect, "профиль", Color.rgb(58, 58, 58), COLOR_TEXT, scale, 18f);
        drawButton(canvas, deathRestartRect, "заново", COLOR_GREEN, Color.rgb(14, 16, 14), scale, 18f);
    }

    private void drawSettings(Canvas canvas, float w, float h, float scale) {
        float panelW = w;
        float panelH = menuContentHeight(h, scale);
        float left = 0f;
        float top = menuContentTop(scale);

        rect.set(left - 24f * scale, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        drawSettingRow(canvas, left + 42f * scale, top + 34f * scale, "музыка", musicEnabled ? "вкл" : "выкл", musicRect, scale);
        drawSettingSlider(canvas, left + 42f * scale, top + 106f * scale, "громкость", musicVolume, musicVolumeSliderRect, scale);
        drawSettingRow(canvas, left + 42f * scale, top + 178f * scale, "звуки", soundsEnabled ? "вкл" : "выкл", soundsRect, scale);
        drawSettingSlider(canvas, left + 42f * scale, top + 250f * scale, "эффекты", effectsVolume, effectsVolumeSliderRect, scale);
        drawSettingRow(canvas, left + 42f * scale, top + 322f * scale, "джойстик движения", controlMode == 0 ? "слева" : "справа", controlRect, scale);
        drawSettingRow(canvas, left + 42f * scale, top + 408f * scale, "графика", graphicsLabel(), graphicsRect, scale);
        loadServerRect.set(left + 42f * scale, top + 496f * scale, left + 270f * scale, top + 556f * scale);
        saveServerRect.set(left + 284f * scale, top + 496f * scale, left + 512f * scale, top + 556f * scale);
        logoutRect.set(left + 526f * scale, top + 496f * scale, left + 778f * scale, top + 556f * scale);
        drawButton(canvas, loadServerRect, "загрузить", COLOR_GREEN, COLOR_BACKGROUND, scale, 16f);
        drawButton(canvas, saveServerRect, "сохранить", COLOR_FIELD, COLOR_TEXT, scale, 16f);
        drawButton(canvas, logoutRect, "выйти", Color.rgb(110, 54, 54), COLOR_TEXT, scale, 16f);

        drawSettingsPreviewPanel(canvas, left + 820f * scale, top + 34f * scale, w - 862f * scale, panelH - 70f * scale, scale);
        drawStatus(canvas, w, h, scale);
    }

    private void drawSettingsPreviewPanel(Canvas canvas, float left, float top, float width, float height, float scale) {
        if (width < 280f * scale || height < 360f * scale) {
            return;
        }
        float gap = 14f * scale;
        float cardH = (height - gap * 2f) / 3f;
        drawEffectsPreview(canvas, new RectF(left, top, left + width, top + cardH), scale);
        drawJoystickPreview(canvas, new RectF(left, top + cardH + gap, left + width, top + cardH * 2f + gap), scale);
        drawGraphicsPreview(canvas, new RectF(left, top + cardH * 2f + gap * 2f, left + width, top + cardH * 3f + gap * 2f), scale);
    }

    private void drawPreviewCard(Canvas canvas, RectF bounds, String title, float scale) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(bounds, 20f * scale, 20f * scale, paint);
        paint.setTypeface(titleFont);
        paint.setTextSize(20f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText(title, bounds.left + 18f * scale, bounds.top + 30f * scale, paint);
    }

    private void drawEffectsPreview(Canvas canvas, RectF bounds, float scale) {
        drawPreviewCard(canvas, bounds, "пример эффектов", scale);
        float level = clamp(effectsVolume / 100f, 0f, 1f);
        float cx = bounds.left + bounds.width() * 0.5f;
        float cy = bounds.top + bounds.height() * 0.62f;
        int count = Math.max(2, Math.round(12f * level));
        for (int i = 0; i < count; i++) {
            float age = (animationSeconds() * 1.35f + i * 0.17f) % 1f;
            float angle = (float) (Math.PI * 2.0 * i / count + i * 0.63f);
            float radius = (14f + age * (40f + 34f * level)) * scale;
            int alpha = clampInt(Math.round(70 + 150 * level), 45, 230);
            paint.setColor(Color.argb(alpha, 128 + (i * 19) % 90, 216, 90 + (i * 23) % 120));
            canvas.drawCircle(cx + (float) Math.cos(angle) * radius,
                    cy + (float) Math.sin(angle) * radius + age * age * 15f * scale,
                    (3f + (1f - age) * 7f * level) * scale, paint);
        }
        paint.setColor(COLOR_BLOCK);
        canvas.drawCircle(cx, cy, 8f * scale, paint);
        paint.setTypeface(boldFont);
        paint.setTextSize(14f * scale);
        paint.setColor(COLOR_MUTED);
        drawCenteredText(canvas, effectsVolume + "% частиц и громкости", cx, bounds.bottom - 16f * scale, paint);
    }

    private void drawJoystickPreview(Canvas canvas, RectF bounds, float scale) {
        drawPreviewCard(canvas, bounds, "пример джойстика", scale);
        RectF screen = new RectF(bounds.left + 24f * scale, bounds.top + 45f * scale,
                bounds.right - 24f * scale, bounds.bottom - 18f * scale);
        paint.setColor(COLOR_FIELD);
        canvas.drawRoundRect(screen, 16f * scale, 16f * scale, paint);
        paint.setColor(Color.rgb(34, 35, 35));
        canvas.drawLine(screen.centerX(), screen.top + 8f * scale, screen.centerX(), screen.bottom - 8f * scale, paint);
        float joyX = controlMode == 0 ? screen.left + 58f * scale : screen.right - 58f * scale;
        float joyY = screen.bottom - 46f * scale;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f * scale);
        paint.setColor(Color.argb(150, 128, 216, 90));
        canvas.drawCircle(joyX, joyY, 34f * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(220, 128, 216, 90));
        canvas.drawCircle(joyX + 11f * scale, joyY - 8f * scale, 15f * scale, paint);
        paint.setTypeface(boldFont);
        paint.setTextSize(14f * scale);
        paint.setColor(COLOR_MUTED);
        drawCenteredText(canvas, controlMode == 0 ? "движение слева" : "движение справа", screen.centerX(), screen.bottom - 10f * scale, paint);
    }

    private void drawGraphicsPreview(Canvas canvas, RectF bounds, float scale) {
        drawPreviewCard(canvas, bounds, "пример графики", scale);
        RectF scene = new RectF(bounds.left + 24f * scale, bounds.top + 45f * scale,
                bounds.right - 24f * scale, bounds.bottom - 18f * scale);
        paint.setColor(COLOR_WORLD_BACKGROUND);
        canvas.drawRoundRect(scene, 16f * scale, 16f * scale, paint);
        int gridAlpha = graphicsQuality == 0 ? 45 : graphicsQuality == 1 ? 85 : 130;
        paint.setColor(Color.argb(gridAlpha, 128, 216, 90));
        paint.setStrokeWidth(1.5f * scale);
        int lines = graphicsQuality == 0 ? 3 : graphicsQuality == 1 ? 5 : 7;
        for (int i = 1; i < lines; i++) {
            float x = scene.left + scene.width() * i / lines;
            float y = scene.top + scene.height() * i / lines;
            canvas.drawLine(x, scene.top, x, scene.bottom, paint);
            canvas.drawLine(scene.left, y, scene.right, y, paint);
        }
        float blockSize = 31f * scale;
        paint.setColor(COLOR_BLOCK);
        canvas.drawRoundRect(scene.left + 25f * scale, scene.top + 23f * scale,
                scene.left + 25f * scale + blockSize, scene.top + 23f * scale + blockSize,
                7f * scale, 7f * scale, paint);
        canvas.drawRoundRect(scene.right - 58f * scale, scene.bottom - 52f * scale,
                scene.right - 27f * scale, scene.bottom - 21f * scale,
                7f * scale, 7f * scale, paint);
        if (graphicsQuality >= 1) {
            paint.setColor(Color.argb(70, 0, 0, 0));
            canvas.drawCircle(scene.centerX() + 7f * scale, scene.centerY() + 9f * scale, 31f * scale, paint);
        }
        drawBlob(canvas, scene.centerX(), scene.centerY(), graphicsQuality == 0 ? 20f * scale : 25f * scale, COLOR_GREEN, scale);
        if (graphicsQuality >= 2) {
            for (int i = 0; i < 8; i++) {
                paint.setColor(Color.argb(160, 160, 255, 110));
                canvas.drawCircle(scene.left + (38f + i * 27f) * scale,
                        scene.top + (26f + (i % 3) * 22f) * scale,
                        3f * scale, paint);
            }
        }
        paint.setTypeface(boldFont);
        paint.setTextSize(14f * scale);
        paint.setColor(COLOR_MUTED);
        drawCenteredText(canvas, graphicsLabel(), scene.centerX(), scene.bottom - 10f * scale, paint);
    }

    private void drawMusicLibrary(Canvas canvas, float w, float h, float scale) {
        musicOpenRects.clear();
        float panelW = w;
        float panelH = menuContentHeight(h, scale);
        float left = 0f;
        float top = menuContentTop(scale);

        rect.set(left - 24f * scale, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(18f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("Newgrounds · треки играют случайно и приглушенно", left + 38f * scale, top + 42f * scale, paint);

        float rowTop = top + 70f * scale;
        float rowH = 78f * scale;
        for (int i = 0; i < MUSIC_TITLES.length; i++) {
            float y = rowTop + i * (rowH + 10f * scale);
            rect.set(left + 32f * scale, y, left + panelW - 32f * scale, y + rowH);
            paint.setColor(COLOR_PANEL_DARK);
            canvas.drawRoundRect(rect, 18f * scale, 18f * scale, paint);

            paint.setTypeface(titleFont);
            paint.setTextSize(fitTextSize(MUSIC_TITLES[i], 28f * scale, 430f * scale, titleFont));
            paint.setColor(COLOR_TEXT);
            canvas.drawText(MUSIC_TITLES[i], rect.left + 20f * scale, y + 31f * scale, paint);

            paint.setTypeface(boldFont);
            paint.setTextSize(17f * scale);
            paint.setColor(COLOR_MUTED);
            canvas.drawText(MUSIC_AUTHORS[i], rect.left + 20f * scale, y + 56f * scale, paint);

            paint.setTextSize(13f * scale);
            canvas.drawText(MUSIC_URLS[i], rect.left + 260f * scale, y + 56f * scale, paint);

            RectF button = new RectF(rect.right - 148f * scale, y + 17f * scale, rect.right - 18f * scale, y + 61f * scale);
            musicOpenRects.add(button);
            drawButton(canvas, button, "открыть", COLOR_GREEN, Color.rgb(14, 16, 14), scale, 14f);
        }

        drawStatus(canvas, w, h, scale);
    }

    private void drawGame(Canvas canvas, float w, float h, float scale) {
        updateFpsCounter();
        ensureWorldGenerated();
        updateJoystickLayout(w, h, scale);
        canvas.drawColor(COLOR_WORLD_BACKGROUND);

        float zoom = (0.95f + graphicsQuality * 0.08f) * scale;
        float cameraLeft = playerX - w * 0.5f / zoom;
        float cameraTop = playerY - h * 0.5f / zoom;

        long now = System.currentTimeMillis();
        float shakeX = bossShakeOffset(now, 0, scale);
        float shakeY = bossShakeOffset(now, 1, scale);
        int worldSave = canvas.save();
        canvas.translate(shakeX, shakeY);
        drawGeneratedWorld(canvas, w, h, zoom, cameraLeft, cameraTop, scale);
        drawGameParticles(canvas, w, h, zoom, cameraLeft, cameraTop, scale, false);
        drawPlayer(canvas, w, h, scale);
        canvas.restoreToCount(worldSave);
        drawGameParticles(canvas, w, h, zoom, cameraLeft, cameraTop, scale, true);
        drawBossSpawnWorldEffect(canvas, w, h, scale, zoom, cameraLeft, cameraTop, now);
        drawBossDirectionIndicator(canvas, w, h, zoom, cameraLeft, cameraTop, scale, now);
        drawGameLevelProgress(canvas, w, scale);
        gamePauseRect.set(22f * scale, 18f * scale, 170f * scale, 72f * scale);
        drawButton(canvas, gamePauseRect, "ПАУЗА", COLOR_PANEL_DARK, COLOR_TEXT, scale, 17f);
        drawGameChat(canvas, scale);
        drawLiveLeaderboard(canvas, w, scale);
        drawGameJoysticks(canvas, w, h, scale);
        drawDeathOverlay(canvas, w, h, scale);
        drawFps(canvas, w, h, scale);
    }

    private void updateFpsCounter() {
        long now = System.currentTimeMillis();
        if (fpsWindowStartMs == 0L) {
            fpsWindowStartMs = now;
        }
        fpsFrameCount++;
        long elapsed = now - fpsWindowStartMs;
        if (elapsed >= 500L) {
            displayedFps = Math.round(fpsFrameCount * 1000f / Math.max(1L, elapsed));
            fpsFrameCount = 0;
            fpsWindowStartMs = now;
        }
    }

    private void drawFps(Canvas canvas, float w, float h, float scale) {
        paint.setTypeface(boldFont);
        paint.setTextSize(18f * scale);
        paint.setColor(Color.argb(185, 255, 255, 255));
        String pingText = displayedPingMs >= 0 ? displayedPingMs + " ms" : "-- ms";
        String text = displayedFps + " fps  |  " + pingText;
        canvas.drawText(text, w - paint.measureText(text) - 18f * scale, h - 18f * scale, paint);
    }

    private void drawGeneratedWorld(Canvas canvas, float w, float h, float zoom, float cameraLeft, float cameraTop, float scale) {
        drawWorldGrid(canvas, w, h, zoom, cameraLeft, cameraTop, scale);

        int startX = Math.max(0, (int) Math.floor(cameraLeft / WORLD_TILE_SIZE) - 1);
        int startY = Math.max(0, (int) Math.floor(cameraTop / WORLD_TILE_SIZE) - 1);
        int endX = Math.min(WORLD_TILES - 1, (int) Math.ceil((cameraLeft + w / zoom) / WORLD_TILE_SIZE) + 1);
        int endY = Math.min(WORLD_TILES - 1, (int) Math.ceil((cameraTop + h / zoom) / WORLD_TILE_SIZE) + 1);
        float tileScreen = WORLD_TILE_SIZE * zoom;
        float radius = 20f * zoom;

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int tile = worldTiles[y][x];
                float sx = (x * WORLD_TILE_SIZE - cameraLeft) * zoom;
                float sy = (y * WORLD_TILE_SIZE - cameraTop) * zoom;
                rect.set(sx, sy, sx + tileScreen, sy + tileScreen);
                drawWorldTile(canvas, rect, tile, radius, zoom, scale, x, y);
            }
        }
        drawPowerUps(canvas, w, h, zoom, cameraLeft, cameraTop, scale);
        drawServerDnaPickups(canvas, w, h, zoom, cameraLeft, cameraTop, scale);
        drawBossDnaOrbs(canvas, w, h, zoom, cameraLeft, cameraTop, scale);
        drawOpponents(canvas, w, h, zoom, cameraLeft, cameraTop, scale);
        drawBoss(canvas, w, h, zoom, cameraLeft, cameraTop, scale);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f * zoom);
        paint.setColor(COLOR_WORLD_BORDER);
        rect.set(
                (worldLeftLimit() - cameraLeft) * zoom,
                (worldTopLimit() - cameraTop) * zoom,
                (worldRightLimit() - cameraLeft) * zoom,
                (worldBottomLimit() - cameraTop) * zoom
        );
        canvas.drawRect(rect, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawWorldGrid(Canvas canvas, float w, float h, float zoom, float cameraLeft, float cameraTop, float scale) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, 1.2f * scale));
        paint.setColor(COLOR_GRID);

        int startX = Math.max(0, (int) Math.floor(cameraLeft / WORLD_TILE_SIZE));
        int endX = Math.min(WORLD_TILES, (int) Math.ceil((cameraLeft + w / zoom) / WORLD_TILE_SIZE));
        for (int x = startX; x <= endX; x++) {
            float sx = (x * WORLD_TILE_SIZE - cameraLeft) * zoom;
            canvas.drawLine(sx, 0f, sx, h, paint);
        }

        int startY = Math.max(0, (int) Math.floor(cameraTop / WORLD_TILE_SIZE));
        int endY = Math.min(WORLD_TILES, (int) Math.ceil((cameraTop + h / zoom) / WORLD_TILE_SIZE));
        for (int y = startY; y <= endY; y++) {
            float sy = (y * WORLD_TILE_SIZE - cameraTop) * zoom;
            canvas.drawLine(0f, sy, w, sy, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawPowerUps(Canvas canvas, float w, float h, float zoom, float cameraLeft, float cameraTop, float scale) {
        for (PowerUpPickup powerUp : powerUps) {
            if (!powerUp.active) {
                continue;
            }
            float sx = (powerUp.x - cameraLeft) * zoom;
            float sy = (powerUp.y - cameraTop) * zoom;
            if (sx < -60f * scale || sy < -60f * scale || sx > w + 60f * scale || sy > h + 60f * scale) {
                continue;
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(170, 0, 0, 0));
            canvas.drawCircle(sx, sy, 28f * scale, paint);
            Bitmap icon = powerUpBitmap(powerUp.type);
            if (icon != null) {
                RectF iconRect = new RectF(sx - 24f * scale, sy - 24f * scale, sx + 24f * scale, sy + 24f * scale);
                paint.setAlpha(255);
                canvas.drawBitmap(icon, null, iconRect, paint);
            } else {
                paint.setColor(powerUpColor(powerUp.type));
                canvas.drawCircle(sx, sy, 21f * scale, paint);
                paint.setTypeface(titleFont);
                paint.setTextSize(16f * scale);
                paint.setColor(Color.rgb(14, 16, 14));
                drawCenteredText(canvas, powerUpLabel(powerUp.type), sx, sy + 6f * scale, paint);
            }
        }
    }

    private void drawGameParticles(Canvas canvas, float w, float h, float zoom, float cameraLeft, float cameraTop, float scale, boolean screenSpace) {
        long now = System.currentTimeMillis();
        paint.setStyle(Paint.Style.FILL);
        for (GameParticle particle : gameParticles) {
            if (particle.screenSpace != screenSpace) {
                continue;
            }
            float age = (now - particle.birthMs) / (float) Math.max(1L, particle.lifeMs);
            if (age < 0f || age >= 1f) {
                continue;
            }
            float seconds = (now - particle.birthMs) / 1000f;
            float px = particle.x + particle.vx * seconds;
            float py = particle.y + particle.vy * seconds + particle.gravity * seconds * seconds * 0.5f;
            if (!screenSpace) {
                px = (px - cameraLeft) * zoom;
                py = (py - cameraTop) * zoom;
                if (px < -80f * scale || py < -80f * scale || px > w + 80f * scale || py > h + 80f * scale) {
                    continue;
                }
            }
            float alphaCurve = (float) Math.pow(1f - age, 1.45f);
            int alpha = clampInt(Math.round(Color.alpha(particle.color) * alphaCurve), 0, 255);
            float size = particle.size * (screenSpace ? scale : zoom) * (0.68f + age * 0.72f);
            paint.setColor(withAlpha(particle.color, alpha));
            if (particle.square) {
                float rotation = particle.spin * seconds;
                int save = canvas.save();
                canvas.rotate(rotation, px, py);
                rect.set(px - size, py - size, px + size, py + size);
                canvas.drawRoundRect(rect, 3f * scale, 3f * scale, paint);
                canvas.restoreToCount(save);
            } else {
                canvas.drawCircle(px, py, size, paint);
            }
        }
    }

    private void updateGameParticles(long now) {
        for (int i = gameParticles.size() - 1; i >= 0; i--) {
            GameParticle particle = gameParticles.get(i);
            if (now - particle.birthMs >= particle.lifeMs) {
                gameParticles.remove(i);
            }
        }
        while (gameParticles.size() > 260) {
            gameParticles.remove(0);
        }
    }

    private void spawnDnaEatEffect(float x, float y, int value, boolean big) {
        playGameSoundEffect(SOUND_DNA_EAT);
        int count = big ? 34 : Math.min(18, 7 + Math.max(0, value));
        int color = big ? Color.rgb(142, 255, 88) : COLOR_BLOCK;
        for (int i = 0; i < count; i++) {
            float angle = effectsRandom.nextFloat() * (float) Math.PI * 2f;
            float speed = (big ? 145f : 82f) + effectsRandom.nextFloat() * (big ? 150f : 90f);
            float size = (big ? 5.5f : 3.2f) + effectsRandom.nextFloat() * (big ? 8.5f : 5.5f);
            addParticle(x, y, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed - 25f,
                    90f, size, withAlpha(color, 230), big ? 980L : 620L, false, i % 3 == 0);
        }
    }

    private void spawnLevelUpEffect() {
        long now = System.currentTimeMillis();
        float baseX = safeContentWidth() * 0.5f;
        float baseY = safeContentHeight() * 0.55f;
        for (int i = 0; i < 54; i++) {
            float spread = (effectsRandom.nextFloat() - 0.5f) * 460f;
            float vx = spread * 0.22f;
            float vy = -220f - effectsRandom.nextFloat() * 260f;
            int color = i % 3 == 0 ? Color.rgb(255, 255, 255) : (i % 3 == 1 ? COLOR_GREEN : Color.rgb(190, 255, 140));
            GameParticle particle = new GameParticle(baseX + spread * 0.35f, baseY + effectsRandom.nextFloat() * 90f,
                    vx, vy, 155f, 4.5f + effectsRandom.nextFloat() * 9f, withAlpha(color, 235), now, 1150L, true, false);
            particle.spin = -180f + effectsRandom.nextFloat() * 360f;
            gameParticles.add(particle);
        }
    }

    private void spawnDeathDisintegration(float x, float y, int color) {
        deathEffectUntilMs = System.currentTimeMillis() + 1000L;
        for (int i = 0; i < 72; i++) {
            float angle = effectsRandom.nextFloat() * (float) Math.PI * 2f;
            float speed = 70f + effectsRandom.nextFloat() * 280f;
            float offsetY = (effectsRandom.nextFloat() - 0.5f) * PLAYER_HEIGHT;
            addParticle(x, y + offsetY, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed,
                    58f, 3.5f + effectsRandom.nextFloat() * 8f, withAlpha(color, 235), 1000L, false, true);
        }
        float sx = safeContentWidth() * 0.5f;
        float sy = safeContentHeight() * 0.5f;
        for (int i = 0; i < 64; i++) {
            float angle = effectsRandom.nextFloat() * (float) Math.PI * 2f;
            float speed = 95f + effectsRandom.nextFloat() * 330f;
            addParticle(sx, sy + (effectsRandom.nextFloat() - 0.5f) * 145f,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    80f,
                    3.4f + effectsRandom.nextFloat() * 8.5f,
                    withAlpha(color, 230),
                    1000L,
                    true,
                    true);
        }
    }

    private void spawnKillBurst(float x, float y, int color) {
        playGameSoundEffect(SOUND_ENEMY_KILL);
        for (int i = 0; i < 52; i++) {
            float angle = effectsRandom.nextFloat() * (float) Math.PI * 2f;
            float speed = 110f + effectsRandom.nextFloat() * 340f;
            addParticle(x, y, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed,
                    76f, 4f + effectsRandom.nextFloat() * 9f, withAlpha(color, 235), 820L, false, true);
        }
    }

    private void spawnBossSpawnEffect(float x, float y, long now) {
        bossSpawnFlashUntilMs = now + 900L;
        bossShakeUntilMs = now + 650L;
        for (int i = 0; i < 82; i++) {
            float angle = effectsRandom.nextFloat() * (float) Math.PI * 2f;
            float speed = 130f + effectsRandom.nextFloat() * 420f;
            int color = i % 4 == 0 ? Color.rgb(255, 190, 190) : Color.rgb(255, 82, 82);
            addParticle(x, y, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed,
                    42f, 5f + effectsRandom.nextFloat() * 12f, withAlpha(color, 220), 1100L, false, i % 2 == 0);
        }
    }

    private void addParticle(float x, float y, float vx, float vy, float gravity, float size, int color, long lifeMs, boolean screenSpace, boolean square) {
        GameParticle particle = new GameParticle(x, y, vx, vy, gravity, size, color, System.currentTimeMillis(), lifeMs, screenSpace, square);
        particle.spin = -240f + effectsRandom.nextFloat() * 480f;
        gameParticles.add(particle);
    }

    private float bossShakeOffset(long now, int axis, float scale) {
        if (now >= bossShakeUntilMs) {
            return 0f;
        }
        float left = (bossShakeUntilMs - now) / 650f;
        float phase = animationSeconds() * (axis == 0 ? 62f : 71f);
        return (float) Math.sin(phase) * 9f * scale * left;
    }

    private void drawBossSpawnWorldEffect(Canvas canvas, float w, float h, float scale, float zoom, float cameraLeft, float cameraTop, long now) {
        if (now < bossSpawnFlashUntilMs) {
            float left = (bossSpawnFlashUntilMs - now) / 900f;
            int alpha = clampInt(Math.round(150f * left), 0, 150);
            float sx = (boss.x - cameraLeft) * zoom;
            float sy = (boss.y - cameraTop) * zoom;
            if (sx < -260f * scale || sy < -260f * scale || sx > w + 260f * scale || sy > h + 260f * scale) {
                return;
            }
            float pulse = (1f - left) * 95f * scale;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(Math.max(24, alpha / 3), 255, 35, 35));
            canvas.drawCircle(sx, sy, 92f * scale + pulse, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(7f * scale * left);
            paint.setColor(Color.argb(alpha, 255, 70, 70));
            canvas.drawCircle(sx, sy, 118f * scale + pulse, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawServerDnaPickups(Canvas canvas, float w, float h, float zoom, float cameraLeft, float cameraTop, float scale) {
        ServerDnaPickup big = serverBigDna;
        if (big != null && big.active) {
            float sx = (big.x - cameraLeft) * zoom;
            float sy = (big.y - cameraTop) * zoom;
            float zone = big.zoneRadius * zoom;
            drawBigDnaZone(canvas, sx, sy, zone, distanceSq(playerX, playerY, big.x, big.y) <= big.zoneRadius * big.zoneRadius, scale);
            drawServerDnaIcon(canvas, sx, sy, Math.max(42f * scale, big.radius * zoom), big.value, true, scale);
        }

        for (ServerDnaPickup pickup : serverDnaPickups) {
            if (!pickup.active) {
                continue;
            }
            float sx = (pickup.x - cameraLeft) * zoom;
            float sy = (pickup.y - cameraTop) * zoom;
            if ("big".equals(pickup.kind)) {
                float zone = pickup.zoneRadius * zoom;
                drawBigDnaZone(canvas, sx, sy, zone, distanceSq(playerX, playerY, pickup.x, pickup.y) <= pickup.zoneRadius * pickup.zoneRadius, scale);
                drawServerDnaIcon(canvas, sx, sy, Math.max(42f * scale, pickup.radius * zoom), pickup.value, true, scale);
                continue;
            }
            float radius = Math.max((pickup.kind.equals("ultra") ? 30f : 18f) * scale, pickup.radius * zoom);
            if (sx < -radius || sy < -radius || sx > w + radius || sy > h + radius) {
                continue;
            }
            drawServerDnaIcon(canvas, sx, sy, radius, pickup.value, pickup.kind.equals("ultra"), scale);
        }
    }

    private void drawBigDnaZone(Canvas canvas, float sx, float sy, float radius, boolean playerInside, float scale) {
        float time = animationSeconds();
        int waves = 54;
        float react = playerInside ? 1f : 0f;
        path.reset();
        for (int i = 0; i <= waves; i++) {
            float t = i / (float) waves;
            float angle = (float) (Math.PI * 2.0 * t);
            float wobble = (float) Math.sin(angle * 5.0 + time * 2.8f) * radius * (0.030f + react * 0.020f)
                    + (float) Math.sin(angle * 9.0 - time * 1.9f) * radius * (0.015f + react * 0.014f);
            if (playerInside) {
                float playerAngle = (float) Math.atan2((getHeight() * 0.5f) - sy, (getWidth() * 0.5f) - sx);
                float pull = (float) Math.cos(angle - playerAngle);
                wobble += Math.max(0f, pull) * radius * 0.035f;
            }
            float r = radius + wobble;
            float x = sx + (float) Math.cos(angle) * r;
            float y = sy + (float) Math.sin(angle) * r;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(playerInside ? 62 : 34, 131, 213, 92));
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((playerInside ? 5f : 3f) * scale);
        paint.setColor(Color.argb(playerInside ? 190 : 130, 142, 255, 88));
        canvas.drawPath(path, paint);

        for (int i = 0; i < 3; i++) {
            float ring = radius * (0.48f + i * 0.18f + fract(time * 0.12f + i * 0.22f) * 0.08f);
            paint.setStrokeWidth((1.4f + react * 1.2f) * scale);
            paint.setColor(Color.argb(playerInside ? 92 : 42, 190, 255, 156));
            canvas.drawCircle(sx, sy, ring, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawServerDnaIcon(Canvas canvas, float sx, float sy, float radius, int value, boolean ultra, float scale) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ultra ? Color.argb(92, 142, 255, 88) : Color.argb(58, 131, 213, 92));
        canvas.drawCircle(sx, sy, radius * 1.55f, paint);
        paint.setColor(ultra ? Color.rgb(142, 255, 88) : COLOR_BLOCK);
        canvas.drawCircle(sx, sy, radius, paint);
        drawDnaTexture(canvas, sx, sy, radius * 0.75f, scale, Color.rgb(18, 24, 18));
        if (value > 1) {
            paint.setTypeface(titleFont);
            paint.setTextSize(13f * scale);
            paint.setColor(Color.rgb(12, 16, 12));
            drawCenteredText(canvas, "+" + value, sx, sy + radius + 15f * scale, paint);
        }
    }

    private void drawBossDnaOrbs(Canvas canvas, float w, float h, float zoom, float cameraLeft, float cameraTop, float scale) {
        for (BossDnaOrb orb : bossDnaOrbs) {
            if (!orb.active) {
                continue;
            }
            float sx = (orb.x - cameraLeft) * zoom;
            float sy = (orb.y - cameraTop) * zoom;
            if (sx < -80f * scale || sy < -80f * scale || sx > w + 80f * scale || sy > h + 80f * scale) {
                continue;
            }

            float radius = (22f + Math.min(22f, orb.amount * 0.035f)) * scale;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(96, 131, 213, 92));
            canvas.drawCircle(sx, sy, radius * 1.55f, paint);
            paint.setColor(Color.rgb(142, 255, 88));
            canvas.drawCircle(sx, sy, radius, paint);
            paint.setColor(Color.argb(150, 255, 255, 255));
            canvas.drawCircle(sx - radius * 0.28f, sy - radius * 0.28f, radius * 0.32f, paint);

            paint.setTypeface(titleFont);
            paint.setTextSize(14f * scale);
            paint.setColor(Color.rgb(12, 16, 12));
            drawCenteredText(canvas, String.valueOf(orb.amount), sx, sy + 5f * scale, paint);
        }
    }

    private void drawBoss(Canvas canvas, float w, float h, float zoom, float cameraLeft, float cameraTop, float scale) {
        if (!boss.active) {
            return;
        }

        float sx = (boss.x - cameraLeft) * zoom;
        float sy = (boss.y - cameraTop) * zoom;
        float width = BOSS_WIDTH * zoom;
        float height = BOSS_HEIGHT * zoom;
        if (sx < -height || sy < -height || sx > w + height || sy > h + height) {
            return;
        }

        int save = canvas.save();
        canvas.rotate(boss.angle, sx, sy);
        RectF body = new RectF(sx - width * 0.5f, sy - height * 0.5f, sx + width * 0.5f, sy + height * 0.5f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(190, 70, 82));
        canvas.drawRoundRect(body, width * 0.5f, width * 0.5f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(9f * scale);
        paint.setColor(COLOR_BLACK);
        canvas.drawRoundRect(body, width * 0.5f, width * 0.5f, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.restoreToCount(save);

        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, "БОСС  " + boss.dna + " ДНК", sx, sy - height * 0.5f - 18f * scale, paint);

        RectF barBack = new RectF(sx - 120f * scale, sy - height * 0.5f - 10f * scale, sx + 120f * scale, sy - height * 0.5f + 2f * scale);
        paint.setColor(Color.rgb(38, 16, 18));
        canvas.drawRoundRect(barBack, 8f * scale, 8f * scale, paint);
        float progress = clamp((boss.dna - BOSS_DEFEAT_DNA) / (float) (BOSS_START_DNA - BOSS_DEFEAT_DNA), 0f, 1f);
        rect.set(barBack.left, barBack.top, barBack.left + barBack.width() * progress, barBack.bottom);
        paint.setColor(Color.rgb(255, 95, 95));
        canvas.drawRoundRect(rect, 8f * scale, 8f * scale, paint);
    }

    private void drawBossDirectionIndicator(Canvas canvas, float w, float h, float zoom, float cameraLeft, float cameraTop, float scale, long now) {
        if (!boss.active) {
            return;
        }

        float sx = (boss.x - cameraLeft) * zoom;
        float sy = (boss.y - cameraTop) * zoom;
        float safeLeft = 42f * scale;
        float safeTop = 104f * scale;
        float safeRight = w - 42f * scale;
        float safeBottom = h - 42f * scale;
        if (safeRight <= safeLeft || safeBottom <= safeTop) {
            return;
        }
        if (sx >= safeLeft && sx <= safeRight && sy >= safeTop && sy <= safeBottom) {
            return;
        }

        float centerX = w * 0.5f;
        float centerY = h * 0.5f;
        float dx = sx - centerX;
        float dy = sy - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.001f) {
            return;
        }

        float tx = Float.POSITIVE_INFINITY;
        if (dx > 0f) {
            tx = (safeRight - centerX) / dx;
        } else if (dx < 0f) {
            tx = (safeLeft - centerX) / dx;
        }
        float ty = Float.POSITIVE_INFINITY;
        if (dy > 0f) {
            ty = (safeBottom - centerY) / dy;
        } else if (dy < 0f) {
            ty = (safeTop - centerY) / dy;
        }
        float t = Math.min(tx, ty);
        float indicatorX = Float.isInfinite(t) || t <= 0f ? clamp(sx, safeLeft, safeRight) : centerX + dx * t;
        float indicatorY = Float.isInfinite(t) || t <= 0f ? clamp(sy, safeTop, safeBottom) : centerY + dy * t;
        indicatorX = clamp(indicatorX, safeLeft, safeRight);
        indicatorY = clamp(indicatorY, safeTop, safeBottom);

        float pulse = 0.5f + 0.5f * (float) Math.sin(now / 170.0);
        float halo = (28f + pulse * 7f) * scale;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(92, 255, 73, 83));
        canvas.drawCircle(indicatorX, indicatorY, halo, paint);
        paint.setColor(Color.argb(205, 15, 12, 13));
        canvas.drawCircle(indicatorX, indicatorY, 26f * scale, paint);

        float size = 22f * scale;
        path.reset();
        path.moveTo(size, 0f);
        path.lineTo(-size * 0.36f, -size * 0.62f);
        path.lineTo(-size * 0.16f, -size * 0.20f);
        path.lineTo(-size, -size * 0.20f);
        path.lineTo(-size, size * 0.20f);
        path.lineTo(-size * 0.16f, size * 0.20f);
        path.lineTo(-size * 0.36f, size * 0.62f);
        path.close();

        int save = canvas.save();
        canvas.translate(indicatorX, indicatorY);
        canvas.rotate((float) Math.toDegrees(Math.atan2(dy, dx)));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f * scale);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(COLOR_BLACK);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 88, 94));
        canvas.drawPath(path, paint);
        canvas.restoreToCount(save);
        paint.setStrokeJoin(Paint.Join.MITER);

        paint.setTypeface(titleFont);
        paint.setTextSize(16f * scale);
        paint.setColor(COLOR_TEXT);
        float labelY = indicatorY > h - 105f * scale ? indicatorY - 34f * scale : indicatorY + 45f * scale;
        drawCenteredText(canvas, "БОСС", indicatorX, labelY, paint);
    }

    private void drawOpponents(Canvas canvas, float w, float h, float zoom, float cameraLeft, float cameraTop, float scale) {
        float width = PLAYER_WIDTH * zoom;
        float height = PLAYER_HEIGHT * zoom;
        float radius = 100f * zoom;
        for (CellOpponent opponent : opponents) {
            float sx = (opponent.x - cameraLeft) * zoom;
            float sy = (opponent.y - cameraTop) * zoom;
            if (sx < -height || sy < -height || sx > w + height || sy > h + height) {
                continue;
            }

            int save = canvas.save();
            canvas.rotate(opponent.angle, sx, sy);
            RectF body = new RectF(sx - width * 0.5f, sy - height * 0.5f, sx + width * 0.5f, sy + height * 0.5f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(opponent.color);
            canvas.drawRoundRect(body, radius, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4f * scale);
            paint.setColor(COLOR_BLACK);
            canvas.drawRoundRect(body, radius, radius, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.restoreToCount(save);

            paint.setTypeface(boldFont);
            paint.setTextSize(14f * scale);
            paint.setColor(Color.rgb(255, 132, 132));
            drawCenteredText(canvas, "HP " + Math.max(0, Math.round(opponent.hp)) + "/100", sx, sy - height * 0.5f - 30f * scale, paint);

            paint.setTextSize(18f * scale);
            paint.setColor(COLOR_TEXT);
            drawCenteredText(canvas, opponent.name + "  " + opponent.dna + " ДНК", sx, sy - height * 0.5f - 9f * scale, paint);
        }
    }

    private void drawGameLevelProgress(Canvas canvas, float w, float scale) {
        float barW = 460f * scale;
        float left = (w - barW) * 0.5f;
        float top = 14f * scale;
        RectF panel = new RectF(left, top, left + barW, top + 64f * scale);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(166, 0, 0, 0));
        canvas.drawRoundRect(panel, 22f * scale, 22f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(30f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("ур. " + playerLevel(), left + 18f * scale, top + 40f * scale, paint);

        RectF xpBack = new RectF(left + 105f * scale, top + 17f * scale, left + 438f * scale, top + 37f * scale);
        paint.setColor(COLOR_FIELD);
        canvas.drawRoundRect(xpBack, 16f * scale, 16f * scale, paint);
        RectF xpFill = new RectF(xpBack.left, xpBack.top, xpBack.left + xpBack.width() * levelProgress(), xpBack.bottom);
        paint.setColor(COLOR_GREEN);
        canvas.drawRoundRect(xpFill, 16f * scale, 16f * scale, paint);

        RectF hpBack = new RectF(left + 105f * scale, top + 43f * scale, left + 438f * scale, top + 55f * scale);
        paint.setColor(Color.rgb(38, 16, 18));
        canvas.drawRoundRect(hpBack, 16f * scale, 16f * scale, paint);
        RectF hpFill = new RectF(hpBack.left, hpBack.top, hpBack.left + hpBack.width() * clamp(playerHp / 100f, 0f, 1f), hpBack.bottom);
        paint.setColor(Color.rgb(204, 90, 90));
        canvas.drawRoundRect(hpFill, 16f * scale, 16f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(14f * scale);
        paint.setColor(Color.argb(220, 255, 255, 255));
        String xpText = playerXpInLevel() + "/" + xpForCurrentLevel();
        drawCenteredText(canvas, xpText, xpBack.centerX(), xpBack.centerY() + 5f * scale, paint);

        long now = System.currentTimeMillis();
        String buffs = "";
        if (speedBoostUntilMs > now) {
            buffs += " x2";
        }
        if (dnaBoostUntilMs > now) {
            buffs += " ДНКx2";
        }
        if (!buffs.isEmpty()) {
            drawCenteredText(canvas, buffs.trim(), xpBack.centerX(), top + 78f * scale, paint);
        }
        if (boss.active) {
            paint.setColor(Color.rgb(255, 95, 95));
            drawCenteredText(canvas, "БОСС: " + boss.dna + " ДНК", xpBack.centerX(), top + 78f * scale, paint);
        } else if (nextBossSpawnMs > now) {
            long seconds = Math.max(0L, (nextBossSpawnMs - now) / 1000L);
            paint.setColor(COLOR_MUTED);
            drawCenteredText(canvas, "босс через " + (seconds / 60L) + ":" + String.format(Locale.US, "%02d", seconds % 60L), xpBack.centerX(), top + 78f * scale, paint);
        }
    }

    private int powerUpColor(int type) {
        if (type == POWER_SPEED) {
            return Color.rgb(78, 221, 255);
        }
        if (type == POWER_DNA) {
            return Color.rgb(255, 210, 77);
        }
        return Color.rgb(196, 120, 255);
    }

    private Bitmap powerUpBitmap(int type) {
        if (type == POWER_SPEED) {
            return powerSpeedBitmap;
        }
        if (type == POWER_DNA) {
            return powerDnaBitmap;
        }
        return powerTeleportBitmap;
    }

    private String powerUpLabel(int type) {
        if (type == POWER_SPEED) {
            return "x2";
        }
        if (type == POWER_DNA) {
            return "ДНК";
        }
        return "ТП";
    }

    private void drawPlayer(Canvas canvas, float w, float h, float scale) {
        float cx = w * 0.5f;
        float cy = h * 0.5f;
        float width = PLAYER_WIDTH * scale;
        float height = PLAYER_HEIGHT * scale;
        float stroke = 5f * scale;
        int baseColor = playerBaseColor();

        int save = canvas.save();
        canvas.rotate(playerAngleDeg, cx, cy);
        buildMeshPlayerPath(cx, cy, width, height, scale);
        paint.setStyle(Paint.Style.FILL);
        Bitmap skinTexture = selectedSkinTexture();
        if (skinTexture != null) {
            rect.set(cx - width * 0.5f, cy - height * 0.5f, cx + width * 0.5f, cy + height * 0.5f);
            applyTextureShader(skinTexture, rect);
        } else {
            paint.setColor(baseColor);
        }
        canvas.drawPath(path, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(COLOR_BLACK);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.restoreToCount(save);

        paint.setTypeface(titleFont);
        paint.setTextSize(22f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, displayName() + "  " + dnaEaten + " ДНК", cx, cy - height * 0.5f - 14f * scale, paint);
    }

    private void buildMeshPlayerPath(float cx, float cy, float width, float height, float scale) {
        path.reset();
        for (int i = 0; i < PLAYER_MESH_SEGMENTS; i++) {
            float[] base = playerMeshBasePoint(i, width, height);
            playerMeshPointX[i] = cx + base[0] + playerMeshOffsetX[i] * scale;
            playerMeshPointY[i] = cy + base[1] + playerMeshOffsetY[i] * scale;
        }

        float startX = (playerMeshPointX[0] + playerMeshPointX[PLAYER_MESH_SEGMENTS - 1]) * 0.5f;
        float startY = (playerMeshPointY[0] + playerMeshPointY[PLAYER_MESH_SEGMENTS - 1]) * 0.5f;
        path.moveTo(startX, startY);
        for (int i = 0; i < PLAYER_MESH_SEGMENTS; i++) {
            int next = (i + 1) % PLAYER_MESH_SEGMENTS;
            float midX = (playerMeshPointX[i] + playerMeshPointX[next]) * 0.5f;
            float midY = (playerMeshPointY[i] + playerMeshPointY[next]) * 0.5f;
            path.quadTo(playerMeshPointX[i], playerMeshPointY[i], midX, midY);
        }
        path.close();
    }

    private float[] playerMeshBasePoint(int index, float width, float height) {
        float radius = width * 0.5f;
        float halfLine = height * 0.5f - radius;
        float t = index / (float) PLAYER_MESH_SEGMENTS;
        float angle = (float) (Math.PI * 2.0 * t - Math.PI * 0.5);
        float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        float x = cos * radius;
        float y = sin >= 0f ? halfLine + sin * radius : -halfLine + sin * radius;
        return new float[]{x, y};
    }

    private void drawPlayerMeshFacets(Canvas canvas, float cx, float cy, int baseColor, float scale) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < PLAYER_MESH_SEGMENTS; i += 2) {
            int next = (i + 2) % PLAYER_MESH_SEGMENTS;
            float innerA = 0.47f + (float) Math.sin(playerMeshWave + i * 0.53f) * 0.035f;
            float innerB = 0.47f + (float) Math.cos(playerMeshWave + next * 0.41f) * 0.035f;
            float ax = playerMeshPointX[i];
            float ay = playerMeshPointY[i];
            float bx = playerMeshPointX[next];
            float by = playerMeshPointY[next];
            float aix = cx + (ax - cx) * innerA;
            float aiy = cy + (ay - cy) * innerA;
            float bix = cx + (bx - cx) * innerB;
            float biy = cy + (by - cy) * innerB;

            meshPath.reset();
            meshPath.moveTo(ax, ay);
            meshPath.lineTo(bx, by);
            meshPath.lineTo(bix, biy);
            meshPath.lineTo(aix, aiy);
            meshPath.close();

            int color = (i % 4 == 0) ? playerHotColor(baseColor) : playerCoolColor(baseColor);
            paint.setColor(withAlpha(color, 34 + (i % 6) * 4));
            canvas.drawPath(meshPath, paint);
        }
    }

    private void drawPlayerMeshEdges(Canvas canvas, float cx, float cy, float scale) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f * scale);
        paint.setColor(Color.argb(72, 0, 0, 0));
        for (int i = 0; i < PLAYER_MESH_SEGMENTS; i += 2) {
            int next = (i + 2) % PLAYER_MESH_SEGMENTS;
            canvas.drawLine(playerMeshPointX[i], playerMeshPointY[i], playerMeshPointX[next], playerMeshPointY[next], paint);
        }
        paint.setStrokeWidth(1.05f * scale);
        paint.setColor(Color.argb(48, 0, 0, 0));
        for (int i = 0; i < PLAYER_MESH_SEGMENTS; i += 4) {
            float inner = 0.48f;
            float ix = cx + (playerMeshPointX[i] - cx) * inner;
            float iy = cy + (playerMeshPointY[i] - cy) * inner;
            int next = (i + 4) % PLAYER_MESH_SEGMENTS;
            float nx = cx + (playerMeshPointX[next] - cx) * inner;
            float ny = cy + (playerMeshPointY[next] - cy) * inner;
            canvas.drawLine(ix, iy, playerMeshPointX[i], playerMeshPointY[i], paint);
            canvas.drawLine(ix, iy, nx, ny, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(96, 0, 0, 0));
        for (int i = 0; i < PLAYER_MESH_SEGMENTS; i += 4) {
            canvas.drawCircle(playerMeshPointX[i], playerMeshPointY[i], 1.9f * scale, paint);
        }
    }

    private void drawPlayerDent(Canvas canvas, float cx, float cy, float width, float height, float scale) {
        float squeeze = clamp(playerDentStrength, 0f, 1f);
        float dentLength = (float) Math.sqrt(playerDentWorldX * playerDentWorldX + playerDentWorldY * playerDentWorldY);
        if (squeeze <= 0.03f || dentLength <= 0.05f) {
            return;
        }

        float angle = (float) Math.toRadians(playerAngleDeg);
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float localX = (playerDentWorldX * cos + playerDentWorldY * sin) / dentLength;
        float localY = (-playerDentWorldX * sin + playerDentWorldY * cos) / dentLength;
        float depth = (7f + 13f * squeeze) * scale;
        float dentW = width * (0.62f + squeeze * 0.22f);
        float dentH = height * (0.22f + squeeze * 0.10f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb((int) (84 + 62 * squeeze), 4, 5, 5));
        if (Math.abs(localX) > Math.abs(localY)) {
            float side = localX >= 0f ? 1f : -1f;
            float centerX = cx + side * (width * 0.5f - depth * 0.10f);
            float centerY = cy + clamp(localY, -0.55f, 0.55f) * height * 0.32f;
            rect.set(centerX - depth * 1.55f, centerY - dentH * 0.5f, centerX + depth * 1.55f, centerY + dentH * 0.5f);
            canvas.drawOval(rect, paint);

            paint.setColor(withAlpha(playerHotColor(playerBaseColor()), (int) (42 + 42 * squeeze)));
            float bulgeX = cx - side * width * 0.22f;
            rect.set(bulgeX - dentW * 0.36f, cy - height * 0.34f, bulgeX + dentW * 0.36f, cy + height * 0.34f);
            canvas.drawOval(rect, paint);
        } else {
            float side = localY >= 0f ? 1f : -1f;
            float centerX = cx + clamp(localX, -0.55f, 0.55f) * width * 0.30f;
            float centerY = cy + side * (height * 0.5f - depth * 0.10f);
            rect.set(centerX - dentW * 0.5f, centerY - depth * 1.55f, centerX + dentW * 0.5f, centerY + depth * 1.55f);
            canvas.drawOval(rect, paint);

            paint.setColor(withAlpha(playerCoolColor(playerBaseColor()), (int) (42 + 42 * squeeze)));
            float bulgeY = cy - side * height * 0.18f;
            rect.set(cx - width * 0.42f, bulgeY - dentH * 0.68f, cx + width * 0.42f, bulgeY + dentH * 0.68f);
            canvas.drawOval(rect, paint);
        }
    }

    private void buildJellyPlayerPath(float cx, float cy, float width, float height) {
        float radius = width * 0.5f;
        float halfLine = height * 0.5f - radius;
        float dentLength = (float) Math.sqrt(playerDentWorldX * playerDentWorldX + playerDentWorldY * playerDentWorldY);
        float localDentX = 0f;
        float localDentY = 0f;
        if (dentLength > 0.01f && playerDentStrength > 0.01f) {
            float angle = (float) Math.toRadians(playerAngleDeg);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            localDentX = (playerDentWorldX * cos + playerDentWorldY * sin) / dentLength;
            localDentY = (-playerDentWorldX * sin + playerDentWorldY * cos) / dentLength;
        }

        path.reset();
        boolean first = true;
        for (int i = 0; i <= 72; i++) {
            float t = i / 72f;
            float angle = (float) (Math.PI * 2.0 * t - Math.PI * 0.5);
            float sin = (float) Math.sin(angle);
            float cos = (float) Math.cos(angle);
            float localX = cos * radius;
            float localY = sin >= 0f ? halfLine + sin * radius : -halfLine + sin * radius;
            float normalLength = (float) Math.sqrt(localX * localX + localY * localY);
            float normalX = normalLength > 0f ? localX / normalLength : 0f;
            float normalY = normalLength > 0f ? localY / normalLength : 0f;
            float hit = Math.max(0f, normalX * localDentX + normalY * localDentY);
            if (hit > 0f) {
                float dent = playerDentStrength * hit * hit * radius * 0.62f;
                localX -= normalX * dent;
                localY -= normalY * dent;
                float ripple = (float) Math.sin(t * Math.PI * 8f) * playerDentStrength * hit * radius * 0.08f;
                localX += -normalY * ripple;
                localY += normalX * ripple;
            }
            float px = cx + localX;
            float py = cy + localY;
            if (first) {
                path.moveTo(px, py);
                first = false;
            } else {
                path.lineTo(px, py);
            }
        }
        path.close();
    }

    private void drawWorldTile(Canvas canvas, RectF bounds, int tile, float radius, float zoom, float scale, int tileX, int tileY) {
        if (tile == TILE_EMPTY) {
            return;
        }

        if (tile == TILE_DNA) {
            drawDnaPickup(canvas, bounds, zoom, scale);
            return;
        }

        if (isWorldBorder(tileX, tileY)) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_WORLD_BORDER);
            canvas.drawRect(bounds, paint);
            return;
        }

        drawBlock(canvas, bounds, radius, 5f * zoom, tileX, tileY);
    }

    private void drawBlock(Canvas canvas, RectF bounds, float radius, float stroke, int tileX, int tileY) {
        boolean topBlocked = hasBlock(tileX, tileY - 1);
        boolean rightBlocked = hasBlock(tileX + 1, tileY);
        boolean bottomBlocked = hasBlock(tileX, tileY + 1);
        boolean leftBlocked = hasBlock(tileX - 1, tileY);

        float tl = topBlocked || leftBlocked ? 0f : radius;
        float tr = topBlocked || rightBlocked ? 0f : radius;
        float br = bottomBlocked || rightBlocked ? 0f : radius;
        float bl = bottomBlocked || leftBlocked ? 0f : radius;

        path.reset();
        path.addRoundRect(
                bounds,
                new float[]{tl, tl, tr, tr, br, br, bl, bl},
                Path.Direction.CW
        );
        drawBlockOutline(canvas, bounds, stroke, tl, tr, br, bl, !topBlocked, !rightBlocked, !bottomBlocked, !leftBlocked);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_BLOCK);
        canvas.drawPath(path, paint);
    }

    private void drawBlockOutline(
            Canvas canvas,
            RectF bounds,
            float stroke,
            float tl,
            float tr,
            float br,
            float bl,
            boolean topOpen,
            boolean rightOpen,
            boolean bottomOpen,
            boolean leftOpen
    ) {
        float left = bounds.left;
        float top = bounds.top;
        float right = bounds.right;
        float bottom = bounds.bottom;
        float maxRadius = Math.max(0f, Math.min((right - left) * 0.5f, (bottom - top) * 0.5f));
        tl = Math.min(tl, maxRadius);
        tr = Math.min(tr, maxRadius);
        br = Math.min(br, maxRadius);
        bl = Math.min(bl, maxRadius);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setColor(COLOR_BLACK);

        if (topOpen) {
            canvas.drawLine(left + tl, top, right - tr, top, paint);
        }
        if (rightOpen) {
            canvas.drawLine(right, top + tr, right, bottom - br, paint);
        }
        if (bottomOpen) {
            canvas.drawLine(right - br, bottom, left + bl, bottom, paint);
        }
        if (leftOpen) {
            canvas.drawLine(left, bottom - bl, left, top + tl, paint);
        }

        RectF arc = new RectF();
        if (topOpen && leftOpen && tl > 0f) {
            arc.set(left, top, left + tl * 2f, top + tl * 2f);
            canvas.drawArc(arc, 180f, 90f, false, paint);
        }
        if (topOpen && rightOpen && tr > 0f) {
            arc.set(right - tr * 2f, top, right, top + tr * 2f);
            canvas.drawArc(arc, 270f, 90f, false, paint);
        }
        if (rightOpen && bottomOpen && br > 0f) {
            arc.set(right - br * 2f, bottom - br * 2f, right, bottom);
            canvas.drawArc(arc, 0f, 90f, false, paint);
        }
        if (bottomOpen && leftOpen && bl > 0f) {
            arc.set(left, bottom - bl * 2f, left + bl * 2f, bottom);
            canvas.drawArc(arc, 90f, 90f, false, paint);
        }

        paint.setStyle(Paint.Style.FILL);
    }

    private void drawDnaPickup(Canvas canvas, RectF bounds, float zoom, float scale) {
        if (dnaBitmap != null) {
            float dnaWidth = 40f * zoom;
            float dnaHeight = 50f * zoom;
            RectF dna = new RectF(
                    bounds.centerX() - dnaWidth * 0.5f,
                    bounds.centerY() - dnaHeight * 0.5f,
                    bounds.centerX() + dnaWidth * 0.5f,
                    bounds.centerY() + dnaHeight * 0.5f
            );
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(255);
            canvas.drawBitmap(dnaBitmap, null, dna, paint);
        } else {
            drawDnaTexture(canvas, bounds.centerX(), bounds.centerY(), bounds.width() * 0.32f, scale, COLOR_BLOCK);
        }
    }

    private void drawDnaTexture(Canvas canvas, float cx, float cy, float radius, float scale, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f * scale, radius * 0.09f));
        paint.setColor(color);
        path.reset();
        for (int i = 0; i <= 18; i++) {
            float t = i / 18f;
            float y = cy - radius + t * radius * 2f;
            float x = cx + (float) Math.sin(t * Math.PI * 3.2f) * radius * 0.34f;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, paint);
        path.reset();
        for (int i = 0; i <= 18; i++) {
            float t = i / 18f;
            float y = cy - radius + t * radius * 2f;
            float x = cx - (float) Math.sin(t * Math.PI * 3.2f) * radius * 0.34f;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, paint);

        paint.setStrokeWidth(Math.max(1.5f * scale, radius * 0.045f));
        for (int i = 2; i <= 16; i += 3) {
            float t = i / 18f;
            float y = cy - radius + t * radius * 2f;
            float wave = (float) Math.sin(t * Math.PI * 3.2f) * radius * 0.34f;
            canvas.drawLine(cx - wave, y, cx + wave, y, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawGameChat(Canvas canvas, float scale) {
        float left = 14f * scale;
        float top = 88f * scale;
        float width = 276f * scale;
        float height = 204f * scale;
        rect.set(left, top, left + width, top + height);
        paint.setColor(Color.argb(128, 0, 0, 0));
        canvas.drawRoundRect(rect, 14f * scale, 14f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(15f * scale);
        paint.setColor(Color.argb(220, 255, 255, 255));
        float messageWidth = width - 24f * scale;
        int maxLines = 6;
        List<String> lines = new ArrayList<>();
        for (int i = chatMessages.size() - 1; i >= 0 && lines.size() < maxLines; i--) {
            List<String> wrapped = wrapChatMessage(chatMessages.get(i), messageWidth);
            if (lines.size() + wrapped.size() > maxLines) {
                break;
            }
            lines.addAll(0, wrapped);
        }
        float y = top + 27f * scale;
        for (String line : lines) {
            canvas.drawText(line, left + 12f * scale, y, paint);
            y += 19f * scale;
        }

        chatInputRect.set(left + 8f * scale, top + height - 45f * scale,
                left + width - 8f * scale, top + height - 7f * scale);
        paint.setColor(COLOR_FIELD);
        canvas.drawRoundRect(chatInputRect, 9f * scale, 9f * scale, paint);
        paint.setTypeface(titleFont);
        paint.setTextSize(16f * scale);
        paint.setColor(COLOR_TEXT);
        String text = chatDraft.isEmpty() ? "введите текст" : chatDraft;
        canvas.drawText(ellipsize(text, chatInputRect.width() - 18f * scale, paint),
                chatInputRect.left + 9f * scale, chatInputRect.top + 25f * scale, paint);
    }

    private List<String> wrapChatMessage(String message, float maxWidth) {
        List<String> lines = new ArrayList<>();
        String remaining = message == null ? "" : message.trim();
        while (!remaining.isEmpty()) {
            int count = paint.breakText(remaining, true, maxWidth, null);
            if (count >= remaining.length()) {
                lines.add(remaining);
                break;
            }
            int split = remaining.lastIndexOf(' ', count);
            if (split <= 0) {
                split = count;
            }
            lines.add(remaining.substring(0, split).trim());
            remaining = remaining.substring(split).trim();
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    private String ellipsize(String text, float maxWidth, Paint textPaint) {
        if (textPaint.measureText(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int count = textPaint.breakText(text, true, Math.max(0f, maxWidth - textPaint.measureText(suffix)), null);
        return text.substring(0, Math.max(0, count)) + suffix;
    }

    private void drawLeaderboardScreen(Canvas canvas, float w, float h, float scale) {
        List<LeaderboardEntry> leaders = currentLeaderboard();
        float panelW = Math.min(w - 48f * scale, 920f * scale);
        float left = (w - panelW) * 0.5f;
        float top = menuContentTop(scale);
        float panelH = Math.min(menuContentHeight(h, scale), 650f * scale);

        rect.set(left, top, left + panelW, top + panelH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 24f * scale, 24f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(30f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("ЛУЧШИЕ ИГРОКИ", left + 30f * scale, top + 48f * scale, paint);
        paint.setTypeface(boldFont);
        paint.setTextSize(16f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("РЕЙТИНГ БАКТЕРИЙ ОНЛАЙН ПО УРОВНЮ", left + 31f * scale, top + 75f * scale, paint);

        if (leaders.isEmpty()) {
            paint.setTextSize(20f * scale);
            paint.setColor(COLOR_MUTED);
            drawCenteredText(canvas, "нет данных о игроках", left + panelW * 0.5f, top + panelH * 0.5f, paint);
            return;
        }

        float rowTop = top + 102f * scale;
        float rowH = 48f * scale;
        int visible = Math.min(10, leaders.size());
        for (int i = 0; i < visible; i++) {
            drawLeaderboardScreenRow(canvas, leaders.get(i), i + 1, left, rowTop + i * rowH, panelW, rowH, scale);
        }

        int selfRank = leaderboardRank(leaders);
        if (selfRank > visible) {
            float selfTop = Math.min(top + panelH - rowH - 22f * scale, rowTop + visible * rowH + 16f * scale);
            drawLeaderboardScreenRow(canvas, leaders.get(selfRank - 1), selfRank, left, selfTop, panelW, rowH, scale);
        }
    }

    private void drawLeaderboardScreenRow(Canvas canvas, LeaderboardEntry entry, int rank,
                                          float left, float top, float width, float height, float scale) {
        rect.set(left + 18f * scale, top, left + width - 18f * scale, top + height - 6f * scale);
        paint.setColor(entry.self ? Color.rgb(50, 75, 43) : COLOR_PANEL_DARK);
        canvas.drawRoundRect(rect, 12f * scale, 12f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(19f * scale);
        paint.setColor(rank <= 3 ? COLOR_GREEN : COLOR_MUTED);
        canvas.drawText("#" + rank, rect.left + 16f * scale, rect.top + 29f * scale, paint);

        String level = "Ур. " + entry.level;
        paint.setTypeface(boldFont);
        paint.setTextSize(17f * scale);
        float levelWidth = paint.measureText(level);
        float levelX = rect.right - 16f * scale - levelWidth;
        paint.setColor(entry.self ? COLOR_GREEN : COLOR_TEXT);
        canvas.drawText(level, levelX, rect.top + 27f * scale, paint);

        // The full leaderboard is an account list: show the player and level
        // there, while the compact in-game board shows session DNA separately.
        String name = entry.name;
        float nameWidth = levelX - (rect.left + 76f * scale) - 12f * scale;
        paint.setTextSize(fitTextSize(name, 18f * scale, nameWidth, boldFont));
        paint.setColor(COLOR_TEXT);
        canvas.drawText(name, rect.left + 76f * scale, rect.top + 28f * scale, paint);
    }

    private int leaderboardRank(List<LeaderboardEntry> leaders) {
        for (int i = 0; i < leaders.size(); i++) {
            if (leaders.get(i).self) {
                return i + 1;
            }
        }
        return -1;
    }

    private void drawLeaderboard(Canvas canvas, float w, float scale) {
        rect.set(w - 200f * scale, 0f, w, 126f * scale);
        paint.setColor(Color.argb(128, 0, 0, 0));
        canvas.drawRoundRect(rect, 0f, 25f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(20f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, "лидеры", w - 100f * scale, 18f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(16f * scale);
        canvas.drawText("1. " + displayName(), w - 184f * scale, 55f * scale, paint);
        paint.setColor(COLOR_MUTED);
        canvas.drawText("2. bot-cell", w - 184f * scale, 82f * scale, paint);
        canvas.drawText("3. micro-7", w - 184f * scale, 109f * scale, paint);
    }

    private void drawLiveLeaderboard(Canvas canvas, float w, float scale) {
        List<LeaderboardEntry> leaders = currentLeaderboard();
        float panelW = 276f * scale;
        float rowH = 27f * scale;
        int visible = Math.min(5, leaders.size());
        float panelH = (46f + Math.max(1, visible) * 30f) * scale;
        rect.set(w - panelW, 0f, w, panelH);
        paint.setColor(Color.argb(128, 0, 0, 0));
        canvas.drawRoundRect(rect, 0f, 25f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(20f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, "лидеры", w - panelW * 0.5f, 20f * scale, paint);

        if (leaders.isEmpty()) {
            paint.setTypeface(boldFont);
            paint.setTextSize(16f * scale);
            paint.setColor(COLOR_MUTED);
            canvas.drawText("нет игроков", w - panelW + 18f * scale, 58f * scale, paint);
            return;
        }

        for (int i = 0; i < visible; i++) {
            LeaderboardEntry entry = leaders.get(i);
            float rowTop = 36f * scale + i * rowH;
            if (entry.self) {
                rect.set(w - panelW + 10f * scale, rowTop - 16f * scale, w - 10f * scale, rowTop + 8f * scale);
                paint.setColor(Color.argb(70, 128, 216, 90));
                canvas.drawRoundRect(rect, 10f * scale, 10f * scale, paint);
            }

            String rankName = (i + 1) + ". " + entry.name;
            String dna = entry.dna + " ДНК";
            paint.setTypeface(boldFont);
            paint.setTextSize(fitTextSize(dna, 15f * scale, 70f * scale, boldFont));
            float dnaWidth = paint.measureText(dna);
            float dnaX = w - 16f * scale - dnaWidth;
            paint.setColor(entry.self ? COLOR_GREEN : COLOR_MUTED);
            canvas.drawText(dna, dnaX, rowTop, paint);

            float nameWidth = dnaX - (w - panelW + 18f * scale) - 8f * scale;
            paint.setTypeface(boldFont);
            paint.setTextSize(fitTextSize(rankName, 17f * scale, nameWidth, boldFont));
            paint.setColor(entry.self ? COLOR_TEXT : Color.argb(235, 255, 255, 255));
            canvas.drawText(rankName, w - panelW + 18f * scale, rowTop, paint);
        }
    }

    private void drawGameJoysticks(Canvas canvas, float w, float h, float scale) {
        if (controlMode == 0) {
            drawJoystickZone(canvas, 0f, h - 355f * scale, 433f * scale, 355f * scale, leftJoyBaseX, leftJoyBaseY, leftJoyKnobX, leftJoyKnobY, scale);
        } else {
            drawJoystickZone(canvas, w - 433f * scale, h - 358f * scale, 433f * scale, 355f * scale, rightJoyBaseX, rightJoyBaseY, rightJoyKnobX, rightJoyKnobY, scale);
        }
    }

    private void drawJoystickZone(Canvas canvas, float left, float top, float width, float height, float baseX, float baseY, float knobX, float knobY, float scale) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f * scale);
        paint.setColor(Color.rgb(75, 75, 75));
        canvas.drawCircle(baseX, baseY, 60f * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(115, 128, 216, 90));
        canvas.drawCircle(knobX, knobY, 22f * scale, paint);
    }

    private void drawTabs(Canvas canvas, float h, float scale) {
        tabRects.clear();
        int count = tabCount();
        float icon = 58f * scale;
        float gap = 16f * scale;
        float barW = (20f + count * 58f + (count - 1) * 16f + 14f) * scale;
        float barH = 108f * scale;
        float left = 0f;
        float top = h - barH - 22f * scale;

        float appear = easeOutCubic(menuTransitionProgress());
        int tabSave = canvas.saveLayerAlpha(left - 42f * scale, top - 58f * scale, left + barW + 48f * scale, h + 16f * scale,
                clampInt(Math.round(70f + 185f * appear), 0, 255));
        canvas.translate(0f, (1f - appear) * 46f * scale);

        rect.set(left - 24f * scale, top, left + barW, top + barH);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 25f * scale, 25f * scale, paint);

        float iconTop = top + 11f * scale;
        for (int i = 0; i < count; i++) {
            float iconLeft = 10f * scale + i * (icon + gap);
            rect.set(iconLeft, iconTop, iconLeft + icon, iconTop + icon);
            tabRects.add(new RectF(rect));
            Screen tabScreen = tabScreenAt(i);
            boolean active = screen == tabScreen;
            float itemProgress = menuStaggerProgress(i, count);
            float itemScale = 0.72f + Math.min(1f, easeOutBack(itemProgress)) * 0.28f;
            if (active) {
                itemScale += (0.022f + 0.014f * (float) Math.sin(animationSeconds() * 4.7f));
            }
            int itemSave = canvas.save();
            canvas.scale(itemScale, itemScale, rect.centerX(), rect.centerY());
            paint.setColor(COLOR_PANEL_DARK);
            canvas.drawRoundRect(rect, 13f * scale, 13f * scale, paint);
            if (active) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3f * scale);
                paint.setColor(Color.argb(150, 230, 230, 230));
                canvas.drawRoundRect(rect, 13f * scale, 13f * scale, paint);
                paint.setStyle(Paint.Style.FILL);
            }

            int iconColor = active ? COLOR_TEXT : Color.rgb(130, 130, 130);
            if (i == 0) {
                drawTabBitmapIcon(canvas, rect, tabMainBitmap, iconColor, scale);
            } else if (i == 1) {
                drawTabBitmapIcon(canvas, rect, tabRewardsBitmap, iconColor, scale);
            } else if (i == 2) {
                drawTabBitmapIcon(canvas, rect, tabProfileBitmap, iconColor, scale);
            } else if (i == 3) {
                drawTabBitmapIcon(canvas, rect, tabShopBitmap, iconColor, scale);
            } else if (i == 4) {
                drawTabBitmapIcon(canvas, rect, tabLeaderboardBitmap, iconColor, scale);
            } else if (i == 5) {
                drawTabBitmapIcon(canvas, rect, tabEditorBitmap, skinsUnlocked() ? iconColor : Color.rgb(70, 70, 70), scale);
            } else if (i == 6) {
                drawTabBitmapIcon(canvas, rect, tabMusicBitmap, iconColor, scale);
            } else {
                drawTabBitmapIcon(canvas, rect, tabSettingsBitmap, iconColor, scale);
            }
            paint.setTypeface(boldFont);
            paint.setTextSize(12f * scale);
            paint.setColor(active ? COLOR_TEXT : COLOR_MUTED);
            drawCenteredText(canvas, tabLabelAt(i), rect.centerX(), rect.bottom + 17f * scale, paint);
            canvas.restoreToCount(itemSave);
        }
        canvas.restoreToCount(tabSave);
    }

    private int tabCount() {
        return 8;
    }

    private String tabLabelAt(int index) {
        String[] labels = {"главная", "награды", "профиль", "магазин", "топ", "скины", "музыка", "настройки"};
        return index >= 0 && index < labels.length ? labels[index] : "";
    }

    private Screen tabScreenAt(int index) {
        if (index == 0) {
            return Screen.MAIN;
        }
        if (index == 1) {
            return Screen.REWARDS;
        }
        if (index == 2) {
            return Screen.PROFILE;
        }
        if (index == 3) {
            return Screen.SHOP;
        }
        if (index == 4) {
            return Screen.LEADERBOARD;
        }
        if (index == 5) {
            return Screen.EDITOR;
        }
        if (index == 6) {
            return Screen.MUSIC;
        }
        return Screen.SETTINGS;
    }

    private void drawInput(Canvas canvas, RectF bounds, String label, String value, boolean focused, float scale, boolean passwordField) {
        paint.setColor(COLOR_FIELD);
        canvas.drawRoundRect(bounds, 20f * scale, 20f * scale, paint);
        if (focused) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4f * scale);
            paint.setColor(COLOR_GREEN);
            canvas.drawRoundRect(bounds, 20f * scale, 20f * scale, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        paint.setTypeface(boldFont);
        paint.setTextSize(17f * scale);
        paint.setColor(COLOR_MUTED);
        canvas.drawText(label, bounds.left + 20f * scale, bounds.top + 22f * scale, paint);

        String shown = value;
        if (passwordField && !value.isEmpty()) {
            shown = repeat("•", value.length());
        }
        if (shown.isEmpty()) {
            shown = label.equals("USERNAME") ? "игрок" : label.equals("EMAIL") ? "mail@example.com" : "пароль";
            paint.setColor(Color.rgb(70, 70, 70));
        } else {
            paint.setColor(COLOR_TEXT);
        }
        paint.setTypeface(titleFont);
        paint.setTextSize(25f * scale);
        canvas.drawText(shown, bounds.left + 20f * scale, bounds.top + 51f * scale, paint);
    }

    private void drawServerRow(Canvas canvas, float left, float top, float width, float height, String label, int index, float scale) {
        RectF row = new RectF(left, top, left + width, top + height);
        serverRects.add(row);
        paint.setColor(COLOR_FIELD);
        canvas.drawRoundRect(row, 13f * scale, 13f * scale, paint);

        if (selectedServer == index) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5f * scale);
            paint.setColor(Color.rgb(210, 210, 210));
            canvas.drawRoundRect(row, 13f * scale, 13f * scale, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        paint.setColor(selectedServer == index ? COLOR_GREEN : Color.rgb(90, 90, 90));
        canvas.drawCircle(row.left + 14f * scale, row.centerY(), 5f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(26f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, label, row.centerX() + 8f * scale, row.centerY() + 9f * scale, paint);
    }

    private void drawAvatar(Canvas canvas, RectF bounds, float scale) {
        paint.setColor(Color.rgb(214, 214, 214));
        canvas.drawRoundRect(bounds, 25f * scale, 25f * scale, paint);
        if (avatarBitmap != null) {
            canvas.drawBitmap(avatarBitmap, null, bounds, paint);
            return;
        }
        drawRewardMicrobe(canvas, bounds.centerX(), bounds.centerY(), playerBaseColor(), scale * 2.2f);
    }

    private void drawGeneRow(Canvas canvas, float left, float top, float width, int index, float scale) {
        rect.set(left, top, left + width, top + 88f * scale);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 18f * scale, 18f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(25f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText(geneNames[index], left + 20f * scale, top + 32f * scale, paint);

        paint.setTypeface(boldFont);
        paint.setTextSize(fitTextSize(geneDescriptions[index], 16f * scale, 172f * scale, boldFont));
        paint.setColor(COLOR_MUTED);
        canvas.drawText(geneDescriptions[index], left + 20f * scale, top + 58f * scale, paint);

        float buttonW = 98f * scale;
        float buttonLeft = left + width - buttonW - 18f * scale;
        float barLeft = left + 214f * scale;
        float barTop = top + 28f * scale;
        float barW = Math.max(82f * scale, buttonLeft - barLeft - 18f * scale);
        rect.set(barLeft, barTop, barLeft + barW, barTop + 20f * scale);
        paint.setColor(COLOR_FIELD);
        canvas.drawRoundRect(rect, 10f * scale, 10f * scale, paint);
        rect.set(barLeft, barTop, barLeft + Math.min(barW, geneLevels[index] * (barW / (float) GENE_MAX_LEVEL)), barTop + 20f * scale);
        paint.setColor(COLOR_GREEN);
        canvas.drawRoundRect(rect, 10f * scale, 10f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(20f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText("ур. " + geneLevels[index], barLeft, top + 70f * scale, paint);

        RectF button = new RectF(buttonLeft, top + 18f * scale, buttonLeft + buttonW, top + 70f * scale);
        geneUpgradeRects.add(button);
        boolean maxed = geneLevels[index] >= GENE_MAX_LEVEL;
        boolean affordable = !maxed && coins >= geneCost(index);
        drawButton(canvas, button, maxed ? "max" : geneCost(index) + "", affordable ? COLOR_GREEN : Color.rgb(58, 58, 58), affordable ? Color.rgb(14, 16, 14) : COLOR_MUTED, scale, 18f);
    }

    private void drawSkinShop(Canvas canvas, float left, float top, float width, float scale) {
        skinRects.clear();
        boolean unlocked = skinsUnlocked();

        rect.set(left, top, left + width, top + 48f * scale);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, 16f * scale, 16f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(18f * scale);
        paint.setColor(unlocked ? COLOR_TEXT : COLOR_MUTED);
        canvas.drawText(unlocked ? "магазин скинов" : "скины: максимум генов", left + 14f * scale, top + 30f * scale, paint);

        float swatch = 52f * scale;
        float start = left + width - (swatch * skinNames.length) - 18f * scale;
        for (int i = 0; i < skinNames.length; i++) {
            RectF skin = new RectF(start + i * swatch, top + 7f * scale, start + i * swatch + 42f * scale, top + 42f * scale);
            skinRects.add(skin);
            drawSkinSwatch(canvas, skin, 14f * scale, i, unlocked, scale);
            if (unlocked && selectedSkin == i) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3f * scale);
                paint.setColor(COLOR_TEXT);
                canvas.drawRoundRect(skin, 14f * scale, 14f * scale, paint);
                paint.setStyle(Paint.Style.FILL);
            }
            if (unlocked && !skinOwned[i]) {
                paint.setTypeface(boldFont);
                paint.setTextSize(12f * scale);
                paint.setColor(Color.rgb(14, 16, 14));
                drawCenteredText(canvas, String.valueOf(SKIN_COSTS[i]), skin.centerX(), skin.centerY() + 4f * scale, paint);
            }
        }
    }

    private void drawSettingRow(Canvas canvas, float left, float top, String title, String value, RectF touchRect, float scale) {
        rect.set(left, top, left + 736f * scale, top + 66f * scale);
        touchRect.set(rect);
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(rect, 20f * scale, 20f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(29f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText(title, left + 24f * scale, top + 42f * scale, paint);

        RectF valueRect = new RectF(left + 505f * scale, top + 10f * scale, left + 715f * scale, top + 56f * scale);
        paint.setColor(COLOR_GREEN);
        canvas.drawRoundRect(valueRect, 18f * scale, 18f * scale, paint);
        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(Color.rgb(14, 16, 14));
        drawCenteredText(canvas, value, valueRect.centerX(), valueRect.centerY() + 8f * scale, paint);
    }

    private void drawSettingSlider(Canvas canvas, float left, float top, String title, int value, RectF sliderRect, float scale) {
        rect.set(left, top, left + 736f * scale, top + 58f * scale);
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawRoundRect(rect, 18f * scale, 18f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_TEXT);
        canvas.drawText(title, left + 24f * scale, top + 37f * scale, paint);

        sliderRect.set(left + 245f * scale, top + 21f * scale, left + 620f * scale, top + 39f * scale);
        paint.setColor(COLOR_FIELD);
        canvas.drawRoundRect(sliderRect, 12f * scale, 12f * scale, paint);
        float progress = clamp(value / 100f, 0f, 1f);
        rect.set(sliderRect.left, sliderRect.top, sliderRect.left + sliderRect.width() * progress, sliderRect.bottom);
        paint.setColor(COLOR_GREEN);
        canvas.drawRoundRect(rect, 12f * scale, 12f * scale, paint);
        float knobX = sliderRect.left + sliderRect.width() * progress;
        paint.setColor(COLOR_TEXT);
        canvas.drawCircle(knobX, sliderRect.centerY(), 16f * scale, paint);
        paint.setColor(COLOR_GREEN);
        canvas.drawCircle(knobX, sliderRect.centerY(), 10f * scale, paint);

        paint.setTypeface(titleFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, value + "%", left + 682f * scale, top + 38f * scale, paint);
    }

    private void drawButton(Canvas canvas, RectF bounds, String label, int fill, int text, float scale, float radius) {
        boolean lively = fill == COLOR_GREEN || fill == COLOR_GREEN_DARK;
        float pulse = lively ? 1f + (float) Math.sin(animationSeconds() * 3.8f + bounds.left * 0.018f + bounds.top * 0.011f) * 0.012f : 1f;
        int save = canvas.save();
        canvas.scale(pulse, pulse, bounds.centerX(), bounds.centerY());
        if (lively) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(42, 128, 216, 90));
            rect.set(bounds.left - 4f * scale, bounds.top - 4f * scale, bounds.right + 4f * scale, bounds.bottom + 4f * scale);
            canvas.drawRoundRect(rect, (radius + 4f) * scale, (radius + 4f) * scale, paint);
        }
        paint.setColor(fill);
        canvas.drawRoundRect(bounds, radius * scale, radius * scale, paint);
        if (lively) {
            paint.setColor(Color.argb(42, 255, 255, 255));
            rect.set(bounds.left + 10f * scale, bounds.top + 7f * scale, bounds.right - 10f * scale, bounds.top + bounds.height() * 0.42f);
            canvas.drawRoundRect(rect, radius * 0.7f * scale, radius * 0.7f * scale, paint);
        }
        paint.setTypeface(titleFont);
        paint.setTextSize(26f * scale);
        paint.setColor(text);
        drawCenteredText(canvas, label, bounds.centerX(), bounds.centerY() + 9f * scale, paint);
        canvas.restoreToCount(save);
        paint.setAlpha(255);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawStatus(Canvas canvas, float w, float h, float scale) {
        if (statusMessage.isEmpty()) {
            return;
        }
        paint.setTypeface(boldFont);
        paint.setTextSize(24f * scale);
        paint.setColor(COLOR_MUTED);
        drawCenteredText(canvas, statusMessage, w * 0.5f, h - 58f * scale, paint);
    }

    private void drawPlayIcon(Canvas canvas, RectF box, int color, float scale) {
        path.reset();
        path.moveTo(box.left + 18f * scale, box.top + 13f * scale);
        path.lineTo(box.right - 13f * scale, box.centerY());
        path.lineTo(box.left + 18f * scale, box.bottom - 13f * scale);
        path.close();
        paint.setColor(color);
        canvas.drawPath(path, paint);
    }

    private void drawCalendarIcon(Canvas canvas, RectF box, int color, float scale) {
        rect.set(box.left + 14f * scale, box.top + 18f * scale, box.right - 14f * scale, box.bottom - 12f * scale);
        paint.setColor(Color.rgb(62, 62, 62));
        canvas.drawRoundRect(rect, 11f * scale, 11f * scale, paint);
        paint.setColor(color);
        canvas.drawRect(rect.left, rect.top + 13f * scale, rect.right, rect.top + 21f * scale, paint);
        paint.setStrokeWidth(4f * scale);
        canvas.drawLine(rect.left + 15f * scale, rect.top - 2f * scale, rect.left + 15f * scale, rect.top + 8f * scale, paint);
        canvas.drawLine(rect.right - 15f * scale, rect.top - 2f * scale, rect.right - 15f * scale, rect.top + 8f * scale, paint);
        paint.setTypeface(titleFont);
        paint.setTextSize(14f * scale);
        paint.setColor(COLOR_TEXT);
        drawCenteredText(canvas, "день", box.centerX(), box.centerY() + 12f * scale, paint);
    }

    private void drawUserIcon(Canvas canvas, RectF box, int color, float scale) {
        paint.setColor(color);
        canvas.drawCircle(box.centerX(), box.top + 22f * scale, 13f * scale, paint);
        rect.set(box.left + 17f * scale, box.top + 37f * scale, box.right - 17f * scale, box.bottom - 10f * scale);
        canvas.drawArc(rect, 180f, 180f, true, paint);
    }

    private void drawShopIcon(Canvas canvas, RectF box, int color, float scale) {
        paint.setColor(color);
        rect.set(box.left + 15f * scale, box.top + 27f * scale, box.right - 15f * scale, box.bottom - 12f * scale);
        canvas.drawRoundRect(rect, 8f * scale, 8f * scale, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f * scale);
        canvas.drawLine(box.left + 20f * scale, box.top + 28f * scale, box.left + 27f * scale, box.top + 15f * scale, paint);
        canvas.drawLine(box.right - 20f * scale, box.top + 28f * scale, box.right - 27f * scale, box.top + 15f * scale, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawEditorIcon(Canvas canvas, RectF box, int color, float scale) {
        paint.setColor(color);
        rect.set(box.left + 24f * scale, box.top + 12f * scale, box.right - 24f * scale, box.bottom - 10f * scale);
        canvas.drawRoundRect(rect, 18f * scale, 18f * scale, paint);
        paint.setColor(COLOR_PANEL_DARK);
        canvas.drawCircle(box.centerX(), box.top + 28f * scale, 7f * scale, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f * scale);
        paint.setColor(color);
        canvas.drawLine(box.left + 14f * scale, box.bottom - 14f * scale, box.right - 14f * scale, box.top + 14f * scale, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawGearIcon(Canvas canvas, RectF box, int color, float scale) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7f * scale);
        canvas.drawCircle(box.centerX(), box.centerY(), 17f * scale, paint);
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * i / 4.0;
            float sx = box.centerX() + (float) Math.cos(angle) * 24f * scale;
            float sy = box.centerY() + (float) Math.sin(angle) * 24f * scale;
            float ex = box.centerX() + (float) Math.cos(angle) * 30f * scale;
            float ey = box.centerY() + (float) Math.sin(angle) * 30f * scale;
            canvas.drawLine(sx, sy, ex, ey, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(box.centerX(), box.centerY(), 5f * scale, paint);
    }

    private void drawMusicIcon(Canvas canvas, RectF box, int color, float scale) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f * scale);
        paint.setStrokeCap(Paint.Cap.ROUND);
        float stemX = box.centerX() + 10f * scale;
        canvas.drawLine(stemX, box.top + 16f * scale, stemX, box.bottom - 22f * scale, paint);
        canvas.drawLine(stemX, box.top + 16f * scale, box.centerX() - 10f * scale, box.top + 22f * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(
                box.centerX() - 22f * scale,
                box.bottom - 27f * scale,
                box.centerX() + 5f * scale,
                box.bottom - 10f * scale,
                paint
        );
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawTabBitmapIcon(Canvas canvas, RectF box, Bitmap bitmap, int color, float scale) {
        if (bitmap == null) {
            drawUserIcon(canvas, box, color, scale);
            return;
        }
        float maxW = 42f * scale;
        float maxH = 42f * scale;
        float bitmapW = Math.max(1f, bitmap.getWidth());
        float bitmapH = Math.max(1f, bitmap.getHeight());
        float ratio = Math.min(maxW / bitmapW, maxH / bitmapH);
        float drawW = bitmapW * ratio;
        float drawH = bitmapH * ratio;
        rect.set(
                box.centerX() - drawW * 0.5f,
                box.centerY() - drawH * 0.5f,
                box.centerX() + drawW * 0.5f,
                box.centerY() + drawH * 0.5f
        );
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, null, rect, paint);
        paint.setColorFilter(null);
    }

    private void drawBlob(Canvas canvas, float cx, float cy, float radius, int color, float scale) {
        paint.setColor(color);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setColor(Color.argb(34, 255, 255, 255));
        canvas.drawCircle(cx - radius * 0.28f, cy - radius * 0.24f, radius * 0.18f, paint);
        paint.setColor(color);
        canvas.drawCircle(cx - radius * 0.92f, cy + radius * 0.16f, 7f * scale, paint);
        canvas.drawCircle(cx + radius * 0.82f, cy - radius * 0.12f, 6f * scale, paint);
    }

    private void drawRewardMicrobe(Canvas canvas, float cx, float cy, int color, float scale) {
        paint.setColor(color);
        canvas.drawCircle(cx, cy, 22f * scale, paint);
        paint.setColor(Color.argb(90, 255, 255, 255));
        canvas.drawCircle(cx - 7f * scale, cy - 8f * scale, 5f * scale, paint);
    }

    private void drawCenteredText(Canvas canvas, String text, float cx, float baselineY, Paint textPaint) {
        canvas.drawText(text, cx - textPaint.measureText(text) * 0.5f, baselineY, textPaint);
    }

    private void drawWrappedCenteredText(Canvas canvas, String text, float left, float top, float width, float lineHeight, int maxLines, Paint textPaint) {
        String[] words = text.split(" ");
        String line = "";
        int lines = 0;
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (textPaint.measureText(candidate) <= width || line.isEmpty()) {
                line = candidate;
                continue;
            }
            drawCenteredText(canvas, line, left + width * 0.5f, top + lines * lineHeight, textPaint);
            lines++;
            if (lines >= maxLines) {
                return;
            }
            line = word;
        }
        if (!line.isEmpty() && lines < maxLines) {
            drawCenteredText(canvas, line, left + width * 0.5f, top + lines * lineHeight, textPaint);
        }
    }

    private void handleMainTouch(float x, float y) {
        if (battlePassOpenRect.contains(x, y)) {
            setScreen(Screen.PASS);
            statusMessage = "";
            invalidate();
            return;
        }
        if (!buyProPassRect.isEmpty() && buyProPassRect.contains(x, y)) {
            if (sendAccountAction("buyProPass", "")) {
                statusMessage = "покупка Pro pass отправлена на сервер";
            } else {
                statusMessage = "нет соединения с сервером";
            }
            invalidate();
            return;
        }
        for (int i = 0; i < serverRects.size(); i++) {
            if (serverRects.get(i).contains(x, y)) {
                selectedServer = i;
                statusMessage = "сервер выбран";
                invalidate();
                return;
            }
        }
        if (playRect.contains(x, y)) {
            startGame();
            invalidate();
        }
    }

    private void handlePassTouch(float x, float y) {
        if (battlePassBackRect.contains(x, y)) {
            setScreen(Screen.MAIN);
            statusMessage = "";
            invalidate();
            return;
        }
        if (!buyProPassRect.isEmpty() && buyProPassRect.contains(x, y)) {
            if (sendAccountAction("buyProPass", "")) {
                statusMessage = "покупка Pro pass отправлена на сервер";
            } else {
                statusMessage = "нет соединения с сервером";
            }
            invalidate();
        }
    }

    private boolean handleGameTouch(MotionEvent event) {
        float viewW = safeContentWidth();
        float viewH = safeContentHeight();
        float scale = Math.min(viewW / 1600f, viewH / 900f);
        updateJoystickLayout(viewW, viewH, scale);
        if (deathOverlayActive) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                float x = event.getX();
                float y = event.getY();
                if (deathMenuRect.contains(x, y)) {
                    deathOverlayActive = false;
                    setScreen(Screen.MAIN);
                } else if (deathProfileRect.contains(x, y)) {
                    deathOverlayActive = false;
                    viewingExternalProfile = true;
                    externalProfileName = deathKillerName;
                    externalProfileId = deathKillerProfileId.isEmpty() ? "killer" : deathKillerProfileId;
                    externalProfileColor = deathKillerColor;
                    setScreen(Screen.PROFILE);
                } else if (deathRestartRect.contains(x, y)) {
                    deathOverlayActive = false;
                    lastGameFrameMs = 0L;
                    startGame();
                }
                invalidate();
            }
            return true;
        }

        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            float x = event.getX(actionIndex);
            float y = event.getY(actionIndex);
            int pointerId = event.getPointerId(actionIndex);

            if (gamePauseRect.contains(x, y)) {
                exitGameToMainMenu();
                return true;
            }

            if (chatInputRect.contains(x, y)) {
                focusField = FocusField.CHAT;
                showKeyboard();
                invalidate();
                return true;
            }

            focusField = FocusField.NONE;
            hideKeyboard();
            boolean movementSide = (controlMode == 0 && x < viewW * 0.5f) || (controlMode == 1 && x >= viewW * 0.5f);
            if (movementSide && controlMode == 0 && y >= viewH - 380f * scale && leftPointerId == -1) {
                leftPointerId = pointerId;
                updateJoystick(pointerId, x, y, true, scale);
            } else if (movementSide && controlMode == 1 && y >= viewH - 380f * scale && rightPointerId == -1) {
                rightPointerId = pointerId;
                updateJoystick(pointerId, x, y, false, scale);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                int pointerId = event.getPointerId(i);
                if (pointerId == leftPointerId) {
                    updateJoystick(pointerId, event.getX(i), event.getY(i), true, scale);
                } else if (pointerId == rightPointerId) {
                    updateJoystick(pointerId, event.getX(i), event.getY(i), false, scale);
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
            int pointerId = event.getPointerId(actionIndex);
            if (pointerId == leftPointerId) {
                leftPointerId = -1;
                moveX = 0f;
                moveY = 0f;
                leftJoyKnobX = leftJoyBaseX;
                leftJoyKnobY = leftJoyBaseY;
            }
            if (pointerId == rightPointerId) {
                rightPointerId = -1;
                moveX = 0f;
                moveY = 0f;
                rightJoyKnobX = rightJoyBaseX;
                rightJoyKnobY = rightJoyBaseY;
            }
        }

        invalidate();
        return true;
    }

    private void updateJoystick(int pointerId, float x, float y, boolean left, float scale) {
        float baseX = left ? leftJoyBaseX : rightJoyBaseX;
        float baseY = left ? leftJoyBaseY : rightJoyBaseY;
        float radius = 72f * scale;
        float dx = x - baseX;
        float dy = y - baseY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > radius && length > 0f) {
            dx = dx / length * radius;
            dy = dy / length * radius;
        }

        if (left && pointerId == leftPointerId) {
            leftJoyKnobX = baseX + dx;
            leftJoyKnobY = baseY + dy;
            moveX = dx / radius;
            moveY = dy / radius;
            updatePlayerAngle(moveX, moveY);
        } else if (!left && pointerId == rightPointerId) {
            rightJoyKnobX = baseX + dx;
            rightJoyKnobY = baseY + dy;
            moveX = dx / radius;
            moveY = dy / radius;
            updatePlayerAngle(moveX, moveY);
        }
    }

    private void updatePlayerAngle(float dx, float dy) {
        if (Math.abs(dx) <= 0.02f && Math.abs(dy) <= 0.02f) {
            return;
        }
        float newAngle = (float) Math.toDegrees(Math.atan2(dy, dx)) + 90f;
        float[] fit = findPlayerBodyFit(playerX, playerY, newAngle);
        if (fit == null) {
            registerPlayerDeformation(dx, dy, PLAYER_MIN_WIDTH, PLAYER_MIN_HEIGHT);
            return;
        }

        playerAngleDeg = newAngle;
        if (fit[0] < PLAYER_WIDTH || fit[1] < PLAYER_HEIGHT) {
            registerPlayerDeformation(dx, dy, fit[0], fit[1]);
        }
    }

    private void updateJoystickLayout(float w, float h, float scale) {
        leftJoyBaseX = 217f * scale;
        leftJoyBaseY = h - 177f * scale;
        rightJoyBaseX = w - 216f * scale;
        rightJoyBaseY = h - 181f * scale;
        if (leftPointerId == -1) {
            leftJoyKnobX = leftJoyBaseX;
            leftJoyKnobY = leftJoyBaseY;
        }
        if (rightPointerId == -1) {
            rightJoyKnobX = rightJoyBaseX;
            rightJoyKnobY = rightJoyBaseY;
        }
    }

    private void startGame() {
        connectGameServerIfNeeded();
        sendAuthToServer();
        requestCurrentProfile();
        ensureWorldGenerated();
        setScreen(Screen.GAME);
        statusMessage = "";
        focusField = FocusField.NONE;
        lastGameFrameMs = 0L;
        fpsWindowStartMs = 0L;
        fpsFrameCount = 0;
        hideKeyboard();
        if (chatMessages.isEmpty()) {
            chatMessages.add("server: добро пожаловать");
            chatMessages.add("map: 10000x10000, острова");
        }
    }

    private void connectGameServerIfNeeded() {
        gameServerWanted = true;
        if (gameServerConnected || gameServerConnecting) {
            return;
        }
        gameServerConnecting = true;
        Thread thread = new Thread(() -> {
            long reconnectDelayMs = 900L;
            while (gameServerWanted) {
                Socket socket = null;
                BufferedWriter writer = null;
                try {
                    socket = new Socket(GAME_SERVER_HOST, GAME_SERVER_PORT);
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                    gameSocket = socket;
                    gameWriter = writer;
                    gameServerConnected = true;
                    reconnectDelayMs = 900L;

                    sendAuthToServer();
                    if (pendingDailyClaim && sendServerMessage("{\"type\":\"daily\"}")) {
                        pendingDailyClaim = false;
                    }
                    startServerSender();

                    String line;
                    while (gameServerWanted && (line = reader.readLine()) != null) {
                        serverInbox.add(line);
                        postInvalidate();
                    }
                } catch (IOException error) {
                    if (gameServerWanted) {
                        serverInbox.add("{\"type\":\"offline\"}");
                        postInvalidate();
                    }
                } finally {
                    gameServerConnected = false;
                    if (gameWriter == writer) {
                        gameWriter = null;
                    }
                    if (gameSocket == socket) {
                        gameSocket = null;
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException ignored) {
                        }
                    }
                }

                if (gameServerWanted) {
                    try {
                        Thread.sleep(reconnectDelayMs);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    reconnectDelayMs = Math.min(8_000L, reconnectDelayMs * 2L);
                }
            }
            gameServerConnecting = false;
        }, "bacteria-game-server");
        thread.setDaemon(true);
        thread.start();
    }

    private void disconnectGameServer() {
        gameServerWanted = false;
        gameServerConnected = false;
        gameServerConnecting = false;
        gameWriter = null;
        Socket socket = gameSocket;
        gameSocket = null;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void sendAuthToServer() {
        String safeName = username.trim().isEmpty() ? "USERNAME" : username.trim();
        String json = "{\"type\":\"auth\",\"name\":\"" + jsonEscape(safeName)
                + "\",\"password\":\"" + jsonEscape(password) + "\"}";
        if (!sendServerMessage(json)) {
            connectGameServerIfNeeded();
        }
    }

    private void requestCurrentProfile() {
        // A local SharedPreferences cache is only for offline display.  Each
        // game entry asks the server for the account state again.
        sendServerMessage("{\"type\":\"profileGet\"}");
    }

    private boolean sendAccountAction(String action, String fields) {
        String extra = fields == null || fields.isEmpty() ? "" : fields;
        return sendServerMessage("{\"type\":\"accountAction\",\"action\":\"" + jsonEscape(action) + "\"" + extra + "}");
    }

    private boolean sendServerMessage(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        connectGameServerIfNeeded();
        if (gameWriter == null && isTransientServerMessage(message)) {
            return false;
        }
        serverOutbox.add(message);
        startServerSender();
        return true;
    }

    private boolean isTransientServerMessage(String message) {
        return message.contains("\"type\":\"move\"") || message.contains("\"type\":\"ping\"");
    }

    private void startServerSender() {
        if (gameWriter == null) {
            return;
        }
        synchronized (serverOutbox) {
            if (serverSenderRunning) {
                return;
            }
            serverSenderRunning = true;
        }

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String message = serverOutbox.poll();
                    if (message == null) {
                        synchronized (serverOutbox) {
                            if (serverOutbox.isEmpty()) {
                                serverSenderRunning = false;
                                return;
                            }
                        }
                        continue;
                    }
                    if (!writeServerMessage(message)) {
                        serverOutbox.add(message);
                        closeCurrentGameSocket();
                        connectGameServerIfNeeded();
                        return;
                    }
                }
            } finally {
                synchronized (serverOutbox) {
                    serverSenderRunning = false;
                }
            }
        }, "bacteria-server-sender");
        thread.setDaemon(true);
        thread.start();
    }

    private boolean writeServerMessage(String message) {
        BufferedWriter writer = gameWriter;
        if (writer == null) {
            return false;
        }
        try {
            synchronized (writer) {
                writer.write(message);
                writer.newLine();
                writer.flush();
            }
            return true;
        } catch (IOException error) {
            closeCurrentGameSocket();
            return false;
        }
    }

    private void closeCurrentGameSocket() {
        gameServerConnected = false;
        gameWriter = null;
        Socket socket = gameSocket;
        gameSocket = null;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void drainServerMessages() {
        String message;
        while ((message = serverInbox.poll()) != null) {
            applyServerMessage(message);
        }
    }

    private void applyServerMessage(String message) {
        String type = readJsonString(message, "type", "");
        if ("offline".equals(type)) {
            statusMessage = "сервер недоступен, включен локальный режим";
            return;
        }
        if ("welcome".equals(type)) {
            applyServerSeed(readJsonInt(message, "seed", worldSeed));
            latestNewsText = readJsonString(message, "newsText", latestNewsText);
            latestNewsDate = readJsonString(message, "newsDate", latestNewsDate);
            return;
        }
        if ("authError".equals(type)) {
            waitingForAuthentication = false;
            sessionSaved = false;
            statusMessage = readJsonString(message, "message", "не удалось войти");
            disconnectGameServer();
            setScreen(Screen.AUTH);
            saveState();
            return;
        }
        if ("profile".equals(type)) {
            accountId = readJsonString(message, "id", accountId);
            username = readJsonString(message, "name", username);
            sessionSaved = true;
            // The server is authoritative: loading a profile must replace any
            // stale SharedPreferences data left on this device.
            applyServerAccountState(message);
            applyServerSeed(readJsonInt(message, "levelSeed", worldSeed));
            if (waitingForAuthentication) {
                waitingForAuthentication = false;
                setScreen(Screen.MAIN);
                statusMessage = "";
            }
            saveState();
            return;
        }
        if ("profileSaved".equals(type)) {
            applyServerAccountState(message);
            saveState();
            return;
        }
        if ("daily".equals(type)) {
            pendingDailyClaim = false;
            if (readJsonBoolean(message, "ok", false)) {
                int reward = readJsonInt(message, "reward", DAILY_REWARD);
                coins = Math.max(0, readJsonInt(message, "currency", coins));
                dailyClaimed = true;
                prefs.edit().putLong("lastDailyDay", currentDay()).apply();
                statusMessage = "+" + reward + " ген-монет";
            } else {
                coins = Math.max(0, readJsonInt(message, "currency", coins));
                dailyClaimed = true;
                statusMessage = "награда уже забрана на сервере";
            }
            saveState();
            return;
        }
        if ("state".equals(type)) {
            latestNewsText = readJsonString(message, "newsText", latestNewsText);
            latestNewsDate = readJsonString(message, "newsDate", latestNewsDate);
            applyServerSeed(readJsonInt(message, "seed", worldSeed));
            updateServerPlayers(message);
            updateServerLeaderboard(message);
            updateMenuLeaderboard(message);
            updateServerDnaPickups(message);
            updateServerChat(message);
            updateServerBossState(message);
            return;
        }
        if ("market".equals(type)) {
            applyMarketState(message);
            return;
        }
        if ("marketResult".equals(type)) {
            statusMessage = readJsonString(message, "message", readJsonBoolean(message, "ok", false) ? "ТП обновлена" : "ошибка ТП");
            requestMarketState(true);
            return;
        }
        if ("accountResult".equals(type)) {
            statusMessage = readJsonString(message, "message", readJsonBoolean(message, "ok", false) ? "аккаунт обновлён" : "сервер отклонил действие");
            return;
        }
        if ("caseOpened".equals(type)) {
            int skin = readJsonInt(message, "skin", -1);
            int tier = readJsonInt(message, "tier", CASE_TIER_2K);
            int index = readJsonInt(message, "index", -1);
            if (skin >= 0 && skin < skinOwned.length) {
                startCaseOpeningAnimation(index, tier, skin);
            }
            return;
        }
        if ("chat".equals(type)) {
            int seq = readJsonInt(message, "seq", -1);
            String name = readJsonString(message, "name", "player");
            String text = readJsonString(message, "text", "");
            appendServerChat(seq, name, text);
            return;
        }
        if ("boss".equals(type)) {
            String serverMessage = readJsonString(message, "message", "босс обновлен сервером");
            statusMessage = serverMessage;
            chatMessages.add("server: " + serverMessage);
            trimChatMessages();
            return;
        }
        if ("eatResult".equals(type)) {
            int gain = Math.max(0, readJsonInt(message, "gain", 0));
            if (gain > 0) {
                int serverCurrency = readJsonInt(message, "currency", coins + gain);
                addLocalDnaAndHeal(gain);
                coins = Math.max(coins, serverCurrency);
                saveState();
            }
            return;
        }
        if ("pong".equals(type)) {
            if (pingSentAtMs > 0L) {
                displayedPingMs = (int) Math.max(0L, System.currentTimeMillis() - pingSentAtMs);
                pingSentAtMs = 0L;
            }
            return;
        }
        if ("kill".equals(type)) {
            saveState();
            return;
        }
        if ("death".equals(type)) {
            spawnDeathDisintegration(playerX, playerY, playerBaseColor());
            showDeathOverlay(readJsonString(message, "killerName", "игрок"),
                    readJsonString(message, "killerId", "killer"),
                    COLOR_GREEN);
            respawnPlayer();
            return;
        }
        if ("kick".equals(type)) {
            applyServerSeed(readJsonInt(message, "seed", worldSeed));
            setScreen(Screen.MAIN);
            statusMessage = "сервер обновил seed мира";
            chatMessages.add("server: новый seed мира, переподключение");
        }
    }

    private void applyServerSeed(int seed) {
        if (seed <= 0 || seed == worldSeed) {
            return;
        }
        worldSeed = seed;
        worldGenerated = false;
        resetBossBorder();
        boss.active = false;
        powerUps.clear();
        bossDnaOrbs.clear();
        serverDnaPickups.clear();
        serverBigDna = null;
        lastServerLeaderboardMs = 0L;
        nextBossSpawnMs = 0L;
        serverBossSpawnId = 0;
    }

    private void applyServerAccountState(String message) {
        accountId = readJsonString(message, "id", accountId);
        username = readJsonString(message, "name", username);
        int previousLevel = playerLevel;
        playerLevel = Math.max(1, readJsonInt(message, "level", playerLevel));
        playerXp = Math.max(0, readJsonInt(message, "xp", playerXp));
        totalDnaEarned = Math.max(0, readJsonInt(message, "dna", totalDnaEarned));
        coins = Math.max(0, readJsonInt(message, "currency", coins));
        proPassOwned = "pro".equals(readJsonString(message, "bkPass", proPassOwned ? "pro" : "free"));
        passSeason = readJsonInt(message, "passSeason", passSeason);
        proPassLevel = clampInt(readJsonInt(message, "proPassLevel", proPassLevel), 1, 50);
        ensurePassSeason();

        applyIntArray(readJsonString(message, "genes", ""), geneLevels, 0, GENE_MAX_LEVEL);
        applyBooleanArray(readJsonString(message, "skins", ""), skinOwned);
        if (skinOwned.length > 0) {
            skinOwned[0] = true;
        }
        applyBooleanArray(readJsonString(message, "freeRewards", ""), freePassRewardsClaimed);
        applyBooleanArray(readJsonString(message, "proRewards", ""), proPassRewardsClaimed);
        applyBooleanArray(readJsonString(message, "showcase", ""), skinShowcase);
        String serverInventory = readJsonString(message, "inventory", "");
        if (!serverInventory.trim().isEmpty()) {
            loadInventoryItems(serverInventory);
        } else {
            ensureInventorySeededFromOwnedSkins();
        }
        loadFriendIds(readJsonString(message, "friends", joinFriendIds()));
        loadFriendRequestIds(readJsonString(message, "friendRequests", joinFriendRequestIds()));

        selectedSkin = readJsonInt(message, "selectedSkin", selectedSkin);
        applyAvatar(readJsonString(message, "avatar", avatarData));
        if (selectedSkin < 0 || selectedSkin >= skinOwned.length || !skinOwned[selectedSkin]) {
            selectedSkin = 0;
        }
        while (playerXp >= xpForCurrentLevel()) {
            playerXp -= xpForCurrentLevel();
            playerLevel++;
        }
        if (playerLevel > previousLevel) {
            spawnLevelUpEffect();
        }
    }

    public void setAvatar(Uri uri) {
        try {
            Bitmap source = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(uri));
            if (source == null) return;
            int side = Math.min(256, Math.min(source.getWidth(), source.getHeight()));
            avatarBitmap = Bitmap.createBitmap(source, (source.getWidth() - side) / 2, (source.getHeight() - side) / 2, side, side);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 82, output);
            avatarData = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
            sendAccountAction("setAvatar", ",\"avatar\":\"" + jsonEscape(avatarData) + "\"");
            statusMessage = "аватар отправлен на сервер";
        } catch (Exception ignored) {
            statusMessage = "не удалось загрузить аватар";
        }
    }

    private void applyAvatar(String data) {
        avatarData = data == null ? "" : data;
        avatarBitmap = null;
        int comma = avatarData.indexOf(',');
        if (comma < 0) return;
        try {
            byte[] bytes = Base64.getDecoder().decode(avatarData.substring(comma + 1));
            avatarBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void updateServerBossState(String message) {
        long now = System.currentTimeMillis();
        boolean active = readJsonBoolean(message, "bossActive", false);
        int spawnId = readJsonInt(message, "bossSpawnId", serverBossSpawnId);
        if (active) {
            nextBossSpawnMs = 0L;
            int serverDna = Math.max(0, readJsonInt(message, "bossDna", BOSS_START_DNA));
            if (!boss.active || spawnId != serverBossSpawnId) {
                float x = readJsonFloat(message, "bossX", WORLD_SIZE * 0.5f);
                float y = readJsonFloat(message, "bossY", WORLD_SIZE * 0.5f);
                int dna = Math.max(BOSS_DEFEAT_DNA, serverDna);
                spawnBossFromServer(now, x, y, dna, spawnId);
            } else {
                boss.dna = Math.min(boss.dna, serverDna);
            }
            return;
        }

        long cooldownMs = Math.max(0L, readJsonInt(message, "bossCooldownMs", 0));
        nextBossSpawnMs = cooldownMs > 0L ? now + cooldownMs : 0L;
        serverBossSpawnId = spawnId;
        if (boss.active) {
            boss.active = false;
            bossDnaOrbs.clear();
            resetBossBorder();
        }
    }

    private void updateServerPlayers(String message) {
        int key = message.indexOf("\"players\"");
        if (key < 0) {
            return;
        }
        int arrayStart = message.indexOf('[', key);
        int arrayEnd = findMatchingArrayEnd(message, arrayStart);
        if (arrayStart < 0 || arrayEnd <= arrayStart) {
            return;
        }

        long now = System.currentTimeMillis();
        for (CellOpponent opponent : opponents) {
            if (opponent.serverControlled) {
                opponent.lastSeenMs = 0L;
            }
        }

        int cursor = arrayStart + 1;
        while (cursor < arrayEnd) {
            int objectStart = message.indexOf('{', cursor);
            if (objectStart < 0 || objectStart >= arrayEnd) {
                break;
            }
            int objectEnd = message.indexOf('}', objectStart);
            if (objectEnd < 0 || objectEnd > arrayEnd) {
                break;
            }
            String playerJson = message.substring(objectStart, objectEnd + 1);
            String id = readJsonString(playerJson, "id", "");
            String name = readJsonString(playerJson, "name", "player");
            boolean self = (!accountId.isEmpty() && accountId.equals(id)) || name.equalsIgnoreCase(username.trim());
            float hp = clamp(readJsonFloat(playerJson, "hp", 100f), 0f, 100f);
            if (self) {
                playerHp = hp;
                cursor = objectEnd + 1;
                continue;
            }

            CellOpponent opponent = findServerOpponent(id);
            if (opponent == null) {
                opponent = new CellOpponent();
                opponent.id = id;
                opponent.serverControlled = true;
                opponent.color = SKIN_COLORS[Math.abs(id.hashCode() % SKIN_COLORS.length)];
                opponents.add(opponent);
            }

            float x = readJsonFloat(playerJson, "x", opponent.x);
            float y = readJsonFloat(playerJson, "y", opponent.y);
            float dx = x - opponent.x;
            float dy = y - opponent.y;
            if (dx * dx + dy * dy > 0.5f) {
                opponent.angle = (float) Math.toDegrees(Math.atan2(dy, dx)) + 90f;
            }
            opponent.x = x;
            opponent.y = y;
            opponent.name = name;
            opponent.hp = hp;
            opponent.dna = Math.max(0, readJsonInt(playerJson, "dna", opponent.dna));
            int skin = clampInt(readJsonInt(playerJson, "skin", 0), 0, SKIN_COLORS.length - 1);
            opponent.color = SKIN_COLORS[skin];
            opponent.lastSeenMs = now;
            cursor = objectEnd + 1;
        }

        for (int i = opponents.size() - 1; i >= 0; i--) {
            CellOpponent opponent = opponents.get(i);
            if (opponent.serverControlled && opponent.lastSeenMs == 0L) {
                opponents.remove(i);
            }
        }
    }

    private CellOpponent findServerOpponent(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (CellOpponent opponent : opponents) {
            if (opponent.serverControlled && id.equals(opponent.id)) {
                return opponent;
            }
        }
        return null;
    }

    private void updateServerLeaderboard(String message) {
        int key = message.indexOf("\"leaderboard\"");
        boolean totalDnaRanking = key >= 0;
        if (!totalDnaRanking) {
            key = message.indexOf("\"players\"");
        }
        if (key < 0) {
            return;
        }
        int arrayStart = message.indexOf('[', key);
        int arrayEnd = findMatchingArrayEnd(message, arrayStart);
        if (arrayStart < 0 || arrayEnd <= arrayStart) {
            return;
        }

        serverLeaderboardEntries.clear();
        int cursor = arrayStart + 1;
        while (cursor < arrayEnd) {
            int objectStart = message.indexOf('{', cursor);
            if (objectStart < 0 || objectStart >= arrayEnd) {
                break;
            }
            int objectEnd = message.indexOf('}', objectStart);
            if (objectEnd < 0 || objectEnd > arrayEnd) {
                break;
            }
            String playerJson = message.substring(objectStart, objectEnd + 1);
            String id = readJsonString(playerJson, "id", "");
            String name = readJsonString(playerJson, "name", "player");
            int dna = Math.max(0, readJsonInt(playerJson, totalDnaRanking ? "totalDna" : "dna", 0));
            int level = Math.max(1, readJsonInt(playerJson, "level", 1));
            boolean self = (!accountId.isEmpty() && accountId.equals(id)) || name.equalsIgnoreCase(username.trim());
            upsertLeaderboardEntry(serverLeaderboardEntries, id, name, dna, level, self);
            cursor = objectEnd + 1;
        }
        sortLeaderboard(serverLeaderboardEntries);
        lastServerLeaderboardMs = System.currentTimeMillis();
    }

    private void updateMenuLeaderboard(String message) {
        int key = message.indexOf("\"menuLeaderboard\"");
        int arrayStart = key < 0 ? -1 : message.indexOf('[', key);
        int arrayEnd = findMatchingArrayEnd(message, arrayStart);
        if (arrayStart < 0 || arrayEnd <= arrayStart) return;
        menuLeaderboardEntries.clear();
        int cursor = arrayStart + 1;
        while (cursor < arrayEnd) {
            int objectStart = message.indexOf('{', cursor);
            if (objectStart < 0 || objectStart >= arrayEnd) break;
            int objectEnd = message.indexOf('}', objectStart);
            if (objectEnd < 0 || objectEnd > arrayEnd) break;
            String item = message.substring(objectStart, objectEnd + 1);
            String id = readJsonString(item, "id", "");
            String name = readJsonString(item, "name", "player");
            int level = Math.max(1, readJsonInt(item, "level", 1));
            boolean self = (!accountId.isEmpty() && accountId.equals(id)) || name.equalsIgnoreCase(username.trim());
            upsertLeaderboardEntry(menuLeaderboardEntries, id, name, 0, level, self);
            cursor = objectEnd + 1;
        }
    }

    private void updateServerChat(String message) {
        int serverSeq = readJsonInt(message, "chatSeq", -1);
        if (serverSeq >= 0 && serverSeq < lastServerChatSeq) {
            lastServerChatSeq = 0;
        }
        int key = message.indexOf("\"chat\"");
        int arrayStart = key < 0 ? -1 : message.indexOf('[', key);
        int arrayEnd = findMatchingArrayEnd(message, arrayStart);
        if (arrayStart < 0 || arrayEnd <= arrayStart) {
            return;
        }

        int cursor = arrayStart + 1;
        while (cursor < arrayEnd) {
            int objectStart = message.indexOf('{', cursor);
            if (objectStart < 0 || objectStart >= arrayEnd) {
                break;
            }
            int objectEnd = message.indexOf('}', objectStart);
            if (objectEnd < 0 || objectEnd > arrayEnd) {
                break;
            }
            String chatJson = message.substring(objectStart, objectEnd + 1);
            int seq = readJsonInt(chatJson, "seq", -1);
            String name = readJsonString(chatJson, "name", "player");
            String text = readJsonString(chatJson, "text", "");
            appendServerChat(seq, name, text);
            cursor = objectEnd + 1;
        }
    }

    private void appendServerChat(int seq, String name, String text) {
        String cleanText = text == null ? "" : text.trim();
        if (cleanText.isEmpty()) {
            return;
        }
        if (seq > 0) {
            if (seq <= lastServerChatSeq) {
                return;
            }
            lastServerChatSeq = seq;
        }
        String cleanName = name == null || name.trim().isEmpty() ? "player" : name.trim();
        chatMessages.add(cleanName + ": " + cleanText);
        trimChatMessages();
    }

    private void updateServerDnaPickups(String message) {
        int key = message.indexOf("\"dna\"");
        int arrayStart = key < 0 ? -1 : message.indexOf('[', key);
        int arrayEnd = findMatchingArrayEnd(message, arrayStart);
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            serverDnaPickups.clear();
            int cursor = arrayStart + 1;
            while (cursor < arrayEnd) {
                int objectStart = message.indexOf('{', cursor);
                if (objectStart < 0 || objectStart >= arrayEnd) {
                    break;
                }
                int objectEnd = message.indexOf('}', objectStart);
                if (objectEnd < 0 || objectEnd > arrayEnd) {
                    break;
                }
                ServerDnaPickup pickup = parseServerDnaPickup(message.substring(objectStart, objectEnd + 1));
                if (pickup != null && "big".equals(pickup.kind)) {
                    serverDnaPickups.add(pickup);
                }
                cursor = objectEnd + 1;
            }
        }

        int bigKey = message.indexOf("\"bigDna\"");
        if (bigKey < 0) {
            return;
        }
        int objectStart = message.indexOf('{', bigKey);
        if (objectStart < 0) {
            serverBigDna = null;
            return;
        }
        int objectEnd = message.indexOf('}', objectStart);
        serverBigDna = objectEnd > objectStart ? parseServerDnaPickup(message.substring(objectStart, objectEnd + 1)) : null;
    }

    private void requestMarketState(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastMarketRequestMs < 2500L) {
            return;
        }
        lastMarketRequestMs = now;
        sendServerMessage("{\"type\":\"marketList\"}");
    }

    private void applyMarketState(String message) {
        int listingsKey = message.indexOf("\"listings\"");
        int listingsStart = listingsKey < 0 ? -1 : message.indexOf('[', listingsKey);
        int listingsEnd = findMatchingArrayEnd(message, listingsStart);
        marketListings.clear();
        if (listingsStart >= 0 && listingsEnd > listingsStart) {
            int cursor = listingsStart + 1;
            while (cursor < listingsEnd) {
                int objectStart = message.indexOf('{', cursor);
                if (objectStart < 0 || objectStart >= listingsEnd) {
                    break;
                }
                int objectEnd = message.indexOf('}', objectStart);
                if (objectEnd < 0 || objectEnd > listingsEnd) {
                    break;
                }
                String listingJson = message.substring(objectStart, objectEnd + 1);
                int id = readJsonInt(listingJson, "id", -1);
                int skin = readJsonInt(listingJson, "skin", -1);
                int price = readJsonInt(listingJson, "price", 0);
                String ownerId = readJsonString(listingJson, "ownerId", "");
                String owner = readJsonString(listingJson, "owner", "player");
                if (id >= 0 && skin >= 0 && skin < skinNames.length && price >= 100) {
                    marketListings.add(new MarketListing(id, skin, price, ownerId, owner));
                }
                cursor = objectEnd + 1;
            }
        }

        int salesKey = message.indexOf("\"lastSales\"");
        int salesStart = salesKey < 0 ? -1 : message.indexOf('[', salesKey);
        int salesEnd = findMatchingArrayEnd(message, salesStart);
        if (salesStart >= 0 && salesEnd > salesStart) {
            int cursor = salesStart + 1;
            while (cursor < salesEnd) {
                int objectStart = message.indexOf('{', cursor);
                if (objectStart < 0 || objectStart >= salesEnd) {
                    break;
                }
                int objectEnd = message.indexOf('}', objectStart);
                if (objectEnd < 0 || objectEnd > salesEnd) {
                    break;
                }
                String saleJson = message.substring(objectStart, objectEnd + 1);
                int skin = readJsonInt(saleJson, "skin", -1);
                int price = readJsonInt(saleJson, "price", 0);
                if (skin >= 0 && skin < skinLastSalePrices.length && price >= 100) {
                    skinLastSalePrices[skin] = price;
                }
                cursor = objectEnd + 1;
            }
        }
    }

    private ServerDnaPickup parseServerDnaPickup(String json) {
        String id = readJsonString(json, "id", "");
        if (id.isEmpty()) {
            return null;
        }
        ServerDnaPickup pickup = new ServerDnaPickup();
        pickup.id = id;
        pickup.x = readJsonFloat(json, "x", 0f);
        pickup.y = readJsonFloat(json, "y", 0f);
        pickup.value = Math.max(1, readJsonInt(json, "value", 1));
        pickup.radius = Math.max(8f, readJsonFloat(json, "radius", 20f));
        pickup.zoneRadius = Math.max(pickup.radius, readJsonFloat(json, "zone", pickup.radius));
        pickup.kind = readJsonString(json, "kind", "normal");
        pickup.active = true;
        return pickup;
    }

    private void upsertLeaderboardEntry(List<LeaderboardEntry> entries, String id, String name, int dna, int level, boolean self) {
        String safeId = id == null ? "" : id;
        for (LeaderboardEntry entry : entries) {
            if (!safeId.isEmpty() && safeId.equals(entry.id)) {
                entry.name = name;
                entry.dna = dna;
                entry.level = level;
                entry.self = entry.self || self;
                return;
            }
        }
        entries.add(new LeaderboardEntry(safeId, name, dna, level, self));
    }

    private void rebuildLocalLeaderboard() {
        leaderboardEntries.clear();
        String selfId = accountId.isEmpty() ? "local" : accountId;
        upsertLeaderboardEntry(leaderboardEntries, selfId, displayName(), totalDnaEarned, playerLevel(), true);
        for (CellOpponent opponent : opponents) {
            upsertLeaderboardEntry(leaderboardEntries, opponent.name, opponent.name, opponent.dna, 1, false);
        }
        sortLeaderboard(leaderboardEntries);
    }

    private void sortLeaderboard(List<LeaderboardEntry> entries) {
        entries.sort((first, second) -> {
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
    }

    private List<LeaderboardEntry> currentLeaderboard() {
        if (screen != Screen.GAME && !menuLeaderboardEntries.isEmpty()) {
            return menuLeaderboardEntries;
        }
        if (!serverLeaderboardEntries.isEmpty() && System.currentTimeMillis() - lastServerLeaderboardMs < 3500L) {
            return serverLeaderboardEntries;
        }
        rebuildLocalLeaderboard();
        return leaderboardEntries;
    }

    private void syncServerPosition(long now) {
        if (now - lastServerMoveMs < 140L) {
            return;
        }
        lastServerMoveMs = now;
        connectGameServerIfNeeded();
        String message = String.format(Locale.US,
                "{\"type\":\"move\",\"x\":%.1f,\"y\":%.1f,\"hp\":%.1f,\"dna\":%d}",
                playerX,
                playerY,
                playerHp,
                Math.max(0, dnaEaten));
        sendServerMessage(message);
    }

    private void updatePing(long now) {
        if (pingSentAtMs > 0L && now - pingSentAtMs > 5_000L) {
            pingSentAtMs = 0L;
            displayedPingMs = -1;
        }
        if (now < nextPingAtMs || pingSentAtMs > 0L) {
            return;
        }
        nextPingAtMs = now + 2_000L;
        connectGameServerIfNeeded();
        if (sendServerMessage("{\"type\":\"ping\",\"clientTime\":" + now + "}")) {
            pingSentAtMs = now;
        } else {
            displayedPingMs = -1;
        }
    }

    private void updateGame() {
        long now = System.currentTimeMillis();
        updatePing(now);
        updateGameParticles(now);
        if (lastGameFrameMs == 0L) {
            lastGameFrameMs = now;
            return;
        }

        float delta = Math.min(0.05f, (now - lastGameFrameMs) / 1000f);
        lastGameFrameMs = now;
        playerBodyTargetWidth = PLAYER_WIDTH;
        playerBodyTargetHeight = PLAYER_HEIGHT;
        playerDentTargetWorldX = 0f;
        playerDentTargetWorldY = 0f;
        playerDentTargetStrength = 0f;
        updateBoss(delta, now);
        updateOpponents(delta, now);
        resolveOpponentContacts(now);
        resolvePlayerContacts(now);
        resolveBossContacts(now);
        consumePowerUps(now);
        consumeBossDnaOrbs(now);
        consumeServerDnaPickups(now);
        canPlayerOccupy(playerX, playerY);
        maintainLocalDnaPickups(now);
        if (playerTouchesBlockDeathTrigger()) {
            return;
        }

        float moveLength = (float) Math.sqrt(moveX * moveX + moveY * moveY);
        if (moveLength <= 0.02f) {
            consumeCurrentTile();
            consumeBossDnaOrbs(now);
            consumeServerDnaPickups(now);
            syncServerPosition(now);
            relaxPlayerBody(delta);
            return;
        }

        float speed = 292f + geneLevels[0] * 38f;
        if (speedBoostUntilMs > now) {
            speed *= 2f;
        }
        if (dnaBoostUntilMs > now) {
            speed /= 1.5f;
        }
        movePlayerWithSliding((moveX / moveLength) * speed * delta, (moveY / moveLength) * speed * delta);
        consumeCurrentTile();
        consumeBossDnaOrbs(now);
        consumeServerDnaPickups(now);
        syncServerPosition(now);
        relaxPlayerBody(delta);
    }

    private boolean playerTouchesBlockDeathTrigger() {
        float triggerSize = WORLD_TILE_SIZE * 0.5f;
        float triggerRadius = PLAYER_WIDTH * PLAYER_COLLISION_RADIUS;
        int minX = Math.max(0, (int) Math.floor((playerX - triggerRadius) / WORLD_TILE_SIZE));
        int minY = Math.max(0, (int) Math.floor((playerY - triggerRadius) / WORLD_TILE_SIZE));
        int maxX = Math.min(WORLD_TILES - 1, (int) Math.floor((playerX + triggerRadius) / WORLD_TILE_SIZE));
        int maxY = Math.min(WORLD_TILES - 1, (int) Math.floor((playerY + triggerRadius) / WORLD_TILE_SIZE));
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (!hasBlock(x, y)) {
                    continue;
                }
                float left = x * WORLD_TILE_SIZE + (WORLD_TILE_SIZE - triggerSize) * 0.5f;
                float top = y * WORLD_TILE_SIZE + (WORLD_TILE_SIZE - triggerSize) * 0.5f;
                if (circleIntersectsRect(playerX, playerY, triggerRadius, left, top,
                        left + triggerSize, top + triggerSize)) {
                    spawnDeathDisintegration(playerX, playerY, playerBaseColor());
                    respawnPlayer();
                    showDeathOverlay("опасный блок", "block", Color.rgb(120, 120, 120));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean circleIntersectsRect(float cx, float cy, float radius,
                                         float left, float top, float right, float bottom) {
        float nearestX = clamp(cx, left, right);
        float nearestY = clamp(cy, top, bottom);
        float dx = cx - nearestX;
        float dy = cy - nearestY;
        return dx * dx + dy * dy <= radius * radius;
    }

    private void movePlayerWithSliding(float dx, float dy) {
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / 14f));
        float stepX = dx / steps;
        float stepY = dy / steps;

        for (int i = 0; i < steps; i++) {
            float targetX = clamp(playerX + stepX, worldLeftLimit() + playerRadius, worldRightLimit() - playerRadius);
            float targetY = clamp(playerY + stepY, worldTopLimit() + playerRadius, worldBottomLimit() - playerRadius);
            if (canPlayerOccupy(targetX, targetY)) {
                playerX = targetX;
                playerY = targetY;
                continue;
            }

            registerPlayerDeformation(stepX, stepY, PLAYER_MIN_WIDTH, PLAYER_MIN_HEIGHT);
            boolean moved = false;
            if (canPlayerOccupy(targetX, playerY)) {
                playerX = targetX;
                moved = true;
            }
            if (canPlayerOccupy(playerX, targetY)) {
                playerY = targetY;
                moved = true;
            }
            if (!moved) {
                float slideX = -stepY * 0.55f;
                float slideY = stepX * 0.55f;
                if (tryMoveBy(slideX, slideY)) {
                    continue;
                }
                tryMoveBy(-slideX, -slideY);
            }
        }
    }

    private boolean tryMoveBy(float dx, float dy) {
        float targetX = clamp(playerX + dx, worldLeftLimit() + playerRadius, worldRightLimit() - playerRadius);
        float targetY = clamp(playerY + dy, worldTopLimit() + playerRadius, worldBottomLimit() - playerRadius);
        if (!canPlayerOccupy(targetX, targetY)) {
            registerPlayerDeformation(dx, dy, PLAYER_MIN_WIDTH, PLAYER_MIN_HEIGHT);
            return false;
        }
        playerX = targetX;
        playerY = targetY;
        return true;
    }

    private void registerPlayerDeformation(float worldX, float worldY, float targetWidth, float targetHeight) {
        float widthPressure = (PLAYER_WIDTH - clamp(targetWidth, PLAYER_MIN_WIDTH, PLAYER_WIDTH)) / Math.max(1f, PLAYER_WIDTH - PLAYER_MIN_WIDTH);
        float heightPressure = (PLAYER_HEIGHT - clamp(targetHeight, PLAYER_MIN_HEIGHT, PLAYER_HEIGHT)) / Math.max(1f, PLAYER_HEIGHT - PLAYER_MIN_HEIGHT);
        float length = (float) Math.sqrt(worldX * worldX + worldY * worldY);
        float impact = clamp(length / 22f, 0.22f, 0.92f);
        playerDentTargetStrength = Math.max(playerDentTargetStrength, 0.18f + Math.max(widthPressure, heightPressure) * 0.42f + impact * 0.26f);
        if (length > 0.001f) {
            playerDentTargetWorldX = worldX / length;
            playerDentTargetWorldY = worldY / length;
        }
    }

    private void relaxPlayerBody(float delta) {
        float t = Math.min(1f, delta * 11f);
        playerBodyWidth = PLAYER_WIDTH;
        playerBodyHeight = PLAYER_HEIGHT;
        playerBodyTargetWidth = PLAYER_WIDTH;
        playerBodyTargetHeight = PLAYER_HEIGHT;
        playerDentWorldX += (playerDentTargetWorldX - playerDentWorldX) * t;
        playerDentWorldY += (playerDentTargetWorldY - playerDentWorldY) * t;
        playerDentStrength += (playerDentTargetStrength - playerDentStrength) * t;
        playerMeshWave += delta * (playerDentStrength > 0.02f ? 13f : 4f);
        if (Math.abs(playerDentWorldX) < 0.01f) {
            playerDentWorldX = 0f;
        }
        if (Math.abs(playerDentWorldY) < 0.01f) {
            playerDentWorldY = 0f;
        }
        if (playerDentStrength < 0.01f) {
            playerDentStrength = 0f;
        }
        updatePlayerMeshTargets();
        relaxPlayerMesh(delta);
    }

    private void updatePlayerMeshTargets() {
        float dentLength = (float) Math.sqrt(playerDentWorldX * playerDentWorldX + playerDentWorldY * playerDentWorldY);
        float localDentX = 0f;
        float localDentY = 0f;
        if (dentLength > 0.001f && playerDentStrength > 0.001f) {
            float angle = (float) Math.toRadians(playerAngleDeg);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            localDentX = (playerDentWorldX * cos + playerDentWorldY * sin) / dentLength;
            localDentY = (-playerDentWorldX * sin + playerDentWorldY * cos) / dentLength;
        }

        float contactX = 0f;
        float contactY = 0f;
        float bestContactDot = Float.NEGATIVE_INFINITY;
        if (playerDentStrength > 0f) {
            for (int i = 0; i < PLAYER_MESH_SEGMENTS; i++) {
                float[] candidate = playerMeshBasePoint(i, PLAYER_WIDTH, PLAYER_HEIGHT);
                float dot = candidate[0] * localDentX + candidate[1] * localDentY;
                if (dot > bestContactDot) {
                    bestContactDot = dot;
                    contactX = candidate[0];
                    contactY = candidate[1];
                }
            }
        }

        for (int i = 0; i < PLAYER_MESH_SEGMENTS; i++) {
            float[] base = playerMeshBasePoint(i, PLAYER_WIDTH, PLAYER_HEIGHT);
            float normalLength = (float) Math.sqrt(base[0] * base[0] + base[1] * base[1]);
            float normalX = normalLength > 0f ? base[0] / normalLength : 0f;
            float normalY = normalLength > 0f ? base[1] / normalLength : 0f;
            float hit = normalX * localDentX + normalY * localDentY;
            float side = clamp((hit + 1f) * 0.5f, 0f, 1f);

            float targetX = 0f;
            float targetY = 0f;
            if (playerDentStrength > 0f) {
                float strength = clamp(playerDentStrength, 0f, 1.05f);
                float liquidPull = clamp(0.05f * strength + (float) Math.pow(side, 1.7f) * strength * 0.26f, 0f, 0.42f);
                float contactSink = (float) Math.pow(side, 2.2f) * strength * PLAYER_MESH_DENT_DEPTH * 0.10f;
                float wave = (float) Math.sin(playerMeshWave + i * 0.92f) * strength * (0.22f + side) * 1.45f;
                targetX += (contactX - base[0]) * liquidPull;
                targetY += (contactY - base[1]) * liquidPull;
                targetX -= localDentX * contactSink;
                targetY -= localDentY * contactSink;
                targetX += -normalY * wave;
                targetY += normalX * wave;
            }

            playerMeshTargetOffsetX[i] = targetX;
            playerMeshTargetOffsetY[i] = targetY;
        }
    }

    private void relaxPlayerMesh(float delta) {
        float follow = Math.min(1f, delta * 9f);
        for (int i = 0; i < PLAYER_MESH_SEGMENTS; i++) {
            playerMeshOffsetX[i] += (playerMeshTargetOffsetX[i] - playerMeshOffsetX[i]) * follow;
            playerMeshOffsetY[i] += (playerMeshTargetOffsetY[i] - playerMeshOffsetY[i]) * follow;
            if (Math.abs(playerMeshOffsetX[i]) < 0.02f) {
                playerMeshOffsetX[i] = 0f;
            }
            if (Math.abs(playerMeshOffsetY[i]) < 0.02f) {
                playerMeshOffsetY[i] = 0f;
            }
        }
    }

    private void updateBoss(float delta, long now) {
        if (!boss.active) {
            trimBossDnaOrbs();
            return;
        }

        float targetX = playerX;
        float targetY = playerY;
        float bestDistanceSq = distanceSq(boss.x, boss.y, playerX, playerY) - dnaEaten * 40f;
        for (CellOpponent opponent : opponents) {
            float score = distanceSq(boss.x, boss.y, opponent.x, opponent.y) - opponent.dna * 36f;
            if (score < bestDistanceSq) {
                bestDistanceSq = score;
                targetX = opponent.x;
                targetY = opponent.y;
            }
        }

        float dx = targetX - boss.x;
        float dy = targetY - boss.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0.001f) {
            boss.moveX = dx / length;
            boss.moveY = dy / length;
            boss.angle = (float) Math.toDegrees(Math.atan2(boss.moveY, boss.moveX)) + 90f;
            moveBoss(boss.moveX * BOSS_SPEED * delta, boss.moveY * BOSS_SPEED * delta, now);
        }
        trimBossDnaOrbs();
        if (boss.dna < BOSS_DEFEAT_DNA) {
            defeatBoss(now);
        }
    }

    private void spawnBoss(long now) {
        float[] point = randomOpenPoint(5);
        spawnBossAt(now, point[0], point[1], BOSS_START_DNA, serverBossSpawnId);
    }

    private void spawnBossFromServer(long now, float x, float y, int dna, int spawnId) {
        serverBossSpawnId = spawnId;
        spawnBossAt(now, x, y, dna, spawnId);
    }

    private void spawnBossAt(long now, float x, float y, int dna, int spawnId) {
        boss.active = true;
        boss.x = clamp(x, worldLeftLimit() + BOSS_CONTACT_RADIUS, worldRightLimit() - BOSS_CONTACT_RADIUS);
        boss.y = clamp(y, worldTopLimit() + BOSS_CONTACT_RADIUS, worldBottomLimit() - BOSS_CONTACT_RADIUS);
        boss.dna = Math.max(BOSS_DEFEAT_DNA, dna);
        boss.angle = 0f;
        boss.moveX = 0f;
        boss.moveY = 0f;
        boss.nextBlockPushMs = now + 800L;
        boss.nextBorderMoveMs = now + 5_000L;
        boss.hitCooldownMs = 0L;
        spawnBossSpawnEffect(boss.x, boss.y, now);
        statusMessage = "БОСС появился: " + boss.dna + " ДНК";
        chatMessages.add("server: БОСС появился на карте");
        trimChatMessages();
    }

    private void moveBoss(float dx, float dy, long now) {
        boss.x = clamp(boss.x + dx, worldLeftLimit() + BOSS_CONTACT_RADIUS, worldRightLimit() - BOSS_CONTACT_RADIUS);
        boss.y = clamp(boss.y + dy, worldTopLimit() + BOSS_CONTACT_RADIUS, worldBottomLimit() - BOSS_CONTACT_RADIUS);
        if (now >= boss.nextBlockPushMs) {
            pushBlocksByBoss(dx, dy);
            boss.nextBlockPushMs = now + 620L;
        }
    }

    private void pushBlocksByBoss(float dx, float dy) {
        int dirX = 0;
        int dirY = 0;
        if (Math.abs(dx) >= Math.abs(dy)) {
            dirX = dx >= 0f ? 1 : -1;
        } else {
            dirY = dy >= 0f ? 1 : -1;
        }
        if (dirX == 0 && dirY == 0) {
            return;
        }

        int frontX = (int) ((boss.x + dirX * BOSS_CONTACT_RADIUS) / WORLD_TILE_SIZE);
        int frontY = (int) ((boss.y + dirY * BOSS_CONTACT_RADIUS) / WORLD_TILE_SIZE);
        for (int side = -2; side <= 2; side++) {
            int tileX = frontX + (dirY != 0 ? side : 0);
            int tileY = frontY + (dirX != 0 ? side : 0);
            int nextX = tileX + dirX;
            int nextY = tileY + dirY;
            if (!canBossMoveBlock(tileX, tileY, nextX, nextY)) {
                continue;
            }
            worldTiles[nextY][nextX] = worldTiles[tileY][tileX];
            worldTiles[tileY][tileX] = TILE_EMPTY;
            return;
        }
    }

    private boolean canBossMoveBlock(int tileX, int tileY, int nextX, int nextY) {
        if (!isTileInsideDynamicBounds(tileX, tileY, 0) || !isTileInsideDynamicBounds(nextX, nextY, 0)) {
            return false;
        }
        if (isWorldBorder(tileX, tileY) || isWorldBorder(nextX, nextY)) {
            return false;
        }
        return hasBlock(tileX, tileY) && worldTiles[nextY][nextX] == TILE_EMPTY;
    }

    private void updateBossBorder(long now) {
        if (now < boss.nextBorderMoveMs) {
            return;
        }
        int hash = neighborHash((int) (boss.x / WORLD_TILE_SIZE), (int) (boss.y / WORLD_TILE_SIZE) + (int) (now / 1000L));
        int side;
        float left = boss.x - worldLeftLimit();
        float right = worldRightLimit() - boss.x;
        float top = boss.y - worldTopLimit();
        float bottom = worldBottomLimit() - boss.y;
        float nearest = Math.min(Math.min(left, right), Math.min(top, bottom));
        if (nearest == left) {
            side = 0;
        } else if (nearest == right) {
            side = 1;
        } else if (nearest == top) {
            side = 2;
        } else {
            side = 3;
        }
        if ((hash & 7) == 0) {
            side = hash & 3;
        }
        int amount = (hash & 8) == 0 ? 1 : -1;
        if (side == 0) {
            worldInsetLeftTiles = clampInt(worldInsetLeftTiles + amount, 0, MAX_BOSS_BORDER_INSET_TILES);
        } else if (side == 1) {
            worldInsetRightTiles = clampInt(worldInsetRightTiles + amount, 0, MAX_BOSS_BORDER_INSET_TILES);
        } else if (side == 2) {
            worldInsetTopTiles = clampInt(worldInsetTopTiles + amount, 0, MAX_BOSS_BORDER_INSET_TILES);
        } else {
            worldInsetBottomTiles = clampInt(worldInsetBottomTiles + amount, 0, MAX_BOSS_BORDER_INSET_TILES);
        }
        constrainEntitiesToWorld();
        boss.nextBorderMoveMs = now + 4_800L;
    }

    private void resolveBossContacts(long now) {
        if (!boss.active) {
            return;
        }

        float playerDistanceSq = distanceSq(playerX, playerY, boss.x, boss.y);
        float playerContact = BOSS_CONTACT_RADIUS + PLAYER_HEIGHT * 0.36f;
        if (playerDistanceSq <= playerContact * playerContact) {
            float dx = playerX - boss.x;
            float dy = playerY - boss.y;
            registerPlayerDeformation(-dx, -dy, PLAYER_MIN_WIDTH, PLAYER_MIN_HEIGHT);
            pushPlayerFromBoss(dx, dy, playerContact);
            if (now >= playerHitCooldownMs) {
                playerHitCooldownMs = now + 650L;
                float incoming = 22f + boss.dna * 0.0015f;
                if (geneLevels[GENE_SPIKES] > 0) {
                    int reflected = Math.max(8, Math.round(incoming * 0.35f * geneLevels[GENE_SPIKES] + playerDamage() * 2.2f));
                    damageBoss(reflected, playerX, playerY);
                }
                playerHp -= incoming;
                if (playerHp <= 0f) {
                    killPlayerByBoss();
                }
            }
        }

        for (CellOpponent opponent : opponents) {
            if (opponent.serverControlled) {
                continue;
            }
            float distanceSq = distanceSq(opponent.x, opponent.y, boss.x, boss.y);
            float contact = BOSS_CONTACT_RADIUS + PLAYER_HEIGHT * 0.42f;
            if (distanceSq > contact * contact) {
                continue;
            }
            boss.dna += opponent.dna;
            spawnKillBurst(opponent.x, opponent.y, opponent.color);
            respawnOpponent(opponent);
        }
        if (boss.dna < BOSS_DEFEAT_DNA) {
            defeatBoss(now);
        }
    }

    private void pushPlayerFromBoss(float dx, float dy, float contactDistance) {
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.001f) {
            dx = 1f;
            dy = 0f;
            distance = 1f;
        }
        float overlap = Math.max(0f, contactDistance - distance);
        tryMoveBy(dx / distance * Math.min(34f, overlap * 0.52f), dy / distance * Math.min(34f, overlap * 0.52f));
    }

    private void damageBoss(int amount, float hitX, float hitY) {
        if (!boss.active || amount <= 0) {
            return;
        }
        int damage = calculateBossDamageFromDna(dnaEaten);
        if (damage <= 0) {
            return;
        }
        boss.dna = Math.max(0, boss.dna - damage);
        sendServerMessage("{\"type\":\"bossDamage\",\"spawnId\":" + serverBossSpawnId
                + ",\"dna\":" + Math.max(0, dnaEaten)
                + ",\"amount\":" + damage + "}");
        dropBossDnaOrbs(damage, hitX, hitY);
    }

    private static int calculateBossDamageFromDna(int dna) {
        if (dna <= 0) {
            return 0;
        }
        return Math.max(1, Math.round(dna * BOSS_DAMAGE_DNA_HALF * (1f - BOSS_DAMAGE_REDUCTION)));
    }

    private void dropBossDnaOrbs(int damage, float hitX, float hitY) {
        int count = clampInt(1 + damage / 45, 1, 5);
        int total = clampInt(35 + damage * 3, 35, 420);
        for (int i = 0; i < count; i++) {
            float angle = (neighborHash((int) hitX + i * 17, (int) hitY - i * 23) % 628) / 100f;
            float distance = 90f + i * 27f;
            BossDnaOrb orb = new BossDnaOrb();
            orb.x = clamp(hitX + (float) Math.cos(angle) * distance, worldLeftLimit() + 55f, worldRightLimit() - 55f);
            orb.y = clamp(hitY + (float) Math.sin(angle) * distance, worldTopLimit() + 55f, worldBottomLimit() - 55f);
            orb.amount = Math.max(15, total / count);
            orb.active = true;
            bossDnaOrbs.add(orb);
        }
    }

    private void consumeBossDnaOrbs(long now) {
        for (BossDnaOrb orb : bossDnaOrbs) {
            if (!orb.active) {
                continue;
            }
            float dx = playerX - orb.x;
            float dy = playerY - orb.y;
            if (dx * dx + dy * dy > 72f * 72f) {
                continue;
            }
            orb.active = false;
            int gain = dnaBoostUntilMs > now ? orb.amount * 2 : orb.amount;
            gain = applyWeekendDnaMultiplier(gain);
            spawnDnaEatEffect(orb.x, orb.y, gain, true);
            addEatenDnaAndCurrency(gain);
            addExperience(Math.max(1, gain));
            saveState();
        }
    }

    private void consumeServerDnaPickups(long now) {
        ServerDnaPickup big = serverBigDna;
        if (big != null && big.active) {
            float dx = playerX - big.x;
            float dy = playerY - big.y;
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq <= big.zoneRadius * big.zoneRadius && now - lastBigDnaTickMs >= 1000L) {
                lastBigDnaTickMs = now;
                sendServerMessage("{\"type\":\"bigDnaTick\"}");
            }
            if (distanceSq <= SERVER_BIG_DNA_EAT_RADIUS * SERVER_BIG_DNA_EAT_RADIUS) {
                spawnDnaEatEffect(big.x, big.y, big.value, true);
                big.active = false;
                sendServerMessage("{\"type\":\"eat\",\"pickupId\":\"" + jsonEscape(big.id) + "\"}");
            }
        }

        for (ServerDnaPickup pickup : serverDnaPickups) {
            if (!pickup.active) {
                continue;
            }
            float dx = playerX - pickup.x;
            float dy = playerY - pickup.y;
            if ("big".equals(pickup.kind)) {
                float distanceSq = dx * dx + dy * dy;
                if (distanceSq <= pickup.zoneRadius * pickup.zoneRadius && now - lastBigDnaTickMs >= 1000L) {
                    lastBigDnaTickMs = now;
                    sendServerMessage("{\"type\":\"bigDnaTick\",\"pickupId\":\"" + jsonEscape(pickup.id) + "\"}");
                }
                if (distanceSq <= SERVER_BIG_DNA_EAT_RADIUS * SERVER_BIG_DNA_EAT_RADIUS) {
                    spawnDnaEatEffect(pickup.x, pickup.y, pickup.value, true);
                    pickup.active = false;
                    sendServerMessage("{\"type\":\"eat\",\"pickupId\":\"" + jsonEscape(pickup.id) + "\"}");
                }
                continue;
            }
            float eatRadius = pickup.radius + PLAYER_HEIGHT * 0.28f;
            if (dx * dx + dy * dy > eatRadius * eatRadius) {
                continue;
            }
            spawnDnaEatEffect(pickup.x, pickup.y, pickup.value, false);
            pickup.active = false;
        }
    }

    private void trimBossDnaOrbs() {
        for (int i = bossDnaOrbs.size() - 1; i >= 0; i--) {
            if (!bossDnaOrbs.get(i).active) {
                bossDnaOrbs.remove(i);
            }
        }
        while (bossDnaOrbs.size() > 80) {
            bossDnaOrbs.remove(0);
        }
    }

    private void defeatBoss(long now) {
        int reward = 900;
        boss.active = false;
        resetBossBorder();
        addDnaAndCurrency(reward);
        addExperience(reward);
        nextBossSpawnMs = now + BOSS_SPAWN_INTERVAL_MS;
        sendServerMessage("{\"type\":\"bossDefeated\",\"spawnId\":" + serverBossSpawnId + ",\"dna\":" + boss.dna + "}");
        statusMessage = "БОСС убит: +" + reward + " ДНК";
        chatMessages.add(displayName() + " убил босса");
        saveState();
    }

    private void updateOpponents(float delta, long now) {
        for (CellOpponent opponent : opponents) {
            if (opponent.serverControlled) {
                continue;
            }
            updateOpponentBrain(opponent, now);
            float length = (float) Math.sqrt(opponent.moveX * opponent.moveX + opponent.moveY * opponent.moveY);
            if (length <= 0.01f) {
                continue;
            }
            opponent.angle = (float) Math.toDegrees(Math.atan2(opponent.moveY, opponent.moveX)) + 90f;
            float speed = opponentSpeed(opponent, now);
            moveOpponentWithSliding(opponent, opponent.moveX / length * speed * delta, opponent.moveY / length * speed * delta);
            consumeOpponentPickups(opponent, now);
        }
    }

    private void updateOpponentBrain(CellOpponent opponent, long now) {
        float ownStrength = opponentStrength(opponent, now);
        float threatX = 0f;
        float threatY = 0f;
        boolean hasThreat = false;

        float playerDx = playerX - opponent.x;
        float playerDy = playerY - opponent.y;
        float playerDistanceSq = playerDx * playerDx + playerDy * playerDy;
        if (playerDistanceSq < 900f * 900f && playerStrength(now) > ownStrength + 12f) {
            float weight = 1f + (900f * 900f - playerDistanceSq) / (900f * 900f);
            threatX += playerDx * weight;
            threatY += playerDy * weight;
            hasThreat = true;
        }
        if (boss.active) {
            float bossDx = boss.x - opponent.x;
            float bossDy = boss.y - opponent.y;
            float bossDistanceSq = bossDx * bossDx + bossDy * bossDy;
            if (bossDistanceSq < 1500f * 1500f) {
                float weight = 2.8f + (1500f * 1500f - bossDistanceSq) / (1500f * 1500f);
                threatX += bossDx * weight;
                threatY += bossDy * weight;
                hasThreat = true;
            }
        }

        for (CellOpponent other : opponents) {
            if (other == opponent) {
                continue;
            }
            float dx = other.x - opponent.x;
            float dy = other.y - opponent.y;
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq < 780f * 780f && opponentStrength(other, now) > ownStrength + 10f) {
                float weight = 1f + (780f * 780f - distanceSq) / (780f * 780f);
                threatX += dx * weight;
                threatY += dy * weight;
                hasThreat = true;
            }
        }

        if (hasThreat) {
            steerOpponent(opponent, -threatX, -threatY);
            opponent.nextTurnMs = now + 240L;
            return;
        }

        if (now < opponent.nextTurnMs && Math.abs(opponent.moveX) + Math.abs(opponent.moveY) > 0.05f) {
            return;
        }

        boolean boosted = opponent.speedBoostUntilMs > now || opponent.dnaBoostUntilMs > now;
        float preyX = 0f;
        float preyY = 0f;
        float preyScore = Float.NEGATIVE_INFINITY;
        float attackMargin = boosted ? -8f : 8f - opponent.geneMass * 1.3f - opponent.geneSpikes * 1.8f;

        if (playerDistanceSq < 1050f * 1050f && ownStrength > playerStrength(now) + attackMargin) {
            preyX = playerX;
            preyY = playerY;
            preyScore = 900000f - playerDistanceSq;
        }

        for (CellOpponent other : opponents) {
            if (other == opponent) {
                continue;
            }
            float dx = other.x - opponent.x;
            float dy = other.y - opponent.y;
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq < 980f * 980f && ownStrength > opponentStrength(other, now) + attackMargin) {
                float score = 780000f - distanceSq + (opponent.geneSpikes + opponent.geneMass) * 2800f;
                if (score > preyScore) {
                    preyScore = score;
                    preyX = other.x;
                    preyY = other.y;
                }
            }
        }

        PowerUpPickup boost = nearestPowerUp(opponent.x, opponent.y, 1120f);
        if (boosted && preyScore > Float.NEGATIVE_INFINITY) {
            steerOpponentToward(opponent, preyX, preyY);
            opponent.nextTurnMs = now + 300L;
            return;
        }
        if (boost != null) {
            steerOpponentToward(opponent, boost.x, boost.y);
            opponent.nextTurnMs = now + 420L;
            return;
        }
        if (preyScore > Float.NEGATIVE_INFINITY) {
            steerOpponentToward(opponent, preyX, preyY);
            opponent.nextTurnMs = now + 380L;
            return;
        }

        float[] dnaPoint = nearestDnaPoint(opponent.x, opponent.y, 12);
        if (dnaPoint != null) {
            steerOpponentToward(opponent, dnaPoint[0], dnaPoint[1]);
            opponent.nextTurnMs = now + 500L;
            return;
        }

        float angle = (neighborHash((int) opponent.x, (int) opponent.y + (int) (now / 250L)) % 628) / 100f;
        opponent.moveX = (float) Math.cos(angle);
        opponent.moveY = (float) Math.sin(angle);
        opponent.nextTurnMs = now + 760L + Math.abs(neighborHash((int) opponent.y, (int) opponent.x)) % 900L;
    }

    private void steerOpponentToward(CellOpponent opponent, float targetX, float targetY) {
        steerOpponent(opponent, targetX - opponent.x, targetY - opponent.y);
    }

    private void steerOpponent(CellOpponent opponent, float dx, float dy) {
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.001f) {
            opponent.moveX = 0f;
            opponent.moveY = 0f;
            return;
        }
        float desiredAngle = (float) Math.atan2(dy / length, dx / length);
        float[] turnOffsets = {0f, 0.55f, -0.55f, 1.05f, -1.05f, 1.57f, -1.57f, 3.14f};
        for (float offset : turnOffsets) {
            float candidateAngle = desiredAngle + offset;
            float candidateX = opponent.x + (float) Math.cos(candidateAngle) * 150f;
            float candidateY = opponent.y + (float) Math.sin(candidateAngle) * 150f;
            float bodyAngle = (float) Math.toDegrees(candidateAngle) + 90f;
            if (!capsuleCanOccupy(candidateX, candidateY, bodyAngle, PLAYER_WIDTH, PLAYER_HEIGHT)) {
                continue;
            }
            opponent.moveX = (float) Math.cos(candidateAngle);
            opponent.moveY = (float) Math.sin(candidateAngle);
            return;
        }
        opponent.moveX = dx / length;
        opponent.moveY = dy / length;
    }

    private float opponentSpeed(CellOpponent opponent, long now) {
        float speed = 150f + opponent.geneSpeed * 24f + Math.min(95f, opponent.dna * 1.05f);
        if (opponent.speedBoostUntilMs > now) {
            speed *= 2f;
        }
        if (opponent.dnaBoostUntilMs > now) {
            speed /= 1.5f;
        }
        return speed;
    }

    private float playerStrength(long now) {
        float strength = dnaEaten + playerHp * 0.16f + geneLevels[1] * 5.5f + geneLevels[GENE_SPIKES] * 7f;
        if (speedBoostUntilMs > now) {
            strength += 16f;
        }
        if (dnaBoostUntilMs > now) {
            strength += 8f;
        }
        return strength;
    }

    private float opponentStrength(CellOpponent opponent, long now) {
        float strength = opponent.dna + opponent.hp * 0.16f + opponent.geneMass * 5.5f + opponent.geneSpikes * 7f;
        if (opponent.speedBoostUntilMs > now) {
            strength += 14f;
        }
        if (opponent.dnaBoostUntilMs > now) {
            strength += 7f;
        }
        return strength;
    }

    private PowerUpPickup nearestPowerUp(float x, float y, float maxDistance) {
        float bestDistanceSq = maxDistance * maxDistance;
        PowerUpPickup best = null;
        for (PowerUpPickup powerUp : powerUps) {
            if (!powerUp.active) {
                continue;
            }
            float dx = powerUp.x - x;
            float dy = powerUp.y - y;
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                best = powerUp;
            }
        }
        return best;
    }

    private float[] nearestDnaPoint(float x, float y, int tileRadius) {
        int centerX = (int) (x / WORLD_TILE_SIZE);
        int centerY = (int) (y / WORLD_TILE_SIZE);
        float bestDistanceSq = Float.MAX_VALUE;
        float[] best = null;
        for (int ty = Math.max(1, centerY - tileRadius); ty <= Math.min(WORLD_TILES - 2, centerY + tileRadius); ty++) {
            for (int tx = Math.max(1, centerX - tileRadius); tx <= Math.min(WORLD_TILES - 2, centerX + tileRadius); tx++) {
                if (worldTiles[ty][tx] != TILE_DNA) {
                    continue;
                }
                float targetX = (tx + 0.5f) * WORLD_TILE_SIZE;
                float targetY = (ty + 0.5f) * WORLD_TILE_SIZE;
                float dx = targetX - x;
                float dy = targetY - y;
                float distanceSq = dx * dx + dy * dy;
                if (distanceSq < bestDistanceSq) {
                    bestDistanceSq = distanceSq;
                    if (best == null) {
                        best = new float[2];
                    }
                    best[0] = targetX;
                    best[1] = targetY;
                }
            }
        }
        return best;
    }

    private void consumeOpponentPickups(CellOpponent opponent, long now) {
        int tileX = (int) (opponent.x / WORLD_TILE_SIZE);
        int tileY = (int) (opponent.y / WORLD_TILE_SIZE);
        if (tileX > 0 && tileY > 0 && tileX < WORLD_TILES - 1 && tileY < WORLD_TILES - 1 && worldTiles[tileY][tileX] == TILE_DNA) {
            worldTiles[tileY][tileX] = TILE_EMPTY;
            localDnaCount = Math.max(0, localDnaCount - 1);
            spawnLocalDnaPickup();
            opponent.dna += opponent.dnaBoostUntilMs > now ? 2 : 1;
            opponent.hp = Math.min(100f, opponent.hp + 0.6f);
        }

        for (PowerUpPickup powerUp : powerUps) {
            if (!powerUp.active) {
                continue;
            }
            float dx = opponent.x - powerUp.x;
            float dy = opponent.y - powerUp.y;
            if (dx * dx + dy * dy > 64f * 64f) {
                continue;
            }

            powerUp.active = false;
            if (powerUp.type == POWER_SPEED) {
                opponent.speedBoostUntilMs = now + POWER_DURATION_MS;
            } else if (powerUp.type == POWER_DNA) {
                opponent.dnaBoostUntilMs = now + POWER_DURATION_MS;
            } else {
                float[] point = randomOpenPoint(3);
                opponent.x = point[0];
                opponent.y = point[1];
            }
        }
    }

    private void moveOpponentWithSliding(CellOpponent opponent, float dx, float dy) {
        float margin = PLAYER_HEIGHT * 0.5f;
        float targetX = clamp(opponent.x + dx, worldLeftLimit() + margin, worldRightLimit() - margin);
        float targetY = clamp(opponent.y + dy, worldTopLimit() + margin, worldBottomLimit() - margin);
        if (capsuleCanOccupy(targetX, targetY, opponent.angle, PLAYER_WIDTH, PLAYER_HEIGHT)) {
            opponent.x = targetX;
            opponent.y = targetY;
            return;
        }
        if (capsuleCanOccupy(targetX, opponent.y, opponent.angle, PLAYER_WIDTH, PLAYER_HEIGHT)) {
            opponent.x = targetX;
        } else if (capsuleCanOccupy(opponent.x, targetY, opponent.angle, PLAYER_WIDTH, PLAYER_HEIGHT)) {
            opponent.y = targetY;
        } else {
            steerOpponent(opponent, dx, dy);
        }
    }

    private void resolveOpponentContacts(long now) {
        float minDistance = PLAYER_HEIGHT * 0.56f;
        float minDistanceSq = minDistance * minDistance;
        for (int i = 0; i < opponents.size(); i++) {
            CellOpponent first = opponents.get(i);
            if (first.serverControlled) {
                continue;
            }
            for (int j = i + 1; j < opponents.size(); j++) {
                CellOpponent second = opponents.get(j);
                if (second.serverControlled) {
                    continue;
                }
                float dx = first.x - second.x;
                float dy = first.y - second.y;
                float distanceSq = dx * dx + dy * dy;
                if (distanceSq > minDistanceSq) {
                    continue;
                }

                separateOpponents(first, second, dx, dy, minDistance);
                if (now < first.hitCooldownMs || now < second.hitCooldownMs) {
                    continue;
                }

                first.hitCooldownMs = now + 850L;
                second.hitCooldownMs = now + 850L;
                float firstStrength = opponentStrength(first, now);
                float secondStrength = opponentStrength(second, now);
                float damage = 1.5f + Math.min(7f, Math.abs(first.dna - second.dna) * 0.07f);
                if (firstStrength >= secondStrength) {
                    float dealt = damage + opponentDamage(first) * 0.35f;
                    second.hp -= dealt;
                    first.hp -= dealt * 0.10f * second.geneSpikes;
                } else {
                    float dealt = damage + opponentDamage(second) * 0.35f;
                    first.hp -= dealt;
                    second.hp -= dealt * 0.10f * first.geneSpikes;
                }

                if (first.hp <= 0f) {
                    int defeatedDna = first.dna;
                    second.dna += Math.max(1, Math.round(defeatedDna * 0.70f));
                    spawnKillBurst(first.x, first.y, first.color);
                    respawnOpponent(first);
                }
                if (second.hp <= 0f) {
                    int defeatedDna = second.dna;
                    first.dna += Math.max(1, Math.round(defeatedDna * 0.70f));
                    spawnKillBurst(second.x, second.y, second.color);
                    respawnOpponent(second);
                }
            }
        }
    }

    private void separateOpponents(CellOpponent first, CellOpponent second, float dx, float dy, float minDistance) {
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.001f) {
            dx = 1f;
            dy = 0f;
            distance = 1f;
        }
        float push = Math.min(16f, Math.max(0f, minDistance - distance) * 0.36f);
        if (push <= 0f) {
            return;
        }
        float pushX = dx / distance * push;
        float pushY = dy / distance * push;
        moveOpponentWithSliding(first, pushX, pushY);
        moveOpponentWithSliding(second, -pushX, -pushY);
    }

    private void resolvePlayerContacts(long now) {
        for (CellOpponent opponent : opponents) {
            float dx = playerX - opponent.x;
            float dy = playerY - opponent.y;
            float minDistance = PLAYER_HEIGHT * 0.58f;
            if (dx * dx + dy * dy > minDistance * minDistance) {
                continue;
            }

            registerPlayerDeformation(-dx, -dy, PLAYER_MIN_WIDTH, PLAYER_MIN_HEIGHT);
            if (opponent.serverControlled) {
                pushPlayerFromServerOpponent(dx, dy, minDistance);
                if (now >= opponent.hitCooldownMs) {
                    opponent.hitCooldownMs = now + 550L;
                    sendServerHit(opponent);
                }
                continue;
            }

            separatePlayerFromOpponent(opponent, dx, dy, minDistance);
            if (now < opponent.hitCooldownMs) {
                continue;
            }

            opponent.hitCooldownMs = now + 800L;
            float damage = 1.5f + Math.min(7f, Math.abs(dnaEaten - opponent.dna) * 0.08f);
            if (dnaEaten >= opponent.dna) {
                opponent.hp -= damage + playerDamage() * 0.35f;
            } else if (now >= playerHitCooldownMs) {
                playerHitCooldownMs = now + 800L;
                float incoming = damage + opponentDamage(opponent) * 0.35f;
                playerHp -= incoming;
                if (geneLevels.length > GENE_SPIKES && geneLevels[GENE_SPIKES] > 0) {
                    opponent.hp -= playerDamage() + incoming * 0.10f * geneLevels[GENE_SPIKES];
                }
                if (playerHp <= 0f) {
                    killPlayerByOpponent(opponent);
                }
            }

            if (opponent.hp <= 0f) {
                int reward = Math.max(1, Math.round(opponent.dna * 0.70f));
                spawnKillBurst(opponent.x, opponent.y, opponent.color);
                addDnaAndCurrency(reward);
                addExperience(Math.max(1, reward / 2));
                respawnOpponent(opponent);
                saveState();
            }
        }
    }

    private void pushPlayerFromServerOpponent(float dx, float dy, float minDistance) {
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.001f) {
            dx = 1f;
            dy = 0f;
            distance = 1f;
        }
        float overlap = Math.max(0f, minDistance - distance);
        if (overlap <= 0f) {
            return;
        }
        tryMoveBy(dx / distance * Math.min(18f, overlap * 0.38f),
                dy / distance * Math.min(18f, overlap * 0.38f));
    }

    private void sendServerHit(CellOpponent opponent) {
        if (opponent.id == null || opponent.id.isEmpty()) {
            return;
        }
        sendServerMessage("{\"type\":\"hit\",\"targetId\":\"" + jsonEscape(opponent.id) + "\"}");
    }

    private void separatePlayerFromOpponent(CellOpponent opponent, float dx, float dy, float minDistance) {
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.001f) {
            dx = 1f;
            dy = 0f;
            distance = 1f;
        }
        float overlap = Math.max(0f, minDistance - distance);
        if (overlap <= 0f) {
            return;
        }

        float pushX = dx / distance * Math.min(22f, overlap * 0.45f);
        float pushY = dy / distance * Math.min(22f, overlap * 0.45f);
        tryMoveBy(pushX, pushY);
        moveOpponentWithSliding(opponent, -pushX * 0.55f, -pushY * 0.55f);
    }

    private float playerDamage() {
        return 2f + dnaEaten * 0.04f + geneLevels[1] * 0.75f;
    }

    private float opponentDamage(CellOpponent opponent) {
        return 2f + opponent.dna * 0.04f + opponent.geneMass * 0.75f;
    }

    private void killPlayerByOpponent(CellOpponent opponent) {
        opponent.dna += Math.max(0, Math.round(dnaEaten * 0.70f));
        String killer = opponent.name + " · " + opponent.dna + " ДНК";
        int color = opponent.color;
        spawnDeathDisintegration(playerX, playerY, playerBaseColor());
        respawnPlayer();
        showDeathOverlay(killer, opponent.name, color);
    }

    private void killPlayerByBoss() {
        boss.dna += Math.max(0, dnaEaten);
        spawnDeathDisintegration(playerX, playerY, playerBaseColor());
        respawnPlayer();
        showDeathOverlay("БОСС · " + boss.dna + " ДНК", "boss", Color.rgb(255, 95, 95));
    }

    private void showDeathOverlay(String killerName, String killerProfileId, int killerColor) {
        deathKillerName = killerName;
        deathKillerProfileId = killerProfileId;
        deathKillerColor = killerColor;
        deathOverlayActive = true;
        moveX = 0f;
        moveY = 0f;
        leftPointerId = -1;
        rightPointerId = -1;
    }

    private void respawnPlayer() {
        playerHp = 100f;
        dnaEaten = 0;
        placePlayerAtOpenPoint();
        statusMessage = "респавн";
        saveState();
    }

    private void respawnOpponent(CellOpponent opponent) {
        float[] point = randomOpenPoint(3);
        opponent.x = point[0];
        opponent.y = point[1];
        opponent.hp = 100f;
        opponent.dna = 8 + Math.abs(neighborHash((int) point[0], (int) point[1])) % 75;
        opponent.speedBoostUntilMs = 0L;
        opponent.dnaBoostUntilMs = 0L;
        opponent.nextTurnMs = 0L;
    }

    private void consumePowerUps(long now) {
        for (PowerUpPickup powerUp : powerUps) {
            if (!powerUp.active) {
                continue;
            }
            float dx = playerX - powerUp.x;
            float dy = playerY - powerUp.y;
            if (dx * dx + dy * dy > 60f * 60f) {
                continue;
            }

            powerUp.active = false;
            if (powerUp.type == POWER_SPEED) {
                speedBoostUntilMs = now + POWER_DURATION_MS;
                statusMessage = "скорость x2";
            } else if (powerUp.type == POWER_DNA) {
                dnaBoostUntilMs = now + POWER_DURATION_MS;
                statusMessage = "ДНК x2, скорость ниже";
            } else {
                teleportPlayer();
                statusMessage = "телепорт";
            }
        }
    }

    private void ensureWorldGenerated() {
        if (worldGenerated) {
            return;
        }
        generateWorld();
        seedOpponents();
        seedPowerUps();
        worldGenerated = true;
    }

    private void generateWorld() {
        worldRandom.setSeed(worldSeed);
        resetBossBorder();
        boss.active = false;
        bossDnaOrbs.clear();
        for (int y = 0; y < WORLD_TILES; y++) {
            for (int x = 0; x < WORLD_TILES; x++) {
                worldTiles[y][x] = TILE_EMPTY;
            }
        }

        for (int x = 0; x < WORLD_TILES; x++) {
            worldTiles[0][x] = TILE_WALL;
            worldTiles[WORLD_TILES - 1][x] = TILE_WALL;
        }
        for (int y = 0; y < WORLD_TILES; y++) {
            worldTiles[y][0] = TILE_WALL;
            worldTiles[y][WORLD_TILES - 1] = TILE_WALL;
        }

        for (int y = 2; y < WORLD_TILES - 6; y += 4) {
            for (int x = 2; x < WORLD_TILES - 6; x += 4) {
                float centerX = x + 2f;
                float centerY = y + 2f;
                double distance = islandDistance(centerX, centerY);
                double islandMask = Math.max(0.0, 1.0 - Math.pow(distance, 1.55));
                double islandNoise = fractalPerlin(centerX * 0.035 + 18.0, centerY * 0.035 - 23.0);
                double detail = fractalPerlin(centerX * 0.092 + 17.0, centerY * 0.092 - 9.0);
                double gapNoise = fractalPerlin(centerX * 0.018 - 41.0, centerY * 0.018 + 12.0);
                double score = islandMask * 0.42 + islandNoise * 0.54 + detail * 0.18 + gapNoise * 0.12;
                if (score > 0.70 && islandNoise > 0.49) {
                    int hash = neighborHash(x / 4, y / 4);
                    int[][] block = GENERATION_BLOCKS[Math.floorMod(hash, GENERATION_BLOCKS.length)];
                    int tileType = score > 0.96 ? TILE_DENSE : TILE_SOFT;
                    if (score > 1.08 && hash % 7 == 0) {
                        tileType = TILE_WALL;
                    }
                    applyGenerationBlock(x, y, block, tileType, hash & 3);
                }
            }
        }

        int center = WORLD_TILES / 2;
        int low = Math.max(4, WORLD_TILES / 8);
        int high = WORLD_TILES - low - 1;
        carvePath(center, center, low, center - 3, 0);
        carvePath(center, center, center - 3, low, 1);
        carvePath(center, center, high, center + 3, 2);
        carvePath(center, center, center + 3, high, 3);

        for (int y = center - 4; y <= center + 4; y++) {
            for (int x = center - 4; x <= center + 4; x++) {
                worldTiles[y][x] = TILE_EMPTY;
            }
        }

        seedLocalDnaPickups();
    }

    private void seedLocalDnaPickups() {
        localDnaCount = 0;
        for (int i = 0; i < LOCAL_DNA_TARGET; i++) {
            spawnLocalDnaPickup();
        }
    }

    private boolean spawnLocalDnaPickup() {
        int center = WORLD_TILES / 2;
        for (int tries = 0; tries < 900; tries++) {
            int x = 2 + worldRandom.nextInt(WORLD_TILES - 4);
            int y = 2 + worldRandom.nextInt(WORLD_TILES - 4);
            boolean awayFromSpawn = Math.abs(x - center) > 5 || Math.abs(y - center) > 5;
            boolean onIsland = islandDistance(x, y) < 0.95;
            if (awayFromSpawn && onIsland && canSpawnDnaAt(x, y)) {
                worldTiles[y][x] = TILE_DNA;
                localDnaCount++;
                return true;
            }
        }
        return false;
    }

    private void exitGameToMainMenu() {
        deathOverlayActive = false;
        setScreen(Screen.MAIN);
        leftPointerId = -1;
        rightPointerId = -1;
        moveX = 0f;
        moveY = 0f;
        aimX = 0f;
        aimY = 0f;
        focusField = FocusField.NONE;
        hideKeyboard();
        invalidate();
    }

    private void maintainLocalDnaPickups(long now) {
        if (now < nextLocalDnaReplenishMs || localDnaCount >= LOCAL_DNA_TARGET) {
            return;
        }
        nextLocalDnaReplenishMs = now + 1000L;
        int missing = Math.min(24, LOCAL_DNA_TARGET - localDnaCount);
        for (int i = 0; i < missing; i++) {
            if (!spawnLocalDnaPickup()) {
                break;
            }
        }
    }

    private boolean canSpawnDnaAt(int x, int y) {
        if (!isTileInsideDynamicBounds(x, y, 3) || worldTiles[y][x] != TILE_EMPTY) {
            return false;
        }
        int radius = 3;
        int radiusSq = radius * radius;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy > radiusSq) {
                    continue;
                }
                int tx = x + dx;
                int ty = y + dy;
                if (tx < 0 || ty < 0 || tx >= WORLD_TILES || ty >= WORLD_TILES) {
                    return false;
                }
                if (isWorldBlock(worldTiles[ty][tx])) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isWorldBlock(int tile) {
        return tile == TILE_SOFT || tile == TILE_DENSE || tile == TILE_WALL;
    }

    private void seedOpponents() {
        opponents.clear();
        for (int i = 0; i < 9; i++) {
            float[] point = randomOpenPoint(3);
            CellOpponent opponent = new CellOpponent();
            opponent.name = "cell-" + (i + 1);
            opponent.x = point[0];
            opponent.y = point[1];
            opponent.hp = 100f;
            opponent.dna = 8 + Math.abs(neighborHash(i * 31, i * 47)) % 80;
            opponent.color = i % 3 == 0 ? Color.rgb(255, 96, 54) : (i % 3 == 1 ? Color.rgb(78, 221, 255) : Color.rgb(255, 210, 77));
            opponent.geneSpeed = Math.floorMod(i * 2 + 1, 5);
            opponent.geneMass = Math.floorMod(i * 3 + 2, 5);
            opponent.geneSpikes = Math.floorMod(i * 4 + 1, 4);
            opponent.nextTurnMs = System.currentTimeMillis() + i * 190L;
            opponents.add(opponent);
        }
    }

    private void seedPowerUps() {
        powerUps.clear();
        for (int i = 0; i < 42; i++) {
            float[] point = randomOpenPoint(2);
            powerUps.add(new PowerUpPickup(point[0], point[1], i % 3));
        }
    }

    private float[] randomOpenPoint(int clearanceBlocks) {
        for (int tries = 0; tries < 800; tries++) {
            int minX = Math.max(2, worldInsetLeftTiles + 2 + clearanceBlocks);
            int maxX = Math.min(WORLD_TILES - 3, WORLD_TILES - worldInsetRightTiles - 3 - clearanceBlocks);
            int minY = Math.max(2, worldInsetTopTiles + 2 + clearanceBlocks);
            int maxY = Math.min(WORLD_TILES - 3, WORLD_TILES - worldInsetBottomTiles - 3 - clearanceBlocks);
            if (maxX <= minX || maxY <= minY) {
                break;
            }
            int x = minX + worldRandom.nextInt(maxX - minX + 1);
            int y = minY + worldRandom.nextInt(maxY - minY + 1);
            if (isOpenAround(x, y, clearanceBlocks)) {
                return new float[]{(x + 0.5f) * WORLD_TILE_SIZE, (y + 0.5f) * WORLD_TILE_SIZE};
            }
        }
        return new float[]{WORLD_SIZE * 0.5f, WORLD_SIZE * 0.5f};
    }

    private boolean isOpenAround(int tileX, int tileY, int clearanceBlocks) {
        for (int y = tileY - clearanceBlocks; y <= tileY + clearanceBlocks; y++) {
            for (int x = tileX - clearanceBlocks; x <= tileX + clearanceBlocks; x++) {
                if (x <= 0 || y <= 0 || x >= WORLD_TILES - 1 || y >= WORLD_TILES - 1 || !isTileInsideDynamicBounds(x, y, 0) || hasBlock(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void teleportPlayer() {
        float[] point = randomOpenPoint(3);
        playerX = point[0];
        playerY = point[1];
    }

    private void placePlayerAtOpenPoint() {
        float[] point = randomOpenPoint(3);
        playerX = point[0];
        playerY = point[1];
    }

    private void applyGenerationBlock(int originX, int originY, int[][] block, int tileType, int rotation) {
        for (int by = 0; by < block.length; by++) {
            for (int bx = 0; bx < block[by].length; bx++) {
                int value = block[by][bx];
                if (value == 0) {
                    continue;
                }
                int rx = bx;
                int ry = by;
                if (rotation == 1) {
                    rx = block.length - 1 - by;
                    ry = bx;
                } else if (rotation == 2) {
                    rx = block[by].length - 1 - bx;
                    ry = block.length - 1 - by;
                } else if (rotation == 3) {
                    rx = by;
                    ry = block[by].length - 1 - bx;
                }
                int wx = originX + rx;
                int wy = originY + ry;
                if (wx <= 0 || wy <= 0 || wx >= WORLD_TILES - 1 || wy >= WORLD_TILES - 1) {
                    continue;
                }
                worldTiles[wy][wx] = value == 3 ? TILE_WALL : (value == 2 ? TILE_DENSE : tileType);
            }
        }
    }

    private void carvePath(int startX, int startY, int endX, int endY, int salt) {
        int x = startX;
        int y = startY;
        int guard = 0;
        while ((x != endX || y != endY) && guard++ < 240) {
            clearAround(x, y, 1);
            int hash = neighborHash(x + salt * 13, y - salt * 17);
            boolean stepX = Math.abs(endX - x) >= Math.abs(endY - y);
            if (hash % 5 == 0) {
                stepX = !stepX;
            }
            if (stepX && x != endX) {
                x += endX > x ? 1 : -1;
            } else if (y != endY) {
                y += endY > y ? 1 : -1;
            }
        }
    }

    private void clearAround(int x, int y, int radius) {
        for (int yy = y - radius; yy <= y + radius; yy++) {
            for (int xx = x - radius; xx <= x + radius; xx++) {
                if (xx > 0 && yy > 0 && xx < WORLD_TILES - 1 && yy < WORLD_TILES - 1) {
                    worldTiles[yy][xx] = TILE_EMPTY;
                }
            }
        }
    }

    private double islandDistance(float x, float y) {
        double nx = (x / (WORLD_TILES - 1f)) * 2.0 - 1.0;
        double ny = (y / (WORLD_TILES - 1f)) * 2.0 - 1.0;
        double stretch = 1.0 + fractalPerlin(x * 0.025 + 2.0, y * 0.025 - 4.0) * 0.22;
        return Math.sqrt(nx * nx + ny * ny * 1.08) / stretch;
    }

    private double fractalPerlin(double x, double y) {
        double value = 0.0;
        double amplitude = 1.0;
        double frequency = 1.0;
        double total = 0.0;
        for (int octave = 0; octave < 4; octave++) {
            value += perlin(x * frequency, y * frequency) * amplitude;
            total += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }
        return value / total * 0.5 + 0.5;
    }

    private double perlin(double x, double y) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        double sx = fade(x - x0);
        double sy = fade(y - y0);

        double n0 = gradientDot(x0, y0, x - x0, y - y0);
        double n1 = gradientDot(x1, y0, x - x1, y - y0);
        double ix0 = lerp(n0, n1, sx);
        n0 = gradientDot(x0, y1, x - x0, y - y1);
        n1 = gradientDot(x1, y1, x - x1, y - y1);
        double ix1 = lerp(n0, n1, sx);
        return lerp(ix0, ix1, sy);
    }

    private double gradientDot(int gridX, int gridY, double x, double y) {
        switch (neighborHash(gridX, gridY) & 7) {
            case 0:
                return x + y;
            case 1:
                return -x + y;
            case 2:
                return x - y;
            case 3:
                return -x - y;
            case 4:
                return x;
            case 5:
                return -x;
            case 6:
                return y;
            default:
                return -y;
        }
    }

    private static int fastFloor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private int neighborHash(int x, int y) {
        int n = x * 734_287 + y * 912_271 + worldSeed;
        n = (n << 13) ^ n;
        return Math.abs(n * (n * n * 15_731 + 789_221) + 1_376_312_589);
    }

    private int tileColor(int tile) {
        if (tile == TILE_WALL) {
            return COLOR_BLOCK;
        }
        if (tile == TILE_DENSE) {
            return COLOR_BLOCK;
        }
        if (tile == TILE_SOFT) {
            return COLOR_BLOCK;
        }
        if (tile == TILE_DNA) {
            return COLOR_BLOCK;
        }
        return Color.rgb(26, 26, 26);
    }

    private boolean isWorldBorder(int x, int y) {
        return x == 0 || y == 0 || x == WORLD_TILES - 1 || y == WORLD_TILES - 1;
    }

    private boolean hasBlock(int x, int y) {
        if (x < 0 || y < 0 || x >= WORLD_TILES || y >= WORLD_TILES) {
            return false;
        }
        return worldTiles[y][x] == TILE_SOFT || worldTiles[y][x] == TILE_DENSE || worldTiles[y][x] == TILE_WALL;
    }

    private boolean isTileInsideDynamicBounds(int x, int y, int clearance) {
        return x > worldInsetLeftTiles + clearance
                && y > worldInsetTopTiles + clearance
                && x < WORLD_TILES - worldInsetRightTiles - 1 - clearance
                && y < WORLD_TILES - worldInsetBottomTiles - 1 - clearance;
    }

    private float worldLeftLimit() {
        return worldInsetLeftTiles * WORLD_TILE_SIZE;
    }

    private float worldTopLimit() {
        return worldInsetTopTiles * WORLD_TILE_SIZE;
    }

    private float worldRightLimit() {
        return WORLD_SIZE - worldInsetRightTiles * WORLD_TILE_SIZE;
    }

    private float worldBottomLimit() {
        return WORLD_SIZE - worldInsetBottomTiles * WORLD_TILE_SIZE;
    }

    private void resetBossBorder() {
        worldInsetLeftTiles = 0;
        worldInsetTopTiles = 0;
        worldInsetRightTiles = 0;
        worldInsetBottomTiles = 0;
    }

    private void constrainEntitiesToWorld() {
        playerX = clamp(playerX, worldLeftLimit() + playerRadius, worldRightLimit() - playerRadius);
        playerY = clamp(playerY, worldTopLimit() + playerRadius, worldBottomLimit() - playerRadius);
        float opponentMargin = PLAYER_HEIGHT * 0.5f;
        for (CellOpponent opponent : opponents) {
            opponent.x = clamp(opponent.x, worldLeftLimit() + opponentMargin, worldRightLimit() - opponentMargin);
            opponent.y = clamp(opponent.y, worldTopLimit() + opponentMargin, worldBottomLimit() - opponentMargin);
        }
        if (boss.active) {
            boss.x = clamp(boss.x, worldLeftLimit() + BOSS_CONTACT_RADIUS, worldRightLimit() - BOSS_CONTACT_RADIUS);
            boss.y = clamp(boss.y, worldTopLimit() + BOSS_CONTACT_RADIUS, worldBottomLimit() - BOSS_CONTACT_RADIUS);
        }
    }

    private boolean canPlayerOccupy(float worldX, float worldY) {
        float[] fit = findPlayerBodyFit(worldX, worldY, playerAngleDeg);
        if (fit == null) {
            registerPlayerDeformation(moveX, moveY, PLAYER_MIN_WIDTH, PLAYER_MIN_HEIGHT);
            return false;
        }
        if (fit[0] < PLAYER_WIDTH || fit[1] < PLAYER_HEIGHT) {
            registerPlayerDeformation(moveX, moveY, fit[0], fit[1]);
        }
        return true;
    }

    private float[] findPlayerBodyFit(float worldX, float worldY, float angleDeg) {
        for (float[] fit : PLAYER_BODY_FITS) {
            if (capsuleCanOccupy(worldX, worldY, angleDeg, fit[0], fit[1])) {
                return fit;
            }
        }
        return null;
    }

    private boolean capsuleCanOccupy(float worldX, float worldY, float angleDeg, float width, float height) {
        float angle = (float) Math.toRadians(angleDeg);
        float axisX = (float) Math.sin(angle);
        float axisY = -(float) Math.cos(angle);
        float radius = width * PLAYER_COLLISION_RADIUS;
        float halfLine = Math.max(0f, height * 0.5f - radius);
        float step = Math.max(12f, radius * 0.75f);

        for (float offset = -halfLine; offset <= halfLine + 0.01f; offset += step) {
            if (circleIntersectsSolid(worldX + axisX * offset, worldY + axisY * offset, radius)) {
                return false;
            }
        }

        if (circleIntersectsSolid(worldX + axisX * halfLine, worldY + axisY * halfLine, radius)) {
            return false;
        }
        return !circleIntersectsSolid(worldX - axisX * halfLine, worldY - axisY * halfLine, radius);
    }

    private boolean circleCanOccupy(float worldX, float worldY, float radius) {
        return !circleIntersectsSolid(worldX, worldY, radius);
    }

    private boolean circleIntersectsSolid(float cx, float cy, float radius) {
        if (cx - radius < worldLeftLimit() || cy - radius < worldTopLimit() || cx + radius > worldRightLimit() || cy + radius > worldBottomLimit()) {
            return true;
        }

        int minX = Math.max(0, (int) Math.floor((cx - radius) / WORLD_TILE_SIZE));
        int minY = Math.max(0, (int) Math.floor((cy - radius) / WORLD_TILE_SIZE));
        int maxX = Math.min(WORLD_TILES - 1, (int) Math.floor((cx + radius) / WORLD_TILE_SIZE));
        int maxY = Math.min(WORLD_TILES - 1, (int) Math.floor((cy + radius) / WORLD_TILE_SIZE));
        float radiusSq = radius * radius;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (!hasBlock(x, y)) {
                    continue;
                }
                float left = x * WORLD_TILE_SIZE;
                float top = y * WORLD_TILE_SIZE;
                float right = left + WORLD_TILE_SIZE;
                float bottom = top + WORLD_TILE_SIZE;
                float closestX = clamp(cx, left, right);
                float closestY = clamp(cy, top, bottom);
                float dx = cx - closestX;
                float dy = cy - closestY;
                if (dx * dx + dy * dy <= radiusSq) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addDnaAndCurrency(int amount) {
        int gain = Math.max(0, amount);
        if (gain == 0) {
            return;
        }
        dnaEaten += gain;
        totalDnaEarned += gain;
    }

    private int applyWeekendDnaMultiplier(int amount) {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY ? amount * 2 : amount;
    }

    private void addEatenDnaAndCurrency(int amount) {
        addLocalDnaAndHeal(amount);
    }

    private void addLocalDnaAndHeal(int amount) {
        int gain = Math.max(0, amount);
        if (gain == 0) {
            return;
        }
        addDnaAndCurrency(gain);
        healFromDna(gain);
    }

    private void healFromDna(int amount) {
        if (playerHp <= 0f) {
            return;
        }
        playerHp = clamp(playerHp + Math.max(0, amount) * DNA_HEAL_PER_POINT, 0f, 100f);
    }

    private void consumeCurrentTile() {
        int x = (int) (playerX / WORLD_TILE_SIZE);
        int y = (int) (playerY / WORLD_TILE_SIZE);
        if (x < 0 || y < 0 || x >= WORLD_TILES || y >= WORLD_TILES) {
            return;
        }
        if (worldTiles[y][x] == TILE_DNA) {
            worldTiles[y][x] = TILE_EMPTY;
            localDnaCount = Math.max(0, localDnaCount - 1);
            spawnDnaEatEffect((x + 0.5f) * WORLD_TILE_SIZE, (y + 0.5f) * WORLD_TILE_SIZE, 1, false);
            spawnLocalDnaPickup();
            int dnaGain = dnaBoostUntilMs > System.currentTimeMillis() ? 2 : 1;
            dnaGain = applyWeekendDnaMultiplier(dnaGain);
            addEatenDnaAndCurrency(dnaGain);
            addExperience(dnaGain);
            saveState();
        }
    }

    private void sendChatMessage() {
        String message = chatDraft.trim();
        if (message.isEmpty()) {
            return;
        }
        if (handleChatCommand(message)) {
            chatDraft = "";
            focusField = FocusField.NONE;
            hideKeyboard();
            trimChatMessages();
            invalidate();
            return;
        }
        if (!sendServerMessage("{\"type\":\"chat\",\"text\":\"" + jsonEscape(message) + "\"}")) {
            chatMessages.add(displayName() + ": " + message);
            trimChatMessages();
        }
        chatDraft = "";
        focusField = FocusField.NONE;
        hideKeyboard();
        invalidate();
    }

    private boolean handleChatCommand(String message) {
        String command = message.toLowerCase(Locale.ROOT).replace("ё", "е").trim();
        if (!command.equals("/boss")
                && !command.equals("/boss spawn")
                && !command.equals("/spawnboss")
                && !command.equals("/босс")
                && !command.equals("/призвать босса")
                && !command.equals("/призватьбосса")) {
            return false;
        }

        connectGameServerIfNeeded();
        if (sendServerMessage("{\"type\":\"bossCommand\",\"command\":\"spawn\"}")) {
            chatMessages.add("server: команда призыва босса отправлена");
            statusMessage = "команда босса отправлена";
        } else {
            chatMessages.add("server: команда босса доступна только на сервере");
            statusMessage = "сервер недоступен";
        }
        return true;
    }

    private void trimChatMessages() {
        while (chatMessages.size() > 24) {
            chatMessages.remove(0);
        }
    }

    private void handleRewardsTouch(float x, float y) {
        if (rewardClaimRect.contains(x, y)) {
            if (dailyClaimed) {
                statusMessage = "награда уже забрана";
            } else {
                pendingDailyClaim = true;
                connectGameServerIfNeeded();
                if (sendServerMessage("{\"type\":\"daily\"}")) {
                    pendingDailyClaim = false;
                    statusMessage = "ежедневка проверяется на сервере";
                } else {
                    statusMessage = "жду ответ сервера по ежедневке";
                }
            }
            saveState();
            invalidate();
        }
    }

    private boolean handleProfileScrollTouch(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            profileDownX = x;
            profileDownY = y;
            profileStartMedalsScrollX = medalsScrollX;
            profileStartFriendsScrollY = friendsScrollY;
            profileDragged = false;
            if (medalsViewportRect.contains(x, y)) {
                profileDragMode = 1;
                return true;
            }
            if (friendsViewportRect.contains(x, y)) {
                profileDragMode = 2;
                return true;
            }
            profileDragMode = 0;
            return false;
        }
        if (profileDragMode == 0) {
            return false;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            float dx = x - profileDownX;
            float dy = y - profileDownY;
            if (Math.abs(dx) > 6f || Math.abs(dy) > 6f) {
                profileDragged = true;
            }
            if (profileDragMode == 1) {
                float scale = Math.min(safeContentWidth() / 1600f, safeContentHeight() / 900f);
                medalsScrollX = clamp(profileStartMedalsScrollX - dx, 0f, maxMedalsScroll(scale));
            } else if (profileDragMode == 2) {
                float scale = Math.min(safeContentWidth() / 1600f, safeContentHeight() / 900f);
                friendsScrollY = clamp(profileStartFriendsScrollY - dy, 0f, maxFriendsScroll(scale));
            }
            invalidate();
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            boolean consumed = profileDragged || action == MotionEvent.ACTION_CANCEL;
            profileDragMode = 0;
            profileDragged = false;
            return consumed;
        }
        return profileDragMode != 0;
    }

    private void handleProfileTouch(float x, float y) {
        if (viewingExternalProfile) {
            if (externalProfileCloseRect.contains(x, y)) {
                viewingExternalProfile = false;
                statusMessage = "";
                invalidate();
                return;
            }
            if (externalProfileMessageRect.contains(x, y)) {
                statusMessage = "сообщение игроку: " + externalProfileName;
                invalidate();
                return;
            }
            if (friendsTabRect.contains(x, y)) {
                friendPanelTab = 0;
                friendsScrollY = 0f;
                invalidate();
                return;
            }
            if (friendRequestsTabRect.contains(x, y)) {
                friendPanelTab = 1;
                friendsScrollY = 0f;
                invalidate();
                return;
            }
        }
        for (int i = 0; i < geneUpgradeRects.size(); i++) {
            if (!viewingExternalProfile && geneUpgradeRects.get(i).contains(x, y)) {
                int cost = geneCost(i);
                if (geneLevels[i] >= GENE_MAX_LEVEL) {
                    statusMessage = geneNames[i] + " уже максимум";
                } else if (sendAccountAction("upgradeGene", ",\"gene\":" + i)) {
                    statusMessage = geneNames[i] + " отправлен на сервер";
                    invalidate();
                    return;
                } else {
                    statusMessage = "нет соединения с сервером";
                }
                saveState();
                invalidate();
                return;
            }
        }

        if (friendsTabRect.contains(x, y)) {
            friendPanelTab = 0;
            friendsScrollY = 0f;
            focusField = FocusField.NONE;
            hideKeyboard();
            invalidate();
            return;
        }
        if (friendRequestsTabRect.contains(x, y)) {
            friendPanelTab = 1;
            friendsScrollY = 0f;
            focusField = FocusField.NONE;
            hideKeyboard();
            invalidate();
            return;
        }

        if (friendInputRect.contains(x, y)) {
            focusField = FocusField.FRIEND;
            showKeyboard();
            invalidate();
            return;
        }
        if (friendAddRect.contains(x, y)) {
            submitFriendDraft();
            return;
        }
        for (int i = 0; i < friendRequestAcceptRects.size(); i++) {
            if (friendRequestAcceptRects.get(i).contains(x, y)) {
                int requestIndex = friendRequestIndices.get(i);
                if (requestIndex >= 0 && requestIndex < friendRequestIds.size()) {
                    String id = friendRequestIds.get(requestIndex);
                    sendAccountAction("friendAccept", ",\"target\":\"" + jsonEscape(id) + "\"");
                    statusMessage = "принятие заявки отправлено";
                }
                invalidate();
                return;
            }
        }
        for (int i = 0; i < friendRequestDeclineRects.size(); i++) {
            if (friendRequestDeclineRects.get(i).contains(x, y)) {
                int requestIndex = friendRequestIndices.get(i);
                if (requestIndex >= 0 && requestIndex < friendRequestIds.size()) {
                    String id = friendRequestIds.get(requestIndex);
                    sendAccountAction("friendDecline", ",\"target\":\"" + jsonEscape(id) + "\"");
                    statusMessage = "отклонение заявки отправлено";
                }
                invalidate();
                return;
            }
        }
        for (int i = 0; i < friendProfileRects.size(); i++) {
            if (friendProfileRects.get(i).contains(x, y)) {
                int friendIndex = i < friendProfileIndices.size() ? friendProfileIndices.get(i) : i;
                String friendId = friendIds.get(friendIndex);
                viewingExternalProfile = true;
                externalProfileId = friendId;
                externalProfileName = "player-" + friendId;
                externalProfileColor = SKIN_COLORS[Math.abs(friendId.hashCode()) % SKIN_COLORS.length];
                statusMessage = "профиль друга: " + friendId;
                invalidate();
                return;
            }
        }

        for (int i = 0; i < skinRects.size(); i++) {
            if (skinRects.get(i).contains(x, y)) {
                if (!skinsUnlocked()) {
                    statusMessage = "магазин откроется на максимуме генов";
                } else if (skinOwned[i]) {
                    sendAccountAction("equipSkin", ",\"skin\":" + i);
                    statusMessage = "экипировка отправлена на сервер";
                    invalidate();
                    return;
                } else if (sendAccountAction("buySkin", ",\"skin\":" + i)) {
                    statusMessage = "покупка скина отправлена на сервер";
                    invalidate();
                    return;
                } else {
                    statusMessage = "нет соединения с сервером";
                }
                saveState();
                invalidate();
                return;
            }
        }
    }

    private boolean handleShopScrollTouch(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            shopDownY = y;
            shopStartScrollY = shopScrollY;
            shopDragged = false;
            shopDragActive = selectedInventorySkin < 0 && selectedMarketSkin < 0 && shopViewportRect.contains(x, y);
            return shopDragActive;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (!shopDragActive) {
                return false;
            }
            float dy = y - shopDownY;
            if (Math.abs(dy) > 6f) {
                shopDragged = true;
                float scale = Math.min(safeContentWidth() / 1600f, safeContentHeight() / 900f);
                shopScrollY = clamp(shopStartScrollY - dy, 0f, maxShopScroll(scale));
                invalidate();
                return true;
            }
            return false;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            boolean consumed = shopDragged || action == MotionEvent.ACTION_CANCEL;
            shopDragActive = false;
            shopDragged = false;
            return consumed;
        }
        return false;
    }

    private boolean handleEditorInventoryScrollTouch(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            editorInventoryDownY = y;
            editorInventoryStartScrollY = editorInventoryScrollY;
            editorInventoryDragged = false;
            editorInventoryDragActive = selectedInventorySkin < 0 && !caseOpeningActive && editorInventoryViewportRect.contains(x, y);
            return editorInventoryDragActive;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (!editorInventoryDragActive) {
                return false;
            }
            float dy = y - editorInventoryDownY;
            if (Math.abs(dy) > 6f) {
                editorInventoryDragged = true;
                float scale = Math.min(safeContentWidth() / 1600f, safeContentHeight() / 900f);
                editorInventoryScrollY = clamp(editorInventoryStartScrollY - dy, 0f, maxEditorInventoryScroll(scale));
                invalidate();
                return true;
            }
            return false;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            boolean consumed = editorInventoryDragged || action == MotionEvent.ACTION_CANCEL;
            editorInventoryDragActive = false;
            editorInventoryDragged = false;
            return consumed;
        }
        return false;
    }

    private boolean handleSkinActionModalTouch(float x, float y) {
        if (selectedInventorySkin < 0) {
            return false;
        }
        if (modalCloseRect.contains(x, y) || !skinModalRect.contains(x, y)) {
            selectedInventorySkin = -1;
            selectedInventoryItemIndex = -1;
            invalidate();
            return true;
        }
        if (modalPriceMinusRect.contains(x, y)) {
            modalListingPrice = Math.max(100, modalListingPrice - 500);
            invalidate();
            return true;
        }
        if (modalPricePlusRect.contains(x, y)) {
            modalListingPrice = Math.min(999_999, modalListingPrice + 500);
            invalidate();
            return true;
        }
        if (modalEquipRect.contains(x, y)) {
            sendAccountAction("equipSkin", ",\"skin\":" + selectedInventorySkin);
            statusMessage = "экипировка отправлена на сервер";
            invalidate();
            return true;
        }
        if (modalMarketRect.contains(x, y)) {
            listSkinOnMarket(selectedInventorySkin, modalListingPrice);
            selectedInventorySkin = -1;
            selectedInventoryItemIndex = -1;
            setScreen(Screen.SHOP);
            shopMode = 2;
            shopScrollY = 0f;
            requestMarketState(true);
            invalidate();
            return true;
        }
        if (modalShowcaseRect.contains(x, y)) {
            toggleSkinShowcase(selectedInventorySkin);
            invalidate();
            return true;
        }
        return true;
    }

    private void handleShopTouchV2(float x, float y) {
        if (shopInventoryTabRect.contains(x, y)) {
            shopMode = 0; selectedMarketSkin = -1; invalidate(); return;
        }
        if (!viewingExternalProfile && avatarButtonRect.contains(x, y)) {
            if (getContext() instanceof MainActivity) {
                ((MainActivity) getContext()).chooseAvatar();
            }
            return;
        }
        if (shopDnaTabRect.contains(x, y)) {
            shopMode = 1; selectedMarketSkin = -1; invalidate(); return;
        }
        if (shopMarketTabRect.contains(x, y)) {
            shopMode = 2; selectedMarketSkin = -1; requestMarketState(true); invalidate(); return;
        }
        if (selectedMarketSkin >= 0) {
            if (marketDetailCloseRect.contains(x, y) || !marketDetailRect.contains(x, y)) {
                selectedMarketSkin = -1;
                invalidate();
                return;
            }
            for (int i = 0; i < marketOrderBuyRects.size(); i++) {
                if (marketOrderBuyRects.get(i).contains(x, y)) {
                    int listingIndex = i < marketOrderBuyIndices.size() ? marketOrderBuyIndices.get(i) : i;
                    buyMarketListing(listingIndex);
                    selectedMarketSkin = -1;
                    invalidate();
                    return;
                }
            }
            return;
        }
        if (selectedInventorySkin >= 0) {
            if (modalCloseRect.contains(x, y) || !skinModalRect.contains(x, y)) {
                selectedInventorySkin = -1;
                invalidate();
                return;
            }
            if (modalPriceMinusRect.contains(x, y)) {
                modalListingPrice = Math.max(100, modalListingPrice - 500);
                invalidate();
                return;
            }
            if (modalPricePlusRect.contains(x, y)) {
                modalListingPrice = Math.min(999_999, modalListingPrice + 500);
                invalidate();
                return;
            }
            if (modalEquipRect.contains(x, y)) {
                sendAccountAction("equipSkin", ",\"skin\":" + selectedInventorySkin);
                statusMessage = "экипировка отправлена на сервер";
                invalidate();
                return;
            }
            if (modalMarketRect.contains(x, y)) {
                listSkinOnMarket(selectedInventorySkin, modalListingPrice);
                selectedInventorySkin = -1;
                shopMode = 2;
                shopScrollY = 0f;
                requestMarketState(true);
                invalidate();
                return;
            }
            if (modalShowcaseRect.contains(x, y)) {
                toggleSkinShowcase(selectedInventorySkin);
                invalidate();
                return;
            }
            return;
        }
        for (int i = 0; i < donationRects.size(); i++) {
            if (donationRects.get(i).contains(x, y)) {
                int pack = i < donationPacks.size() ? donationPacks.get(i) : 0;
                openDonation(pack);
                invalidate();
                return;
            }
        }
        for (int i = 0; i < caseShopRects.size(); i++) {
            if (caseShopRects.get(i).contains(x, y)) {
                int tier = i < caseShopTiers.size() ? caseShopTiers.get(i) : CASE_TIER_2K;
                buyCaseToInventory(tier);
                invalidate();
                return;
            }
        }
        if (shopMode == 2) {
            for (int i = 0; i < marketSkinRects.size(); i++) {
                if (marketSkinRects.get(i).contains(x, y)) {
                    selectedMarketSkin = i < marketSkinIndices.size() ? marketSkinIndices.get(i) : -1;
                    invalidate();
                    return;
                }
            }
            return;
        }
        for (int i = 0; i < skinRects.size(); i++) {
            if (!skinRects.get(i).contains(x, y)) {
                continue;
            }
            int skinIndex = skinIndexForRect(i);
            if (skinIndex < 0 || skinIndex >= skinOwned.length) {
                return;
            }
            if (!skinOwned[skinIndex]) {
                statusMessage = isPassSkin(skinIndex) ? "скин открывается в bk pass или на ТП" : "скин можно выбить из кейса или купить на ТП";
                invalidate();
                return;
            }
            selectedInventorySkin = skinIndex;
            modalListingPrice = Math.max(100, skinLastSalePrices[skinIndex]);
            invalidate();
            return;
        }
    }

    private void buyCaseToInventory(int tier) {
        int safeTier = clampInt(tier, 0, CASE_COSTS.length - 1);
        if (sendAccountAction("buyCase", ",\"tier\":" + safeTier)) {
            statusMessage = caseTierName(safeTier) + " покупается на сервере";
        } else {
            statusMessage = "нет соединения с сервером";
        }
    }

    private void openDonation(int pack) {
        if (accountId == null || accountId.trim().isEmpty()) {
            statusMessage = "сначала войди в аккаунт";
            return;
        }
        int safePack = clampInt(pack, 0, DONATION_COINS.length - 1);
        Uri uri = Uri.parse(GAME_SERVER_HTTP_URL + "/donate")
                .buildUpon()
                .appendQueryParameter("accountId", accountId)
                .appendQueryParameter("pack", String.valueOf(safePack))
                .build();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            getContext().startActivity(intent);
            statusMessage = "открываю оплату ЮKassa";
        } catch (Exception ignored) {
            statusMessage = "не удалось открыть оплату";
        }
    }

    private void startOpeningInventoryCase(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= inventoryItems.size()) {
            return;
        }
        InventoryItem item = inventoryItems.get(inventoryIndex);
        if (item.type != INVENTORY_ITEM_CASE) {
            return;
        }
        if (!sendAccountAction("openCase", ",\"index\":" + inventoryIndex)) {
            statusMessage = "нет соединения с сервером";
            return;
        }
        statusMessage = "кейс открывается на сервере";
    }

    private void startCaseOpeningAnimation(int inventoryIndex, int tier, int resultSkin) {
        caseOpeningActive = true;
        caseOpeningStartMs = System.currentTimeMillis();
        caseOpeningInventoryIndex = inventoryIndex;
        caseOpeningTier = clampInt(tier, 0, CASE_COSTS.length - 1);
        caseOpeningResultSkin = clampInt(resultSkin, 0, skinOwned.length - 1);
        caseOpeningReel = buildCaseOpeningReel(caseOpeningTier, caseOpeningResultSkin);
    }

    private void finishOpeningCase() {
        if (!caseOpeningActive || caseOpeningResultSkin < 0 || caseOpeningResultSkin >= skinOwned.length) {
            return;
        }
        selectedInventoryItemIndex = caseOpeningInventoryIndex;
        selectedInventorySkin = caseOpeningResultSkin;
        modalListingPrice = Math.max(100, skinLastSalePrices[caseOpeningResultSkin]);
        statusMessage = "выпал: " + skinNames[caseOpeningResultSkin];
        caseOpeningActive = false;
        caseOpeningReel = new int[0];
        saveState();
    }

    private int[] buildCaseOpeningReel(int tier, int resultSkin) {
        int[] reel = new int[CASE_REEL_SLOT_COUNT];
        for (int i = 0; i < reel.length; i++) {
            reel[i] = rollCaseSkin(tier);
        }
        reel[CASE_REEL_RESULT_SLOT] = clampInt(resultSkin, 0, skinNames.length - 1);
        return reel;
    }

    private int caseOpeningReelSkin(int slot) {
        if (slot >= 0 && slot < caseOpeningReel.length) {
            return caseOpeningReel[slot];
        }
        return previewCaseSkin(caseOpeningTier, slot + caseOpeningResultSkin * 3);
    }

    private int rollCaseSkin(int tier) {
        int roll = effectsRandom.nextInt(10_000);
        int commonWeight = tier == CASE_TIER_10K ? 1_700 : (tier == CASE_TIER_5K ? 2_100 : 2_600);
        if (roll < commonWeight) {
            return 1;
        }
        if (roll < commonWeight * 2) {
            return 2;
        }
        int paletteLimit = tier == CASE_TIER_10K ? 8_650 : (tier == CASE_TIER_5K ? 9_100 : 9_450);
        if (roll < paletteLimit) {
            return rollWeightedPaletteSkin();
        }
        int[] rare = {PASS_SKIN_1F51, PASS_SKIN_1P25, PASS_SKIN_1P50};
        return rare[effectsRandom.nextInt(rare.length)];
    }

    private int previewCaseSkin(int tier, int offset) {
        if (Math.floorMod(offset, 5) == 0) {
            return 1;
        }
        if (Math.floorMod(offset, 5) == 1) {
            return 2;
        }
        if (tier == CASE_TIER_10K && Math.floorMod(offset, 11) == 0) {
            int[] rare = {PASS_SKIN_1F51, PASS_SKIN_1P25, PASS_SKIN_1P50};
            return rare[Math.floorMod(offset, rare.length)];
        }
        return CASE_SKIN_START + Math.floorMod(offset * 7, CASE_SKIN_COUNT);
    }

    private int rollWeightedPaletteSkin() {
        int total = 0;
        for (int i = 0; i < CASE_SKIN_COUNT; i++) {
            total += CASE_SKIN_COUNT - i;
        }
        int roll = effectsRandom.nextInt(Math.max(1, total));
        for (int i = 0; i < CASE_SKIN_COUNT; i++) {
            roll -= CASE_SKIN_COUNT - i;
            if (roll < 0) {
                return CASE_SKIN_START + i;
            }
        }
        return CASE_SKIN_START;
    }

    private void listSkinOnMarket(int skinIndex, int price) {
        int safePrice = clampInt(price, 100, 999_999);
        if (sendServerMessage("{\"type\":\"marketSell\",\"skin\":" + skinIndex + ",\"price\":" + safePrice + "}")) {
            selectedInventorySkin = -1;
            selectedInventoryItemIndex = -1;
            statusMessage = "заявка отправлена на сервер";
        } else {
            statusMessage = "нет соединения с сервером";
        }
    }

    private void buyMarketListing(int listingIndex) {
        if (listingIndex < 0 || listingIndex >= marketListings.size()) {
            return;
        }
        MarketListing listing = marketListings.get(listingIndex);
        if (sendServerMessage("{\"type\":\"marketBuy\",\"listingId\":" + listing.id + "}")) {
            statusMessage = "покупка отправлена на сервер";
        } else {
            statusMessage = "нет соединения с сервером";
        }
    }

    private void toggleSkinShowcase(int skinIndex) {
        if (skinIndex < 0 || skinIndex >= skinShowcase.length || !skinOwned[skinIndex]) {
            return;
        }
        if (sendAccountAction("toggleShowcase", ",\"skin\":" + skinIndex)) {
            statusMessage = "витрина отправлена на сервер";
        } else {
            statusMessage = "нет соединения с сервером";
        }
    }

    private int showcaseCount() {
        int count = 0;
        for (boolean value : skinShowcase) {
            if (value) {
                count++;
            }
        }
        return count;
    }

    private int skinDefaultMarketPrice(int skinIndex) {
        if (skinIndex == 1) {
            return 650;
        }
        if (skinIndex == 2) {
            return 800;
        }
        if (isPassSkin(skinIndex)) {
            return 50_000;
        }
        if (skinIndex >= CASE_SKIN_START) {
            int rarity = skinIndex - CASE_SKIN_START;
            return 1_200 + rarity * 260;
        }
        return Math.max(100, SKIN_COSTS[Math.min(skinIndex, SKIN_COSTS.length - 1)]);
    }

    private boolean isPassSkin(int index) {
        return index == PASS_SKIN_1F51 || index == PASS_SKIN_1P25 || index == PASS_SKIN_1P50;
    }

    private int skinIndexForRect(int rectIndex) {
        if (rectIndex >= 0 && rectIndex < skinRectIndices.size()) {
            return skinRectIndices.get(rectIndex);
        }
        return rectIndex;
    }

    private void handleShopTouch(float x, float y) {
        for (int i = 0; i < skinRects.size(); i++) {
            if (!skinRects.get(i).contains(x, y)) {
                continue;
            }
            if (skinOwned[i]) {
                sendAccountAction("equipSkin", ",\"skin\":" + i);
                statusMessage = "экипировка отправлена на сервер";
            } else if (i >= 3) {
                statusMessage = "этот скин открывается в pass";
            } else if (sendAccountAction("buySkin", ",\"skin\":" + i)) {
                statusMessage = "покупка скина отправлена на сервер";
            } else {
                statusMessage = "нет соединения с сервером";
            }
            invalidate();
            return;
        }
    }

    private void handleEditorTouch(float x, float y) {
        if (!skinsUnlocked()) {
            statusMessage = "редактор откроется, когда все характеристики улучшены";
            setScreen(Screen.PROFILE);
            invalidate();
            return;
        }
        if (caseOpeningActive) {
            if (caseOpeningClaimRect.contains(x, y)) {
                finishOpeningCase();
            }
            invalidate();
            return;
        }
        if (handleSkinActionModalTouch(x, y)) {
            return;
        }
        for (int i = 0; i < inventoryItemRects.size(); i++) {
            if (!inventoryItemRects.get(i).contains(x, y)) {
                continue;
            }
            int inventoryIndex = i < inventoryItemIndices.size() ? inventoryItemIndices.get(i) : i;
            if (inventoryIndex < 0 || inventoryIndex >= inventoryItems.size()) {
                return;
            }
            InventoryItem item = inventoryItems.get(inventoryIndex);
            if (item.type == INVENTORY_ITEM_CASE) {
                startOpeningInventoryCase(inventoryIndex);
                invalidate();
                return;
            }
            int skinIndex = clampInt(item.value, 0, skinOwned.length - 1);
            selectedInventoryItemIndex = inventoryIndex;
            selectedInventorySkin = skinIndex;
            modalListingPrice = Math.max(100, skinLastSalePrices[skinIndex]);
            invalidate();
            return;
        }
        for (int i = 0; i < skinRects.size(); i++) {
            if (skinRects.get(i).contains(x, y)) {
                int skinIndex = skinIndexForRect(i);
                if (skinIndex >= 0 && skinIndex < skinOwned.length && skinOwned[skinIndex]) {
                    sendAccountAction("equipSkin", ",\"skin\":" + skinIndex);
                    statusMessage = "экипировка отправлена на сервер";
                } else {
                    statusMessage = "скин не открыт";
                }
                invalidate();
                return;
            }
        }
    }

    private void handleMusicTouch(float x, float y) {
        for (int i = 0; i < musicOpenRects.size() && i < MUSIC_URLS.length; i++) {
            if (musicOpenRects.get(i).contains(x, y)) {
                openMusicPage(i);
                return;
            }
        }
    }

    private void openMusicPage(int index) {
        if (index < 0 || index >= MUSIC_URLS.length) {
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MUSIC_URLS[index]));
            getContext().startActivity(intent);
            statusMessage = "открываю " + MUSIC_TITLES[index];
        } catch (Exception ignored) {
            statusMessage = "не удалось открыть страницу";
        }
        invalidate();
    }

    private boolean handleSettingsSliderTouch(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            if (sliderHitRect(musicVolumeSliderRect).contains(x, y)) {
                activeSettingsSlider = 1;
                updateSettingsSliderValue(x);
                return true;
            }
            if (sliderHitRect(effectsVolumeSliderRect).contains(x, y)) {
                activeSettingsSlider = 2;
                updateSettingsSliderValue(x);
                return true;
            }
            return false;
        }
        if (action == MotionEvent.ACTION_MOVE && activeSettingsSlider != 0) {
            updateSettingsSliderValue(x);
            return true;
        }
        if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) && activeSettingsSlider != 0) {
            updateSettingsSliderValue(x);
            activeSettingsSlider = 0;
            saveState();
            return true;
        }
        return false;
    }

    private RectF sliderHitRect(RectF slider) {
        return new RectF(slider.left - 28f, slider.top - 28f, slider.right + 28f, slider.bottom + 28f);
    }

    private void updateSettingsSliderValue(float x) {
        RectF slider = activeSettingsSlider == 1 ? musicVolumeSliderRect : effectsVolumeSliderRect;
        int value = Math.round(clamp((x - slider.left) / Math.max(1f, slider.width()), 0f, 1f) * 100f);
        if (activeSettingsSlider == 1) {
            musicVolume = value;
            startMusicIfNeeded();
            statusMessage = "громкость: " + musicVolume + "%";
        } else {
            effectsVolume = value;
            statusMessage = "эффекты: " + effectsVolume + "%";
        }
        saveState();
        invalidate();
    }

    private void handleSettingsTouch(float x, float y) {
        if (musicRect.contains(x, y)) {
            musicEnabled = !musicEnabled;
            statusMessage = musicEnabled ? "музыка включена" : "музыка выключена";
            startMusicIfNeeded();
        } else if (soundsRect.contains(x, y)) {
            soundsEnabled = !soundsEnabled;
            statusMessage = soundsEnabled ? "звуки включены" : "звуки выключены";
        } else if (controlRect.contains(x, y)) {
            controlMode = 1 - controlMode;
            leftPointerId = -1;
            rightPointerId = -1;
            moveX = 0f;
            moveY = 0f;
            statusMessage = "джойстик движения: " + (controlMode == 0 ? "слева" : "справа");
        } else if (graphicsRect.contains(x, y)) {
            graphicsQuality = (graphicsQuality + 1) % 3;
            statusMessage = "графика: " + graphicsLabel();
        } else if (loadServerRect.contains(x, y)) {
            sendAuthToServer();
            requestCurrentProfile();
            requestMarketState(true);
            statusMessage = "загружаем профиль с сервера";
        } else if (saveServerRect.contains(x, y)) {
            sendServerMessage("{\"type\":\"profileUpdate\",\"levelSeed\":" + worldSeed + "}");
            statusMessage = "профиль сохранён на сервере";
        } else if (logoutRect.contains(x, y)) {
            disconnectGameServer();
            sessionSaved = false;
            accountId = "";
            password = "";
            setScreen(Screen.AUTH);
            statusMessage = "вы вышли из аккаунта";
        }
        saveState();
        invalidate();
    }

    private void startMusicIfNeeded() {
        if (!musicEnabled || musicVolume <= 0 || MUSIC_TRACKS.length == 0) {
            pauseMusic();
            applyMusicVolume();
            return;
        }
        if (musicPlayer == null) {
            playMusicTrack(nextRandomMusicTrack());
            return;
        }
        applyMusicVolume();
        if (!musicPlayer.isPlaying()) {
            musicPlayer.start();
        }
    }

    private void playMusicTrack(int index) {
        releaseMusicPlayer();
        currentMusicTrack = Math.max(0, index) % MUSIC_TRACKS.length;
        lastMusicTrack = currentMusicTrack;
        musicPlayer = MediaPlayer.create(getContext(), MUSIC_TRACKS[currentMusicTrack]);
        if (musicPlayer == null) {
            return;
        }
        configureUnderwaterEqualizer();
        musicPlayer.setOnCompletionListener(player -> {
            playMusicTrack(nextRandomMusicTrack());
        });
        applyMusicVolume();
        if (musicEnabled && musicVolume > 0) {
            musicPlayer.start();
        }
    }

    private int nextRandomMusicTrack() {
        if (MUSIC_TRACKS.length <= 1) {
            return 0;
        }
        int next = musicRandom.nextInt(MUSIC_TRACKS.length);
        if (next == lastMusicTrack) {
            next = (next + 1 + musicRandom.nextInt(MUSIC_TRACKS.length - 1)) % MUSIC_TRACKS.length;
        }
        return next;
    }

    private void configureUnderwaterEqualizer() {
        releaseMusicEqualizer();
        if (musicPlayer == null) {
            return;
        }
        try {
            musicEqualizer = new Equalizer(0, musicPlayer.getAudioSessionId());
            short bands = musicEqualizer.getNumberOfBands();
            short[] range = musicEqualizer.getBandLevelRange();
            for (short band = 0; band < bands; band++) {
                int hz = musicEqualizer.getCenterFreq(band) / 1000;
                int level;
                if (hz >= 4000) {
                    level = -2400;
                } else if (hz >= 1800) {
                    level = -1800;
                } else if (hz <= 250) {
                    level = -350;
                } else {
                    level = -950;
                }
                musicEqualizer.setBandLevel(band, (short) clampInt(level, range[0], range[1]));
            }
            musicEqualizer.setEnabled(true);
        } catch (RuntimeException ignored) {
            releaseMusicEqualizer();
        }
    }

    private void applyMusicVolume() {
        if (musicPlayer == null) {
            return;
        }
        float volume = musicEnabled ? clamp(musicVolume / 100f, 0f, 1f) * 0.26f : 0f;
        musicPlayer.setVolume(volume, volume);
    }

    private void pauseMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
        }
    }

    private void releaseMusicPlayer() {
        releaseMusicEqualizer();
        if (musicPlayer == null) {
            return;
        }
        musicPlayer.setOnCompletionListener(null);
        musicPlayer.release();
        musicPlayer = null;
    }

    private void releaseMusicEqualizer() {
        if (musicEqualizer == null) {
            return;
        }
        try {
            musicEqualizer.setEnabled(false);
            musicEqualizer.release();
        } catch (RuntimeException ignored) {
        }
        musicEqualizer = null;
    }

    private void initSoundEffects() {
        releaseSoundEffects();
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attributes)
                .build();
        dnaEatSoundId = soundPool.load(getContext(), R.raw.sound_dna_eat, 1);
        enemyKillSoundId = soundPool.load(getContext(), R.raw.sound_enemy_kill, 1);
    }

    private void playGameSoundEffect(int sound) {
        if (!soundsEnabled || effectsVolume <= 0 || soundPool == null) {
            return;
        }
        int soundId = sound == SOUND_ENEMY_KILL ? enemyKillSoundId : dnaEatSoundId;
        if (soundId == 0) {
            return;
        }
        float volume = clamp(effectsVolume / 100f, 0f, 1f);
        soundPool.play(soundId, volume, volume, 1, 0, 1f);
    }

    private void releaseSoundEffects() {
        if (soundPool == null) {
            return;
        }
        soundPool.release();
        soundPool = null;
        dnaEatSoundId = 0;
        enemyKillSoundId = 0;
    }

    private void handleAuthTouch(float x, float y) {
        if (authUserRect.contains(x, y)) {
            focusField = FocusField.USERNAME;
            showKeyboard();
        } else if (authPasswordRect.contains(x, y)) {
            focusField = FocusField.PASSWORD;
            showKeyboard();
        } else if (authPrimaryRect.contains(x, y)) {
            submitAuthForm();
        } else {
            focusField = FocusField.NONE;
            hideKeyboard();
        }
        invalidate();
    }

    private boolean submitAuthForm() {
        String trimmedName = username.trim();
        String trimmedPassword = password.trim();
        if (trimmedName.isEmpty()) {
            statusMessage = "введите ник";
            focusField = FocusField.USERNAME;
            showKeyboard();
            invalidate();
            return false;
        }
        if (trimmedPassword.isEmpty()) {
            statusMessage = "введите пароль";
            focusField = FocusField.PASSWORD;
            showKeyboard();
            invalidate();
            return false;
        }
        username = trimmedName;
        password = trimmedPassword;
        sessionSaved = true;
        selectedServer = 0;
        waitingForAuthentication = true;
        statusMessage = "проверяем аккаунт на сервере";
        connectGameServerIfNeeded();
        sendAuthToServer();
        focusField = FocusField.NONE;
        saveState();
        hideKeyboard();
        invalidate();
        return true;
    }

    private int imeActionForFocusedField() {
        if (focusField == FocusField.CHAT) {
            return EditorInfo.IME_ACTION_SEND;
        }
        if (screen == Screen.AUTH && focusField == FocusField.USERNAME) {
            return EditorInfo.IME_ACTION_NEXT;
        }
        return EditorInfo.IME_ACTION_DONE;
    }

    private boolean handleImeAction(int actionCode) {
        if (focusField == FocusField.CHAT) {
            sendChatMessage();
            return true;
        }
        if (screen == Screen.AUTH && focusField == FocusField.USERNAME) {
            focusField = FocusField.PASSWORD;
            showKeyboard();
            invalidate();
            return true;
        }
        if (screen == Screen.AUTH && focusField == FocusField.EMAIL) {
            focusField = FocusField.PASSWORD;
            showKeyboard();
            invalidate();
            return true;
        }
        if (screen == Screen.AUTH && focusField == FocusField.PASSWORD) {
            return submitAuthForm();
        }
        if (focusField == FocusField.FRIEND) {
            submitFriendDraft();
            return true;
        }
        if (actionCode == EditorInfo.IME_ACTION_DONE || actionCode == EditorInfo.IME_ACTION_GO || actionCode == EditorInfo.IME_ACTION_UNSPECIFIED) {
            focusField = FocusField.NONE;
            hideKeyboard();
            invalidate();
            return true;
        }
        return false;
    }

    private boolean submitFriendDraft() {
        String id = friendDraft.trim();
        if (id.isEmpty()) {
            statusMessage = "введите id друга";
            focusField = FocusField.FRIEND;
            showKeyboard();
            invalidate();
            return false;
        }
        if (friendIds.contains(id) || friendRequestIds.contains(id)) {
            statusMessage = "этот id уже добавлен";
            focusField = FocusField.NONE;
            hideKeyboard();
            invalidate();
            return false;
        }
        sendAccountAction("friendRequest", ",\"target\":\"" + jsonEscape(id) + "\"");
        friendDraft = "";
        friendPanelTab = 0;
        friendsScrollY = 0f;
        statusMessage = "заявка отправлена на сервер";
        focusField = FocusField.NONE;
        saveState();
        hideKeyboard();
        invalidate();
        return true;
    }

    private void appendInput(String input) {
        appendToFocusedInput(input);
        saveState();
        invalidate();
    }

    private void commitInput(String input) {
        String cleaned = cleanInput(input);
        if (!composingInput.isEmpty()) {
            if (cleaned.equals(composingInput)) {
                composingInput = "";
                return;
            }
            removeFocusedInputSuffix(composingInput);
            composingInput = "";
        }
        appendInput(cleaned);
    }

    private void replaceComposingInput(String input) {
        String cleaned = cleanInput(input);
        if (!composingInput.isEmpty()) {
            removeFocusedInputSuffix(composingInput);
        }
        composingInput = appendToFocusedInput(cleaned);
        saveState();
        invalidate();
    }

    private String appendToFocusedInput(String input) {
        String cleaned = cleanInput(input);
        if (cleaned.isEmpty()) {
            return "";
        }
        String before = focusedInputValue();
        String updated = before;
        if (focusField == FocusField.USERNAME) {
            updated = limit(before + cleaned, 16);
            username = updated;
        } else if (focusField == FocusField.EMAIL) {
            updated = limit(before + cleaned, 28);
            email = updated;
        } else if (focusField == FocusField.PASSWORD) {
            updated = limit(before + cleaned, 18);
            password = updated;
        } else if (focusField == FocusField.FRIEND) {
            updated = limit(before + cleaned, 18);
            friendDraft = updated;
        } else if (focusField == FocusField.CHAT) {
            updated = limit(before + cleaned, 64);
            chatDraft = updated;
        }
        return updated.startsWith(before) ? updated.substring(before.length()) : "";
    }

    private String focusedInputValue() {
        if (focusField == FocusField.USERNAME) {
            return username;
        }
        if (focusField == FocusField.EMAIL) {
            return email;
        }
        if (focusField == FocusField.PASSWORD) {
            return password;
        }
        if (focusField == FocusField.FRIEND) {
            return friendDraft;
        }
        if (focusField == FocusField.CHAT) {
            return chatDraft;
        }
        return "";
    }

    private void removeFocusedInputSuffix(String suffix) {
        String current = focusedInputValue();
        if (!suffix.isEmpty() && current.endsWith(suffix)) {
            String updated = current.substring(0, current.length() - suffix.length());
            if (focusField == FocusField.USERNAME) {
                username = updated;
            } else if (focusField == FocusField.EMAIL) {
                email = updated;
            } else if (focusField == FocusField.PASSWORD) {
                password = updated;
            } else if (focusField == FocusField.FRIEND) {
                friendDraft = updated;
            } else if (focusField == FocusField.CHAT) {
                chatDraft = updated;
            }
        }
    }

    private String cleanInput(String input) {
        return input == null ? "" : input.replace("\n", "").replace("\r", "");
    }

    private void backspaceInput() {
        composingInput = "";
        if (focusField == FocusField.USERNAME && !username.isEmpty()) {
            username = username.substring(0, username.length() - 1);
        } else if (focusField == FocusField.EMAIL && !email.isEmpty()) {
            email = email.substring(0, email.length() - 1);
        } else if (focusField == FocusField.PASSWORD && !password.isEmpty()) {
            password = password.substring(0, password.length() - 1);
        } else if (focusField == FocusField.FRIEND && !friendDraft.isEmpty()) {
            friendDraft = friendDraft.substring(0, friendDraft.length() - 1);
        } else if (focusField == FocusField.CHAT && !chatDraft.isEmpty()) {
            chatDraft = chatDraft.substring(0, chatDraft.length() - 1);
        }
        saveState();
        invalidate();
    }

    private void showKeyboard() {
        InputMethodManager input = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (input != null && focusField != FocusField.NONE) {
            composingInput = "";
            requestFocusFromTouch();
            requestFocus();
            input.restartInput(this);
            postDelayed(() -> input.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT), 80L);
        }
    }

    private void hideKeyboard() {
        InputMethodManager input = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (input != null) {
            input.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    private String serverName(int index) {
        return "Россия (СПБ)";
    }

    private void restoreSavedSession() {
        if (!sessionSaved || username.trim().isEmpty()) {
            return;
        }
        selectedServer = 0;
        waitingForAuthentication = true;
        connectGameServerIfNeeded();
        sendAuthToServer();
    }

    private String displayName() {
        String name = username.trim();
        return name.isEmpty() ? "USERNAME" : name.toUpperCase(Locale.ROOT);
    }

    private int geneCost(int index) {
        if (geneLevels[index] >= GENE_MAX_LEVEL) {
            return 0;
        }
        return (geneLevels[index] + 1) * 50;
    }

    private int totalGeneLevel() {
        int total = 0;
        for (int level : geneLevels) {
            total += level;
        }
        return total;
    }

    private String graphicsLabel() {
        if (graphicsQuality == 0) {
            return "низкая";
        }
        if (graphicsQuality == 1) {
            return "средняя";
        }
        return "высокая";
    }

    private int playerLevel() {
        return playerLevel;
    }

    private int battlePassLevel() {
        return clampInt(playerLevel(), 1, 50);
    }

    private int proBattlePassLevel() {
        return proPassOwned ? clampInt(proPassLevel, 1, 50) : 0;
    }

    private boolean unlockPassRewards() {
        return claimBattlePassRewards(false);
    }

    private boolean claimBattlePassRewards(boolean announce) {
        return false;
    }

    private int passClaimedCount(boolean pro) {
        boolean[] claimed = pro ? proPassRewardsClaimed : freePassRewardsClaimed;
        int max = 50;
        int count = 0;
        for (int i = 1; i <= max; i++) {
            if (claimed[i]) {
                count++;
            }
        }
        return count;
    }

    private int playerXpInLevel() {
        return playerXp;
    }

    private int xpForCurrentLevel() {
        return xpForLevel(playerLevel);
    }

    private int xpForLevel(int level) {
        return Math.max(50, level * 50);
    }

    private float levelProgress() {
        return clamp(playerXpInLevel() / (float) xpForCurrentLevel(), 0f, 1f);
    }

    private void addExperience(int amount) {
        int gain = Math.max(0, amount);
        if (gain == 0) {
            return;
        }
        if (!sendAccountAction("localDnaEat", ",\"amount\":" + gain)) {
            statusMessage = "нет соединения с сервером для прогресса";
        }
    }

    private void normalizeLoadedExperience() {
        while (playerXp >= xpForCurrentLevel()) {
            playerXp -= xpForCurrentLevel();
            playerLevel++;
        }
    }

    private boolean skinsUnlocked() {
        return true;
    }

    private int playerBaseColor() {
        if (selectedSkin >= 0 && selectedSkin < SKIN_COLORS.length
                && selectedSkin < skinOwned.length && skinOwned[selectedSkin]) {
            return SKIN_COLORS[selectedSkin];
        }
        return COLOR_PLAYER;
    }

    private int tintByGenes(int color, int step) {
        return Color.rgb(
                clampColor(Color.red(color) + geneLevels[1] * step),
                clampColor(Color.green(color) + geneLevels[2] * step),
                clampColor(Color.blue(color) + geneLevels[0] * step)
        );
    }

    private int playerHotColor(int color) {
        return Color.rgb(
                clampColor(Color.red(color) + 46),
                clampColor(Color.green(color) + 34),
                clampColor(Color.blue(color) + 18)
        );
    }

    private int playerCoolColor(int color) {
        return Color.rgb(
                clampColor(Color.red(color) - 28),
                clampColor(Color.green(color) - 18),
                clampColor(Color.blue(color) + 32)
        );
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(clampColor(alpha), Color.red(color), Color.green(color), Color.blue(color));
    }

    private int playerOutlineColor(int color) {
        return Color.rgb(
                clampColor(Color.red(color) + 34),
                clampColor(Color.green(color) + 36),
                clampColor(Color.blue(color) + 24)
        );
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private void loadState() {
        username = prefs.getString("username", username);
        email = prefs.getString("email", email);
        password = prefs.getString("password", password);
        sessionSaved = prefs.getBoolean("sessionSaved", !username.trim().isEmpty());
        coins = prefs.getInt("coins", coins);
        playerLevel = Math.max(1, prefs.getInt("playerLevel", playerLevel));
        playerXp = prefs.getInt("playerXp", playerXp);
        dnaEaten = prefs.getInt("dnaEaten", dnaEaten);
        totalDnaEarned = prefs.getInt("totalDnaEarned", totalDnaEarned);
        musicEnabled = prefs.getBoolean("musicEnabled", musicEnabled);
        soundsEnabled = prefs.getBoolean("soundsEnabled", soundsEnabled);
        musicVolume = clampInt(prefs.getInt("musicVolume", musicVolume), 0, 100);
        effectsVolume = clampInt(prefs.getInt("effectsVolume", effectsVolume), 0, 100);
        controlMode = prefs.getInt("controlMode", controlMode);
        graphicsQuality = prefs.getInt("graphicsQuality", graphicsQuality);
        selectedSkin = prefs.getInt("selectedSkin", selectedSkin);
        proPassOwned = prefs.getBoolean("proPassOwned", proPassOwned);
        passSeason = prefs.getInt("passSeason", passSeason);
        proPassLevel = clampInt(prefs.getInt("proPassLevel", proPassLevel), 1, 50);
        friendDraft = prefs.getString("friendDraft", friendDraft);
        latestNewsText = prefs.getString("latestNewsText", latestNewsText);
        latestNewsDate = prefs.getString("latestNewsDate", latestNewsDate);
        accountId = prefs.getString("accountId", accountId);
        selectedServer = 0;
        loadFriendIds(prefs.getString("friendIds", ""));
        loadFriendRequestIds(prefs.getString("friendRequestIds", ""));
        loadInventoryItems(prefs.getString("inventoryItems", ""));
        for (int i = 0; i < geneLevels.length; i++) {
            geneLevels[i] = prefs.getInt("gene" + i, geneLevels[i]);
        }
        for (int i = 0; i < skinOwned.length; i++) {
            skinOwned[i] = prefs.getBoolean("skinOwned" + i, skinOwned[i]);
            skinShowcase[i] = prefs.getBoolean("skinShowcase" + i, skinShowcase[i]);
            skinLastSalePrices[i] = prefs.getInt("skinLastSale" + i, skinLastSalePrices[i]);
        }
        if (skinOwned.length > 0) {
            skinOwned[0] = true;
        }
        ensureInventorySeededFromOwnedSkins();
        ensurePassSeason();
        for (int i = 1; i < freePassRewardsClaimed.length; i++) {
            freePassRewardsClaimed[i] = prefs.getBoolean("freePassReward" + i, freePassRewardsClaimed[i]);
        }
        for (int i = 1; i < proPassRewardsClaimed.length; i++) {
            proPassRewardsClaimed[i] = prefs.getBoolean("proPassReward" + i, proPassRewardsClaimed[i]);
        }
        if (selectedSkin < 0 || selectedSkin >= skinOwned.length || !skinOwned[selectedSkin]) {
            selectedSkin = 0;
        }
        normalizeLoadedExperience();
        long lastDailyDay = prefs.getLong("lastDailyDay", -1L);
        dailyClaimed = lastDailyDay == currentDay();
    }

    private void saveState() {
        SharedPreferences.Editor editor = prefs.edit()
                .putString("username", username)
                .putString("email", email)
                .putString("password", password)
                .putBoolean("sessionSaved", sessionSaved)
                .putInt("coins", coins)
                .putInt("playerLevel", playerLevel)
                .putInt("playerXp", playerXp)
                .putInt("dnaEaten", dnaEaten)
                .putInt("totalDnaEarned", totalDnaEarned)
                .putBoolean("musicEnabled", musicEnabled)
                .putBoolean("soundsEnabled", soundsEnabled)
                .putInt("musicVolume", musicVolume)
                .putInt("effectsVolume", effectsVolume)
                .putInt("controlMode", controlMode)
                .putInt("graphicsQuality", graphicsQuality)
                .putInt("selectedSkin", selectedSkin)
                .putBoolean("proPassOwned", proPassOwned)
                .putInt("passSeason", passSeason)
                .putInt("proPassLevel", proPassLevel)
                .putString("friendDraft", friendDraft)
                .putString("friendIds", joinFriendIds())
                .putString("friendRequestIds", joinFriendRequestIds())
                .putString("inventoryItems", encodeInventoryItems())
                .putString("latestNewsText", latestNewsText)
                .putString("latestNewsDate", latestNewsDate)
                .putString("accountId", accountId)
                .remove("avatarUri");
        for (int i = 0; i < geneLevels.length; i++) {
            editor.putInt("gene" + i, geneLevels[i]);
        }
        for (int i = 0; i < skinOwned.length; i++) {
            editor.putBoolean("skinOwned" + i, skinOwned[i]);
            editor.putBoolean("skinShowcase" + i, skinShowcase[i]);
            editor.putInt("skinLastSale" + i, skinLastSalePrices[i]);
        }
        for (int i = 1; i < freePassRewardsClaimed.length; i++) {
            editor.putBoolean("freePassReward" + i, freePassRewardsClaimed[i]);
        }
        for (int i = 1; i < proPassRewardsClaimed.length; i++) {
            editor.putBoolean("proPassReward" + i, proPassRewardsClaimed[i]);
        }
        editor.apply();
    }

    private void ensurePassSeason() {
        if (passSeason == PASS_SEASON) {
            return;
        }
        passSeason = PASS_SEASON;
        proPassOwned = false;
        proPassLevel = 1;
        for (int i = 1; i < proPassRewardsClaimed.length; i++) {
            proPassRewardsClaimed[i] = false;
        }
    }

    private void ensureInventorySeededFromOwnedSkins() {
        if (!inventoryItems.isEmpty()) {
            return;
        }
        for (int i = 0; i < skinOwned.length; i++) {
            if (skinOwned[i]) {
                inventoryItems.add(new InventoryItem(INVENTORY_ITEM_SKIN, i));
            }
        }
    }

    private String encodeInventoryItems() {
        StringBuilder builder = new StringBuilder();
        for (InventoryItem item : inventoryItems) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(item.type).append('_').append(item.value);
        }
        return builder.toString();
    }

    private void loadInventoryItems(String value) {
        inventoryItems.clear();
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        String[] parts = value.split(";");
        for (String part : parts) {
            String[] fields = part.split("_", -1);
            if (fields.length != 2) {
                continue;
            }
            try {
                int type = Integer.parseInt(fields[0].trim());
                int itemValue = Integer.parseInt(fields[1].trim());
                if (type == INVENTORY_ITEM_CASE) {
                    inventoryItems.add(new InventoryItem(type, clampInt(itemValue, 0, CASE_COSTS.length - 1)));
                } else if (type == INVENTORY_ITEM_SKIN) {
                    inventoryItems.add(new InventoryItem(type, clampInt(itemValue, 0, TOTAL_SKINS - 1)));
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void loadFriendIds(String value) {
        friendIds.clear();
        if (value == null || value.isEmpty()) {
            return;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            String id = part.trim();
            if (!id.isEmpty() && friendIds.size() < 20) {
                friendIds.add(id);
            }
        }
    }

    private void loadFriendRequestIds(String value) {
        friendRequestIds.clear();
        if (value == null || value.isEmpty()) {
            return;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            String id = part.trim();
            if (!id.isEmpty() && !friendIds.contains(id) && friendRequestIds.size() < 30) {
                friendRequestIds.add(id);
            }
        }
    }

    private String joinFriendIds() {
        StringBuilder builder = new StringBuilder();
        for (String id : friendIds) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id);
        }
        return builder.toString();
    }

    private String joinFriendRequestIds() {
        StringBuilder builder = new StringBuilder();
        for (String id : friendRequestIds) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id);
        }
        return builder.toString();
    }

    private static int[] buildSkinCosts() {
        int[] costs = new int[TOTAL_SKINS];
        costs[0] = 0;
        costs[1] = 850;
        costs[2] = 1_400;
        for (int i = CASE_SKIN_START; i < TOTAL_SKINS; i++) {
            costs[i] = 1_200 + (i - CASE_SKIN_START) * 260;
        }
        costs[PASS_SKIN_1F51] = 50_000;
        costs[PASS_SKIN_1P25] = 50_000;
        costs[PASS_SKIN_1P50] = 50_000;
        return costs;
    }

    private static int[] buildSkinColors() {
        int[] colors = new int[TOTAL_SKINS];
        colors[0] = Color.rgb(100, 220, 80);
        colors[1] = Color.rgb(78, 221, 255);
        colors[2] = Color.rgb(255, 96, 54);
        colors[PASS_SKIN_1F51] = Color.rgb(255, 220, 86);
        colors[PASS_SKIN_1P25] = Color.rgb(196, 120, 255);
        colors[PASS_SKIN_1P50] = Color.rgb(255, 86, 143);
        for (int i = 0; i < CASE_SKIN_COUNT; i++) {
            float hue = (i * 137.5f) % 360f;
            float saturation = 0.58f + (i % 5) * 0.07f;
            float value = 0.78f + (i % 4) * 0.04f;
            colors[CASE_SKIN_START + i] = Color.HSVToColor(new float[]{hue, Math.min(0.92f, saturation), Math.min(0.96f, value)});
        }
        return colors;
    }

    private static String[] buildSkinNames() {
        String[] names = new String[TOTAL_SKINS];
        names[0] = "лайм";
        names[1] = "небо";
        names[2] = "лава";
        names[PASS_SKIN_1F51] = "1f51";
        names[PASS_SKIN_1P25] = "1p25";
        names[PASS_SKIN_1P50] = "1p50";
        for (int i = 0; i < CASE_SKIN_COUNT; i++) {
            names[CASE_SKIN_START + i] = String.format(Locale.US, "color %02d", i + 1);
        }
        return names;
    }

    private static int[] buildDefaultSalePrices() {
        int[] prices = new int[TOTAL_SKINS];
        prices[0] = 0;
        prices[1] = 650;
        prices[2] = 800;
        prices[PASS_SKIN_1F51] = 50_000;
        prices[PASS_SKIN_1P25] = 50_000;
        prices[PASS_SKIN_1P50] = 50_000;
        for (int i = CASE_SKIN_START; i < TOTAL_SKINS; i++) {
            prices[i] = 1_200 + (i - CASE_SKIN_START) * 260;
        }
        return prices;
    }

    private static long currentDay() {
        return System.currentTimeMillis() / DAY_MS;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float distanceSq(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return dx * dx + dy * dy;
    }

    private static RectF inset(RectF source, float amount) {
        return new RectF(source.left + amount, source.top + amount, source.right - amount, source.bottom - amount);
    }

    private float fitTextSize(String text, float maxSize, float maxWidth, Typeface typeface) {
        paint.setTypeface(typeface);
        paint.setTextSize(maxSize);
        while (paint.measureText(text) > maxWidth && maxSize > 12f) {
            maxSize -= 1f;
            paint.setTextSize(maxSize);
        }
        return maxSize;
    }

    private static String limit(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static String jsonEscape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String encodeIntArray(int[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values[i]);
        }
        return builder.toString();
    }

    private static String encodeBooleanArray(boolean[] values) {
        StringBuilder builder = new StringBuilder(values.length);
        for (boolean value : values) {
            builder.append(value ? '1' : '0');
        }
        return builder.toString();
    }

    private static void applyIntArray(String encoded, int[] target, int min, int max) {
        if (encoded == null || encoded.trim().isEmpty()) {
            return;
        }
        String[] parts = encoded.split(",");
        int count = Math.min(parts.length, target.length);
        for (int i = 0; i < count; i++) {
            try {
                target[i] = clampInt(Integer.parseInt(parts[i].trim()), min, max);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static void applyBooleanArray(String encoded, boolean[] target) {
        if (encoded == null || encoded.trim().isEmpty()) {
            return;
        }
        if (encoded.indexOf(',') >= 0) {
            String[] parts = encoded.split(",");
            int count = Math.min(parts.length, target.length);
            for (int i = 0; i < count; i++) {
                target[i] = "1".equals(parts[i].trim()) || "true".equals(parts[i].trim());
            }
            return;
        }
        int count = Math.min(encoded.length(), target.length);
        for (int i = 0; i < count; i++) {
            target[i] = encoded.charAt(i) == '1';
        }
    }

    private static int readJsonInt(String json, String key, int fallback) {
        String raw = readJsonRaw(json, key);
        if (raw.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static float readJsonFloat(String json, String key, float fallback) {
        String raw = readJsonRaw(json, key);
        if (raw.isEmpty()) {
            return fallback;
        }
        try {
            return Float.parseFloat(raw);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean readJsonBoolean(String json, String key, boolean fallback) {
        String raw = readJsonRaw(json, key);
        if ("true".equals(raw)) {
            return true;
        }
        if ("false".equals(raw)) {
            return false;
        }
        return fallback;
    }

    private static String readJsonRaw(String json, String key) {
        String needle = "\"" + key + "\"";
        int index = json.indexOf(needle);
        if (index < 0) {
            return "";
        }
        int colon = json.indexOf(':', index + needle.length());
        if (colon < 0) {
            return "";
        }
        int end = colon + 1;
        while (end < json.length() && Character.isWhitespace(json.charAt(end))) {
            end++;
        }
        int start = end;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}') {
                break;
            }
            end++;
        }
        return json.substring(start, end).trim();
    }

    private static int findMatchingArrayEnd(String json, int arrayStart) {
        if (arrayStart < 0 || arrayStart >= json.length() || json.charAt(arrayStart) != '[') {
            return -1;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = arrayStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = inString;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String readJsonString(String json, String key, String fallback) {
        String needle = "\"" + key + "\"";
        int index = json.indexOf(needle);
        if (index < 0) {
            return fallback;
        }
        int colon = json.indexOf(':', index + needle.length());
        int startQuote = colon < 0 ? -1 : json.indexOf('"', colon + 1);
        if (startQuote < 0) {
            return fallback;
        }
        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                builder.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return builder.toString();
            } else {
                builder.append(c);
            }
        }
        return fallback;
    }

    private static String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    private static final class LeaderboardEntry {
        private final String id;
        private String name;
        private int dna;
        private int level;
        private boolean self;

        private LeaderboardEntry(String id, String name, int dna, int level, boolean self) {
            this.id = id == null ? "" : id;
            this.name = name == null || name.trim().isEmpty() ? "player" : name.trim();
            this.dna = Math.max(0, dna);
            this.level = Math.max(1, level);
            this.self = self;
        }
    }

    private static final class WorldBoss {
        private boolean active;
        private float x;
        private float y;
        private float moveX;
        private float moveY;
        private float angle;
        private int dna;
        private long hitCooldownMs;
        private long nextBlockPushMs;
        private long nextBorderMoveMs;
    }

    private static final class BossDnaOrb {
        private float x;
        private float y;
        private int amount;
        private boolean active;
    }

    private static final class GameParticle {
        private final float x;
        private final float y;
        private final float vx;
        private final float vy;
        private final float gravity;
        private final float size;
        private final int color;
        private final long birthMs;
        private final long lifeMs;
        private final boolean screenSpace;
        private final boolean square;
        private float spin;

        private GameParticle(float x, float y, float vx, float vy, float gravity, float size,
                             int color, long birthMs, long lifeMs, boolean screenSpace, boolean square) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.gravity = gravity;
            this.size = size;
            this.color = color;
            this.birthMs = birthMs;
            this.lifeMs = lifeMs;
            this.screenSpace = screenSpace;
            this.square = square;
        }
    }

    private static final class ServerDnaPickup {
        private String id = "";
        private float x;
        private float y;
        private int value = 1;
        private float radius = 20f;
        private float zoneRadius = 20f;
        private String kind = "normal";
        private boolean active = true;
    }

    private static final class CellOpponent {
        private String id = "";
        private String name = "cell";
        private float x;
        private float y;
        private float moveX;
        private float moveY;
        private float angle;
        private float hp = 100f;
        private int dna;
        private int color;
        private int geneSpeed;
        private int geneMass;
        private int geneSpikes;
        private long nextTurnMs;
        private long hitCooldownMs;
        private boolean serverControlled;
        private int skin;
        private long lastSeenMs;
        private long speedBoostUntilMs;
        private long dnaBoostUntilMs;
    }

    private static final class PowerUpPickup {
        private final float x;
        private final float y;
        private final int type;
        private boolean active = true;

        private PowerUpPickup(float x, float y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

    private static final class MarketListing {
        private final int id;
        private final int skinIndex;
        private final int price;
        private final String ownerId;
        private final String owner;

        private MarketListing(int id, int skinIndex, int price, String ownerId, String owner) {
            this.id = id;
            this.skinIndex = skinIndex;
            this.price = price;
            this.ownerId = ownerId == null ? "" : ownerId;
            this.owner = owner == null || owner.trim().isEmpty() ? "player" : owner.trim();
        }
    }

    private static final class InventoryItem {
        private final int type;
        private final int value;

        private InventoryItem(int type, int value) {
            this.type = type;
            this.value = value;
        }
    }

    private enum Screen {
        AUTH,
        MAIN,
        PASS,
        REWARDS,
        PROFILE,
        SHOP,
        LEADERBOARD,
        EDITOR,
        MUSIC,
        SETTINGS,
        GAME
    }

    private enum FocusField {
        NONE,
        USERNAME,
        EMAIL,
        PASSWORD,
        FRIEND,
        CHAT
    }
}
