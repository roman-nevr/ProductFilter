package org.berendeev.roma.productfilter.presentation;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.appcompat.BuildConfig;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

public class FlowLayoutManager extends RecyclerView.LayoutManager{

    private int heightSpec;
    private int widthSpec;


    @Override public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        initialFill(recycler);
    }

    private void initialFill(RecyclerView.Recycler recycler){
        Point nextPoint = new Point(getPaddingLeft(), getPaddingTop());

        widthSpec = View.MeasureSpec.makeMeasureSpec(getHorizontalSpace(), View.MeasureSpec.AT_MOST);
        heightSpec = View.MeasureSpec.makeMeasureSpec(getVerticalSpace(), View.MeasureSpec.AT_MOST);

        fillDownFrom(0, nextPoint, recycler);
    }

    private Point fillDownFrom(int position, Point nextPoint, RecyclerView.Recycler recycler){
        boolean fillDown = true;
        int itemCount = getItemCount();
        while (fillDown && position < itemCount){

            View view = placeViewOnLayout(recycler, position);
            nextPoint = postOnLayout(view, nextPoint.x, nextPoint.y);
            fillDown = nextPoint.y <= getBottomBorder();
            if (!fillDown){
                detachView(view);
                recycler.recycleView(view);
            }
            position++;
        }
        return nextPoint;
    }

    private View placeViewOnLayout(RecyclerView.Recycler recycler, int pos){
        View view = recycler.getViewForPosition(pos);
        addView(view);
        measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
        return view;
    }

    private View placeViewOnLayout(RecyclerView.Recycler recycler, int pos, int where){
        View view = recycler.getViewForPosition(pos);
        addView(view, where);
        measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
        return view;
    }

    private Point postOnLayout(View view, int viewLeft, int viewTop){

        int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
        int decoratedMeasuredHeight = getDecoratedMeasuredHeight(view);

        int widthIncrement = 0;
        int heightIncrement = 0;

        if (viewLeft + decoratedMeasuredWidth <= getRightBorder()){
            widthIncrement = decoratedMeasuredWidth;
            heightIncrement = 0;
        }else {
            viewTop += decoratedMeasuredHeight;
            widthIncrement = decoratedMeasuredWidth;
            viewLeft = getPaddingLeft();
        }
        layoutDecorated(view, viewLeft, viewTop, viewLeft + decoratedMeasuredWidth, viewTop + decoratedMeasuredHeight);
        return new Point(viewLeft + widthIncrement, viewTop + heightIncrement);
    }

    private void measureChildWithDecorationsAndMargin(View child, int widthSpec, int heightSpec) {
        Rect decorRect = new Rect();
        calculateItemDecorationsForChild(child, decorRect);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        widthSpec = updateSpecWithExtra(widthSpec, lp.leftMargin + decorRect.left,
                lp.rightMargin + decorRect.right);
        heightSpec = updateSpecWithExtra(heightSpec, lp.topMargin + decorRect.top,
                lp.bottomMargin + decorRect.bottom);
        child.measure(widthSpec, heightSpec);
    }

    private int updateSpecWithExtra(int spec, int startInset, int endInset) {
        if (startInset == 0 && endInset == 0) {
            return spec;
        }
        final int mode = View.MeasureSpec.getMode(spec);
        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(
                    View.MeasureSpec.getSize(spec) - startInset - endInset, mode);
        }
        return spec;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {

        if (getChildCount() == 0){
            return 0;
        }

        final View topView = getChildAt(0);
        final View bottomView = getChildAt(getChildCount()-1);

        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
        if (viewSpan <= getVerticalSpace()) {
            return 0;
        }

        int delta;

        delta = scrollBy(recycler, dy);

        if (delta < 0 && BuildConfig.DEBUG){
            throw new IllegalArgumentException("delta negative");
        }

        return delta;
    }

    private int scrollBy(RecyclerView.Recycler recycler, int dy) {
        offsetChildrenVertical(-dy);

        boolean isScrollUp = dy > 0;
        int overScroll;
        if (isScrollUp){
            overScroll = addBottomViews(recycler);
            offsetChildrenVertical(overScroll);
            recycleTopViews(recycler);
        }else {
            overScroll = addTopViews(recycler);
            offsetChildrenVertical(overScroll);
            recycleBottomViews(recycler);
        }
        return dy - overScroll;

    }

    private Point fillUpFrom(int position, Point nextPoint, RecyclerView.Recycler recycler){
        boolean fillUp = true;
        while (fillUp && position >= 0){
            nextPoint = addTopRowFrom(position, recycler, nextPoint.y);
            fillUp = nextPoint.y > getTopBorder();
            position = getPosition(getChildAt(0)) - 1;
        }
        return nextPoint;
    }

    private Point addTopRowFrom(int adaptedPosition, RecyclerView.Recycler recycler, int fromHeight) {
        boolean fillLeft = true;
        int width = 0;
        List<View> views = new LinkedList<>();
        while (fillLeft && adaptedPosition >= 0){
            View view = placeViewOnLayout(recycler, adaptedPosition, 0);
            width += getDecoratedMeasuredWidth(view);
            fillLeft = width <= getHorizontalSpace();
            if (fillLeft){
                views.add(0, view);
            }else {
                detachView(view);
                recycler.recycleView(view);
            }
            adaptedPosition --;
        }
        Point nextPoint = new Point(getPaddingLeft(), fromHeight - getDecoratedMeasuredHeight(views.get(0)));
        for (View view : views) {
            nextPoint = postOnLayout(view, nextPoint.x, nextPoint.y);
        }
        return nextPoint;
    }

    /*
    returns overscroll
     */
    private int addBottomViews(RecyclerView.Recycler recycler) {
        int adapterPosition = bottomViewAdapterPosition() + 1;
        Point nextPoint = new Point(getPaddingLeft(), bottomViewDecoratedBottom());
        if(nextPoint.y < getBottomBorder()){
            nextPoint = fillDownFrom(adapterPosition, nextPoint, recycler);
            if (nextPoint.y < getBottomBorder() && bottomViewAdapterPosition() == getItemCount() - 1){
                return getBottomBorder() - nextPoint.y;
            }else {
                return 0;
            }
        }else {
            return 0;
        }

    }

    private int addTopViews(RecyclerView.Recycler recycler) {
        int adapterPosition = getPosition(getChildAt(0));
        Point nextPoint = getTopRightPoint(getChildAt(0));
        if (nextPoint.y > getTopBorder()){
            nextPoint = fillUpFrom(adapterPosition - 1, nextPoint, recycler);
            if (nextPoint.y > getTopBorder() && topViewAdapterPosition() == 0){
                return getTopBorder() - nextPoint.y;
            }else {
                return 0;
            }
        }else {
            return 0;
        }
    }

    private void recycleBottomViews(RecyclerView.Recycler recycler) {
        int position = getChildCount() - 1;
        View view = getChildAt(position);
        while (getDecoratedTop(view) > getBottomBorder()) {
            detachView(view);
            recycler.recycleView(view);
            position--;
            view = getChildAt(position);
        }
    }

    private void recycleTopViews(RecyclerView.Recycler recycler) {
        View view = getChildAt(0);
        while (getDecoratedBottom(view) < 0){
            detachView(view);
            recycler.recycleView(view);
            view = getChildAt(0);
        }
    }

    private int bottomViewAdapterPosition(){
        return getPosition(getChildAt(getChildCount() - 1));
    }

    private int topViewAdapterPosition(){
        return getPosition(getChildAt(0));
    }

    private int getRightBorder() {
        return getWidth() - getPaddingRight();
    }

    private int bottomViewDecoratedBottom(){
        return getDecoratedBottom(getChildAt(getChildCount() - 1));
    }

    private int getBottomBorder() {
        return getVerticalSpace() - getPaddingTop();
    }

    private int getTopBorder(){
        return getPaddingTop();
    }

    @Override public boolean canScrollHorizontally() {
        return false;
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private Point getTopRightPoint(View view){
        return new Point(getDecoratedRight(view), getDecoratedTop(view));
    }


}
