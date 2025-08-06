package ex1.siv.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

/** @noinspection unused*/
public class BitmapUtil {
    private final static String PHOTO_SCRAMBLE_EXT = ".isd";
    private final static String[] PHOTO_EXT_LIST = new String[]{PHOTO_SCRAMBLE_EXT, ".jpg", ".jpeg", ".png"};

    private final static String TEXT_SCRAMBLE_EXT = ".tsd";
    private final static String[] TEXT_EXT_LIST = new String[]{TEXT_SCRAMBLE_EXT, ".txt"};

    public static int scrambled;

    private BitmapUtil() {
    }

    public static boolean isPhotoFile(String filename) {
        String lower = filename.toLowerCase();
        for (String ext : BitmapUtil.PHOTO_EXT_LIST) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTextFile(String filename) {
        String lower = filename.toLowerCase();
        for (String ext : BitmapUtil.TEXT_EXT_LIST) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static InputStream getInputStream(InputStream inputStream, String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(PHOTO_SCRAMBLE_EXT)) {
            return new ScrambledInputStream(inputStream, scrambled);
        } else if (lower.endsWith(TEXT_SCRAMBLE_EXT)) {
            return new ScrambledInputStream(inputStream, scrambled);
        } else {
            return inputStream;
        }
    }

    private static InputStream getInputStream(InputStream inputStream, File target) {
        return getInputStream(inputStream, target.getName());
    }

    public static Bitmap loadBitmap(InputStream is, String filename) throws IOException {
        try (InputStream inputStream = getInputStream(is, filename)) {
            return BitmapFactory.decodeStream(inputStream);
        }
    }

    public static Bitmap loadBitmap(File target) throws IOException {
        try (InputStream inputStream = getInputStream(Files.newInputStream(target.toPath()), target)) {
            return BitmapFactory.decodeStream(inputStream);
        }
    }

    public static BitmapFactory.Options loadBitmapOptions(InputStream is, String filename, int reqWidth, int reqHeight) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();

        try (InputStream inputStream = getInputStream(is, filename)) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
        }

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = Math.round((float) height / (float) reqHeight);
            int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.min(heightRatio, widthRatio);
        }
        options.inSampleSize = inSampleSize;

        return options;
    }

    public static Bitmap loadBitmap(InputStream is, String filename, BitmapFactory.Options options) throws IOException {
        try (InputStream inputStream = getInputStream(is, filename)) {
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(inputStream, null, options);
        }
    }

    public static Bitmap loadBitmap(File target, int reqWidth, int reqHeight) throws IOException {
        BitmapFactory.Options options;
        try (InputStream is = Files.newInputStream(target.toPath())) {
            options = loadBitmapOptions(is, target.getName(), reqWidth, reqHeight);
        }
        try (InputStream is = Files.newInputStream(target.toPath())) {
            return BitmapUtil.loadBitmap(is, target.getName(), options);
        }
    }

    public static InputStreamReader getTextReader(InputStream inputStream, String filename) {
        return new InputStreamReader(getInputStream(inputStream, filename));
    }

    public static String getPhotoText(InputStream inputStream, String filename) throws IOException {
        try (InputStreamReader reader = getTextReader(inputStream, filename);
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            String crlf = System.lineSeparator();
            StringBuilder buf = new StringBuilder();
            while ((line = br.readLine()) != null) {
                buf.append(line);
                buf.append(crlf);
            }
            return buf.toString();
        }
    }
}
