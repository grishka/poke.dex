package me.grishka.examples.pokedex.api;

import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import androidx.annotation.CallSuper;
import me.grishka.appkit.api.APIRequest;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.examples.pokedex.BuildConfig;
import me.grishka.examples.pokedex.model.BaseModel;
import okhttp3.Call;
import okhttp3.Response;

public class PokeAPIRequest<T> extends APIRequest<T> {
    private static final String TAG = "PokeAPIRequest";
    public String url;

    boolean canceled;
    Call okhttpCall;
    Class<T> respClass;
    TypeToken<T> respTypeToken;

    public PokeAPIRequest(Class<T> respClass) {
        this.respClass = respClass;
    }

    public PokeAPIRequest(TypeToken<T> respTypeToken) {
        this.respTypeToken = respTypeToken;
    }

    @Override
    public synchronized void cancel() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "canceling request " + this);
        canceled = true;
        if (okhttpCall != null) {
            okhttpCall.cancel();
        }
    }

    @Override
    public APIRequest<T> exec() {
        PokeAPIController.getInstance().submitRequest(this);
        return this;
    }

    @CallSuper
    public void validateAndPostprocessResponse(T respObj, Response httpResponse) throws IOException {
        if (respObj instanceof BaseModel bm) {
            bm.postprocess();
        } else if (respObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof BaseModel bm)
                    bm.postprocess();
            }
        }
    }

    void onError(ErrorResponse err) {
        if (!canceled)
            invokeErrorCallback(err);
    }

    void onError(String msg, int httpStatus, Throwable exception) {
        if (!canceled)
            invokeErrorCallback(new PokeAPIErrorResponse(msg, httpStatus, exception));
    }

    void onSuccess(T resp) {
        if (!canceled)
            invokeSuccessCallback(resp);
    }
}
