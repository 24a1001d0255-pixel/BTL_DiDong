package com.example.btl_didong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Arrays;
import java.util.List;

public class SpinningWheelView extends View {

    private Paint paintSlices;
    private Paint paintDivider;
    private Paint paintText;
    private Paint paintCenterOuter;
    private Paint paintCenterInner;
    private Paint paintBezel;
    private RectF rectF;
    private RectF bezelRectF;

    private List<String> items = Arrays.asList("Phở", "Cơm", "Bánh mì", "Đồ nướng", "Ăn vặt", "Đồ uống", "Bún", "Lẩu");

    private final int[] colors = {
            Color.parseColor("#D95F54"), // Đỏ gạch
            Color.parseColor("#F2B749"), // Vàng nghệ
            Color.parseColor("#82A979"), // Xanh lá xám
            Color.parseColor("#5192A6"), // Xanh dương pastel
            Color.parseColor("#866CA5"), // Tím nhạt
            Color.parseColor("#E07A5F"), // Cam san hô
            Color.parseColor("#2A9D8F"), // Xanh teal
            Color.parseColor("#3D5A80")  // Xanh navy nhạt
    };

    public SpinningWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintBezel = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBezel.setColor(Color.parseColor("#F5F0E6"));
        paintBezel.setStyle(Paint.Style.FILL);

        paintSlices = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSlices.setStyle(Paint.Style.FILL);

        paintDivider = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDivider.setColor(Color.WHITE);
        paintDivider.setStrokeWidth(4f);
        paintDivider.setStyle(Paint.Style.STROKE);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(36f);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        paintCenterOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenterOuter.setColor(Color.WHITE);

        paintCenterInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenterInner.setColor(Color.parseColor("#1A1A1A"));

        rectF = new RectF();
        bezelRectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int totalRadius = Math.min(width, height) / 2;
        int centerX = width / 2;
        int centerY = height / 2;

        bezelRectF.set(centerX - totalRadius, centerY - totalRadius, centerX + totalRadius, centerY + totalRadius);
        canvas.drawOval(bezelRectF, paintBezel);

        int sliceRadius = totalRadius - 32;
        rectF.set(centerX - sliceRadius, centerY - sliceRadius, centerX + sliceRadius, centerY + sliceRadius);

        float sweepAngle = 360f / items.size();
        float startAngle = 0f;

        for (int i = 0; i < items.size(); i++) {
            paintSlices.setColor(colors[i % colors.length]);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paintSlices);

            double rad = Math.toRadians(startAngle);
            float startX = (float) (centerX + sliceRadius * Math.cos(rad));
            float startY = (float) (centerY + sliceRadius * Math.sin(rad));
            canvas.drawLine(centerX, centerY, startX, startY, paintDivider);

            canvas.save();
            canvas.rotate(startAngle + sweepAngle / 2, centerX, centerY);
            canvas.drawText(items.get(i), centerX + sliceRadius * 0.62f, centerY + 12, paintText);
            canvas.restore();

            startAngle += sweepAngle;
        }

        canvas.drawCircle(centerX, centerY, sliceRadius * 0.30f, paintCenterOuter);
        canvas.drawCircle(centerX, centerY, sliceRadius * 0.24f, paintCenterInner);

        paintText.setTextSize(32f);
        canvas.drawText("CHỌN", centerX, centerY + 10, paintText);
    }

    public void setItems(List<String> newItems) {
        if (newItems != null && !newItems.isEmpty()) {
            this.items = newItems;
            invalidate();
        }
    }

    public void spinWheel(int resultIndex) {
        if (items == null || items.isEmpty()) return;
        int size = items.size();
        if (resultIndex < 0 || resultIndex >= size) return;

        float currentRot = getRotation();
        float sweepAngle = 360f / size;

        // Góc tâm của miếng bánh resultIndex (tính từ 0 độ ở hướng 3 giờ - nơi con trỏ bên phải chỉ sang trái)
        float targetSliceCenter = resultIndex * sweepAngle + (sweepAngle / 2f);

        // Góc quay của View để đưa targetSliceCenter về đúng hướng 0 độ (hướng 3 giờ)
        float targetAngleInCircle = (360f - targetSliceCenter) % 360f;

        float currentMod = (currentRot % 360f + 360f) % 360f;
        float diff = targetAngleInCircle - currentMod;
        if (diff < 0) {
            diff += 360f;
        }

        // Thêm 5 vòng quay tròn đầy đủ (5 * 360 = 1800 độ)
        float finalRotation = currentRot + 1800f + diff;

        this.animate()
                .rotation(finalRotation)
                .setDuration(3200)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
