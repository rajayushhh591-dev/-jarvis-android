package com.jarvis.assistant;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class JarvisFaceView extends View {

    public static final int IDLE = 0;
    public static final int LISTENING = 1;
    public static final int THINKING = 2;
    public static final int ACTION = 3;
    public static final int SPEAKING = 4;

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private Drawable avatarDrawable;

    private int state = IDLE;
    private int previousState = IDLE;

    // Main animation time
    private float animation = 0f;

    // Smooth body motion
    private float smoothX = 0f;
    private float smoothY = 0f;
    private float smoothScale = 1f;

    // Target motion
    private float targetX = 0f;
    private float targetY = 0f;
    private float targetScale = 1f;

    // Motion intensity
    private float motionStrength = 1f;

    private final DecelerateInterpolator interpolator =
            new DecelerateInterpolator();

    public JarvisFaceView(Context context) {
        super(context);

        setBackgroundColor(0xFF000000);

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        post(animationLoop);
    }

    // =========================================================
    // AVATAR
    // =========================================================

    public void setAvatarResource(int resourceId) {

        try {

            avatarDrawable =
                    getResources().getDrawable(
                            resourceId,
                            getContext().getTheme()
                    );

        } catch (Exception e) {

            avatarDrawable = null;
        }

        invalidate();
    }

    // =========================================================
    // STATE
    // =========================================================

    public void setState(int newState) {

        if (newState < IDLE ||
                newState > SPEAKING) {

            newState = IDLE;
        }

        previousState = state;
        state = newState;

        // Reset targets when state changes
        targetX = 0f;
        targetY = 0f;
        targetScale = 1f;

        invalidate();
    }

    public int getState() {
        return state;
    }

    // =========================================================
    // MOTION CONTROL
    // =========================================================

    public void setMotionStrength(float strength) {

        motionStrength =
                Math.max(
                        0f,
                        Math.min(
                                2f,
                                strength
                        )
                );
    }

    public float getMotionStrength() {
        return motionStrength;
    }

    // =========================================================
    // ANIMATION LOOP
    // =========================================================

    private final Runnable animationLoop =
            new Runnable() {

                @Override
                public void run() {

                    animation += 0.055f;

                    updateMotion();

                    invalidate();

                    postDelayed(
                            this,
                            16
                    );
                }
            };

    // =========================================================
    // MOTION ENGINE
    // =========================================================

    private void updateMotion() {

        float sin1 =
                (float) Math.sin(animation);

        float sin2 =
                (float) Math.sin(
                        animation * 0.63f
                );

        float sin3 =
                (float) Math.sin(
                        animation * 1.37f
                );

        switch (state) {

            // -------------------------------------------------
            // IDLE
            // -------------------------------------------------

            case IDLE:

                targetX =
                        sin1 *
                        2.0f *
                        motionStrength;

                targetY =
                        sin2 *
                        2.5f *
                        motionStrength;

                targetScale =
                        1f +
                        sin3 *
                        0.008f *
                        motionStrength;

                break;

            // -------------------------------------------------
            // LISTENING
            // -------------------------------------------------

            case LISTENING:

                targetX =
                        sin1 *
                        3.5f *
                        motionStrength;

                targetY =
                        sin2 *
                        4.0f *
                        motionStrength;

                targetScale =
                        1f +
                        0.015f *
                        Math.abs(sin1) *
                        motionStrength;

                break;

            // -------------------------------------------------
            // THINKING
            // -------------------------------------------------

            case THINKING:

                targetX =
                        sin1 *
                        2.0f *
                        motionStrength;

                targetY =
                        sin2 *
                        3.0f *
                        motionStrength;

                targetScale =
                        1f +
                        0.01f *
                        Math.abs(sin3);

                break;

            // -------------------------------------------------
            // ACTION
            // -------------------------------------------------

            case ACTION:

                targetX =
                        sin1 *
                        4.0f *
                        motionStrength;

                targetY =
                        sin2 *
                        4.0f *
                        motionStrength;

                targetScale =
                        1f +
                        0.025f *
                        Math.abs(sin1);

                break;

            // -------------------------------------------------
            // SPEAKING
            // -------------------------------------------------

            case SPEAKING:

                targetX =
                        sin1 *
                        2.5f *
                        motionStrength;

                targetY =
                        sin2 *
                        3.5f *
                        motionStrength;

                targetScale =
                        1f +
                        0.018f *
                        Math.abs(sin1) *
                        motionStrength;

                break;
        }

        // Smooth interpolation
        smoothX +=
                (targetX - smoothX) * 0.08f;

        smoothY +=
                (targetY - smoothY) * 0.08f;

        smoothScale +=
                (targetScale - smoothScale) * 0.08f;
    }

    // =========================================================
    // DRAW
    // =========================================================

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (width <= 0 ||
                height <= 0) {

            return;
        }

        float cx =
                width / 2f +
                        smoothX;

        float cy =
                height / 2f +
                        smoothY;

        float wave =
                (float) Math.sin(animation);

        // -----------------------------------------------------
        // AVATAR
        // -----------------------------------------------------

        if (avatarDrawable != null) {

            int drawableWidth =
                    avatarDrawable.getIntrinsicWidth();

            int drawableHeight =
                    avatarDrawable.getIntrinsicHeight();

            if (drawableWidth > 0 &&
                    drawableHeight > 0) {

                float maxWidth =
                        width * 0.92f;

                float maxHeight =
                        height * 0.92f;

                float scale =
                        Math.min(
                                maxWidth /
                                        drawableWidth,

                                maxHeight /
                                        drawableHeight
                        );

                // Smooth motion scale
                scale *= smoothScale;

                float drawWidth =
                        drawableWidth * scale;

                float drawHeight =
                        drawableHeight * scale;

                float left =
                        cx -
                                drawWidth / 2f;

                float top =
                        cy -
                                drawHeight / 2f;

                float right =
                        cx +
                                drawWidth / 2f;

                float bottom =
                        cy +
                                drawHeight / 2f;

                RectF rect =
                        new RectF(
                                left,
                                top,
                                right,
                                bottom
                        );

                avatarDrawable.setBounds(
                        (int) rect.left,
                        (int) rect.top,
                        (int) rect.right,
                        (int) rect.bottom
                );

                avatarDrawable.setAlpha(255);

                avatarDrawable.draw(canvas);
            }
        }

        // -----------------------------------------------------
        // MOTION RING SIZE
        // -----------------------------------------------------

        float radius =
                Math.min(
                        width,
                        height
                ) * 0.38f;

        // -----------------------------------------------------
        // IDLE
        // -----------------------------------------------------

        if (state == IDLE) {

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(2.5f);

            paint.setAlpha(90);

            float idleRadius =
                    radius +
                            wave * 3f *
                                    motionStrength;

            canvas.drawCircle(
                    cx,
                    cy,
                    idleRadius,
                    paint
            );
        }

        // -----------------------------------------------------
        // LISTENING
        // -----------------------------------------------------

        else if (state == LISTENING) {

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(3.5f);

            paint.setAlpha(180);

            for (int i = 0; i < 3; i++) {

                float r =
                        radius +
                                18 +
                                i * 20 +
                                wave * 5f *
                                        motionStrength;

                canvas.drawCircle(
                        cx,
                        cy,
                        r,
                        paint
                );
            }
        }

        // -----------------------------------------------------
        // THINKING
        // -----------------------------------------------------

        else if (state == THINKING) {

            paint.setStyle(
                    Paint.Style.FILL
            );

            paint.setAlpha(210);

            for (int i = 0; i < 8; i++) {

                double angle =
                        animation +
                                i *
                                        Math.PI /
                                        4;

                float distance =
                        radius +
                                32;

                float x =
                        cx +
                                (float)
                                        Math.cos(angle)
                                        * distance;

                float y =
                        cy +
                                (float)
                                        Math.sin(angle)
                                        * distance;

                float size =
                        4f +
                                Math.abs(wave) *
                                        3f;

                canvas.drawCircle(
                        x,
                        y,
                        size,
                        paint
                );
            }
        }

        // -----------------------------------------------------
        // ACTION
        // -----------------------------------------------------

        else if (state == ACTION) {

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(4.5f);

            paint.setAlpha(200);

            float r1 =
                    radius +
                            35 +
                            wave * 12f;

            float r2 =
                    radius +
                            58 -
                            wave * 8f;

            canvas.drawCircle(
                    cx,
                    cy,
                    r1,
                    paint
            );

            paint.setStrokeWidth(2.5f);

            canvas.drawCircle(
                    cx,
                    cy,
                    r2,
                    paint
            );
        }

        // -----------------------------------------------------
        // SPEAKING
        // -----------------------------------------------------

        else if (state == SPEAKING) {

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(3.5f);

            paint.setAlpha(210);

            float voiceWave =
                    18f +
                            Math.abs(wave) *
                                    32f;

            canvas.drawArc(
                    new RectF(
                            cx - 105,
                            cy + 55,
                            cx - 35,
                            cy + 125
                    ),
                    90,
                    180,
                    false,
                    paint
            );

            canvas.drawArc(
                    new RectF(
                            cx + 35,
                            cy + 55,
                            cx + 105,
                            cy + 125
                    ),
                    -90,
                    180,
                    false,
                    paint
            );

            canvas.drawOval(
                    cx - 42,
                    cy + 90 -
                            voiceWave / 2f,

                    cx + 42,
                    cy + 90 +
                            voiceWave / 2f,

                    paint
            );
        }

        paint.setAlpha(255);
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDetachedFromWindow() {

        removeCallbacks(
                animationLoop
        );

        super.onDetachedFromWindow();
    }
}
