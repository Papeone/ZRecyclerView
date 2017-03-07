package com.feinno.zzy.callback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.animation.TranslateAnimation;

public class SwipeTounchCallback extends ItemTouchHelper.SimpleCallback {

    public interface ItemSwipeCallback {

        void itemSwiped(int position, int direction);

    }

    private final ItemSwipeCallback itemSwipeCallback;

    private int bgColorLeft;
    private int bgColorRight;
    private Drawable leaveBehindDrawableLeft;
    private Drawable leaveBehindDrawableRight;

    private Paint bgPaint;
    private int horizontalMargin = Integer.MAX_VALUE;

    public SwipeTounchCallback(SwipeTounchCallback.ItemSwipeCallback itemSwipeCallback, Drawable leaveBehindDrawableLeft) {
        this(itemSwipeCallback, leaveBehindDrawableLeft, ItemTouchHelper.LEFT);
    }

    public SwipeTounchCallback(SwipeTounchCallback.ItemSwipeCallback itemSwipeCallback, Drawable leaveBehindDrawableLeft, int swipeDirs) {
        this(itemSwipeCallback, leaveBehindDrawableLeft, swipeDirs, Color.RED);
    }

    public SwipeTounchCallback(ItemSwipeCallback itemSwipeCallback, Drawable leaveBehindDrawableLeft, int swipeDirs, @ColorInt int bgColor) {
        super(0, swipeDirs);
        this.itemSwipeCallback = itemSwipeCallback;
        this.leaveBehindDrawableLeft = leaveBehindDrawableLeft;
        this.bgColorLeft = bgColor;
    }

    public SwipeTounchCallback withLeaveBehindSwipeLeft(Drawable d) {
        this.leaveBehindDrawableLeft = d;
        setDefaultSwipeDirs(getSwipeDirs(null, null) | ItemTouchHelper.LEFT);
        return this;
    }

    public SwipeTounchCallback withLeaveBehindSwipeRight(Drawable d) {
        this.leaveBehindDrawableRight = d;
        setDefaultSwipeDirs(getSwipeDirs(null, null) | ItemTouchHelper.RIGHT);
        return this;
    }

    public SwipeTounchCallback withHorizontalMarginDp(Context ctx, int dp) {
        return withHorizontalMarginPx((int) (ctx.getResources().getDisplayMetrics().density * dp));
    }

    public SwipeTounchCallback withHorizontalMarginPx(int px) {
        horizontalMargin = px;
        return this;
    }

    public SwipeTounchCallback withBackgroundSwipeLeft(@ColorInt int bgColor) {
        bgColorLeft = bgColor;
        return this;
    }

    public SwipeTounchCallback withBackgroundSwipeRight(@ColorInt int bgColor) {
        bgColorRight = bgColor;
        return this;
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //TODO should disallow this if in swiped state
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            viewHolder.itemView.setTranslationX(0);
            viewHolder.itemView.setTranslationY(0);
        } else {
            TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
            anim.setFillAfter(true);
            anim.setDuration(0);
            viewHolder.itemView.startAnimation(anim);
        }
        int position = viewHolder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            itemSwipeCallback.itemSwiped(position, direction);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // not enabled
        return false;
    }

    //Inspired/modified from: https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete/blob/master/app/src/main/java/net/nemanjakovacevic/recyclerviewswipetodelete/MainActivity.java
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        if (viewHolder.getAdapterPosition() == RecyclerView.NO_POSITION) {
            return;
        }
        if (Math.abs(dX) > Math.abs(dY)) {
            boolean isLeft = dX < 0;
            if (bgPaint == null) {
                bgPaint = new Paint();
                if (horizontalMargin == Integer.MAX_VALUE) {
                    withHorizontalMarginDp(recyclerView.getContext(), 16);
                }
            }
            bgPaint.setColor(isLeft ? bgColorLeft : bgColorRight);

            if (bgPaint.getColor() != Color.TRANSPARENT) {
                int left = isLeft ? itemView.getRight() + (int) dX : itemView.getLeft();
                int right = isLeft ? itemView.getRight() : (itemView.getLeft() + (int) dX);
                c.drawRect(left, itemView.getTop(), right, itemView.getBottom(), bgPaint);
            }

            Drawable drawable = isLeft ? leaveBehindDrawableLeft : leaveBehindDrawableRight;
            if (drawable != null) {
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicWidth();

                int left;
                int right;
                if (isLeft) {
                    left = itemView.getRight() - horizontalMargin - intrinsicWidth;
                    right = itemView.getRight() - horizontalMargin;
                } else {
                    left = itemView.getLeft() + horizontalMargin;
                    right = itemView.getLeft() + horizontalMargin + intrinsicWidth;
                }
                int top = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int bottom = top + intrinsicHeight;
                drawable.setBounds(left, top, right, bottom);

                drawable.draw(c);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
