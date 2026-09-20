package com.jarvis.assistant;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

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

    private float animation = 0f;

    public JarvisFaceView(Context context) {

        super(context);

        setBackgroundColor(0xFF000000);

        paint.setAntiAlias(true);

        post(animationLoop);
    }

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

    public void setState(int newState) {

        state = newState;

        invalidate();
    }

    public int getState() {

        return state;
    }

    private final Runnable animationLoop =
            new Runnable() {

                @Override
                public void run() {

                    animation += 0.08f;

                    invalidate();

                    postDelayed(
                            this,
                            30
                    );
                }
            };

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float cx = width / 2f;
        float cy = height / 2f;

        float wave =
                (float) Math.sin(animation);

        if (avatarDrawable != null &&
                width > 0 &&
                height > 0) {

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
                                maxWidth / drawableWidth,
                                maxHeight / drawableHeight
                        );

                float drawWidth =
                        drawableWidth * scale;

                float drawHeight =
                        drawableHeight * scale;

                float left =
                        cx - drawWidth / 2f;

                float top =
                        cy - drawHeight / 2f;

                float right =
                        cx + drawWidth / 2f;

                float bottom =
                        cy + drawHeight / 2f;

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

        float radius =
                Math.min(width, height) * 0.38f;

        if (state == IDLE) {

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setAlpha(120);

            canvas.drawCircle(
                    cx,
                    cy,
                    radius + wave * 4,
                    paint
            );

        } else if (state == LISTENING) {

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            paint.setAlpha(220);

            for (int i = 0; i < 4; i++) {

                float r =
                        radius +
                        20 +
                        i * 22 +
                        wave * 8;

                canvas.drawCircle(
                        cx,
                        cy,
                        r,
                        paint
                );
            }

        } else if (state == THINKING) {

            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(230);

            for (int i = 0; i < 8; i++) {

                double angle =
                        animation +
                        i * Math.PI / 4;

                float x =
                        cx +
                        (float) Math.cos(angle)
                        * (radius + 35);

                float y =
                        cy +
                        (float) Math.sin(angle)
                        * (radius + 35);

                float size =
                        5 +
                        Math.abs(wave) * 4;

                canvas.drawCircle(
                        x,
                        y,
                        size,
                        paint
                );
            }

        } else if (state == ACTION) {

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setAlpha(230);

            float r =
                    radius +
                    40 +
                    wave * 18;

            canvas.drawCircle(
                    cx,
                    cy,
                    r,
                    paint
            );

            paint.setStrokeWidth(3);

            canvas.drawCircle(
                    cx,
                    cy,
                    radius +
                    65 -
                    wave * 12,
                    paint
            );

        } else if (state == SPEAKING) {

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            paint.setAlpha(230);

            float voiceWave =
                    20 +
                    Math.abs(wave) * 35;

            canvas.drawArc(
                    new RectF(
                            cx - 100,
                            cy + 65,
                            cx - 35,
                            cy + 135
                    ),
                    90,
                    180,
                    false,
                    paint
            );

            canvas.drawArc(
                    new RectF(
                            cx + 35,
                            cy + 65,
                            cx + 100,
                            cy + 135
                    ),
                    -90,
                    180,
                    false,
                    paint
            );

            canvas.drawOval(
                    cx - 45,
                    cy + 95 - voiceWave / 2,
                    cx + 45,
                    cy + 95 + voiceWave / 2,
                    paint
            );
        }

        paint.setAlpha(255);
    }

    @Override
    protected void onDetachedFromWindow() {

        removeCallbacks(animationLoop);

        super.onDetachedFromWindow();
    }
}
