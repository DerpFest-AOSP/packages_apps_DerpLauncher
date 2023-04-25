package com.android.launcher3.util;

import android.content.res.Resources;
import androidx.annotation.ArrayRes;

import java.util.Random;

public final class RandomResUtils {
    public static String getRandomString(Resources res, @ArrayRes int id, int until) {
        final Random r = new Random();
        return res.getStringArray(id)[r.nextInt(until)];
    }
}
