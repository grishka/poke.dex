package me.grishka.examples.pokedex.api.caching;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.utils.WorkerThread;
import me.grishka.examples.pokedex.BuildConfig;
import me.grishka.examples.pokedex.PokeDexApplication;
import me.grishka.examples.pokedex.api.PokeAPIErrorResponse;
import me.grishka.examples.pokedex.api.requests.GetPokemonDetails;
import me.grishka.examples.pokedex.api.requests.GetPokemonList;
import me.grishka.examples.pokedex.model.ListPokemon;
import me.grishka.examples.pokedex.model.PaginatedList;
import me.grishka.examples.pokedex.model.PokemonDetails;
import me.grishka.examples.pokedex.model.PokemonDetailsResponse;

public class PokemonCache {
    private static final String TAG = "PokemonCache";
    private static final int SCHEMA_VERSION = 1;
    private static final WorkerThread databaseThread = new WorkerThread("databaseThread");
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final PokemonCache INSTANCE = new PokemonCache();

    static {
        databaseThread.start();
    }

    private DatabaseHelper db;
    private final Runnable databaseCloseRunnable = this::closeDatabase;

    private PokemonCache() {
        //no instance
    }

    public static PokemonCache getInstance() {
        return INSTANCE;
    }

    public void getList(int offset, int count, boolean clearCache, Callback<PaginatedList<ListPokemon>> callback) {
        runOnDbThread(db -> {
            if (clearCache) {
                db.delete("pokemon_list", null, null);
                db.delete("pokemon_details", null, null);
            }
            try (Cursor cursor = db.query("pokemon_list", null, null, null, null, null, "id ASC", offset + "," + count)) {
                if (cursor.moveToFirst()) {
                    ArrayList<ListPokemon> list = new ArrayList<>();
                    do {
                        list.add(new ListPokemon(cursor));
                    } while (cursor.moveToNext());
                    PaginatedList<ListPokemon> res = new PaginatedList<>();
                    res.next = "fake next url whatever absolute urls in api responses are stupid";
                    res.results = list;
                    uiHandler.post(() -> callback.onSuccess(res));
                    return;
                }
            }
            new GetPokemonList(offset, count)
                    .setCallback(new Callback<>() {
                        @Override
                        public void onSuccess(PaginatedList<ListPokemon> resp) {
                            callback.onSuccess(resp);
                            runOnDbThread(db -> {
                                ContentValues values = new ContentValues();
                                for (ListPokemon lp : resp.results) {
                                    lp.toContentValues(values);
                                    db.insertWithOnConflict("pokemon_list", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                }
                            });
                        }

                        @Override
                        public void onError(ErrorResponse err) {
                            callback.onError(err);
                        }
                    })
                    .exec();
        }, x -> uiHandler.post(() -> callback.onError(new PokeAPIErrorResponse(x.getMessage(), -1, x))));
    }

    public void getDetails(ListPokemon pokemon, Callback<PokemonDetails> callback) {
        runOnDbThread(db -> {
            try (Cursor cursor = db.query("pokemon_details", null, "id=?", new String[]{String.valueOf(pokemon.index)}, null, null, null)) {
                if (cursor.moveToFirst()) {
                    PokemonDetails res = new PokemonDetails(cursor);
                    uiHandler.post(() -> callback.onSuccess(res));
                    return;
                }
            }
            new GetPokemonDetails(pokemon.url)
                    .setCallback(new Callback<>() {
                        @Override
                        public void onSuccess(PokemonDetailsResponse resp) {
                            PokemonDetails pd = new PokemonDetails(resp);
                            callback.onSuccess(pd);
                            runOnDbThread(db -> {
                                ContentValues values = new ContentValues();
                                pd.toContentValues(values);
                                db.insertWithOnConflict("pokemon_details", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                            });
                        }

                        @Override
                        public void onError(ErrorResponse err) {
                            callback.onError(err);
                        }
                    })
                    .exec();
        }, x -> uiHandler.post(() -> callback.onError(new PokeAPIErrorResponse(x.getMessage(), -1, x))));
    }

    private void closeDelayed() {
        databaseThread.postRunnable(databaseCloseRunnable, 10_000);
    }

    public void closeDatabase() {
        if (db != null) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "closeDatabase");
            db.close();
            db = null;
        }
    }

    private void cancelDelayedClose() {
        if (db != null) {
            databaseThread.handler.removeCallbacks(databaseCloseRunnable);
        }
    }

    private SQLiteDatabase getOrOpenDatabase() {
        if (db == null)
            db = new DatabaseHelper();
        return db.getWritableDatabase();
    }

    private void runOnDbThread(DatabaseRunnable r) {
        runOnDbThread(r, null);
    }

    private void runOnDbThread(DatabaseRunnable r, Consumer<Exception> onError) {
        cancelDelayedClose();
        databaseThread.postRunnable(() -> {
            try {
                SQLiteDatabase db = getOrOpenDatabase();
                r.run(db);
            } catch (SQLiteException | IOException x) {
                Log.w(TAG, x);
                if (onError != null)
                    onError.accept(x);
            } finally {
                closeDelayed();
            }
        }, 0);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper() {
            super(PokeDexApplication.context, "cache.db", null, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("""
                    CREATE TABLE `pokemon_list` (
                    	`id` integer PRIMARY KEY,
                    	`name` text NOT NULL,
                    	`url` text NOT NULL
                    )""");
            db.execSQL("""
                    CREATE TABLE `pokemon_details` (
                    	`id` integer PRIMARY KEY,
                    	`weight` integer NOT NULL,
                    	`height` integer NOT NULL,
                    	`types` integer NOT NULL,
                    	`health` integer NOT NULL,
                    	`attack` integer NOT NULL,
                    	`defense` integer NOT NULL,
                    	`special_attack` integer NOT NULL,
                    	`special_defense` integer NOT NULL,
                    	`speed` integer NOT NULL
                    )""");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
