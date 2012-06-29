package com.scheffsblend.smw;
import com.scheffsblend.smw.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class AnimatedImageView extends ImageView implements AnimationListener {

	private Animation zoomInPartial;
	private Animation zoomOutPartial;
	private boolean mDoAnimation = true;

	public AnimatedImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AnimatedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AnimatedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		// load the animations used for this button
		zoomInPartial = AnimationUtils.loadAnimation(this.getContext(), R.anim.zoom_in_partial);
		zoomOutPartial = AnimationUtils.loadAnimation(this.getContext(), R.anim.zoom_out_partial);
		
		// set our AnimationListener for these animations
		zoomInPartial.setAnimationListener(this);
		zoomOutPartial.setAnimationListener(this);
		
		// start the zoomInAnim first
		this.startAnimation(zoomOutPartial);
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
		if ( animation == this.zoomOutPartial ) {
			this.zoomInPartial.reset();
			this.clearAnimation();
			if( mDoAnimation )
				this.startAnimation( zoomInPartial );
		} else if ( animation == this.zoomInPartial ) {
			this.zoomOutPartial.reset();
			this.clearAnimation();
			if( mDoAnimation )
				this.startAnimation( zoomOutPartial );
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}
	
	public void stopAnimating() {
		this.clearAnimation();
	}
}
