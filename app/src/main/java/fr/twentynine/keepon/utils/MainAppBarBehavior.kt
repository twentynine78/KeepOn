package fr.twentynine.keepon.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import fr.twentynine.keepon.R

class MainAppBarBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.Behavior(context, attrs) {
    private var isSheetTouched = false

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        isSheetTouched = target.id == R.id.bottomSheetScrollView
        return !isSheetTouched && super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_CANCEL) {
            isSheetTouched = false
        }
        return !isSheetTouched && super.onInterceptTouchEvent(parent, child, ev)
    }
}
