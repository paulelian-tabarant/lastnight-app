package com.pact41.lastnight;

import android.app.Application;

import com.pact41.lastnight.model.LastNightStateManager;

/**
 * Created by Paul-Elian on 16/02/2017.
 */

public class LastNight extends Application {

    private LastNightStateManager appStateManager;

    public LastNightStateManager getAppStateManager()
    {
        return appStateManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appStateManager = new LastNightStateManager(this);
    }
}
