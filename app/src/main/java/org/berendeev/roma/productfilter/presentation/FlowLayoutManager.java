package org.berendeev.roma.productfilter.presentation;

import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import java.util.HashMap;

public class FlowLayoutManager extends RecyclerView.LayoutManager{

    private SparseArray<View> viewCache = new SparseArray<>();

    @Override public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        initialFill(recycler);
//        fillDown(recycler);
//        LinearLayoutManager
    }

    private void initialFill(RecyclerView.Recycler recycler){
        int pos = 0;
        boolean fillDown = true;
        boolean fillRight = true;
        int height = getHeight();
        int viewTop = getPaddingTop();
        int viewLeft = getPaddingLeft();
        int itemCount = getItemCount();

        int widthIncrement = 0;
        int heightIncrement = 0;

        while (fillDown && pos < itemCount){

            View view = recycler.getViewForPosition(pos);
            addView(view);
            final int widthSpec = View.MeasureSpec.makeMeasureSpec(getHorizontalSpace(), View.MeasureSpec.AT_MOST);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(getVerticalSpace(), View.MeasureSpec.AT_MOST);
            measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
            int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
            int decoratedMeasuredHeight = getDecoratedMeasuredHeight(view);

            if (viewLeft + decoratedMeasuredWidth <= getRightBorder()){
                //fill right
                widthIncrement = decoratedMeasuredWidth;
                heightIncrement = 0;
            }else {
                //fill bottom
                viewTop += decoratedMeasuredHeight;
                widthIncrement = decoratedMeasuredWidth;
//                viewTop = getDecoratedBottom(view);
                fillDown = viewTop <= height;
                viewLeft = getPaddingLeft();
            }
            layoutDecorated(view, viewLeft, viewTop, viewLeft + decoratedMeasuredWidth, viewTop + decoratedMeasuredHeight);
            viewLeft += widthIncrement;
            viewTop += heightIncrement;
            pos++;
        }
    }

    private int getRightBorder() {
        return getWidth() - getPaddingRight();
    }

    private void fill(RecyclerView.Recycler recycler) {
        View anchorView = getAnchorView();
        viewCache.clear();

        //Помещаем вьюшки в кэш и...
        for (int i = 0, cnt = getChildCount(); i < cnt; i++) {
            View view = getChildAt(i);
            int pos = getPosition(view);
            viewCache.put(pos, view);
        }

        //... и удалям из лэйаута
        for (int i = 0; i < viewCache.size(); i++) {
            detachView(viewCache.valueAt(i));
        }

        fillUp(anchorView, recycler);
        fillDown(anchorView, recycler);

        //отправляем в корзину всё, что не потребовалось в этом цикле лэйаута
        //эти вьюшки или ушли за экран или не понадобились, потому что соответствующие элементы
        //удалились из адаптера
        for (int i=0; i < viewCache.size(); i++) {
            recycler.recycleView(viewCache.valueAt(i));
        }
    }

    private void fillUp(View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos = 0;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
//            anchorTop = a
        }

        boolean fillUp = true;
        int pos = anchorPos - 1;
        int viewBottom = anchorTop; //нижняя граница следующей вьюшки будет начитаться от верхней границы предыдущей

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.AT_MOST);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(getHeight(), View.MeasureSpec.AT_MOST);
        while (fillUp && pos >= 0) {
            View view = viewCache.get(pos); //проверяем кэш
            if (view == null){
                //если вьюшки нет в кэше - просим у recycler новую, измеряем и лэйаутим её
                view = recycler.getViewForPosition(pos);
                addView(view, 0);
//                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                measureChildWithMargins(view, 0, 0);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                layoutDecorated(view, 0, viewBottom - view.getMeasuredHeight(), decoratedMeasuredWidth, viewBottom);
            } else {
                //если вьюшка есть в кэше - просто аттачим её обратно
                //нет необходимости проводить measure/layout цикл.
                attachView(view);
                viewCache.remove(pos);
            }
            viewBottom = getDecoratedTop(view);
            fillUp = viewBottom > 0;
            pos--;
        }

    }
    private void fillDown(@Nullable View anchorView, RecyclerView.Recycler recycler){
        int anchorPosition = 0;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPosition = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        }
        int position = anchorPosition;
        boolean fillDown = true;
        int height = getHeight();
        int viewTop = anchorTop;
        int itemCount = getItemCount();

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.AT_MOST);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(getHeight(), View.MeasureSpec.AT_MOST);

        while (fillDown && position < itemCount){
            View view = viewCache.get(position);
            if (view == null){
                view = recycler.getViewForPosition(position);
                addView(view);
//                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                measureChildWithMargins(view, 0, 0);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                layoutDecorated(view, 0, viewTop, view.getMeasuredWidth(), viewTop + view.getMeasuredHeight());
            } else {
                attachView(view);
                viewCache.remove(position);
            }
            viewTop = getDecoratedBottom(view);
            fillDown = viewTop <= height;
            position++;


//            addView(view);
//            //Todo find out about decoration size
//            measureChildWithMargins(view, 0, 0);
//            layoutDecorated(view, 0, viewTop, view.getMeasuredWidth(), viewTop + view.getMeasuredHeight());
//            viewTop = getDecoratedBottom(view);
//            fillDown = viewTop > height;
//            position++;
        }
    }

    /*
    final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY);
    final int heightSpec = View.MeasureSpec.makeMeasureSpec(getHeight(), View.MeasureSpec.EXACTLY);
    measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
     */

    //метод вернет вьюшку с максимальной видимой площадью
    private View getAnchorView() {
        int childCount = getChildCount();
        HashMap<Integer, View> viewsOnScreen = new HashMap<>();
        Rect mainRect = new Rect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            int top = getDecoratedTop(view);
            int bottom = getDecoratedBottom(view);
            int left = getDecoratedLeft(view);
            int right = getDecoratedRight(view);
            Rect viewRect = new Rect(left, top, right, bottom);
            boolean intersect = viewRect.intersect(mainRect);
            if (intersect){
                int square = viewRect.width() * viewRect.height();
                viewsOnScreen.put(square, view);
            }
        }
        if (viewsOnScreen.isEmpty()){
            return null;
        }
        Integer maxSquare = null;
        for (Integer square : viewsOnScreen.keySet()) {
            if (maxSquare == null){
                maxSquare = square;
            } else {
                maxSquare = Math.max(maxSquare, square);
            }
        }
        return viewsOnScreen.get(maxSquare);
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
        return 0;
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
