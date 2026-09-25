package com.VoidSoul.daymark;

/**
 * Keeps the unlock state only for the current app process. A process restart
 * deliberately returns the app to the lock screen.
 */
final class AppLockSession {
    private static boolean unlocked;

    private AppLockSession() {
    }

    static boolean isUnlocked() {
        return unlocked;
    }

    static void unlock() {
        unlocked = true;
    }

    static void lock() {
        unlocked = false;
    }
}
