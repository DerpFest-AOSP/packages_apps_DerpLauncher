package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceAction;
import android.app.smartspace.SmartspaceTarget;
import android.app.smartspace.SmartspaceTargetEvent;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.bcsmartspace.R;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;
import com.android.systemui.plugins.FalsingManager;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLogger;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.utils.ContentDescriptionUtil;
import java.util.List;
import java.util.Objects;

public class WeatherSmartspaceView extends FrameLayout implements BcSmartspaceDataPlugin.SmartspaceTargetListener, BcSmartspaceDataPlugin.SmartspaceView {
    public static final boolean DEBUG = Log.isLoggable("WeatherSmartspaceView", 3);
    public final ContentObserver mAodSettingsObserver;
    public BcSmartspaceDataPlugin mDataProvider;
    public float mDozeAmount;
    public final DoubleShadowIconDrawable mIconDrawable;
    public final int mIconSize;
    public boolean mIsAodEnabled;
    public BcSmartspaceCardLoggingInfo mLoggingInfo;
    public int mPrimaryTextColor;
    public final boolean mRemoveTextDescent;
    public final int mTextDescentExtraPadding;
    public String mUiSurface;
    public DoubleShadowTextView mView;

    public WeatherSmartspaceView(Context context) {
        this(context, null);
    }

