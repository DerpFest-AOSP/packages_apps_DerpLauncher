package com.android.launcher3.qsb;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_BIND;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PROVIDER;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class QsbSetupActivity extends Activity {
    private static final int REQUEST_BIND_QSB = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            Intent newIntent = new Intent(ACTION_APPWIDGET_BIND);
            newIntent.putExtra(EXTRA_APPWIDGET_ID, intent.getIntExtra(EXTRA_APPWIDGET_ID, -1));
            newIntent.putExtra(EXTRA_APPWIDGET_PROVIDER, intent.getParcelableExtra(EXTRA_APPWIDGET_PROVIDER, ComponentName.class));
            startActivityForResult(newIntent, REQUEST_BIND_QSB);
        } else {
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BIND_QSB) {
            if (resultCode == Activity.RESULT_OK) {
                QsbContainerView.saveWidgetId(this, data.getIntExtra(EXTRA_APPWIDGET_ID, -1));
            } else {
                QsbContainerView.saveWidgetId(this, -1);
            }
            finish();
        }
    }

}
