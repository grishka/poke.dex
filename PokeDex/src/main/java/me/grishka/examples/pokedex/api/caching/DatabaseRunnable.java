package me.grishka.examples.pokedex.api.caching;

import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;

@FunctionalInterface
public interface DatabaseRunnable {
    void run(SQLiteDatabase db) throws IOException;
}
