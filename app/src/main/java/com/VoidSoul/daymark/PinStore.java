package com.VoidSoul.daymark;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Stores a salted PIN verifier, never the entered PIN itself. */
final class PinStore {
    private static final String PREFERENCES_NAME = "daymark_lock";
    private static final String KEY_SALT = "salt";
    private static final String KEY_VERIFIER = "verifier";
    private static final int SALT_BYTES = 16;
    private static final int DERIVED_KEY_BITS = 256;
    private static final int ITERATIONS = 210000;

    private PinStore() {
    }

    static boolean hasPin(Context context) {
        SharedPreferences preferences = preferences(context);
        return preferences.contains(KEY_SALT) && preferences.contains(KEY_VERIFIER);
    }

    static void save(Context context, char[] pin) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] verifier = derive(pin, salt);
        boolean saved = preferences(context).edit()
                .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(KEY_VERIFIER, Base64.encodeToString(verifier, Base64.NO_WRAP))
                .commit();
        if (!saved) {
            throw new IllegalStateException("Unable to save lock PIN");
        }
    }

    static boolean matches(Context context, char[] pin) {
        SharedPreferences preferences = preferences(context);
        String encodedSalt = preferences.getString(KEY_SALT, null);
        String encodedVerifier = preferences.getString(KEY_VERIFIER, null);
        if (encodedSalt == null || encodedVerifier == null) {
            return false;
        }

        try {
            byte[] salt = Base64.decode(encodedSalt, Base64.NO_WRAP);
            byte[] expected = Base64.decode(encodedVerifier, Base64.NO_WRAP);
            byte[] actual = derive(pin, salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private static byte[] derive(char[] pin, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(pin, salt, ITERATIONS, DERIVED_KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to derive PIN verifier", exception);
        } finally {
            spec.clearPassword();
        }
    }
}
