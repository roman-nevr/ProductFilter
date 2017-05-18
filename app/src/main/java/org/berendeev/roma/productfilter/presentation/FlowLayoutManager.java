package org.berendeev.roma.productfilter.presentation;

import android.graphics.Point;
import android.graphics.Rect;
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
        int pos = 0;
        Point nextPoint = new Point(getPaddingLeft(), getPaddingTop());
        int itemCount = getItemCount();

        widthSpec = View.MeasureSpec.makeMeasureSpec(getHorizontalSpace(), View.MeasureSpec.AT_MOST);
        heightSpec = View.MeasureSpec.makeMeasureSpec(getVerticalSpace(), View.MeasureSpec.AT_MOST);

        fillDownFrom(pos, nextPoint, itemCount, recycler);
    }

    private Point fillDownFrom(int position, Point nextPoint, int itemCount, RecyclerView.Recycler recycler){
        boolean fillDown = true;
        while (fillDown && position < itemCount){

            View view = placeViewOnLayout(recycler, position);
            nextPoint = postOnLayout(view, nextPoint.x, nextPoint.y);
            fillDown = getDecoratedBottom(view) <= getBottomBorder();
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

    private int getRightBorder() {
        return getWidth() - getPaddingRight();
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
        if(dy > 0){
            if (lastRow(bottomView)){
                int bottomOffset = getVerticalSpace() - getDecoratedBottom(bottomView)
                            + getPaddingBottom();
                delta = Math.max(-dy, bottomOffset);
            }else {
                delta = -dy;
            }
        }else {
            if (firstRow(topView)){
                int topOffset = -getDecoratedTop(topView) + getPaddingTop();

                delta = Math.min(-dy, topOffset);
            }else {
                delta = -dy;
            }
        }

        offsetChildrenVertical(delta);

        updateLayout(recycler, delta < 0);

        return delta;
    }

    private void updateLayout(RecyclerView.Recycler recycler, boolean isScrollUp) {
        if (isScrollUp){
            recycleTopViews(recycler);
            int overScroll = addBottomViews(recycler);
            offsetChildrenVertical(overScroll);
        }else {
            recycleBottomViews(recycler);
            addTopViews(recycler);
        }

    }

    private void addTopViews(RecyclerView.Recycler recycler) {
        View view = getChildAt(0);
        if (getDecoratedTop(view) > 0){
            int adaptedPosition = getPosition(view) - 1;
            addTopRowFrom(adaptedPosition, recycler, getDecoratedTop(view));
        }
//        LinearLayoutManager layoutManager;
//        layoutManager.scrollVerticallyBy()
    }

    /*
    returns overflight
     */

    private int addBottomViews(RecyclerView.Recycler recycler) {
        int adapterPosition = lastViewAdapterPosition() + 1;
        int itemCount = getItemCount();
        Point nextPoint = new Point(getPaddingLeft(), lastViewDecoratedBottom());
        nextPoint = fillDownFrom(adapterPosition, nextPoint, itemCount, recycler);

        if (nextPoint.y < getBottomBorder()){
            return getBottomBorder() - nextPoint.y;
        }else {
            return 0;
        }
    }

    private void addTopRowFrom(int adaptedPosition, RecyclerView.Recycler recycler, int fromHeight) {
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
    }

    private void addBottomRow(int adapterPosition, RecyclerView.Recycler recycler, int height) {
        boolean fillRight = true;
        int width = 0;
        Point nextPoint = new Point(width, height);
        while (fillRight &&adapterPosition < getItemCount()){
            View view = placeViewOnLayout(recycler, adapterPosition);
            nextPoint = postOnLayout(view, nextPoint.x, nextPoint.y);
        }
    }

    private int lastViewAdapterPosition(){
        return getPosition(getChildAt(getChildCount() - 1));
    }

    private int lastViewDecoratedBottom(){
        return getDecoratedBottom(getChildAt(getChildCount() - 1));
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

    private int getBottomBorder() {
        return getVerticalSpace() - getPaddingTop();
    }

    private boolean firstRow(View view) {
        return getPosition(view) == 0;
    }

    private boolean lastRow(View view) {
        int position = getPosition(view);
        return position == getItemCount() - 1;
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
