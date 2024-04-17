package me.grishka.examples.pokedex;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import me.grishka.appkit.imageloader.ImageCache;
import me.grishka.appkit.utils.NetworkUtils;
import me.grishka.appkit.utils.V;

public class PokeDexApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        V.setApplicationContext(context);
        ImageCache.Parameters params = new ImageCache.Parameters();
        params.diskCacheSize = 100 * 1024 * 1024;
        params.maxMemoryCacheSize = Integer.MAX_VALUE;
        ImageCache.setParams(params);
        NetworkUtils.setUserAgent("poke.dex/" + BuildConfig.VERSION_NAME);
    }
}
