package me.grishka.examples.pokedex;

import android.os.Bundle;

import androidx.annotation.Nullable;
import me.grishka.appkit.FragmentStackActivity;
import me.grishka.examples.pokedex.fragments.PokemonListFragment;

public class MainActivity extends FragmentStackActivity{
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(savedInstanceState==null){
			showFragment(new PokemonListFragment());
		}
	}
}
