package me.grishka.examples.pokedex.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import org.parceler.Parcels;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.fragments.AppKitFragment;
import me.grishka.appkit.fragments.CustomTransitionsFragment;
import me.grishka.appkit.imageloader.ViewImageLoader;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.CubicBezierInterpolator;
import me.grishka.appkit.utils.V;
import me.grishka.examples.pokedex.R;
import me.grishka.examples.pokedex.api.caching.PokemonCache;
import me.grishka.examples.pokedex.model.ListPokemon;
import me.grishka.examples.pokedex.model.PokemonDetails;
import me.grishka.examples.pokedex.model.PokemonType;
import me.grishka.examples.pokedex.util.BitmapDrawableWithPalette;
import me.grishka.examples.pokedex.views.AnimatedLabeledProgressBar;

public class PokemonDetailsFragment extends AppKitFragment implements CustomTransitionsFragment {
    private int lastTopInset;
    private View view;
    private FrameLayout imageWrap;
    private ImageView image;
    private TextView name;
    private ProgressBar progress;
    private LinearLayout typesContainer;
    private View sizeStats;
    private TextView weight, height;
    private View stats;
    private AnimatedLabeledProgressBar hpBar, attackBar, defenseBar, specialAttachBar, specialDefenseBar, speedBar;
    private ScrollView scroller;
    private View scrollableContent;
    private Animator currentTransition;
    private int headerRadius;

