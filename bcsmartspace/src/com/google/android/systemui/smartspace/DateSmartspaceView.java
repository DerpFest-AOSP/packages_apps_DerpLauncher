package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceAction;
import android.app.smartspace.SmartspaceTarget;
import android.content.ComponentName;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.bcsmartspace.R;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;
import com.android.systemui.plugins.FalsingManager;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLogger;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.utils.DateSmartspaceViewUtil;

public class DateSmartspaceView extends LinearLayout implements BcSmartspaceDataPlugin.SmartspaceView {
    public static final boolean DEBUG = Log.isLoggable("DateSmartspaceView", 3);
    public final ContentObserver mAodSettingsObserver;
    public int mCurrentTextColor;
    public BcSmartspaceDataPlugin mDataProvider;
    public final SmartspaceAction mDateAction;
    public final SmartspaceTarget mDateTarget;
    public IcuDateTextView mDateView;
    public final DoubleShadowIconDrawable mDndIconDrawable;
    public ImageView mDndImageView;
    public float mDozeAmount;
    public boolean mIsAodEnabled;
    public BcSmartspaceCardLoggingInfo mLoggingInfo;
    public final BcNextAlarmData mNextAlarmData;
    public final DoubleShadowIconDrawable mNextAlarmIconDrawable;
    public DoubleShadowTextView mNextAlarmTextView;
    public int mPrimaryTextColor;
    public String mUiSurface;

    public DateSmartspaceView(Context context) {
        this(context, null);
    }

