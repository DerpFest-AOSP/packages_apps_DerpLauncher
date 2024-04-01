/*
 * Copyright (C) 2024 The LibreMobileOS Foundation
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

package com.android.launcher3.settings;

import android.os.Bundle;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.uioverrides.flags.DeveloperOptionsUI;
import com.android.launcher3.util.Executors;

public class DeveloperOptionsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.launcher_developer_preferences, rootKey);

        PreferenceScreen screen = getPreferenceScreen();
        for (int i = screen.getPreferenceCount() - 1; i >= 0; i--) {
            Preference preference = screen.getPreference(i);
            if (!initPreference(preference)) {
                screen.removePreference(preference);
            }
        }

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.dev_options_title));
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View listView = getListView();
        final int bottomPadding = listView.getPaddingBottom();
        listView.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottomPadding + insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });

        // Overriding Text Direction in the Androidx preference library to support RTL
        view.setTextDirection(View.TEXT_DIRECTION_LOCALE);
    }

    /**
     * Initializes a preference. This is called for every preference. Returning false here
     * will remove that preference from the list.
     */
    protected boolean initPreference(Preference preference) {
        switch (preference.getKey()) {
            case "pref_developer_flags":
                if (preference instanceof PreferenceCategory pc) {
                    Executors.MAIN_EXECUTOR.post(() -> new DeveloperOptionsUI(this, pc));
                    return true;
                }
                return false;
        }
        return true;
    }
}
