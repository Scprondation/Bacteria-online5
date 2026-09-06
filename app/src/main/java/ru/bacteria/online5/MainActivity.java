package ru.bacteria.online5;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private static final int AVATAR_PICK_REQUEST = 410;
    private static final String UPDATE_MANIFEST_URL = "http://31.207.76.76:5056/update.json";
    private BacteriaMenuView menuView;
    private long updateDownloadId = -1L;
    private BroadcastReceiver updateReceiver;
    private OnBackInvokedCallback backInvokedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        menuView = new BacteriaMenuView(this);
        setContentView(menuView);
        registerBackCallback();
        requestBestRefreshRate();
        checkForUpdate();
    }

    private void registerBackCallback() {
        if (Build.VERSION.SDK_INT < 33) {
            return;
        }
        backInvokedCallback = this::handleBackNavigation;
        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                backInvokedCallback
        );
    }

    private void requestBestRefreshRate() {
        float bestRefreshRate = 0f;
        if (Build.VERSION.SDK_INT >= 23) {
            Display display = getWindowManager().getDefaultDisplay();
            if (display != null) {
                for (Display.Mode mode : display.getSupportedModes()) {
                    bestRefreshRate = Math.max(bestRefreshRate, mode.getRefreshRate());
                }
            }
        }
        if (bestRefreshRate <= 0f) {
            return;
        }

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.preferredRefreshRate = bestRefreshRate;
        getWindow().setAttributes(params);

        if (Build.VERSION.SDK_INT >= 30 && menuView != null) {
            try {
                View.class
                        .getMethod("setFrameRate", float.class, int.class, int.class)
                        .invoke(menuView, bestRefreshRate, 0, 1);
            } catch (Exception ignored) {
            }
        }
    }

    private void checkForUpdate() {
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(UPDATE_MANIFEST_URL).openConnection();
                connection.setConnectTimeout(2500);
                connection.setReadTimeout(2500);
                StringBuilder json = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }
                }
                int versionCode = readInt(json.toString(), "versionCode", 1);
                String apkUrl = readString(json.toString(), "apkUrl", "");
                if (versionCode > currentVersionCode() && !apkUrl.isEmpty()) {
                    runOnUiThread(() -> downloadUpdate(apkUrl, versionCode));
                }
            } catch (Exception ignored) {
            }
        }, "bacteria-update-check").start();
    }

    public void chooseAvatar() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, AVATAR_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AVATAR_PICK_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null && menuView != null) {
            menuView.setAvatar(data.getData());
        }
    }

    private void downloadUpdate(String apkUrl, int versionCode) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle("Бактерии Онлайн 5");
        request.setDescription("Скачивание обновления");
        request.setMimeType("application/vnd.android.package-archive");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String fileName = "bacteria-online-5-update-" + versionCode + "-" + System.currentTimeMillis() + ".apk";
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) {
            return;
        }
        updateDownloadId = manager.enqueue(request);
        registerUpdateReceiver(manager);
    }

    private void registerUpdateReceiver(DownloadManager manager) {
        if (updateReceiver != null) {
            return;
        }
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
                if (id != updateDownloadId) {
                    return;
                }
                Uri apkUri = manager.getUriForDownloadedFile(updateDownloadId);
                if (apkUri == null) {
                    return;
                }
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(install);
            }
        };
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter);
        }
    }

    private int currentVersionCode() {
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                return (int) getPackageManager().getPackageInfo(getPackageName(), 0).getLongVersionCode();
            }
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception ignored) {
            return 1;
        }
    }

    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }

    private void handleBackNavigation() {
        if (menuView != null && menuView.handleBackPressed()) {
            return;
        }
        finish();
    }

    @Override
    protected void onPause() {
        if (menuView != null) {
            menuView.pauseAudio();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (menuView != null) {
            menuView.resumeAudio();
        }
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= 33 && backInvokedCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backInvokedCallback);
            backInvokedCallback = null;
        }
        if (updateReceiver != null) {
            unregisterReceiver(updateReceiver);
            updateReceiver = null;
        }
        super.onDestroy();
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
}
