package com.jarvis.assistant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class JarvisFaceView extends View {

    public static final int IDLE = 0;
    public static final int LISTENING = 1;
    public static final int THINKING = 2;
    public static final int ACTION = 3;
    public static final int SPEAKING = 4;

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap faceBitmap;

    private int state = IDLE;

    private float animation = 0f;

    public JarvisFaceView(Context context) {

        super(context);

        setBackgroundColor(0xFF000000);

        paint.setAntiAlias(true);

        faceBitmap = BitmapFactory.decodeResource(
                getResources(),
                getResources().getIdentifier(
                        "jarvis_base_face",
                        "drawable",
                        context.getPackageName()
                )
        );

        post(animationLoop);
    }

    private final Runnable animationLoop =
            new Runnable() {

        @Override
        public void run() {

            animation += 0.10f;

            invalidate();

            postDelayed(
                    this,
                    30
            );
        }
    };

    public void setState(int newState) {

        state = newState;

        invalidate();
    }

    public int getState() {

        return state;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float cx =
                getWidth() / 2f;

        float cy =
                getHeight() / 2f;

        float wave =
                (float) Math.sin(animation);

        // =====================================
        // JARVIS FACE IMAGE
        // =====================================

        if (faceBitmap != null) {

            float maxWidth =
                    getWidth() * 0.82f;

            float maxHeight =
                    getHeight() * 0.72f;

            float scale =
                    Math.min(
                            maxWidth /
                                    faceBitmap.getWidth(),

                            maxHeight /
                                    faceBitmap.getHeight()
                    );

            float width =
                    faceBitmap.getWidth()
                            * scale;

            float height =
                    faceBitmap.getHeight()
                            * scale;

            RectF destination =
                    new RectF(
                            cx - width / 2f,
                            cy - height / 2f,
                            cx + width / 2f,
                            cy + height / 2f
                    );

            paint.setStyle(
                    Paint.Style.FILL
            );

            paint.setAlpha(255);

            canvas.drawBitmap(
                    faceBitmap,
                    null,
                    destination,
                    paint
            );
        }

        // =====================================
        // ANIMATION RADIUS
        // =====================================

        float radius =
                Math.min(
                        getWidth(),
                        getHeight()
                ) * 0.38f;

        // =====================================
        // IDLE
        // =====================================

        if (state == IDLE) {

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(3);

            paint.setAlpha(110);

            canvas.drawCircle(
                    cx,
                    cy,
                    radius + wave * 4,
                    paint
            );
        }

        // =====================================
        // LISTENING
        // =====================================

        else if (state == LISTENING) {

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(4);

            paint.setAlpha(220);

            for (int i = 0; i < 4; i++) {

                float listeningRadius =
                        radius
                                + 20
                                + i * 22
                                + wave * 8;

                canvas.drawCircle(
                        cx,
                        cy,
                        listeningRadius,
                        paint
                );
            }
        }

        // =====================================
        // THINKING
        // =====================================

        else if (state == THINKING) {

            paint.setStyle(
                    Paint.Style.FILL
            );

            paint.setAlpha(230);

            for (int i = 0; i < 8; i++) {

                double angle =
                        animation
                                + i * Math.PI / 4;

                float x =
                        cx +
                        (float) Math.cos(angle)
                                * (radius + 35);

                float y =
                        cy +
                        (float) Math.sin(angle)
                                * (radius + 35);

                float dotSize =
                        5 +
                        Math.abs(wave) * 4;

                canvas.drawCircle(
                        x,
                        y,
                        dotSize,
                        paint
                );
            }
        }

        // =====================================
        // ACTION
        // =====================================

        else if (state == ACTION) {

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(5);

            paint.setAlpha(230);

            float actionRadius =
                    radius
                            + 40
                            + wave * 18;

            canvas.drawCircle(
                    cx,
                    cy,
                    actionRadius,
                    paint
            );

            paint.setStrokeWidth(3);

            canvas.drawCircle(
                    cx,
                    cy,
                    radius + 65 - wave * 12,
                    paint
            );
        }

        // =====================================
        // SPEAKING
        // =====================================

        else if (state == SPEAKING) {

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(4);

            paint.setAlpha(230);

            float voiceWave =
                    20 +
                    Math.abs(wave) * 35;

            // Left voice wave

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

            // Right voice wave

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

            // Speaking pulse

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
