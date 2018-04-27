package com.corleois.craft.craft_o2;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.corleois.craft.craft_o2.Activities.Interfaces.ColorChangeListener;


/**
 * 再生画面のスワイプ操作を制御するためのクラス
 */
public class MusicPlayerMotionController implements View.OnTouchListener, View.OnDragListener {

	// View
	public Activity activity;
	public View rootView;
	private View container;
	private View otherView;

	private RelativeLayout unit_albumArt;
	private LinearLayout unit_title;
	private TextView title;
	private TextView artist;
	private LinearLayout unit_seekBar;
	private RelativeLayout unit_operations;
	private LinearLayout unit_settings;

	private int maxPadding_container;
	private int maxPaddingRight_albumArt;
	private int paddingBottom_unit_albumArt;
	private int paddingBottom_unit_title;
	private int paddingBottom_unit_seekBar;
	private int paddingBottom_unit_operations;
	private int settingsHeight;

    private ColorChangeListener changeListener;

	public enum ScreenMode {
		FULL, MINI, SETTINGS
	}

	private ScreenMode screenMode;


	/**
	 * コンストラクタ
	 *
	 * @param activity
	 * @param rootView
	 * @param screenMode
	 */
	public MusicPlayerMotionController(Activity activity, View rootView, ScreenMode screenMode) {

		// Viewの取得
		this.activity = activity;
		this.rootView = rootView;
		this.screenMode = screenMode;

		container = activity.findViewById(R.id.playerContainer);
		otherView = activity.findViewById(R.id.listSelectionArea);

		unit_albumArt = (RelativeLayout) rootView.findViewById(R.id.gestureArea_albumArt);
		unit_title = (LinearLayout) rootView.findViewById(R.id.unit_title);
		title = (TextView) rootView.findViewById(R.id.title);
		artist = (TextView) rootView.findViewById(R.id.artist);
		unit_seekBar = (LinearLayout) rootView.findViewById(R.id.unit_seekBar);
		unit_operations = (RelativeLayout) rootView.findViewById(R.id.unit_operations);
		unit_settings = (LinearLayout) rootView.findViewById(R.id.unit_settings);

		// Viewの位置の調整(Fragment内のViewが生成された時に実行される)
		int padding = (int) (activity.getResources().getDisplayMetrics().density * 8);
		int value = padding / 8;            //汎用データ

		unit_operations.setPadding(0, 0, 0, padding);
		paddingBottom_unit_operations = padding;
		padding += unit_operations.getHeight();
		unit_seekBar.setPadding(0, 0, 0, padding);
		paddingBottom_unit_seekBar = padding;
		padding += unit_seekBar.getHeight();
		unit_title.setPadding(0, 0, 0, padding);
		paddingBottom_unit_title = padding;
//		padding += unit_title.getHeight() + (int) (activity.getResources().getDisplayMetrics().density * 8);
        padding += unit_title.getHeight() + (value * 8);
		unit_albumArt.setPadding(0, 0, 0, padding);
		paddingBottom_unit_albumArt = padding;

//		maxPadding_container = otherView.getHeight() - (int) (activity.getResources().getDisplayMetrics().density * 70);
//		maxPaddingRight_albumArt = otherView.getWidth() - (int) (activity.getResources().getDisplayMetrics().density * 70);
        maxPadding_container = otherView.getHeight() -  (value * 70);
        maxPaddingRight_albumArt = otherView.getWidth() - (value * 70);
		settingsHeight = unit_settings.getHeight();

		changeScreenMode(screenMode);
        TextView sprash = (TextView)rootView.findViewById(R.id.sprash);
        sprash.setVisibility(View.GONE);
	}

