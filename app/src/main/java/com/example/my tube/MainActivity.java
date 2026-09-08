package com.example.mytube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private static final String BRAVE_SEARCH_URL = "https://search.brave.com/search?q=%s+music";
    private static final String YOUTUBE_MUSIC_URL = "https://m.youtube.com/results?search_query=%s";

    private EditText searchInput;
    private Button braveSearchButton;
    private Button youtubeSearchButton;
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchInput = findViewById(R.id.searchInput);
        braveSearchButton = findViewById(R.id.searchButton);
        youtubeSearchButton = findViewById(R.id.youtubeButton);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        setupWebView();
        setupActions();
        setupBackNavigation();

        // Load Brave Music search homepage by default for a better first screen.
        webView.loadUrl(String.format(BRAVE_SEARCH_URL, "popular"));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Use a mobile-like user agent to avoid "unsupported browser" warnings
        String userAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
        settings.setUserAgentString(userAgent);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false; // Let WebView load the page
                }
                return true; // Block other schemes to prevent crashes
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void setupActions() {
        braveSearchButton.setOnClickListener(v -> searchWithBrave());
        youtubeSearchButton.setOnClickListener(v -> searchWithYouTube());

        // Pressing "search" on keyboard triggers Brave search.
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean isKeyboardSearch = actionId == EditorInfo.IME_ACTION_SEARCH;
            boolean isEnterKey = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (isKeyboardSearch || isEnterKey) {
                searchWithBrave();
                return true;
            }
            return false;
        });
    }

    private void searchWithBrave() {
        loadSearchUrl(BRAVE_SEARCH_URL);
    }

    private void searchWithYouTube() {
        loadSearchUrl(YOUTUBE_MUSIC_URL);
    }

    private void loadSearchUrl(String template) {
        String rawQuery = searchInput.getText().toString().trim();
        if (rawQuery.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_search_error), Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide keyboard after starting search
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }

        if (!isInternetAvailable()) {
            Toast.makeText(this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String encodedQuery = URLEncoder.encode(rawQuery, "UTF-8");
            String targetUrl = String.format(template, encodedQuery);
            webView.loadUrl(targetUrl);
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, getString(R.string.search_failed_error), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        android.net.Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                    return;
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
