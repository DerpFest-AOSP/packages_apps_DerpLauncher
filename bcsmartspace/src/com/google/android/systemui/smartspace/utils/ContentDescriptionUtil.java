package com.google.android.systemui.smartspace.utils;

import android.util.Log;
import android.view.View;
import java.util.Arrays;

/* compiled from: go/retraceme 8fa908dd7f7cdf82919b81f8a849d2e4d6278999a179aaed94e232ba94c0a60d */
/* loaded from: /Users/aksrivas/Downloads/SystemUIGoogle_decompile_xml/.cache/classes2.dex */
public abstract class ContentDescriptionUtil {
    public static final void setFormattedContentDescription(String str, View view, CharSequence charSequence, CharSequence charSequence2) {
        boolean z;
        CharSequence string;
        boolean z2 = false;
        if (charSequence != null && charSequence.length() != 0) {
            z = false;
        } else {
            z = true;
        }
        if (z) {
            string = charSequence2;
        } else {
            if (charSequence2 == null || charSequence2.length() == 0) {
                z2 = true;
            }
            if (z2) {
                string = charSequence;
            } else {
                string = view.getContext().getString(2131952544, charSequence2, charSequence);
            }
        }
        Log.i(str, String.format("setFormattedContentDescription: text=%s, iconDescription=%s, contentDescription=%s", Arrays.copyOf(new Object[]{charSequence, charSequence2, string}, 3)));
        view.setContentDescription(string);
    }
}
