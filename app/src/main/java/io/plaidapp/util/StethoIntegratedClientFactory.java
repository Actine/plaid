package io.plaidapp.util;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;
import retrofit.client.OkClient;

/**
 * <p></p>
 *
 * @author Paul Danyliuk
 */
public abstract class StethoIntegratedClientFactory {

    public static OkHttpClient makeClient() {
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new StethoInterceptor());
        return client;
    }

    public static OkClient makeClientForRetrofit() {
        return new OkClient(makeClient());
    }

}