    /**
     * 色変更を通知するためのリスナーを持つ
     * @param changeListener
     */
    public void setChangeListener(ColorChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    /**
	 * 再生画面を規定の位置に戻すメソッド
	 * 起動時、スワイプ終了後に呼ぶ
	 *
	 * @param screenMode
	 */
	public void changeScreenMode(ScreenMode screenMode) {
		ValueAnimator valueAnimator;
		int duration = 100;
		if (this.screenMode == screenMode) duration = 0;

		final int containerPaddingTop_from;
		final int albumArtPaddingBottom_from;
		final int albumArtMarginRight_from;
		final int titleUnitPaddingBottom_from;
		final int titlePaddingBottom_from;
		final int titlePaddingBottom_to;
		final float titleTextSize_from;
		final float titleTextSize_to;
		final int seekBarPaddingBottom_from;
		final int seekBarPaddingBottom_to;
		final float seekBarAlpha_from;
		final int operationPaddingBottom_from;
		final int settingsHeight_from;

        final View view = rootView.getRootView().findViewById(R.id.player_separator);

		ViewGroup.LayoutParams layoutParams;
		switch (screenMode) {

			case MINI:
				containerPaddingTop_from = container.getPaddingTop();
				albumArtPaddingBottom_from = unit_albumArt.getPaddingBottom();
				albumArtMarginRight_from = ((ViewGroup.MarginLayoutParams) unit_albumArt.getLayoutParams()).rightMargin;
				titleUnitPaddingBottom_from = unit_title.getPaddingBottom();
				titlePaddingBottom_from = title.getPaddingBottom();
				titleTextSize_from = title.getTextSize();
				titleTextSize_to = activity.getResources().getDisplayMetrics().density * 20;
				seekBarPaddingBottom_from = unit_seekBar.getPaddingBottom();
				seekBarPaddingBottom_to = (int)(activity.getResources().getDisplayMetrics().density * -50);
				seekBarAlpha_from = unit_seekBar.getAlpha();
				operationPaddingBottom_from = unit_operations.getPaddingBottom();

				valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
				valueAnimator.setDuration(duration);
				valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						float rate = Float.parseFloat(animation.getAnimatedValue().toString());

						container.setPadding(0, (int) ((maxPadding_container - containerPaddingTop_from) * rate) + containerPaddingTop_from, 0, 0);
						otherView.setPadding(0, 0, 0, container.getHeight() - container.getPaddingTop());

						ViewGroup.MarginLayoutParams margin;

						unit_albumArt.setPadding(0, 0, 0, (int) (albumArtPaddingBottom_from * (1.0f - rate)));
						margin = (ViewGroup.MarginLayoutParams) unit_albumArt.getLayoutParams();
						margin.setMargins(0, 0, (int) ((maxPaddingRight_albumArt - albumArtMarginRight_from) * rate) + albumArtMarginRight_from, 0);
						unit_albumArt.setLayoutParams(margin);

						unit_title.setPadding(0, 0, 0, (int) (titleUnitPaddingBottom_from * (1.0f - rate)));
						title.setPadding(0, 0, 0, (int) (titlePaddingBottom_from * (1.0f - rate)));
						title.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)((titleTextSize_to - titleTextSize_from) * rate) + titleTextSize_from);
						artist.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize());

						unit_seekBar.setPadding(0, 0, 0, (int) ((seekBarPaddingBottom_to - seekBarPaddingBottom_from) *  rate) + seekBarPaddingBottom_from);
//						unit_seekBar.setAlpha(seekBarAlpha_from * (1.0f - rate));

						unit_operations.setPadding(0, 0, 0, (int) (operationPaddingBottom_from * (1.0f - rate)));
						margin = (ViewGroup.MarginLayoutParams) unit_operations.getLayoutParams();
						margin.setMargins((int) ((maxPaddingRight_albumArt - albumArtMarginRight_from) * rate) + albumArtMarginRight_from, 0, 0, 0);
						unit_operations.setLayoutParams(margin);

						if (rate != 1.0f) return;
						// 以下、アニメーション終了時の処理

						ViewGroup.LayoutParams layoutParams;
						int newPlayerHeight = (int) (activity.getResources().getDisplayMetrics().density * 70);
						layoutParams = container.getLayoutParams();
						layoutParams.height = newPlayerHeight;
						container.setLayoutParams(layoutParams);
						container.setPadding(0, 0, 0, 0);
						otherView.setPadding(0, 0, 0, newPlayerHeight);

