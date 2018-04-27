package com.corleois.craft.craft_o2;

import android.content.ClipData;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

public class Gesture implements View.OnTouchListener, View.OnDragListener {

	OnGestureListener onGestureListener;

	float startX;
	float startY;
	float viewPositionX;

	public Gesture(OnGestureListener onGestureListener) {
		this.onGestureListener = onGestureListener;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ClipData data = ClipData.newPlainText("text", "text : " + v.toString());
		View.DragShadowBuilder shadow = new View.DragShadowBuilder(null);
		v.startDrag(data, shadow, v, 0);
		return false;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
//	    System.out.println("DragEvent.ACTION_DRAG_STARTED = " + DragEvent.ACTION_DRAG_STARTED);     // 1
//	    System.out.println("DragEvent.ACTION_DRAG_LOCATION = " + DragEvent.ACTION_DRAG_LOCATION);   // 2
//	    System.out.println("DragEvent.ACTION_DRAG_ENDED = " + DragEvent.ACTION_DRAG_ENDED);         // 4

		if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
			// ジェスチャー開始時
			startX = event.getX();
			viewPositionX = v.getX();

		} else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {

			v.setX(viewPositionX + (event.getX() - startX));
			v.setAlpha(1.0f - Math.abs(event.getX() - startX) / 200);

		} else if (event.getAction() == DragEvent.ACTION_DROP) {
			// ジェスチャー終了時

			v.setX(viewPositionX);
			v.setAlpha(1.0f);

			float distance = event.getX() - startX;
			System.out.println("移動距離: " + distance);

			if (distance > 200) {
				onGestureListener.onPrev(v);
			} else if (distance < -200) {
				onGestureListener.onNext(v);
			}
		}
		return true;
	}

	public interface OnGestureListener {
		void onNext(View v);
		void onPrev(View v);
	}
}
