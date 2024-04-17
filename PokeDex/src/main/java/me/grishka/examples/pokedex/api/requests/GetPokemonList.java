package me.grishka.examples.pokedex.api.requests;

import android.net.Uri;

import com.google.gson.reflect.TypeToken;

import me.grishka.examples.pokedex.api.PokeAPIRequest;
import me.grishka.examples.pokedex.model.ListPokemon;
import me.grishka.examples.pokedex.model.PaginatedList;

public class GetPokemonList extends PokeAPIRequest<PaginatedList<ListPokemon>> {
    public GetPokemonList(int offset, int count) {
        super(new TypeToken<>() {
        });
        url = new Uri.Builder()
                .scheme("https")
                .authority("pokeapi.co")
                .path("/api/v2/pokemon")
                .appendQueryParameter("offset", String.valueOf(offset))
                .appendQueryParameter("limit", String.valueOf(count))
                .build()
                .toString();
    }
}
