package dev.sanskar.fileboi.utilities;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

public class HttpUtils {
    private static final int NETWORK_TIMEOUT_SEC = 60;

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
            .connectTimeout(NETWORK_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SEC, TimeUnit.SECONDS)
            .build();

    public static OkHttpClient getHttpClient() {
        return client;
    }
}
