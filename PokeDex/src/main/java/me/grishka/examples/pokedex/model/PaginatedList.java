package me.grishka.examples.pokedex.model;

import java.util.List;

import me.grishka.examples.pokedex.api.ObjectValidationException;
import me.grishka.examples.pokedex.api.RequiredField;

public class PaginatedList<T extends BaseModel> extends BaseModel {
    @RequiredField
    public List<T> results;
    public String next;
    public String previous;
    public int count;

    @Override
    public void postprocess() throws ObjectValidationException {
        super.postprocess();
        for (T result : results) {
            result.postprocess();
        }
    }
}
