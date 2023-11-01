/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.quickstep;

import static com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR;

import android.annotation.NonNull;
import android.annotation.UserIdInt;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.RemoteAnimationTarget;

import androidx.annotation.Nullable;

import com.android.launcher3.pm.UserCache;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.system.ActivityManagerWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Bundle;

/**
 * Contains helpful methods for retrieving data from {@link Task}s.
 */
public final class TaskUtils {

    private static final String TAG = "TaskUtils";

    private static final String SEPARATOR = "#";

    private static final List<String> mLockedApps = new ArrayList<> ();

    private static final String RECENT_LOCK_LIST = "recent_lock_list";

    private static final ExecutorService mIoExecutor = Executors.newSingleThreadExecutor();
    private static final ComponentName CMP_COM_ANDROID_DIALER = new ComponentName("com.android.dialer", "");

    private TaskUtils() {}

    /**
     * TODO: remove this once we switch to getting the icon and label from IconCache.
     */
    public static CharSequence getTitle(Context context, Task task) {
        return getTitle(context, task.key.userId, task.getTopComponent().getPackageName());
    }

    public static CharSequence getTitle(
            @NonNull Context context,
            @UserIdInt @Nullable Integer userId,
            @Nullable String packageName) {
        if (userId == null || packageName == null) {
            if (userId == null) {
                Log.e(TAG, "Failed to get title; missing userId");
            }
            if (packageName == null) {
                Log.e(TAG, "Failed to get title; missing packageName");
            }
            return "";
        }
        UserHandle user = UserHandle.of(userId);
        ApplicationInfo applicationInfo = new PackageManagerHelper(context)
                .getApplicationInfo(packageName, user, 0);
        if (applicationInfo == null) {
            Log.e(TAG, "Failed to get title for userId=" + userId + ", packageName=" + packageName);
            return "";
        }
        PackageManager packageManager = context.getPackageManager();
        return packageManager.getUserBadgedLabel(
                applicationInfo.loadLabel(packageManager), user);
    }

    public static boolean getTaskLockState(Context context, ComponentName componentName, Task.TaskKey taskKey) {
        return updateSpecifiedTaskLockState(context, componentName, taskKey);
    }

    private static boolean updateSpecifiedTaskLockState(Context context, ComponentName componentName, Task.TaskKey taskKey) {
        boolean taskLockState = LauncherLockedStateController.getInstance(context)
                .getTaskLockState(componentName.toShortString(), taskKey.userId);
        Log.d(TAG, "updateSpecifiedTaskLockState: Checking if the task is locked: " + taskLockState);

        if (taskLockState) {
            setTaskLockState(context, taskKey.baseIntent.getComponent(), taskLockState, taskKey);
            Log.i(TAG, "updateSpecifiedTaskLockState: Task is locked, clearing the lock state.");
        }

        return taskLockState;
    }

    public static void setTaskLockState(Context context, ComponentName componentName, boolean isState, Task.TaskKey taskKey) {
        LauncherLockedStateController.getInstance(context).setTaskLockState(componentName.toShortString(), isState, taskKey.userId);
        String formatLockedAppStr = toFormatLockedAppStr(componentName.getPackageName(), taskKey.userId);

        if (isState) {
            addLockedApp(formatLockedAppStr);
        } else {
            removeLockedApp(formatLockedAppStr);
        }
    }

    public static String toFormatLockedAppStr(String packageName, int userId) {
        return packageName + SEPARATOR + userId;
    }

    private static void saveLockedApps(List<String> lockedApps) {
        if (lockedApps != null) {
            mIoExecutor.execute(() -> TaskUtils.saveListToFileSync(lockedApps));
        }
    }

    public static void saveListToFileSync(List<String> lockedApps) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(RECENT_LOCK_LIST, new ArrayList<>(lockedApps));
    }

    public static void addLockedApp(String appStr) {
        if (!mLockedApps.contains(appStr)) {
            Log.d(TAG, "addLockedApp: " + appStr);
            mLockedApps.add(appStr);
            saveLockedApps(mLockedApps);
        }
    }

    public static void removeLockedApp(String appStr) {
        if (mLockedApps.contains(appStr)) {
            Log.d(TAG, "removeLockedApp: " + appStr);
            mLockedApps.remove(appStr);
            saveLockedApps(mLockedApps);
        }
    }

    public static ComponentKey getLaunchComponentKeyForTask(Task.TaskKey taskKey) {
        final ComponentName cn = taskKey.sourceComponent != null
                ? taskKey.sourceComponent
                : taskKey.getComponent();
        return new ComponentKey(cn, UserHandle.of(taskKey.userId));
    }


    public static boolean taskIsATargetWithMode(RemoteAnimationTarget[] targets,
            int taskId, int mode) {
        for (RemoteAnimationTarget target : targets) {
            if (target.mode == mode && target.taskId == taskId) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkCurrentOrManagedUserId(int currentUserId, Context context) {
        if (currentUserId == UserHandle.myUserId()) {
            return true;
        }
        List<UserHandle> allUsers = UserCache.INSTANCE.get(context).getUserProfiles();
        for (int i = allUsers.size() - 1; i >= 0; i--) {
            if (currentUserId == allUsers.get(i).getIdentifier()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requests that the system close any open system windows (including other SystemUI).
     */
    public static void closeSystemWindowsAsync(String reason) {
        UI_HELPER_EXECUTOR.execute(
                () -> ActivityManagerWrapper.getInstance().closeSystemWindows(reason));
    }
}
