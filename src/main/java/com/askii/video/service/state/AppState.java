package com.askii.video.service.state;

public class AppState {

    private static volatile boolean isRunning = true;

    public static void switchState(boolean state) {
        isRunning = state;
    }

    public static boolean isIsRunning() {
        return isRunning;
    }
}
