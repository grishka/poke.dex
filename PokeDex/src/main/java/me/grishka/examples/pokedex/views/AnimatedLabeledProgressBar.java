package me.grishka.examples.pokedex.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;

import java.util.Locale;

import androidx.annotation.NonNull;
import me.grishka.appkit.utils.CubicBezierInterpolator;
import me.grishka.appkit.utils.CustomViewHelper;
import me.grishka.examples.pokedex.R;

public class AnimatedLabeledProgressBar extends View implements CustomViewHelper {
    private static final Property<AnimatedLabeledProgressBar, Float> FRACTION_PROP = new Property<>(Float.class, "dsfsafsdafds") {
        @Override
        public Float get(AnimatedLabeledProgressBar object) {
            return object.fraction;
        }

        @Override
        public void set(AnimatedLabeledProgressBar object, Float value) {
            object.fraction = value;
            object.invalidate();
        }
    };

    private int progress, maxValue;
    private int fgColor, bgColor, textInnerColor, textOuterColor;
    private float fraction;
    private RectF tmpRect = new RectF();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Animator currentAnim;

    public AnimatedLabeledProgressBar(Context context) {
        this(context, null);
    }

    public AnimatedLabeledProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedLabeledProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AnimatedLabeledProgressBar, defStyle, 0);
        maxValue = ta.getInt(R.styleable.AnimatedLabeledProgressBar_android_max, 100);
        progress = ta.getInt(R.styleable.AnimatedLabeledProgressBar_android_progress, 0);
        fgColor = ta.getColor(R.styleable.AnimatedLabeledProgressBar_android_color, 0xff00ff00);
        bgColor = ta.getColor(R.styleable.AnimatedLabeledProgressBar_android_colorBackground, 0xffffffff);
        textInnerColor = ta.getColor(R.styleable.AnimatedLabeledProgressBar_android_numbersInnerTextColor, 0xffffffff);
        textOuterColor = ta.getColor(R.styleable.AnimatedLabeledProgressBar_android_numbersTextColor, 0xff000000);
        ta.recycle();

        paint.setTextSize(dp(11));
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (currentAnim != null)
            currentAnim.cancel();
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, FRACTION_PROP, progress / (float) maxValue);
        anim.setDuration(400);
        anim.setInterpolator(CubicBezierInterpolator.DEFAULT);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnim = null;
            }
        });
        currentAnim = anim;
        anim.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(18));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        tmpRect.set(0, 0, getWidth(), getHeight());
        paint.setColor(bgColor);
        float radius = getHeight() / 2f;
        canvas.drawRoundRect(tmpRect, radius, radius, paint);
        tmpRect.right = getWidth() * fraction;
        paint.setColor(fgColor);
        canvas.drawRoundRect(tmpRect, radius, radius, paint);

        String text = String.format(Locale.getDefault(), "%d/%d", Math.round(maxValue * fraction), maxValue);
        float textWidth = paint.measureText(text);
        float textGap = dp(4);
        float textX;
        if (tmpRect.width() < textWidth + textGap * 2) {
            textX = tmpRect.right + textGap;
            paint.setColor(textOuterColor);
        } else {
            textX = tmpRect.right - textWidth - textGap;
            paint.setColor(textInnerColor);
        }
        canvas.drawText(text, textX, getHeight() / 2f - (paint.descent() - paint.ascent()) / 2f - paint.ascent(), paint);
    }
}
