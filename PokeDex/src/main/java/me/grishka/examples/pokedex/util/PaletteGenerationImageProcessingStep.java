package me.grishka.examples.pokedex.util;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import me.grishka.appkit.imageloader.processing.ImageProcessingStep;

public class PaletteGenerationImageProcessingStep extends ImageProcessingStep{
	private static final PaletteGenerationImageProcessingStep INSTANCE=new PaletteGenerationImageProcessingStep();

	public static PaletteGenerationImageProcessingStep getInstance(){
		return INSTANCE;
	}

	private PaletteGenerationImageProcessingStep(){
		//no instance
	}

	@Override
	public Drawable processDrawable(Drawable drawable){
		if(drawable instanceof BitmapDrawable bd){
			return new BitmapDrawableWithPalette(bd);
		}
		return null;
	}

	@Override
	public String getMemoryCacheKey(){
		return "palette";
	}
}