    @Override // android.view.ViewGroup, android.view.View
    public final void onAttachedToWindow() {
        DateSmartspaceViewUtil dateSmartspaceViewUtil;
        super.onAttachedToWindow();
        if (TextUtils.equals(this.mUiSurface, BcSmartspaceDataPlugin.UI_SURFACE_LOCK_SCREEN_AOD)) {
            boolean z = false;
            try {
                getContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor("doze_always_on"), false, this.mAodSettingsObserver, -1);
            } catch (Exception e) {
                Log.w("DateSmartspaceView", "Unable to register DOZE_ALWAYS_ON content observer: ", e);
            }
            Context context = getContext();
            if (Settings.Secure.getIntForUser(context.getContentResolver(), "doze_always_on", 0, context.getUserId()) == 1) {
                z = true;
            }
            this.mIsAodEnabled = z;
        }
        BcSmartspaceCardLoggingInfo.Builder builder = new BcSmartspaceCardLoggingInfo.Builder();
        builder.mInstanceId = InstanceId.create(this.mDateTarget);
        builder.mFeatureType = this.mDateTarget.getFeatureType();
        builder.mDisplaySurface = BcSmartSpaceUtil.getLoggingDisplaySurface(this.mDozeAmount, this.mUiSurface);
        getContext().getPackageManager();
        builder.mUid = -1;
        BcSmartspaceCardLoggingInfo bcSmartspaceCardLoggingInfo = new BcSmartspaceCardLoggingInfo(builder);
        this.mLoggingInfo = bcSmartspaceCardLoggingInfo;
        IcuDateTextView icuDateTextView = this.mDateView;
        SmartspaceTarget smartspaceTarget = this.mDateTarget;
        SmartspaceAction smartspaceAction = this.mDateAction;
        BcSmartspaceDataPlugin bcSmartspaceDataPlugin = this.mDataProvider;
        if (bcSmartspaceDataPlugin == null) {
            dateSmartspaceViewUtil = null;
        } else {
            dateSmartspaceViewUtil = new DateSmartspaceViewUtil(bcSmartspaceDataPlugin, 2);
        }
        BcSmartSpaceUtil.setOnClickListener(icuDateTextView, smartspaceTarget, smartspaceAction, dateSmartspaceViewUtil, "DateSmartspaceView", bcSmartspaceCardLoggingInfo);
    }

    @Override // android.view.ViewGroup, android.view.View
    public final void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().getContentResolver().unregisterContentObserver(this.mAodSettingsObserver);
    }

    @Override // android.view.View
    public final void onFinishInflate() {
        super.onFinishInflate();
        this.mDateView = (IcuDateTextView) findViewById(R.id.date);
        this.mNextAlarmTextView = (DoubleShadowTextView) findViewById(R.id.alarm_text_view);
        this.mDndImageView = (ImageView) findViewById(R.id.dnd_icon);
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void registerDataProvider(BcSmartspaceDataPlugin bcSmartspaceDataPlugin) {
        this.mDataProvider = bcSmartspaceDataPlugin;
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void setDnd(Drawable drawable, String str) {
        if (drawable == null) {
            BcSmartspaceTemplateDataUtils.updateVisibility(this.mDndImageView, 8);
        } else {
            this.mDndIconDrawable.setIcon(drawable.mutate());
            this.mDndImageView.setImageDrawable(this.mDndIconDrawable);
            this.mDndImageView.setContentDescription(str);
            BcSmartspaceTemplateDataUtils.updateVisibility(this.mDndImageView, 0);
        }
        updateColorForExtras();
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void setDozeAmount(float f) {
        this.mDozeAmount = f;
        int blendARGB = ColorUtils.blendARGB(this.mPrimaryTextColor, -1, f);
        this.mCurrentTextColor = blendARGB;
        this.mDateView.setTextColor(blendARGB);
        updateColorForExtras();
        if (this.mLoggingInfo == null) {
            return;
        }
        int loggingDisplaySurface = BcSmartSpaceUtil.getLoggingDisplaySurface(this.mDozeAmount, this.mUiSurface);
        if (loggingDisplaySurface == -1) {
            return;
        }
        if (loggingDisplaySurface == 3 && !this.mIsAodEnabled) {
            return;
        }
        if (DEBUG) {
            Log.d("DateSmartspaceView", "@" + Integer.toHexString(hashCode()) + ", setDozeAmount: Logging SMARTSPACE_CARD_SEEN, loggingSurface = " + loggingDisplaySurface);
        }
        BcSmartspaceCardLoggingInfo.Builder builder = new BcSmartspaceCardLoggingInfo.Builder();
        BcSmartspaceCardLoggingInfo bcSmartspaceCardLoggingInfo = this.mLoggingInfo;
        builder.mInstanceId = bcSmartspaceCardLoggingInfo.mInstanceId;
        builder.mFeatureType = bcSmartspaceCardLoggingInfo.mFeatureType;
        builder.mDisplaySurface = loggingDisplaySurface;
        builder.mUid = bcSmartspaceCardLoggingInfo.mUid;
        BcSmartspaceCardLoggingInfo bcSmartspaceCardLoggingInfo2 = new BcSmartspaceCardLoggingInfo(builder);
        BcSmartspaceEvent bcSmartspaceEvent = BcSmartspaceEvent.SMARTSPACE_CARD_SEEN;
        BcSmartspaceCardLogger.log(bcSmartspaceEvent, bcSmartspaceCardLoggingInfo2);
        if (this.mNextAlarmData.mImage != null) {
            BcSmartspaceCardLoggingInfo.Builder builder2 = new BcSmartspaceCardLoggingInfo.Builder();
            builder2.mInstanceId = InstanceId.create("upcoming_alarm_card_94510_12684");
            builder2.mFeatureType = 23;
            builder2.mDisplaySurface = loggingDisplaySurface;
            builder2.mUid = this.mLoggingInfo.mUid;
            BcSmartspaceCardLogger.log(bcSmartspaceEvent, new BcSmartspaceCardLoggingInfo(builder2));
        }
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void setFalsingManager(FalsingManager falsingManager) {
        BcSmartSpaceUtil.sFalsingManager = falsingManager;
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void setIntentStarter(BcSmartspaceDataPlugin.IntentStarter intentStarter) {
        BcSmartSpaceUtil.sIntentStarter = intentStarter;
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void setNextAlarm(Drawable drawable, String str) {
        DateSmartspaceViewUtil dateSmartspaceViewUtil0;
        DateSmartspaceViewUtil dateSmartspaceViewUtil1;
        BcNextAlarmData bcNextAlarmData = this.mNextAlarmData;
        bcNextAlarmData.mImage = drawable;
        if (drawable != null) {
            drawable.mutate();
        }
        bcNextAlarmData.mDescription = str;
        if (this.mNextAlarmData.mImage == null) {
            BcSmartspaceTemplateDataUtils.updateVisibility(this.mNextAlarmTextView, 8);
        } else {
            this.mNextAlarmTextView.setContentDescription(getContext().getString(2131951778, str));
            this.mNextAlarmTextView.setText(this.mNextAlarmData.getDescription(null));
            DoubleShadowIconDrawable doubleShadowIconDrawable = this.mNextAlarmIconDrawable;
            Drawable drawable2 = this.mNextAlarmData.mImage;
            int dimensionPixelSize = getContext().getResources().getDimensionPixelSize(2131165881);
            drawable2.setBounds(0, 0, dimensionPixelSize, dimensionPixelSize);
            doubleShadowIconDrawable.setIcon(drawable2);
            this.mNextAlarmTextView.setCompoundDrawablesRelative(this.mNextAlarmIconDrawable, null, null, null);
            BcSmartspaceTemplateDataUtils.updateVisibility(this.mNextAlarmTextView, 0);
            BcNextAlarmData bcNextAlarmData2 = this.mNextAlarmData;
            DoubleShadowTextView doubleShadowTextView = this.mNextAlarmTextView;
            BcSmartspaceDataPlugin bcSmartspaceDataPlugin = this.mDataProvider;
            if (bcSmartspaceDataPlugin == null) {
                dateSmartspaceViewUtil0 = null;
            } else {
                dateSmartspaceViewUtil0 = new DateSmartspaceViewUtil(bcSmartspaceDataPlugin, 0);
            }
            bcNextAlarmData2.setOnClickListener(doubleShadowTextView, null, dateSmartspaceViewUtil0, BcSmartSpaceUtil.getLoggingDisplaySurface(this.mDozeAmount, this.mUiSurface));
            BcNextAlarmData bcNextAlarmData3 = this.mNextAlarmData;
            DoubleShadowTextView doubleShadowTextView2 = this.mNextAlarmTextView;
            BcSmartspaceDataPlugin bcSmartspaceDataPlugin2 = this.mDataProvider;
            if (bcSmartspaceDataPlugin2 == null) {
                dateSmartspaceViewUtil1 = null;
            } else {
                dateSmartspaceViewUtil1 = new DateSmartspaceViewUtil(bcSmartspaceDataPlugin2, 1);
            }
            bcNextAlarmData3.setOnClickListener(doubleShadowTextView2, null, dateSmartspaceViewUtil1, BcSmartSpaceUtil.getLoggingDisplaySurface(this.mDozeAmount, this.mUiSurface));
        }
        updateColorForExtras();
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void setPrimaryTextColor(int i) {
        this.mPrimaryTextColor = i;
        int blendARGB = ColorUtils.blendARGB(i, -1, this.mDozeAmount);
        this.mCurrentTextColor = blendARGB;
        this.mDateView.setTextColor(blendARGB);
        updateColorForExtras();
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void setUiSurface(String str) {
        if (!isAttachedToWindow()) {
            this.mUiSurface = str;
            if (TextUtils.equals(str, BcSmartspaceDataPlugin.UI_SURFACE_LOCK_SCREEN_AOD)) {
                this.mDateView.setUpdatesOnAod();
                return;
            }
            return;
        }
        throw new IllegalStateException("Must call before attaching view to window.");
    }

    public final void updateColorForExtras() {
        DoubleShadowTextView doubleShadowTextView = this.mNextAlarmTextView;
        if (doubleShadowTextView != null) {
            doubleShadowTextView.setTextColor(this.mCurrentTextColor);
            this.mNextAlarmIconDrawable.setTint(this.mCurrentTextColor);
        }
        ImageView imageView = this.mDndImageView;
        if (imageView != null && imageView.getDrawable() != null) {
            imageView.getDrawable().setTint(this.mCurrentTextColor);
            imageView.invalidate();
        }
    }

    public DateSmartspaceView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* JADX WARN: Type inference failed for: r4v10, types: [com.google.android.systemui.smartspace.DateSmartspaceView$1] */
    public DateSmartspaceView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mUiSurface = null;
        this.mDozeAmount = 0.0f;
        this.mDateTarget = new SmartspaceTarget.Builder("date_card_794317_92634", new ComponentName(getContext(), getClass()), getContext().getUser()).setFeatureType(1).build();
        this.mDateAction = new SmartspaceAction.Builder("dateId", "Date").setIntent(BcSmartSpaceUtil.getOpenCalendarIntent()).build();
        this.mNextAlarmData = new BcNextAlarmData();
        this.mAodSettingsObserver = new ContentObserver(new Handler()) { // from class: com.google.android.systemui.smartspace.DateSmartspaceView.1
            @Override // android.database.ContentObserver
            public final void onChange(boolean z) {
                DateSmartspaceView dateSmartspaceView = DateSmartspaceView.this;
                boolean z2 = DateSmartspaceView.DEBUG;
                Context context2 = dateSmartspaceView.getContext();
                boolean z3 = false;
                if (Settings.Secure.getIntForUser(context2.getContentResolver(), "doze_always_on", 0, context2.getUserId()) == 1) {
                    z3 = true;
                }
                DateSmartspaceView dateSmartspaceView2 = DateSmartspaceView.this;
                if (dateSmartspaceView2.mIsAodEnabled == z3) {
                    return;
                }
                dateSmartspaceView2.mIsAodEnabled = z3;
            }
        };
        context.getTheme().applyStyle(2132017751, false);
        this.mNextAlarmIconDrawable = new DoubleShadowIconDrawable(context);
        this.mDndIconDrawable = new DoubleShadowIconDrawable(context);
    }
}
