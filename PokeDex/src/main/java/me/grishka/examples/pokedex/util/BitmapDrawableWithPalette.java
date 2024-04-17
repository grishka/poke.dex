package me.grishka.examples.pokedex.util;

import android.graphics.drawable.BitmapDrawable;

import androidx.palette.graphics.Palette;

public class BitmapDrawableWithPalette extends BitmapDrawable {
    public final Palette palette;

    public BitmapDrawableWithPalette(BitmapDrawable drawable) {
        super(drawable.getBitmap());
        palette = new Palette.Builder(getBitmap()).generate();
    }

    public int getCardBackgroundColor(boolean darkTheme) {
        if (palette != null) {
            Palette.Swatch swatch = darkTheme ? palette.getDarkMutedSwatch() : palette.getLightMutedSwatch();
            if (swatch != null)
                return swatch.getRgb();
        }
        return darkTheme ? 0xff000000 : 0xffffffff;
    }

    public int getCardTextColor(boolean darkTheme) {
        if (palette != null) {
            Palette.Swatch swatch = darkTheme ? palette.getDarkMutedSwatch() : palette.getLightMutedSwatch();
            if (swatch != null)
                return swatch.getTitleTextColor() | 0xff000000;
        }
        return darkTheme ? 0xffffffff : 0xff000000;
    }

    public int[] getGradientColors() {
        if (palette != null) {
            Palette.Swatch vibrantSwatch = palette.getVibrantSwatch(), darkVibrantSwatch = palette.getDarkVibrantSwatch(), dominantSwatch = palette.getDominantSwatch();
            if (vibrantSwatch != null && darkVibrantSwatch != null) {
                return new int[]{vibrantSwatch.getRgb(), darkVibrantSwatch.getRgb()};
            } else if (vibrantSwatch != null && dominantSwatch != null) {
                return new int[]{vibrantSwatch.getRgb(), dominantSwatch.getRgb()};
            } else if (dominantSwatch != null) {
                return new int[]{dominantSwatch.getRgb(), dominantSwatch.getRgb()};
            }
        }
        return new int[]{0xff000000, 0xff000000};
    }
}
