package me.grishka.examples.pokedex.model;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class PokemonDetails {
    public int index;
    public int weight;
    public int height;
    public EnumSet<PokemonType> types;
    public int health;
    public int attack;
    public int defense;
    public int specialAttack;
    public int specialDefense;
    public int speed;

    public PokemonDetails(Cursor cursor) {
        index = cursor.getInt(0);
        weight = cursor.getInt(1);
        height = cursor.getInt(2);
        int typesMask = cursor.getInt(3);
        types = EnumSet.noneOf(PokemonType.class);
        for (PokemonType type : PokemonType.values()) {
            if ((typesMask & (1 << type.ordinal())) != 0)
                types.add(type);
        }
        health = cursor.getInt(4);
        attack = cursor.getInt(5);
        defense = cursor.getInt(6);
        specialAttack = cursor.getInt(7);
        specialDefense = cursor.getInt(8);
        speed = cursor.getInt(9);
    }

    public PokemonDetails(PokemonDetailsResponse resp) {
        index = resp.id;
        weight = resp.weight;
        height = resp.height;
        types = resp.types.stream()
                .map(t -> {
                    try {
                        return PokemonType.valueOf(t.type.name.toUpperCase(Locale.US));
                    } catch (IllegalArgumentException x) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PokemonType.class)));
        for (PokemonDetailsResponse.Stat stat : resp.stats) {
            int value = stat.baseStat;
            switch (stat.stat.name) {
                case "hp" -> health = value;
                case "attack" -> attack = value;
                case "defense" -> defense = value;
                case "special-attack" -> specialAttack = value;
                case "special-defense" -> specialDefense = value;
                case "speed" -> speed = value;
            }
        }
    }

    public void toContentValues(ContentValues values) {
        values.put("id", index);
        values.put("weight", weight);
        values.put("height", height);
        int typesMask = 0;
        for (PokemonType type : types) {
            typesMask |= 1 << type.ordinal();
        }
        values.put("types", typesMask);
        values.put("health", health);
        values.put("attack", attack);
        values.put("defense", defense);
        values.put("special_attack", specialAttack);
        values.put("special_defense", specialDefense);
        values.put("speed", speed);
    }
}
