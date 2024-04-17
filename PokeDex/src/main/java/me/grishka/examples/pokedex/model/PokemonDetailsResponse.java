package me.grishka.examples.pokedex.model;

import java.util.List;

import me.grishka.examples.pokedex.api.AllFieldsAreRequired;
import me.grishka.examples.pokedex.api.ObjectValidationException;
import me.grishka.examples.pokedex.api.RequiredField;

@AllFieldsAreRequired
public class PokemonDetailsResponse extends BaseModel {
    public int id;
    public int weight;
    public int height;
    public List<Stat> stats;
    public List<Type> types;

    @Override
    public void postprocess() throws ObjectValidationException {
        super.postprocess();
        for (Stat stat : stats) {
            stat.postprocess();
        }
        for (Type type : types) {
            type.postprocess();
        }
    }

    public static class Stat extends BaseModel {
        public int baseStat;
        public int effort;
        @RequiredField
        public StatRef stat;

        @Override
        public void postprocess() throws ObjectValidationException {
            super.postprocess();
            stat.postprocess();
        }
    }

    @AllFieldsAreRequired
    public static class StatRef extends BaseModel {
        public String name;
        public String url;
    }

    @AllFieldsAreRequired
    public static class Type extends BaseModel {
        public int slot;
        public TypeRef type;

        @Override
        public void postprocess() throws ObjectValidationException {
            super.postprocess();
            type.postprocess();
        }
    }

    @AllFieldsAreRequired
    public static class TypeRef extends BaseModel {
        public String name;
        public String url;
    }
}
