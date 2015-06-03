package com.netcosports.flip;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

/**
 * Created by stephane on 11/28/13.
 */
public class FlipCountdownView extends View {
	private Paint mPaint;
	private int mTextColor;
	private int mTextColorDark;
	private int mTextHeight;

	public FlipCountdownView(Context context) {
		this(context, null);
	}

	public FlipCountdownView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FlipCountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		final Resources res = context.getResources();

		float textSize;
		if (attrs == null) {
			mTextColor = res.getColor(R.color.flip_countdown_text);
			textSize = res.getDimension(R.dimen.flip_countdown_text_size);
		} else {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlipCountdownView);
			mTextColor = a.getColor(R.styleable.FlipCountdownView_flipTextColor, res.getColor(R.color.flip_countdown_text));
			textSize = a.getDimension(R.styleable.FlipCountdownView_flipTextSize, res.getDimension(R.dimen.flip_countdown_text_size));
			a.recycle();
		}
		mTextColorDark = darker(mTextColor, 0.8);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextSize(textSize);
		mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
		Rect bounds = new Rect();
		mPaint.getTextBounds("0", 0, 1, bounds);
		mTextHeight = bounds.height();

		duration = res.getInteger(android.R.integer.config_shortAnimTime);
	}

	private int mCurrentValue = -1;
	private int mValue = -1;

	public void setValue(int value) {
		if (value >= 0) {
			mValue = value;
			invalidate();
		}
	}

	private float duration;

	public void setDuration(int duration) {
		this.duration = duration;
	}

	private long startAnimTimestamp = 0;

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		super.onDraw(canvas);

		if (mValue >= 0) {
			if (mCurrentValue == -1 || mCurrentValue == mValue) {
				mCurrentValue = mValue;
				drawNumber(canvas, mValue);
			} else if (mCurrentValue != mValue) {
				//animation in progress

				if (startAnimTimestamp == 0) {
					//start animation
					startAnimTimestamp = AnimationUtils.currentAnimationTimeMillis();
					drawNumber(canvas, mCurrentValue);

					ViewCompat.postInvalidateOnAnimation(this);
				} else {
					final float currentPercentage =
							(AnimationUtils.currentAnimationTimeMillis() - startAnimTimestamp) / duration;
					if (currentPercentage >= 1) {
						//animation finished
						mCurrentValue = mValue;
						startAnimTimestamp = 0;
						drawNumber(canvas, mCurrentValue);
					} else {
						if (currentPercentage < 0.5f) {
							drawHalfUpperNumber(canvas, mCurrentValue, 1 - 2 * currentPercentage);
							drawHalfBottomNumber(canvas, mCurrentValue);
						} else {
							drawHalfUpperNumber(canvas, mValue);
							drawHalfBottomNumber(canvas, mValue, 2 * (currentPercentage - 0.5f));
						}

						ViewCompat.postInvalidateOnAnimation(this);
					}
				}
			}
		}
	}

	private void drawNumber(Canvas canvas, int value) {
		drawHalfBottomNumber(canvas, value);
		drawHalfUpperNumber(canvas, value);
	}

	private void drawHalfBottomNumber(Canvas canvas, int value) {
		drawHalfBottomNumber(canvas, value, 1);
	}

	private void drawHalfBottomNumber(Canvas canvas, int value, float percent) {
		percent = getRealPercent(percent);
		if (percent != 0) {
			String text = String.valueOf(value);

			canvas.save();
			canvas.clipRect(getPaddingLeft(),
					getMeasuredHeight() / 2,
					getMeasuredWidth() - getPaddingRight(),
					getMeasuredHeight() - getPaddingBottom()
			);
			canvas.scale(1, percent, 0, getMeasuredHeight() / 2);

			mPaint.setColor(mTextColor);
			float widthText = mPaint.measureText(text);
			canvas.drawText(text,
					(getMeasuredWidth() - widthText) / 2,
					(getMeasuredHeight() + mTextHeight) / 2 - 2,
					mPaint
			);
			canvas.restore();
		}
	}

	private static int alphaPercent(int alphaPercent, int color) {
		return Color.argb(
				alphaPercent * 255 / 100, Color.red(color), Color.green(color), Color.blue(color)
		);
	}

	private static int darker(int color, double percent) {
		return Color.argb(
				Color.alpha(color),
				(int) (percent * Color.red(color)),
				(int) (percent * Color.green(color)),
				(int) (percent * Color.blue(color))
		);
	}

	private void drawHalfUpperNumber(Canvas canvas, int value) {
		drawHalfUpperNumber(canvas, value, 1);
	}

	private void drawHalfUpperNumber(Canvas canvas, int value, float percent) {
		percent = getRealPercent(percent);
		if (percent != 0) {
			String text = String.valueOf(value);

			canvas.save();
			canvas.clipRect(getPaddingLeft(), getPaddingTop(),
					getMeasuredWidth() - getPaddingRight(),
					getMeasuredHeight() / 2
			);
			canvas.scale(1, percent, 0, getMeasuredHeight() / 2);

			mPaint.setColor(mTextColorDark);
			float widthText = mPaint.measureText(text);
			canvas.drawText(text,
					(getMeasuredWidth() - widthText) / 2,
					(getMeasuredHeight() + mTextHeight) / 2 - 2,
					mPaint
			);
			canvas.restore();
		}
	}

	private float getRealPercent(float percent) {
		if (percent > 1)
			percent = 1;
		else if (percent < 0)
			percent = 0;
		return percent;
	}
}
