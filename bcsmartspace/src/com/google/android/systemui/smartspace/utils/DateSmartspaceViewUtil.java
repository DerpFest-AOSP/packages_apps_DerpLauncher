package com.google.android.systemui.smartspace.utils;

import android.app.smartspace.SmartspaceTargetEvent;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;

public final class DateSmartspaceViewUtil implements BcSmartspaceDataPlugin.SmartspaceEventNotifier {
    public final int Id;
    public final BcSmartspaceDataPlugin mBcSmartspaceDataPlugin;

    public DateSmartspaceViewUtil(BcSmartspaceDataPlugin bcSmartspaceDataPlugin, int i) {
        this.Id = i;
        this.mBcSmartspaceDataPlugin = bcSmartspaceDataPlugin;
    }

    @Override // com.android.systemui.plugins.BcSmartspaceDataPlugin.SmartspaceEventNotifier
    public final void notifySmartspaceEvent(SmartspaceTargetEvent smartspaceTargetEvent) {
        int i = this.Id;
        BcSmartspaceDataPlugin bcSmartspaceDataPlugin = this.mBcSmartspaceDataPlugin;
        switch (i) {
            case 0:
                bcSmartspaceDataPlugin.notifySmartspaceEvent(smartspaceTargetEvent);
                return;
            case 1:
                bcSmartspaceDataPlugin.notifySmartspaceEvent(smartspaceTargetEvent);
                return;
            default:
                bcSmartspaceDataPlugin.notifySmartspaceEvent(smartspaceTargetEvent);
                return;
        }
    }
}
