// ImageProcessing.java
package com.sd.facultyfacialrecognition;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;

public class ImageProcessing {

    // Load bitmap from URI with sample-size safety
    public static Bitmap loadBitmapFromUri(Context ctx, Uri uri) {
        try {
            ContentResolver cr = ctx.getContentResolver();
            InputStream is = cr.openInputStream(uri);

            // decode bounds first
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, o);
            is.close();

            // calculate inSampleSize to avoid huge bitmaps
            int req = 1024; // max dimension
            int inSampleSize = 1;
            while ((o.outWidth / inSampleSize) > req || (o.outHeight / inSampleSize) > req) {
                inSampleSize *= 2;
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = inSampleSize;

            is = cr.openInputStream(uri);
            Bitmap bm = BitmapFactory.decodeStream(is, null, opts);
            is.close();

            return bm;
        } catch (Exception e) {
            Log.e("ImageProcessing", "loadBitmapFromUri err: " + e.getMessage());
            return null;
        }
    }

    // EXIF orientation correction
    public static Bitmap rotateBitmapIfRequired(Context ctx, Uri uri, Bitmap img) {
        try {
            InputStream in = ctx.getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(in);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            in.close();
            int rotationDegrees = exifToDegrees(orientation);
            if (rotationDegrees == 0) return img;

            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            Bitmap rotated = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            return rotated;
        } catch (Exception e) {
            // fallback: return original
            return img;
        }
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) { return 180; }
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) { return 270; }
        return 0;
    }

    // Simple blur detection: variance of Laplacian approximation using downscale + edges
    public static boolean isBlurry(Bitmap bmp) {
        try {
            if (bmp == null) return true;
            // resize to speed up
            Bitmap small = Bitmap.createScaledBitmap(bmp, 200, 200 * bmp.getHeight() / bmp.getWidth(), true);
            int width = small.getWidth(), height = small.getHeight();
            int[] pix = new int[width * height];
            small.getPixels(pix, 0, width, 0, 0, width, height);

            // compute a crude 'laplacian' variance: sum squared differences with neighbors
            long sum = 0, sumSq = 0;
            int count = 0;
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    int idx = y * width + x;
                    int c = pix[idx];
                    int r = (c >> 16) & 0xff;
                    int g = (c >> 8) & 0xff;
                    int b = c & 0xff;
                    int gray = (r + g + b) / 3;

                    // neighbor average
                    int nsum = 0;
                    for (int ny = -1; ny <= 1; ny++) {
                        for (int nx = -1; nx <= 1; nx++) {
                            if (ny == 0 && nx == 0) continue;
                            int nc = pix[(y + ny) * width + (x + nx)];
                            int nr = (nc >> 16) & 0xff;
                            int ng = (nc >> 8) & 0xff;
                            int nb = nc & 0xff;
                            nsum += (nr + ng + nb) / 3;
                        }
                    }
                    int navg = nsum / 8;
                    int diff = gray - navg;
                    sum += diff;
                    sumSq += diff * diff;
                    count++;
                }
            }
            if (count == 0) return true;
            double mean = (double) sum / count;
            double variance = ((double) sumSq / count) - (mean * mean);

            // threshold tuned experimentally; tweak if needed
            return variance < 15.0;
        } catch (Exception e) {
            return true;
        }
    }

    // resize utility
    public static Bitmap resize(Bitmap bm, int w, int h) {
        try {
            return Bitmap.createScaledBitmap(bm, w, h, true);
        } catch (Exception e) {
            return bm;
        }
    }
}