						unit_seekBar.setVisibility(View.GONE);

						layoutParams = unit_settings.getLayoutParams();
						layoutParams.height = 0;
						unit_settings.setLayoutParams(layoutParams);


						//僭越ながら追記
                        title.setTextColor(Color.DKGRAY);
						title.canScrollHorizontally(1);
						title.setEllipsize(TextUtils.TruncateAt.END);
						title.setLines(1);
                        
                        artist.setTextColor(Color.DKGRAY);
                        artist.canScrollHorizontally(1);
                        artist.setEllipsize(TextUtils.TruncateAt.END);
                        artist.setLines(1);

                        unit_albumArt.setBackgroundColor(Color.WHITE);
                        title.setBackgroundColor(Color.TRANSPARENT);
                        artist.setBackgroundColor(Color.TRANSPARENT);

                        //画面上部のセパレーターを表示
                        view.setVisibility(View.VISIBLE);

                    }
				});
				valueAnimator.start();
				break;

			case FULL:
				containerPaddingTop_from = container.getPaddingTop();
				albumArtPaddingBottom_from = unit_albumArt.getPaddingBottom();
				albumArtMarginRight_from = ((ViewGroup.MarginLayoutParams) unit_albumArt.getLayoutParams()).rightMargin;
				titleUnitPaddingBottom_from = unit_title.getPaddingBottom();
				titlePaddingBottom_from = title.getPaddingBottom();
				titlePaddingBottom_to = (int)(activity.getResources().getDisplayMetrics().density * 8);
				titleTextSize_from = title.getTextSize();
				titleTextSize_to = activity.getResources().getDisplayMetrics().density * 30;
				seekBarPaddingBottom_from = unit_seekBar.getPaddingBottom();
				seekBarAlpha_from = unit_seekBar.getAlpha();
				operationPaddingBottom_from = unit_operations.getPaddingBottom();
				settingsHeight_from = unit_settings.getHeight();

				valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
				valueAnimator.setDuration(duration);
				valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						float rate = Float.parseFloat(animation.getAnimatedValue().toString());

						container.setPadding(0, (int) (containerPaddingTop_from * (1.0f - rate)), 0, 0);
						otherView.setPadding(0, 0, 0, container.getHeight() - container.getPaddingTop());

						ViewGroup.MarginLayoutParams margin;

						unit_albumArt.setPadding(0, 0, 0, (int) ((paddingBottom_unit_albumArt - albumArtPaddingBottom_from) * rate) + albumArtPaddingBottom_from);
						margin = (ViewGroup.MarginLayoutParams) unit_albumArt.getLayoutParams();
						margin.setMargins(0, 0, (int) (albumArtMarginRight_from * (1.0f - rate)), 0);
						unit_albumArt.setLayoutParams(margin);

						unit_title.setPadding(0, 0, 0, (int) ((paddingBottom_unit_title - titleUnitPaddingBottom_from) * rate) + titleUnitPaddingBottom_from);
						title.setPadding(0, 0, 0, (int) ((titlePaddingBottom_to - titlePaddingBottom_from) * rate) + titlePaddingBottom_from);
						title.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)((titleTextSize_to - titleTextSize_from) * rate) + titleTextSize_from);
						artist.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize());

						unit_seekBar.setPadding(0, 0, 0, (int) ((paddingBottom_unit_seekBar - seekBarPaddingBottom_from) * rate) + seekBarPaddingBottom_from);
						unit_seekBar.setAlpha((1.0f - seekBarAlpha_from) * rate + seekBarAlpha_from);

						unit_operations.setPadding(0, 0, 0, (int) ((paddingBottom_unit_operations - operationPaddingBottom_from) * rate) + operationPaddingBottom_from);
						margin = (ViewGroup.MarginLayoutParams) unit_operations.getLayoutParams();
						margin.setMargins((int) (albumArtMarginRight_from * (1.0f - rate)), 0, 0, 0);
						unit_operations.setLayoutParams(margin);

						ViewGroup.LayoutParams layoutParams = unit_settings.getLayoutParams();
						layoutParams.height = (int)(settingsHeight_from * (1.0f - rate));
						unit_settings.setLayoutParams(layoutParams);

                        //画面上部のセパレーターを非表示
                        view.setVisibility(View.GONE);

                        title.canScrollHorizontally(0);
                        title.setEllipsize(TextUtils.TruncateAt.END);
                        title.setMinLines(1);
                        title.setMaxLines(3);

                        artist.canScrollHorizontally(0);
                        artist.setEllipsize(TextUtils.TruncateAt.END);
                        artist.setMinLines(1);
                        artist.setMaxLines(3);


                        //色変更の必要を通知
                        if(changeListener != null){
                            changeListener.onColorDesignNeed();
                        }
					}
				});
				valueAnimator.start();
				break;

			case SETTINGS:
				settingsHeight_from = unit_settings.getHeight();

				valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
				valueAnimator.setDuration(duration);
				valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						float rate = Float.parseFloat(animation.getAnimatedValue().toString());

						ViewGroup.LayoutParams layoutParams = unit_settings.getLayoutParams();
						layoutParams.height = (int)((settingsHeight - settingsHeight_from) * rate) + settingsHeight_from;
						unit_settings.setLayoutParams(layoutParams);

					}
				});
				valueAnimator.start();



