/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.uioverrides.touchcontrollers;

import static com.android.launcher3.LauncherState.HINT_STATE;
import static com.android.launcher3.LauncherState.NORMAL;
import static com.android.launcher3.LauncherState.OVERVIEW;
import static com.android.launcher3.LauncherStateManager.ATOMIC_OVERVIEW_PEEK_COMPONENT;
import static com.android.launcher3.Utilities.EDGE_NAV_BAR;
import static com.android.launcher3.util.VibratorWrapper.OVERVIEW_HAPTIC;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.MotionEvent;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.LauncherStateManager;
import com.android.launcher3.Utilities;
import com.android.launcher3.anim.AnimationSuccessListener;
import com.android.launcher3.anim.AnimatorSetBuilder;
import com.android.launcher3.userevent.nano.LauncherLogProto.Action.Touch;
import com.android.launcher3.util.VibratorWrapper;
import com.android.quickstep.util.StaggeredWorkspaceAnim;

/**
 * Touch controller which handles swipe and hold from the nav bar to go to Overview. Swiping above
 * the nav bar falls back to go to All Apps. Swiping from the nav bar without holding goes to the
 * first home screen instead of to Overview.
 */
public class NoButtonNavbarToOverviewTouchController extends FlingAndHoldTouchController {

    private boolean mDidTouchStartInNavBar;
    private boolean mReachedOverview;

    public NoButtonNavbarToOverviewTouchController(Launcher l) {
        super(l);
    }

    @Override
    protected boolean canInterceptTouch(MotionEvent ev) {
        mDidTouchStartInNavBar = (ev.getEdgeFlags() & EDGE_NAV_BAR) != 0;
        return super.canInterceptTouch(ev);
    }

    @Override
    protected LauncherState getTargetState(LauncherState fromState, boolean isDragTowardPositive) {
        if (fromState == NORMAL && mDidTouchStartInNavBar) {
            return HINT_STATE;
        } else if (fromState == OVERVIEW && isDragTowardPositive) {
            // Don't allow swiping up to all apps.
            return OVERVIEW;
        }
        return super.getTargetState(fromState, isDragTowardPositive);
    }

    @Override
    protected float initCurrentAnimation(int animComponents) {
        float progressMultiplier = super.initCurrentAnimation(animComponents);
        if (mToState == HINT_STATE) {
            // Track the drag across the entire height of the screen.
            progressMultiplier = -1 / getShiftRange();
        }
        return progressMultiplier;
    }

    @Override
    public void onDragStart(boolean start) {
        super.onDragStart(start);

        mReachedOverview = false;
    }

    @Override
    protected void updateSwipeCompleteAnimation(ValueAnimator animator, long expectedDuration,
            LauncherState targetState, float velocity, boolean isFling) {
        super.updateSwipeCompleteAnimation(animator, expectedDuration, targetState, velocity,
                isFling);
        if (targetState == HINT_STATE) {
            // Normally we compute the duration based on the velocity and distance to the given
            // state, but since the hint state tracks the entire screen without a clear endpoint, we
            // need to manually set the duration to a reasonable value.
            animator.setDuration(HINT_STATE.transitionDuration);
        }
    }

    @Override
    protected void onMotionPauseChanged(boolean isPaused) {
        if (mCurrentAnimation == null) {
            return;
        }
        mCurrentAnimation.dispatchOnCancelWithoutCancelRunnable(() -> {
            mLauncher.getStateManager().goToState(OVERVIEW, true, () -> {
                mReachedOverview = true;
                maybeSwipeInteractionToOverviewComplete();
            });
        });
        VibratorWrapper.INSTANCE.get(mLauncher).vibrate(OVERVIEW_HAPTIC);
    }

    private void maybeSwipeInteractionToOverviewComplete() {
        if (mReachedOverview && mDetector.isSettlingState()) {
            onSwipeInteractionCompleted(OVERVIEW, Touch.SWIPE);
        }
    }

    @Override
    protected boolean handlingOverviewAnim() {
        return mDidTouchStartInNavBar && super.handlingOverviewAnim();
    }

    @Override
    public boolean onDrag(float displacement, MotionEvent event) {
        if (mMotionPauseDetector.isPaused()) {
            // Stay in Overview.
            return true;
        }
        return super.onDrag(displacement, event);
    }

    @Override
    protected void goToOverviewOnDragEnd(float velocity) {
        float velocityDp = dpiFromPx(velocity);
        boolean isFling = Math.abs(velocityDp) > 1;
        LauncherStateManager stateManager = mLauncher.getStateManager();
        if (isFling) {
            // When flinging, go back to home instead of overview.
            if (velocity > 0) {
                stateManager.goToState(NORMAL, true,
                        () -> onSwipeInteractionCompleted(NORMAL, Touch.FLING));
            } else {
                StaggeredWorkspaceAnim staggeredWorkspaceAnim = new StaggeredWorkspaceAnim(
                        mLauncher, velocity, false /* animateOverviewScrim */);
                staggeredWorkspaceAnim.start();

                // StaggeredWorkspaceAnim doesn't animate overview, so we handle it here.
                stateManager.cancelAnimation();
                AnimatorSetBuilder builder = new AnimatorSetBuilder();
                long duration = OVERVIEW.transitionDuration;
                AnimatorSet anim = stateManager.createAtomicAnimation(
                        stateManager.getState(), NORMAL, builder,
                        ATOMIC_OVERVIEW_PEEK_COMPONENT, duration);
                anim.addListener(new AnimationSuccessListener() {
                    @Override
                    public void onAnimationSuccess(Animator animator) {
                        onSwipeInteractionCompleted(NORMAL, Touch.SWIPE);
                    }
                });
                anim.start();
            }
        } else {
            maybeSwipeInteractionToOverviewComplete();
        }
    }

    private float dpiFromPx(float pixels) {
        return Utilities.dpiFromPx(pixels, mLauncher.getResources().getDisplayMetrics());
    }
}
