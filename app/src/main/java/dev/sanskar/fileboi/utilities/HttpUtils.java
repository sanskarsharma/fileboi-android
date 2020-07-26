package dev.sanskar.fileboi.utilities;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

    public static Retrofit getRetrofitInstance (String baseUrl) {

        // initializing Gson serializer with custom config to serialize request and response data
        // here FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES means that Gson will always :
        // 1. convert request fields to LOWER_CASE_WITH_UNDERSCORES when sending to server
        // 2. map/serialize response fields to corresponding camelCase fields
        //      assuming server response always follows LOWER_CASE_WITH_UNDERSCORES as FieldNamingPolicy
        // this applies to nested objects too, in both cases i.e request data and response data.
        // this is phantastic.
        // for more sample config, see : https://futurestud.io/tutorials/retrofit-2-adding-customizing-the-gson-converter
        // or see the official Gson javadoc

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        // Set the custom client, converter (gson) and base url provided when building retrofit instance
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }
}