    @Override // android.view.ViewGroup, android.view.View
    public final void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (TextUtils.equals(this.mUiSurface, BcSmartspaceDataPlugin.UI_SURFACE_LOCK_SCREEN_AOD)) {
            boolean z = false;
            try {
                getContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor("doze_always_on"), false, this.mAodSettingsObserver, -1);
            } catch (Exception e) {
                Log.w("WeatherSmartspaceView", "Unable to register DOZE_ALWAYS_ON content observer: ", e);
            }
            Context context = getContext();
            if (Settings.Secure.getIntForUser(context.getContentResolver(), "doze_always_on", 0, context.getUserId()) == 1) {
                z = true;
            }
            this.mIsAodEnabled = z;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public final void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().getContentResolver().unregisterContentObserver(this.mAodSettingsObserver);
        BcSmartspaceDataPlugin bcSmartspaceDataPlugin = this.mDataProvider;
        if (bcSmartspaceDataPlugin != null) {
            bcSmartspaceDataPlugin.unregisterListener(this);
        }
    }

    @Override // android.view.View
    public final void onFinishInflate() {
        super.onFinishInflate();
        this.mView = (DoubleShadowTextView) findViewById(R.id.weather_text_view);
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceTargetListener
    public final void onSmartspaceTargetsUpdated(List list) {
        SmartspaceAction headerAction;
        DoubleShadowTextView doubleShadowTextView = this.mView;
        if (list.size() > 1) {
            return;
        }
        if (list.isEmpty() && TextUtils.equals(this.mUiSurface, BcSmartspaceDataPlugin.UI_SURFACE_DREAM)) {
            return;
        }
        if (list.isEmpty()) {
            BcSmartspaceTemplateDataUtils.updateVisibility(this.mView, 8);
            return;
        }
        BcSmartspaceTemplateDataUtils.updateVisibility(this.mView, 0);
        SmartspaceTarget smartspaceTarget = (SmartspaceTarget) list.get(0);
        if (smartspaceTarget.getFeatureType() != 1 || (headerAction = smartspaceTarget.getHeaderAction()) == null) {
            return;
        }
        CharSequence title = headerAction.getTitle();
        this.mView.setText(title.toString());
        ContentDescriptionUtil.setFormattedContentDescription("WeatherSmartspaceView", this.mView, title, headerAction.getContentDescription());
        this.mIconDrawable.setIcon(BcSmartSpaceUtil.getIconDrawableWithCustomSize(headerAction.getIcon(), getContext(), this.mIconSize));
        this.mView.setCompoundDrawablesRelative(this.mIconDrawable, null, null, null);
        if (this.mRemoveTextDescent) {
            this.mView.setPaddingRelative(0, 0, 0, this.mTextDescentExtraPadding - ((int) Math.floor(doubleShadowTextView.getPaint().getFontMetrics().descent)));
        }
        BcSmartspaceCardLoggingInfo.Builder builder = new BcSmartspaceCardLoggingInfo.Builder();
        builder.mInstanceId = InstanceId.create(smartspaceTarget);
        builder.mFeatureType = smartspaceTarget.getFeatureType();
        builder.mDisplaySurface = BcSmartSpaceUtil.getLoggingDisplaySurface(this.mDozeAmount, this.mUiSurface);
        getContext().getPackageManager();
        builder.mUid = -1;
        this.mLoggingInfo = builder.build();
        DoubleShadowTextView doubleShadowTextView2 = this.mView;
        final BcSmartspaceDataPlugin bcSmartspaceDataPlugin = this.mDataProvider;
        final BcSmartspaceDataPlugin.SmartspaceEventNotifier[] smartspaceEventNotifier = {null};
        if (bcSmartspaceDataPlugin != null) {
            Objects.requireNonNull(bcSmartspaceDataPlugin);
            smartspaceEventNotifier[0] = new BcSmartspaceDataPlugin.SmartspaceEventNotifier() {
                @Override
                public void notifySmartspaceEvent(SmartspaceTargetEvent smartspaceTargetEvent) {
                    smartspaceEventNotifier[0].notifySmartspaceEvent(smartspaceTargetEvent);
                }
            };
        }
        BcSmartSpaceUtil.setOnClickListener((View) doubleShadowTextView2, smartspaceTarget, headerAction, smartspaceEventNotifier[0], "WeatherSmartspaceView", this.mLoggingInfo);
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void registerDataProvider(BcSmartspaceDataPlugin bcSmartspaceDataPlugin) {
        BcSmartspaceDataPlugin bcSmartspaceDataPlugin2 = this.mDataProvider;
        if (bcSmartspaceDataPlugin2 != null) {
            bcSmartspaceDataPlugin2.unregisterListener(this);
        }
        this.mDataProvider = bcSmartspaceDataPlugin;
        bcSmartspaceDataPlugin.registerListener(this);
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void setDozeAmount(float f) {
        this.mDozeAmount = f;
        this.mView.setTextColor(ColorUtils.blendARGB(this.mPrimaryTextColor, -1, f));
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
        BcSmartspaceCardLoggingInfo.Builder builder = new BcSmartspaceCardLoggingInfo.Builder();
        BcSmartspaceCardLoggingInfo bcSmartspaceCardLoggingInfo = this.mLoggingInfo;
        builder.mInstanceId = bcSmartspaceCardLoggingInfo.mInstanceId;
        builder.mFeatureType = bcSmartspaceCardLoggingInfo.mFeatureType;
        builder.mDisplaySurface = loggingDisplaySurface;
        builder.mUid = bcSmartspaceCardLoggingInfo.mUid;
        BcSmartspaceCardLoggingInfo bcSmartspaceCardLoggingInfo2 = new BcSmartspaceCardLoggingInfo(builder);
        if (DEBUG) {
            Log.d("WeatherSmartspaceView", "@" + Integer.toHexString(hashCode()) + ", setDozeAmount: Logging SMARTSPACE_CARD_SEEN, loggingSurface = " + loggingDisplaySurface);
        }
        BcSmartspaceCardLogger.log(BcSmartspaceEvent.SMARTSPACE_CARD_SEEN, bcSmartspaceCardLoggingInfo2);
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
    public final void setPrimaryTextColor(int i) {
        this.mPrimaryTextColor = i;
        this.mView.setTextColor(ColorUtils.blendARGB(i, -1, this.mDozeAmount));
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceView
    public final void setUiSurface(String str) {
        if (!isAttachedToWindow()) {
            this.mUiSurface = str;
            return;
        }
        throw new IllegalStateException("Must call before attaching view to window.");
    }

    public WeatherSmartspaceView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* JADX WARN: Type inference failed for: r6v2, types: [com.google.android.systemui.smartspace.WeatherSmartspaceView$1] */
    public WeatherSmartspaceView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mUiSurface = null;
        this.mDozeAmount = 0.0f;
        this.mLoggingInfo = null;
        this.mAodSettingsObserver = new ContentObserver(new Handler()) { // from class: com.google.android.systemui.smartspace.WeatherSmartspaceView.1
            @Override // android.database.ContentObserver
            public final void onChange(boolean z) {
                WeatherSmartspaceView weatherSmartspaceView = WeatherSmartspaceView.this;
                boolean z2 = WeatherSmartspaceView.DEBUG;
                Context context2 = weatherSmartspaceView.getContext();
                boolean z3 = false;
                if (Settings.Secure.getIntForUser(context2.getContentResolver(), "doze_always_on", 0, context2.getUserId()) == 1) {
                    z3 = true;
                }
                WeatherSmartspaceView weatherSmartspaceView2 = WeatherSmartspaceView.this;
                if (weatherSmartspaceView2.mIsAodEnabled == z3) {
                    return;
                }
                weatherSmartspaceView2.mIsAodEnabled = z3;
            }
        };
        context.getTheme().applyStyle(2132017751, false);
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.WeatherSmartspaceView, 0, 0);
        try {
            int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(1, context.getResources().getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_size));
            int dimensionPixelSize2 = obtainStyledAttributes.getDimensionPixelSize(0, context.getResources().getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_inset));
            this.mRemoveTextDescent = obtainStyledAttributes.getBoolean(2, false);
            this.mTextDescentExtraPadding = obtainStyledAttributes.getDimensionPixelSize(3, 0);
            obtainStyledAttributes.recycle();
            this.mIconSize = dimensionPixelSize;
            this.mIconDrawable = new DoubleShadowIconDrawable(context, dimensionPixelSize, dimensionPixelSize2);
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }
}
