package me.grishka.examples.pokedex.api.requests;

import me.grishka.examples.pokedex.api.PokeAPIRequest;
import me.grishka.examples.pokedex.model.PokemonDetailsResponse;

public class GetPokemonDetails extends PokeAPIRequest<PokemonDetailsResponse> {
    public GetPokemonDetails(String url) {
        super(PokemonDetailsResponse.class);
        this.url = url;
    }
}
