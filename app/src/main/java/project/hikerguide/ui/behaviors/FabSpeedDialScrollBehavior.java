package project.hikerguide.ui.behaviors;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.FabSpeedDialBehaviour;

/**
 * Created by Alvin on 8/15/2017.
 */

public class FabSpeedDialScrollBehavior extends FabSpeedDialBehaviour {

    public FabSpeedDialScrollBehavior() {
    }

    public FabSpeedDialScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FabSpeedDial child, View directTargetChild, View target, int nestedScrollAxes) {
        super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);

        return true;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, final FabSpeedDial child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        // Check the direction being scrolled
        if (dyConsumed > 0) {

            // Hide the FAB
            child.hide();

            // Show the FAB again after 1 second
            child.postDelayed(new Runnable() {
                @Override
                public void run() {

                    child.show();
                }
            }, 1000);
        } else {
            // Scrolling up, show the FAB
            child.show();
        }
    }
}