//				container.setPadding(0, 0, 0, 0);
//				otherView.setPadding(0, 0, 0, otherView.getHeight());

				break;
		}
		this.screenMode = screenMode;
	}


	/**
	 * 画面のタッチを検出した時に実行。
	 * ドラッグイベントを開始する。
	 *
	 * @param v
	 * @param event
	 * @return
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
		layoutParams.height = otherView.getHeight();
		container.setLayoutParams(layoutParams);
		switch (screenMode) {
			case FULL:
			case SETTINGS:
				break;
			case MINI:
				container.setPadding(0, maxPadding_container, 0, 0);
				unit_seekBar.setVisibility(View.VISIBLE);
				unit_seekBar.setAlpha(0.0f);
				break;
		}
		otherView.setPadding(0, 0, 0, otherView.getHeight() - container.getPaddingTop());

		ClipData data = ClipData.newPlainText("text", "text : " + v.toString());
		View.DragShadowBuilder shadow = new View.DragShadowBuilder(null);
		v.startDrag(data, shadow, v, 0);
		return false;
	}

	private int startPadding;
	private float startX;
	private float startY;
	private int distance;
	private boolean isSwiping;

	/**
	 * @param v
	 * @param event
	 * @return
	 */
	@Override
	public boolean onDrag(View v, DragEvent event) {
//		System.out.println("DragEvent.ACTION_DRAG_STARTED = " + DragEvent.ACTION_DRAG_STARTED);     // 1
//		System.out.println("DragEvent.ACTION_DRAG_LOCATION = " + DragEvent.ACTION_DRAG_LOCATION);   // 2
//		System.out.println("DragEvent.ACTION_DROP = " + DragEvent.ACTION_DROP);                     // 3
//		System.out.println("DragEvent.ACTION_DRAG_ENDED = " + DragEvent.ACTION_DRAG_ENDED);         // 4
//		System.out.println("DragEvent.ACTION_DRAG_ENTERED = " + DragEvent.ACTION_DRAG_ENTERED);     // 5
//		System.out.println("DragEvent.ACTION_DRAG_EXITED = " + DragEvent.ACTION_DRAG_EXITED);       // 6
		ViewGroup.LayoutParams layoutParams;
		switch (event.getAction()) {

			// スワイプ開始時
			case DragEvent.ACTION_DRAG_STARTED:
				// ACTION_DRAG_STARTEDとACTION_DRAG_LOCATIONでevent.getY()の値が合わないので
				// 以下のコードをACTION_DRAG_LOCATIONに移動
//				startPadding = container.getPaddingTop() - unit_settings.getHeight();
//				startY = event.getY();
//				isSwiping = true;
				break;

			// スワイプ中
			case DragEvent.ACTION_DRAG_LOCATION:
				if (!isSwiping) {
					// スワイプ開始時の処理
					startPadding = container.getPaddingTop() - unit_settings.getHeight();
					startX = event.getX();
					startY = event.getY();
					isSwiping = true;
					break;
				}

				distance = (int) (event.getY() - startY);
				int playerY = startPadding + distance;    // フル画面を基準にしたスワイプ中の再生画面のY座標の位置
				switch (screenMode) {
					case MINI:
						if (playerY < 0) {
							playerY = 0;
						}
						break;
					case SETTINGS:
						if (playerY > 0) {
							playerY = 0;
						}
						break;
				}

				// 再生画面の位置
				if (playerY > maxPadding_container) {
					// MINIよりさらに下の時

					container.setPadding(0, maxPadding_container, 0, 0);
					otherView.setPadding(0, 0, 0, container.getHeight() - container.getPaddingTop());

				} else if (playerY > 0) {
					// MINIとFULLの間の時

					container.setPadding(0, playerY, 0, 0);
					otherView.setPadding(0, 0, 0, container.getHeight() - container.getPaddingTop());

					float rate = 1.0f - (float) playerY / maxPadding_container;
					ViewGroup.MarginLayoutParams margin;

					unit_albumArt.setPadding(0, 0, 0, (int) (paddingBottom_unit_albumArt * rate));
					margin = (ViewGroup.MarginLayoutParams) unit_albumArt.getLayoutParams();
					margin.setMargins(0, 0, (int) (maxPaddingRight_albumArt * (1.0f - rate)), 0);
					unit_albumArt.setLayoutParams(margin);

					unit_title.setPadding(0, 0, 0, (int) (paddingBottom_unit_title * rate));
					title.setPadding(0, 0, 0, (int) (activity.getResources().getDisplayMetrics().density * 8 * rate));
					title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (int)(10 * rate) + 20);
					artist.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (int)(10 * rate) + 20);

					unit_seekBar.setPadding(0, 0, 0, (int) (paddingBottom_unit_seekBar * rate));
					unit_seekBar.setAlpha(rate);

					unit_operations.setPadding(0, 0, 0, (int) (paddingBottom_unit_operations * rate));
					margin = (ViewGroup.MarginLayoutParams) unit_operations.getLayoutParams();
					margin.setMargins((int) (maxPaddingRight_albumArt * (1.0f - rate)), 0, 0, 0);
					unit_operations.setLayoutParams(margin);

					layoutParams = unit_settings.getLayoutParams();
					layoutParams.height = 0;
					unit_settings.setLayoutParams(layoutParams);

				} else {
					// FULLとSETTINGSの間の時

					container.setPadding(0, 0, 0, 0);
					otherView.setPadding(0, 0, 0, container.getHeight());
					layoutParams = unit_settings.getLayoutParams();
					if (-playerY > settingsHeight) {
						playerY = -settingsHeight;
					}
					layoutParams.height = -playerY;
					unit_settings.setLayoutParams(layoutParams);
				}
				break;

			// スワイプ終了時
			case DragEvent.ACTION_DROP:
			case DragEvent.ACTION_DRAG_EXITED:
				if (!isSwiping) {
					break;
				}
				isSwiping = false;

				int dis = (int)(Math.pow(event.getX() - startX, 2) + Math.pow(event.getY() - startY, 2));
				if (screenMode == ScreenMode.MINI && dis < 100) {
					changeScreenMode(ScreenMode.FULL);
					break;
				}

				// スワイプした距離で分岐
				if (distance > 200) {
					// 下にスワイプ
					if (screenMode == ScreenMode.SETTINGS) {
						changeScreenMode(ScreenMode.FULL);
					} else {
						changeScreenMode(ScreenMode.MINI);
					}

				} else if (distance < -200) {
					// 上にスワイプ
					if (screenMode == ScreenMode.MINI) {
						changeScreenMode(ScreenMode.FULL);
					} else {
						changeScreenMode(ScreenMode.SETTINGS);
					}

				} else {
					// スワイプが少ない
					changeScreenMode(screenMode);
				}
				break;
		}
		return true;
	}


	public ScreenMode getScreenMode() {
		return screenMode;
	}
}