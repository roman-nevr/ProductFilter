package org.berendeev.roma.productfilter.presentation;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FlowLayoutManager extends RecyclerView.LayoutManager{

    private SparseArray<View> viewCache = new SparseArray<>();
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

        fillFrom(pos, nextPoint, itemCount, recycler);
    }

    private void fillFrom(int position, Point nextPoint, int itemCount, RecyclerView.Recycler recycler){
        boolean fillDown = true;
        while (fillDown && position < itemCount){

            View view = placeViewOnLayout(recycler, position);
            nextPoint = postOnLayout(view, nextPoint.x, nextPoint.y);
            fillDown = getDecoratedBottom(view) <= getBottomBorder();
            position++;
        }
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
            //fill right
            widthIncrement = decoratedMeasuredWidth;
            heightIncrement = 0;
        }else {
            //fill bottom
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

        //Take top measurements from the top-left child
        final View topView = getChildAt(0);
        //Take bottom measurements from the bottom-right child.
        final View bottomView = getChildAt(getChildCount()-1);

        //Optimize the case where the entire data set is too small to scroll
        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
        if (viewSpan <= getVerticalSpace()) {
            //We cannot scroll in either direction
            return 0;
        }

        int delta;
        if(dy > 0){// Contents are scrolling up
            //Check against bottom bound
            if (lastRow(bottomView)){
                int bottomOffset = getVerticalSpace() - getDecoratedBottom(bottomView)
                            + getPaddingBottom();
                delta = Math.max(-dy, bottomOffset);
            }else {
                delta = -dy;
            }
        }else {// Contents are scrolling down
            //Check against top bound
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

//        int delta = scrollVerticallyInternal(dy);
//        offsetChildrenVertical(-delta);
////        fill(recycler);

        /*if (getChildCount() == 0) {
            return 0;
        }

        //Take top measurements from the top-left child
        final View topView = getChildAt(0);
        //Take bottom measurements from the bottom-right child.
        final View bottomView = getChildAt(getChildCount()-1);

        //Optimize the case where the entire data set is too small to scroll
        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
        if (viewSpan <= getVerticalSpace()) {
            //We cannot scroll in either direction
            return 0;
        }

        int delta;

        if (dy > 0) { // Contents are scrolling up
            //Check against bottom bound
            if (isLastRow()) {
                //If we've reached the last row, enforce limits
//                int bottomOffset;
//                if (rowOfIndex(getChildCount() - 1) >= (maxRowCount - 1)) {
//                    //We are truly at the bottom, determine how far
//                    bottomOffset = getVerticalSpace() - getDecoratedBottom(bottomView)
//                            + getPaddingBottom();
//                } else {
//                    *//*
//                     * Extra space added to account for allowing bottom space in the grid.
//                     * This occurs when the overlap in the last row is not large enough to
//                     * ensure that at least one element in that row isn't fully recycled.
//                     *//*
//                    bottomOffset = getVerticalSpace() - (getDecoratedBottom(bottomView)
//                            + mDecoratedChildHeight) + getPaddingBottom();
//                }

                delta = Math.max(-dy, bottomOffset);
            } else {
                //No limits while the last row isn't visible
                delta = -dy;
            }
        } else { // Contents are scrolling down
            //Check against top bound
            if (isFirstRow()) {
//                int topOffset = -getDecoratedTop(topView) + getPaddingTop();

                delta = Math.min(-dy, topOffset);
            } else {
                delta = -dy;
            }
        }

        offsetChildrenVertical(delta);

        return delta;*/
    }

    private void updateLayout(RecyclerView.Recycler recycler, boolean isScrollUp) {
        logChilds();
        if (isScrollUp){
            recycleTopViews(recycler);
            addBottomViews(recycler);
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

    private void logChilds(){
        int childsCount = getChildCount();
        List<String> list = new ArrayList<>();
        for (int index = 0; index < childsCount; index++) {

            System.out.print(getChildText(index));
        }
        System.out.println("");

    }

    private String getChildText(int index){
        String childText = ((TextView)((CardView) ((LinearLayout) getChildAt(index)).getChildAt(0)).getChildAt(0)).getText().toString();
        return childText;
    }

    private void addBottomViews(RecyclerView.Recycler recycler) {
        int position = getChildCount() - 1;
        View view = getChildAt(position);
        if (getDecoratedBottom(view) > getBottomBorder()){
            //add views
            int adaptedPosition = getPosition(view);
            Point nextPoint = getTopRightPoint(view);

            fillFrom(adaptedPosition + 1, nextPoint, getItemCount(), recycler);
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
//        int position = 0;
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

    private int scrollVerticallyInternal(int dy) {
        int childCount = getChildCount();
        int itemCount = getItemCount();
        if (childCount == 0){
            return 0;
        }

        final View topView = getChildAt(0);
        final View bottomView = getChildAt(childCount - 1);

        //Случай, когда все вьюшки поместились на экране
        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
        if (viewSpan <= getHeight()) {
            return 0;
        }

        int delta = 0;
        //если контент уезжает вниз
        if (dy < 0){
            View firstView = getChildAt(0);
            int firstViewAdapterPos = getPosition(firstView);
            if (firstViewAdapterPos > 0){ //если верхняя вюшка не самая первая в адаптере
                delta = dy;
            } else { //если верхняя вьюшка самая первая в адаптере и выше вьюшек больше быть не может
                int viewTop = getDecoratedTop(firstView);
                delta = Math.max(viewTop, dy);
            }
        } else if (dy > 0){ //если контент уезжает вверх
//            View lastView = getChildAt(childCount - 1);
            View lastView = bottomView;
            int lastViewAdapterPos = getPosition(lastView);
            if (lastViewAdapterPos < itemCount - 1){ //если нижняя вюшка не самая последняя в адаптере
                delta = dy;
            } else { //если нижняя вьюшка самая последняя в адаптере и ниже вьюшек больше быть не может
                int viewBottom = getDecoratedBottom(lastView);
                int parentBottom = getHeight();
                delta = Math.min(viewBottom - parentBottom, dy);
            }
        }
        return delta;
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
    /*
    private void fillUp(@Nullable View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos = 0;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        }

        boolean fillUp = true;
        int pos = anchorPos - 1;
        int viewBottom = anchorTop; //нижняя граница следующей вьюшки будет начитаться от верхней границы предыдущей
        int viewHeight = (int) (getHeight() * VIEW_HEIGHT_PERCENT);
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY);
        while (fillUp && pos >= 0){
            View view = viewCache.get(pos); //проверяем кэш
            if (view == null){
                //если вьюшки нет в кэше - просим у recycler новую, измеряем и лэйаутим её
                view = recycler.getViewForPosition(pos);
                addView(view, 0);
                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                layoutDecorated(view, 0, viewBottom - viewHeight, decoratedMeasuredWidth, viewBottom);
            } else {
                //если вьюшка есть в кэше - просто аттачим её обратно
                //нет необходимости проводить measure/layout цикл.
                attachView(view);
                viewCache.remove(pos);
            }
            viewBottom = getDecoratedTop(view);
            fillUp = (viewBottom > 0);
            pos--;
        }
    }
     */

    /*
    private void fillDown(@Nullable View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos = 0;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        }

        int pos = anchorPos;
        boolean fillDown = true;
        int height = getHeight();
        int viewTop = anchorTop;
        int itemCount = getItemCount();
        int viewHeight = (int) (getHeight() * VIEW_HEIGHT_PERCENT);
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY);

        while (fillDown && pos < itemCount){
            View view = viewCache.get(pos);
            if (view == null){
                view = recycler.getViewForPosition(pos);
                addView(view);
                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                layoutDecorated(view, 0, viewTop, decoratedMeasuredWidth, viewTop + viewHeight);
            } else {
                attachView(view);
                viewCache.remove(pos);
            }
            viewTop = getDecoratedBottom(view);
            fillDown = viewTop <= height;
            pos++;
        }
    }
     */
}
