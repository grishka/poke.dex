package me.grishka.examples.pokedex.api;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

import me.grishka.appkit.utils.WorkerThread;
import me.grishka.examples.pokedex.BuildConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PokeAPIController{
	private static final String TAG="PokeAPIController";

	public static final Gson gson=new GsonBuilder()
			.disableHtmlEscaping()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.create();
	private static WorkerThread thread=new WorkerThread("PokeAPIController");
	private static OkHttpClient httpClient=new OkHttpClient.Builder()
			.connectTimeout(60, TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.build();
	private static final PokeAPIController instance=new PokeAPIController();

	static{
		thread.start();
	}

	public static PokeAPIController getInstance(){
		return instance;
	}

	public <T> void submitRequest(PokeAPIRequest<T> req){
		thread.postRunnable(()->{
			try{
				Request hReq=new Request.Builder()
						.url(req.url)
						.addHeader("User-Agent", "poke.dex/"+BuildConfig.VERSION_NAME)
						.build();

				Call call=httpClient.newCall(hReq);
				req.okhttpCall=call;
				call.enqueue(new Callback(){
					@Override
					public void onFailure(Call call, IOException e){
						req.okhttpCall=null;
						req.onError(e.getMessage(), -1, e);
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException{
						req.okhttpCall=null;
						if(req.canceled)
							return;
						T respObj;
						try(ResponseBody body=response.body()){
							Reader reader=body.charStream();
							try{
								if(req.respTypeToken!=null)
									respObj=gson.fromJson(reader, req.respTypeToken.getType());
								else if(req.respClass!=null)
									respObj=gson.fromJson(reader, req.respClass);
								else
									respObj=null;
							}catch(JsonIOException|JsonSyntaxException x){
								if(BuildConfig.DEBUG)
									Log.w(TAG, response+" error parsing or reading body", x);
								req.onError(x.getLocalizedMessage(), response.code(), x);
								return;
							}

							try{
								req.validateAndPostprocessResponse(respObj, response);
							}catch(IOException x){
								if(BuildConfig.DEBUG)
									Log.w(TAG, response+" error post-processing or validating response", x);
								req.onError(x.getLocalizedMessage(), response.code(), x);
								return;
							}

							if(BuildConfig.DEBUG)
								Log.d(TAG, response+" parsed successfully: "+respObj);

							req.onSuccess(respObj);
						}catch(Exception x){
							Log.w(TAG, "onResponse: error processing response", x);
							req.onError(x.getMessage(), -1, x);
						}
					}
				});
			}catch(Throwable x){
				Log.w(TAG, "Request "+req+" failed", x);
				req.onError(x.getLocalizedMessage(), 0, x);
			}
		}, 0);
	}
}