    private ListPokemon pokemon;
    private PokemonDetails details;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pokemon = Parcels.unwrap(getArguments().getParcelable("pokemon"));
        try {
            pokemon.postprocess();
        } catch (IOException ignore) {
        }
        setTitle(pokemon.name);
        loadDetails();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_details, container, false);
        imageWrap = view.findViewById(R.id.image_wrap);
        image = view.findViewById(R.id.image);
        name = view.findViewById(R.id.name);
        progress = view.findViewById(R.id.progress);
        typesContainer = view.findViewById(R.id.types);
        weight = view.findViewById(R.id.weight);
        height = view.findViewById(R.id.height);
        sizeStats = view.findViewById(R.id.size_stats);
        stats = view.findViewById(R.id.stats);
        hpBar = view.findViewById(R.id.hp_bar);
        attackBar = view.findViewById(R.id.attack_bar);
        defenseBar = view.findViewById(R.id.defense_bar);
        specialAttachBar = view.findViewById(R.id.special_attack_bar);
        specialDefenseBar = view.findViewById(R.id.special_defense_bar);
        speedBar = view.findViewById(R.id.speed_bar);
        scroller = view.findViewById(R.id.scroller);
        scrollableContent = view.findViewById(R.id.scrollable_content);

        headerRadius = V.dp(64);
        imageWrap.setBackgroundColor(0xff000000);
        imageWrap.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, -headerRadius, view.getWidth(), view.getHeight(), headerRadius);
            }
        });
        imageWrap.setClipToOutline(true);

        ViewImageLoader.load(new ViewImageLoader.Target() {
            @Override
            public void setImageDrawable(Drawable drawable) {
                image.setImageDrawable(drawable);
                if (drawable instanceof BitmapDrawableWithPalette bwp) {
                    imageWrap.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, bwp.getGradientColors()));
                }
            }

            @Override
            public View getView() {
                return image;
            }
        }, null, pokemon.imgRequest, false);
        name.setText(pokemon.name);

        scroller.getViewTreeObserver().addOnScrollChangedListener(() -> {
            Toolbar toolbar = getToolbar();
            if (toolbar == null)
                return;
            int scrollY = scroller.getScrollY();
            int defaultRadius = V.dp(64);
            int headerHeight = imageWrap.getHeight();
            int topBarsHeight = toolbar.getHeight() + lastTopInset;
            int newRadius;
            if (headerHeight - scrollY - topBarsHeight < defaultRadius) {
                newRadius = Math.max(0, headerHeight - scrollY - topBarsHeight);
                imageWrap.setTranslationY(Math.max(0, scrollY - headerHeight + topBarsHeight));
            } else {
                newRadius = defaultRadius;
                imageWrap.setTranslationY(0);
            }
            if (currentTransition == null)
                image.setAlpha(1f - Math.max(0, Math.min(1, scrollY / (float) (headerHeight - topBarsHeight))));
            if (newRadius != headerRadius) {
                headerRadius = newRadius;
                imageWrap.invalidateOutline();
            }
        });

        return view;
    }

    @Override
    protected void onUpdateToolbar() {
        super.onUpdateToolbar();
        Toolbar toolbar = getToolbar();
        toolbar.setBackground(null);
        ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = lastTopInset;

        TextView index = (TextView) LayoutInflater.from(toolbar.getContext()).inflate(R.layout.pokemon_index_view, null);
        toolbar.addView(index, new Toolbar.LayoutParams(Gravity.END));
        index.setText(String.format("#%03d", pokemon.index));
    }

    @Override
    public void onApplyWindowInsets(WindowInsets insets) {
        int topInset = insets.getSystemWindowInsetTop();
        ((ViewGroup.MarginLayoutParams) getToolbar().getLayoutParams()).topMargin = topInset;
        int pad = V.dp(16);
        imageWrap.setPadding(pad, pad + topInset + getToolbar().getLayoutParams().height, pad, pad);
        imageWrap.getLayoutParams().height = V.dp(250) + topInset;
        lastTopInset = topInset;
        insets = insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0, insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
        if (Build.VERSION.SDK_INT >= 29 && insets.getTappableElementInsets().bottom == 0) {
            scrollableContent.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            insets = insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0, insets.getSystemWindowInsetRight(), 0);
        }
        super.onApplyWindowInsets(insets);
    }

    @Override
    public Animator onCreateEnterTransition(View prev, View container) {
        return createFragmentTransition(prev, container, true);
    }

    @Override
    public Animator onCreateExitTransition(View prev, View container) {
        return createFragmentTransition(prev, container, false);
    }

    private void showDetails() {
        if (getActivity() == null)
            return;
        TransitionManager.beginDelayedTransition((ViewGroup) view, new TransitionSet()
                .addTransition(new Fade(Fade.IN | Fade.OUT))
                .setDuration(250)
                .setInterpolator(CubicBezierInterpolator.DEFAULT)
        );
        progress.setVisibility(View.GONE);
        typesContainer.setVisibility(View.VISIBLE);
        sizeStats.setVisibility(View.VISIBLE);
        stats.setVisibility(View.VISIBLE);

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(1);
        weight.setText(getString(R.string.weight_kg, format.format(details.weight / 10.0)));
        height.setText(getString(R.string.height_m, format.format(details.height / 10.0)));
        hpBar.setProgress(details.health);
        attackBar.setProgress(details.attack);
        defenseBar.setProgress(details.defense);
        specialAttachBar.setProgress(details.specialAttack);
        specialDefenseBar.setProgress(details.specialDefense);
        speedBar.setProgress(details.speed);

        for (PokemonType type : details.types) {
            TextView typeChip = new TextView(getActivity());
            typeChip.setTextSize(13);
            typeChip.setTextColor(0xffffffff);
            typeChip.setShadowLayer(V.dp(1.5f), 0, V.dp(1), 0x80000000);
            typeChip.setText(type.toString()); // it's a capitalized English name anyway
            typeChip.setSingleLine();
            typeChip.setBackgroundResource(R.drawable.bg_type_chip);
            typeChip.setBackgroundTintList(ColorStateList.valueOf(switch (type) {
                case NORMAL -> 0xffaaaa99;
                case FIRE -> 0xffff4422;
                case WATER -> 0xff3399ff;
                case ELECTRIC -> 0xffffcc33;
                case GRASS -> 0xff77cc55;
                case ICE -> 0xff66ccff;
                case FIGHTING -> 0xffbb5544;
                case POISON -> 0xffaa5599;
                case GROUND -> 0xffddbb55;
                case FLYING -> 0xff8899ff;
                case PSYCHIC -> 0xffff5599;
                case BUG -> 0xffaabb22;
                case ROCK -> 0xffbbaa66;
                case GHOST -> 0xff6666bb;
                case DRAGON -> 0xff7766ee;
                case DARK -> 0xff775544;
                case STEEL -> 0xffaaaabb;
                case FAIRY -> 0xffee99ee;
            }));
            typesContainer.addView(typeChip);
        }
    }

    private void loadDetails() {
        PokemonCache.getInstance().getDetails(pokemon, new Callback<>() {
            @Override
            public void onSuccess(PokemonDetails resp) {
                details = resp;
                showDetails();
            }

            @Override
            public void onError(ErrorResponse err) {
                Activity activity = getActivity();
                if (activity != null)
                    err.showToast(activity);
            }
        });
    }

    private Animator createFragmentTransition(View prev, View container, boolean in) {
        if (currentTransition != null)
            currentTransition.cancel();
        ArrayList<Animator> anims = new ArrayList<>();
        anims.add(ObjectAnimator.ofFloat(container, View.ALPHA, in ? 0f : 1f, in ? 1f : 0f));
        RecyclerView parentList = prev.findViewById(me.grishka.appkit.R.id.list);
        RecyclerView.ViewHolder srcHolder = null;
        if (parentList != null) {
            for (int i = 0; i < parentList.getChildCount(); i++) {
                RecyclerView.ViewHolder holder = parentList.getChildViewHolder(parentList.getChildAt(i));
                if (holder instanceof BindableViewHolder<?> bvh && bvh.getItem() instanceof ListPokemon lp) {
                    if (lp.index == pokemon.index) {
                        srcHolder = holder;
                        break;
                    }
                }
            }
        }

        AnimatorSet set = new AnimatorSet();
        if (srcHolder != null) {
            FrameLayout parent = (FrameLayout) getActivity().getWindow().getDecorView();

            View itemView = srcHolder.itemView;
            itemView.setHasTransientState(true);
            int[] pos = {0, 0};
            ImageView srcImage = srcHolder.itemView.findViewById(R.id.image);
            TextView srcName = srcHolder.itemView.findViewById(R.id.name);
            srcImage.getLocationInWindow(pos);
            int srcImgX = pos[0], srcImgY = pos[1];
            image.getLocationInWindow(pos);
            int dstImgX = pos[0], dstImgY = pos[1];
            int overlayImgSize = V.dp(200);
            float dstImgScale = image.getHeight() / (float) overlayImgSize;
            float srcImgScale = srcImage.getHeight() / (float) overlayImgSize;

            ImageView overlayImage = new ImageView(getActivity());
            overlayImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            overlayImage.setImageDrawable(srcImage.getDrawable());
            parent.addView(overlayImage, new FrameLayout.LayoutParams(overlayImgSize, overlayImgSize, Gravity.TOP | Gravity.LEFT));

            float imgTransXFrom = srcImgX + srcImage.getWidth() / 2f - overlayImgSize / 2f;
            float imgTransYFrom = srcImgY + srcImage.getHeight() / 2f - overlayImgSize / 2f;
            float imgTransXTo = dstImgX + image.getWidth() / 2f - overlayImgSize / 2f;
            float imgTransYTo = dstImgY + image.getHeight() / 2f - overlayImgSize / 2f;
            anims.add(ObjectAnimator.ofFloat(overlayImage, View.TRANSLATION_X, in ? imgTransXFrom : imgTransXTo, in ? imgTransXTo : imgTransXFrom));
            anims.add(ObjectAnimator.ofFloat(overlayImage, View.TRANSLATION_Y, in ? imgTransYFrom : imgTransYTo, in ? imgTransYTo : imgTransYFrom));
            anims.add(ObjectAnimator.ofFloat(overlayImage, View.SCALE_X, in ? srcImgScale : dstImgScale, in ? dstImgScale : srcImgScale));
            anims.add(ObjectAnimator.ofFloat(overlayImage, View.SCALE_Y, in ? srcImgScale : dstImgScale, in ? dstImgScale : srcImgScale));

            TextView overlayNameSmall = new TextView(getActivity());
            overlayNameSmall.setTextSize(TypedValue.COMPLEX_UNIT_PX, srcName.getTextSize());
            overlayNameSmall.setTextColor(srcName.getTextColors());
            overlayNameSmall.setSingleLine();
            overlayNameSmall.setEllipsize(TextUtils.TruncateAt.END);
            overlayNameSmall.setGravity(Gravity.CENTER);
            overlayNameSmall.setText(pokemon.name);
            parent.addView(overlayNameSmall, new FrameLayout.LayoutParams(srcName.getWidth(), srcName.getHeight(), Gravity.TOP | Gravity.LEFT));

            TextView overlayNameLarge = new TextView(getActivity());
            overlayNameLarge.setTextSize(TypedValue.COMPLEX_UNIT_PX, name.getTextSize());
            overlayNameLarge.setTextColor(name.getTextColors());
            overlayNameLarge.setSingleLine();
            overlayNameLarge.setEllipsize(TextUtils.TruncateAt.END);
            overlayNameLarge.setGravity(Gravity.CENTER);
            overlayNameLarge.setTypeface(name.getTypeface());
            overlayNameLarge.setText(pokemon.name);
            parent.addView(overlayNameLarge, new FrameLayout.LayoutParams(name.getWidth(), name.getHeight(), Gravity.TOP | Gravity.LEFT));

            srcName.getLocationInWindow(pos);
            float smNameTransXFrom = pos[0];
            float smNameTransYFrom = pos[1];
            float lgNameTransXFrom = pos[0] + srcName.getWidth() / 2f - name.getWidth() / 2f;
            float lgNameTransYFrom = pos[1] + srcName.getHeight() / 2f - name.getHeight() / 2f;
            name.getLocationInWindow(pos);
            float smNameTransXTo = pos[0] + name.getWidth() / 2f - srcName.getWidth() / 2f;
            float smNameTransYTo = pos[1] + name.getHeight() / 2f - srcName.getHeight() / 2f;
            float lgNameTransXTo = pos[0];
            float lgNameTransYTo = pos[1];
            float smNameScaleTo = name.getHeight() / (float) srcName.getHeight();
            float lgNameScaleFrom = srcName.getHeight() / (float) name.getHeight();
            anims.add(ObjectAnimator.ofFloat(overlayNameSmall, View.TRANSLATION_X, in ? smNameTransXFrom : smNameTransXTo, in ? smNameTransXTo : smNameTransXFrom));
            anims.add(ObjectAnimator.ofFloat(overlayNameSmall, View.TRANSLATION_Y, in ? smNameTransYFrom : smNameTransYTo, in ? smNameTransYTo : smNameTransYFrom));
            anims.add(ObjectAnimator.ofFloat(overlayNameSmall, View.SCALE_X, in ? 1f : smNameScaleTo, in ? smNameScaleTo : 1f));
            anims.add(ObjectAnimator.ofFloat(overlayNameSmall, View.SCALE_Y, in ? 1f : smNameScaleTo, in ? smNameScaleTo : 1f));
            anims.add(ObjectAnimator.ofFloat(overlayNameLarge, View.TRANSLATION_X, in ? lgNameTransXFrom : lgNameTransXTo, in ? lgNameTransXTo : lgNameTransXFrom));
            anims.add(ObjectAnimator.ofFloat(overlayNameLarge, View.TRANSLATION_Y, in ? lgNameTransYFrom : lgNameTransYTo, in ? lgNameTransYTo : lgNameTransYFrom));
            anims.add(ObjectAnimator.ofFloat(overlayNameLarge, View.SCALE_X, in ? lgNameScaleFrom : 1f, in ? 1f : lgNameScaleFrom));
            anims.add(ObjectAnimator.ofFloat(overlayNameLarge, View.SCALE_Y, in ? lgNameScaleFrom : 1f, in ? 1f : lgNameScaleFrom));
            anims.add(ObjectAnimator.ofFloat(overlayNameSmall, View.ALPHA, in ? 1f : 0f, in ? 0f : 1f));
            anims.add(ObjectAnimator.ofFloat(overlayNameLarge, View.ALPHA, in ? 0f : 1f, in ? 1f : 0f));

            image.setAlpha(0f);
            name.setAlpha(0f);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    parent.removeView(overlayImage);
                    parent.removeView(overlayNameSmall);
                    parent.removeView(overlayNameLarge);
                    srcImage.setAlpha(1f);
                    image.setAlpha(1f);
                    srcName.setAlpha(1f);
                    name.setAlpha(1f);
                    itemView.setHasTransientState(false);
                    currentTransition = null;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    // Prevents the image disappearing for one frame because the old one is already hidden but the overlay is not yet visible
                    srcImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            srcImage.getViewTreeObserver().removeOnPreDrawListener(this);
                            srcImage.setAlpha(0f);
                            srcName.setAlpha(0f);
                            return true;
                        }
                    });
                }
            });
        }

        set.playTogether(anims);
        set.setDuration(500);
        set.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        currentTransition = set;
        return set;
    }
}
