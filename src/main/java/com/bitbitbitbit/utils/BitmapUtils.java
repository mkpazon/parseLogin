package com.bitbitbitbit.utils;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by mkpazon on 5/7/15.
 * -=Bitbitbitbit=-
 */
public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

    private BitmapUtils() {
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap, int imageQuality) {
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, bos);
            return bos.toByteArray();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    Log.w(TAG, "Failed to close ByteArrayOutputStream", e);
                    // Ignore exception
                }
            }
        }
    }
}
