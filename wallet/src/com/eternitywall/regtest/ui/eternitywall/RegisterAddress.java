package com.eternitywall.regtest.ui.eternitywall;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.pm.PackageInfo;

import com.eternitywall.regtest.Constants;
import com.eternitywall.regtest.WalletApplication;
import com.eternitywall.regtest.eternitywall.BitcoinEW;
import com.eternitywall.regtest.ui.WalletActivity;
import com.google.common.base.Stopwatch;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.bitcoinj.core.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

class RegisterAddress extends AsyncTaskLoader<Boolean> {

    private final HttpUrl url;
    private final String userAgent;
    private final Logger log = LoggerFactory.getLogger(WalletActivity.class);
    private final String URL = BitcoinEW.EW_RECHARGE_URL;

    public RegisterAddress(final Context context, String phoneNumber) {
        super(context);
        final PackageInfo packageInfo = WalletApplication.packageInfoFromContext(context);
        final String url = URL + "/" + phoneNumber;
        this.url = HttpUrl.parse(url);
        log.info("calling " + url);
        this.userAgent = WalletApplication.httpUserAgent(packageInfo.versionName);
    }


    @Override
    public Boolean loadInBackground() {
        try {
            final Stopwatch watch = Stopwatch.createStarted();
            final Request.Builder request = new Request.Builder();
            request.url(url);
            request.header("User-Agent", userAgent);

            final OkHttpClient httpClient = Constants.HTTP_CLIENT.clone();
            httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
            httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
            httpClient.setReadTimeout(5, TimeUnit.SECONDS);
            final Call call = httpClient.newCall(request.build());
            try {
                final Response response = call.execute();
                final int status = response.code();
                if (status == HttpURLConnection.HTTP_OK) {
                    final ResponseBody body = response.body();

                    BufferedReader br = new BufferedReader(new InputStreamReader(body.byteStream(), "utf-8"));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    log.info("" + sb.toString());

                    watch.stop();
                    log.info("called {}, took {}", url, watch);

                    return true;
                } else {
                    log.warn("HTTP status {} when fetching dynamic fees from {}", response.code(), url);
                }
            } catch (final Exception x) {
                log.warn("Problem when fetching dynamic fees rates from " + url, x);
            }

        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return false;
        }
        return false;
    }


    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

}