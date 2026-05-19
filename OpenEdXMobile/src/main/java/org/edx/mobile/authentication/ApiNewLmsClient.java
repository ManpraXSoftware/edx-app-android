package org.edx.mobile.authentication;

import com.google.inject.Inject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Singleton;
import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Singleton
public class ApiNewLmsClient {

    private static Retrofit retrofit;

    private String apiHostURL;

    @Inject
    public ApiNewLmsClient(String apiHostURL) {
        this.apiHostURL = apiHostURL;
    }
    private static Retrofit chatbotRetrofit;
    private String chatbotBaseUrl;
    private String token;

    @Inject
    public ApiNewLmsClient(String chatbotBaseUrl, String token) {
        this.chatbotBaseUrl = chatbotBaseUrl;
        this.token=token;
    }

    public Retrofit getChatbotClient() {
        String clientBase = chatbotBaseUrl;
        if (chatbotRetrofit == null) {
            OkHttpClient httpClient = buildHttpClient();
            chatbotRetrofit = new Retrofit.Builder()
                    .baseUrl(clientBase)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();
        }
        return chatbotRetrofit;
    }

    public Retrofit getClient() {
        String clientBase = apiHostURL;
        if (retrofit == null) {
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

            // Set timeouts
            httpClientBuilder.connectTimeout(10, TimeUnit.SECONDS);
            httpClientBuilder.readTimeout(30, TimeUnit.SECONDS);
            httpClientBuilder.writeTimeout(30, TimeUnit.SECONDS);

            // Add logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);

            // Disable SSL certificate verification (Only for testing!)
            try {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[]{};
                            }
                        }
                };

                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                httpClientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
                httpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Build OkHttpClient and Retrofit
            OkHttpClient httpClient = httpClientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(clientBase)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();
        }
        return retrofit;
    }

    private OkHttpClient buildHttpClient() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder.addInterceptor(loggingInterceptor);

        // SSL bypass for testing purposes
        disableSSLVerification(httpClientBuilder);

        return httpClientBuilder.build();
    }

    private void disableSSLVerification(OkHttpClient.Builder httpClientBuilder) {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            httpClientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            httpClientBuilder.hostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}