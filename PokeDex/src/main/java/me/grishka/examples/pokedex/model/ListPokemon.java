package me.grishka.examples.pokedex.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

import org.parceler.Parcel;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.examples.pokedex.api.AllFieldsAreRequired;
import me.grishka.examples.pokedex.api.ObjectValidationException;
import me.grishka.examples.pokedex.util.PaletteGenerationImageProcessingStep;

@Parcel
@AllFieldsAreRequired
public class ListPokemon extends BaseModel {
    public String name;
    public String url;
    public transient int index;
    public transient ImageLoaderRequest imgRequest;

    public ListPokemon() {
    }

    public ListPokemon(Cursor cursor) {
        name = cursor.getString(1);
        url = cursor.getString(2);
    }

    @Override
    public void postprocess() throws ObjectValidationException {
        super.postprocess();
        name = Arrays.stream(name.split("_"))
                .map(part -> {
                    if (part.isEmpty())
                        return part;
                    return Character.toUpperCase(part.charAt(0)) + part.substring(1);
                })
                .collect(Collectors.joining(" "));
        String[] urlParts = url.split(Pattern.quote("/"));
        index = Integer.parseInt(urlParts[urlParts.length - 1]);
        // ARGB_8888 because we need to be able to access the pixels for the palette thing
        imgRequest = new UrlImageLoaderRequest(Bitmap.Config.ARGB_8888, 0, 0, List.of(PaletteGenerationImageProcessingStep.getInstance()),
                Uri.parse("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + index + ".png"));
    }

    public void toContentValues(ContentValues values) {
        values.put("id", index);
        values.put("name", name);
        values.put("url", url);
    }
}
