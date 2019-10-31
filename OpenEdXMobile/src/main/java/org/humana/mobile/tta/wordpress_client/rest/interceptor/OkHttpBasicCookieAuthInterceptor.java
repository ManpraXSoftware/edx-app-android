package org.humana.mobile.tta.wordpress_client.rest.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static org.humana.mobile.util.BrowserUtil.loginAPI;

/**
 * Created by JARVICE on 24-11-2017.
 */

public class OkHttpBasicCookieAuthInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .addHeader("Cookie", loginAPI.getConnectCookies())
                .build();
        return chain.proceed(request);
    }
}
