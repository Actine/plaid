package io.plaidapp;

import android.app.Application;
import com.facebook.stetho.Stetho;

/**
 * <p></p>
 *
 * @author Paul Danyliuk
 */
public class StethoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
