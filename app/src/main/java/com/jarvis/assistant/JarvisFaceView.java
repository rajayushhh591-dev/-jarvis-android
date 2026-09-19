package com.jarvis.assistant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class JarvisFaceView extends View {

    // ==========================================
    // JARVIS STATES
    // ==========================================

    public static final int IDLE = 0;
    public static final int LISTENING = 1;
    public static final int THINKING = 2;
    public static final int ACTION = 3;
    public static final int SPEAKING = 4;

    private int state = IDLE;

    // ==========================================
    // PAINT
    // ==========================================

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    // ==========================================
    // FACE IMAGE
    // ==========================================

    private Bitmap faceBitmap;

    // ==========================================
    // ANIMATION
    // ==========================================

    private float animation = 0f;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public JarvisFaceView(Context context) {

        super(context);

        setBackgroundColor(Color.BLACK);

        paint.setAntiAlias(true);

        // Load:
        // app/src/main/res/drawable/jarvis_base_face.jpg

        faceBitmap = BitmapFactory.decodeResource(
                getResources(),
                getResources().getIdentifier(
                        "jarvis_base_face",
                        "drawable",
                        context.getPackageName()
                )
        );

        // Start animation
        post(animationLoop);
    }

    // ==========================================
    // MAIN ANIMATION LOOP
    // ==========================================

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

    // ==========================================
    // CHANGE JARVIS STATE
    // ==========================================

    public void setState(int newState) {

        state = newState;

        invalidate();
    }

    // ==========================================
    // GET CURRENT STATE
    // ==========================================

    public int getState() {

        return state;
    }

    // ==========================================
    // DRAW
    // ==========================================

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float cx =
                getWidth() / 2f;

        float cy =
                getHeight() / 2f;

        float wave =
                (float) Math.sin(animation);

        // ======================================
        // FACE IMAGE
        // ======================================

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

        // ======================================
        // JARVIS RADIUS
        // ======================================
