package com.google.android.systemui.smartspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.android.systemui.bcsmartspace.R;
import java.util.Locale;
import java.util.Objects;

public class IcuDateTextView extends DoubleShadowTextView {
    public final ContentObserver mAodSettingsObserver;
    public DateFormat mFormatter;
    public Handler mHandler;
    public final BroadcastReceiver mIntentReceiver;
    public boolean mIsAodEnabled;
    public boolean mIsInteractive;
    public String mText;
    public final Runnable mTicker;
    public boolean mUpdatesOnAod;

    public IcuDateTextView(Context context) {
        this(context, null);
    }

    public IcuDateTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, 0);
        this.mAodSettingsObserver = new ContentObserver(new Handler()) { // from class: com.google.android.systemui.smartspace.IcuDateTextView.1
            @Override // android.database.ContentObserver
            public final void onChange(boolean z) {
                IcuDateTextView icuDateTextView = IcuDateTextView.this;
                Context context2 = icuDateTextView.getContext();
                boolean z2 = false;
                if (Settings.Secure.getIntForUser(context2.getContentResolver(), "doze_always_on", 0, context2.getUserId()) == 1) {
                    z2 = true;
                }
                IcuDateTextView icuDateTextView2 = IcuDateTextView.this;
                if (icuDateTextView2.mIsAodEnabled == z2) {
                    return;
                }
                icuDateTextView2.mIsAodEnabled = z2;
                icuDateTextView2.rescheduleTicker();
            }
        };
        this.mTicker = this::onTimeTick;
        this.mIntentReceiver = new BroadcastReceiver() { // from class: com.google.android.systemui.smartspace.IcuDateTextView.1
            @Override // android.content.BroadcastReceiver
            public final void onReceive(Context context2, Intent intent) {
                if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                    IcuDateTextView icuDateTextView = IcuDateTextView.this;
                    icuDateTextView.mIsInteractive = true;
                    icuDateTextView.rescheduleTicker();
                } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    IcuDateTextView icuDateTextView2 = IcuDateTextView.this;
                    icuDateTextView2.mIsInteractive = false;
                    icuDateTextView2.rescheduleTicker();
                } else {
                    IcuDateTextView icuDateTextView3 = IcuDateTextView.this;
                    icuDateTextView3.onTimeChanged(!"android.intent.action.TIME_TICK".equals(intent.getAction()));
                }
            }
        };
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mUpdatesOnAod) {
            boolean z = false;
            try {
                getContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor("doze_always_on"), false, this.mAodSettingsObserver, -1);
            } catch (Exception e) {
                Log.w("IcuDateTextView", "Unable to register DOZE_ALWAYS_ON content observer: ", e);
            }
            Context context = getContext();
            if (Settings.Secure.getIntForUser(context.getContentResolver(), "doze_always_on", 0, context.getUserId()) == 1) {
                z = true;
            }
            this.mIsAodEnabled = z;
        }
        this.mHandler = new Handler();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        getContext().registerReceiver(this.mIntentReceiver, intentFilter);
        this.mIsInteractive = ((PowerManager) getContext().getSystemService(PowerManager.class)).isInteractive();
        onTimeChanged(true);
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mHandler != null) {
            getContext().unregisterReceiver(this.mIntentReceiver);
            this.mHandler = null;
        }
        if (this.mUpdatesOnAod) {
            getContext().getContentResolver().unregisterContentObserver(this.mAodSettingsObserver);
        }
    }

    private void onTimeTick() {
        onTimeChanged(false);
        if (this.mHandler != null) {
            long uptimeMillis = SystemClock.uptimeMillis();
            this.mHandler.postAtTime(this.mTicker, uptimeMillis + (1000 - (uptimeMillis % 1000)));
        }
    }

    public final void rescheduleTicker() {
        Handler handler = this.mHandler;
        if (handler == null) {
            return;
        }
        handler.removeCallbacks(this.mTicker);
        if ((!this.mIsInteractive && (!this.mUpdatesOnAod || !this.mIsAodEnabled)) || !isAggregatedVisible()) {
            return;
        }
        this.mTicker.run();
    }

    public final void setUpdatesOnAod() {
        if (!isAttachedToWindow()) {
            this.mUpdatesOnAod = true;
            return;
        }
        throw new IllegalStateException("Must call before attaching view to window.");
    }

    @Override // android.view.View
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        rescheduleTicker();
    }

    public void onTimeChanged(boolean force) {
        if (!isShown()) {
            return;
        }
        if (this.mFormatter == null || force) {
            DateFormat format = DateFormat.getInstanceForSkeleton(getContext().getString(R.string.smartspace_icu_date_pattern), Locale.getDefault());
            this.mFormatter = format;
            format.setContext(DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE);
        }
        String format2 = this.mFormatter.format(Long.valueOf(System.currentTimeMillis()));
        if (!Objects.equals(this.mText, format2)) {
            this.mText = format2;
            setText(format2);
            setContentDescription(format2);
        }
    }
}
