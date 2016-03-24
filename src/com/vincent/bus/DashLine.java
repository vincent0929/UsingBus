package com.vincent.bus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.widget.TextView;

public class DashLine extends TextView {

	public DashLine(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressLint("ResourceAsColor") @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint=new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(android.R.color.background_dark);
		Path path=new Path();
		path.moveTo(0, getHeight()/2);
		path.lineTo(getWidth(), getHeight()/2);
		PathEffect effect=new DashPathEffect(new float[]{5,5,5,5},1);
		paint.setPathEffect(effect);
		canvas.drawPath(path, paint);
	}

}
