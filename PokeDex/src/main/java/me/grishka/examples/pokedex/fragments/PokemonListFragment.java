package me.grishka.examples.pokedex.fragments;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;

import org.parceler.Parcels;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.V;
import me.grishka.appkit.views.UsableRecyclerView;
import me.grishka.examples.pokedex.R;
import me.grishka.examples.pokedex.api.caching.PokemonCache;
import me.grishka.examples.pokedex.model.ListPokemon;
import me.grishka.examples.pokedex.model.PaginatedList;
import me.grishka.examples.pokedex.util.BitmapDrawableWithPalette;

public class PokemonListFragment extends BaseRecyclerFragment<ListPokemon> {
    private GridLayoutManager layoutManager;
    private boolean isDarkTheme;

    public PokemonListFragment() {
        super(25);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setTitle(R.string.app_name);
        loadData();
    }

    @Override
    protected void doLoadData(int offset, int count) {
        PokemonCache.getInstance().getList(offset, count, refreshing, new SimpleCallback<>(this) {
            @Override
            public void onSuccess(PaginatedList<ListPokemon> result) {
                onDataLoaded(result.results, result.next != null);
            }
        });
    }

    @Override
    protected RecyclerView.Adapter<?> getAdapter() {
        return new PokemonAdapter();
    }

    @Override
    protected RecyclerView.LayoutManager onCreateLayoutManager() {
        return layoutManager = new GridLayoutManager(getActivity(), 2);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isDarkTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        UsableRecyclerView urv = (UsableRecyclerView) list;
        urv.setSelector(getResources().getDrawable(R.drawable.card_selector, getActivity().getTheme()));
        urv.setDrawSelectorOnTop(true);
        list.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final int padSmall = V.dp(6);
            private final int padLarge = V.dp(16);

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int index = parent.getChildViewHolder(view).getLayoutPosition();
                int spanCount = layoutManager.getSpanCount();
                int column = index % spanCount;
                outRect.set(column == 0 ? padLarge : padSmall, index < spanCount ? padLarge : padSmall, column == spanCount - 1 ? padLarge : padSmall, padSmall);
            }
        });
    }

    @Override
    public boolean wantsLightNavigationBar() {
        return !isDarkTheme;
    }

    @Override
    public void onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= 29 && insets.getTappableElementInsets().bottom == 0) {
            list.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            insets = insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), 0);
        }
        super.onApplyWindowInsets(insets);
    }

    private int getThemeColor(@AttrRes int attr) {
        TypedArray ta = getActivity().obtainStyledAttributes(new int[]{attr});
        int color = ta.getColor(0, 0xff00ff00);
        ta.recycle();
        return color;
    }

    private class PokemonAdapter extends UsableRecyclerView.Adapter<PokemonViewHolder> implements ImageLoaderRecyclerAdapter {
        public PokemonAdapter() {
            super(imgLoader);
        }

        @NonNull
        @Override
        public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new PokemonViewHolder();
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public void onBindViewHolder(PokemonViewHolder holder, int position) {
            holder.bind(data.get(position));
            super.onBindViewHolder(holder, position);
        }

        @Override
        public int getImageCountForItem(int pos) {
            return 1;
        }

        @Override
        public ImageLoaderRequest getImageRequest(int pos, int image) {
            return data.get(pos).imgRequest;
        }
    }

    private class PokemonViewHolder extends BindableViewHolder<ListPokemon> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable {
        private final TextView name;
        private final ImageView image;

        public PokemonViewHolder() {
            super(getActivity(), R.layout.item_pokemon, list);
            name = findViewById(R.id.name);
            image = findViewById(R.id.image);
        }

        @Override
        public void onBind(ListPokemon listPokemon) {
            name.setText(listPokemon.name);
        }

        @Override
        public void setImage(int index, Drawable drawable) {
            image.setImageDrawable(drawable);
            if (drawable instanceof BitmapDrawableWithPalette dwp) {
                itemView.setBackgroundTintList(ColorStateList.valueOf(dwp.getCardBackgroundColor(isDarkTheme)));
                name.setTextColor(dwp.getCardTextColor(isDarkTheme));
            } else {
                itemView.setBackgroundTintList(null);
                name.setTextColor(getThemeColor(android.R.attr.textColorPrimary));
            }
        }

        @Override
        public void onClick() {
            Bundle args = new Bundle();
            args.putParcelable("pokemon", Parcels.wrap(item));
            Nav.go(getActivity(), PokemonDetailsFragment.class, args);
        }
    }
}
