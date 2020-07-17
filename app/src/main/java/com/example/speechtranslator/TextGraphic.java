package com.example.speechtranslator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

public class TextGraphic extends GraphicOverlay.Graphic {

    private static final float STROKE_WIDTH = 4.05f;
    private static final float TEXT_SIZE = 140.0f;
    private final Paint rectPaint, textPaint;
    private FirebaseVisionText.Element text;


    public TextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element text) {
        super(overlay);

        this.text = text;
        rectPaint = new Paint();
        rectPaint.setColor(Color.BLUE);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        rectPaint.setColor(Color.BLUE);
        rectPaint.setTextSize(TEXT_SIZE);

        //Adjusts the y coordinate from the preview's coordinate system to the view coordinate
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        if (text == null) {
            throw new IllegalStateException("Attempting to draw a null text ");
        }
        //RectF holds four float coordinates for a rectangle
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.right = translateX(rect.right);
        rect.bottom = translateX(rect.bottom);
        rect.top = translateX(rect.top);

        canvas.drawRect(rect, rectPaint);

        canvas.drawText(text.getText(), rect.left, rect.bottom, textPaint);

    }
}
