/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.shoulderkey;

import android.app.Instrumentation;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class ShoulderKeyTileService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        collapseStatusBar();
        ShoulderKeyService.getInstance().showKeyMappingViews();
    }

    
    private void collapseStatusBar() {
        StatusBarManager sbm =
                (StatusBarManager) getBaseContext().getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.collapsePanels();
    }

}
