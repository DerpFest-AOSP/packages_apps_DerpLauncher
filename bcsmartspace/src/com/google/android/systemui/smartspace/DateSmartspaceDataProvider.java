package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceTargetEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.bcsmartspace.R;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* compiled from: go/retraceme 8fa908dd7f7cdf82919b81f8a849d2e4d6278999a179aaed94e232ba94c0a60d */
/* loaded from: /Users/aksrivas/Downloads/SystemUIGoogle_decompile_xml/.cache/classes2.dex */
public final class DateSmartspaceDataProvider implements BcSmartspaceDataPlugin {
    public final Set mViews = new HashSet();
    public final Set mAttachListeners = new HashSet();
    public BcSmartspaceDataPlugin.SmartspaceEventNotifier mEventNotifier = null;
    public final View.OnAttachStateChangeListener mStateChangeListener = new View.OnAttachStateChangeListener() { // from class: com.google.android.systemui.smartspace.DateSmartspaceDataProvider.1
        @Override // android.view.View.OnAttachStateChangeListener
        public final void onViewAttachedToWindow(View view) {
            ((HashSet) DateSmartspaceDataProvider.this.mViews).add(view);
            Iterator it = ((HashSet) DateSmartspaceDataProvider.this.mAttachListeners).iterator();
            while (it.hasNext()) {
                ((View.OnAttachStateChangeListener) it.next()).onViewAttachedToWindow(view);
            }
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public final void onViewDetachedFromWindow(View view) {
            ((HashSet) DateSmartspaceDataProvider.this.mViews).remove(view);
            Iterator it = ((HashSet) DateSmartspaceDataProvider.this.mAttachListeners).iterator();
            while (it.hasNext()) {
                ((View.OnAttachStateChangeListener) it.next()).onViewDetachedFromWindow(view);
            }
        }
    };

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin
    public final void addOnAttachStateChangeListener(View.OnAttachStateChangeListener onAttachStateChangeListener) {
        ((HashSet) this.mAttachListeners).add(onAttachStateChangeListener);
        Iterator it = ((HashSet) this.mViews).iterator();
        while (it.hasNext()) {
            onAttachStateChangeListener.onViewAttachedToWindow((View) it.next());
        }
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin
    public final BcSmartspaceDataPlugin.SmartspaceView getView(ViewGroup viewGroup) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.date_plus_extras, viewGroup, false);
        inflate.addOnAttachStateChangeListener(this.mStateChangeListener);
        return (BcSmartspaceDataPlugin.SmartspaceView) inflate;
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin
    public final void notifySmartspaceEvent(SmartspaceTargetEvent smartspaceTargetEvent) {
        BcSmartspaceDataPlugin.SmartspaceEventNotifier smartspaceEventNotifier = this.mEventNotifier;
        if (smartspaceEventNotifier != null) {
            smartspaceEventNotifier.notifySmartspaceEvent(smartspaceTargetEvent);
        }
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin
    public final void registerSmartspaceEventNotifier(BcSmartspaceDataPlugin.SmartspaceEventNotifier smartspaceEventNotifier) {
        this.mEventNotifier = smartspaceEventNotifier;
    }
}
