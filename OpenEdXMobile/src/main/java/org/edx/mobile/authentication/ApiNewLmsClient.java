package org.edx.mobile.authentication;

import com.google.inject.Inject;

import org.edx.mobile.util.Config;

import java.util.Collections;

import javax.inject.Singleton;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Singleton
public class ApiNewLmsClient {

    private static Retrofit retrofit;

    private Config config;

    @Inject
    public ApiNewLmsClient(Config config) {
        this.config = config;
    }

    public Retrofit getClient() {
        String clientBase = config.getApiHostURL();
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(loggingInterceptor);
            httpClient.protocols(Collections.singletonList(Protocol.HTTP_1_1));
            retrofit = new Retrofit.Builder()
                    .baseUrl(clientBase)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}
