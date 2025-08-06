package ex1.siv.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;

/** @noinspection unused*/
public class FileUtil {
    private final static String TAG = FileUtil.class.getSimpleName();

    private FileUtil() {
    }

    /*****************************************************
     * get Info
     *****************************************************/
    public static String getBasename(String filename) {
        int pos = filename.lastIndexOf(".");
        if (pos != -1) {
            return filename.substring(0, pos);
        }
        return filename;
    }

    public static File[] getUnnecessaryFolder(File topDir, final Collection<String> needs) {
        return topDir.listFiles(f -> f.isDirectory() && !needs.contains(f.getName()));
    }

    public static File[] getUnnecessaryFile(File topDir, final Collection<String> needs) {
        return topDir.listFiles(f -> f.isFile() && !needs.contains(f.getName()));
    }

    public static int getFileCount(File f) {
        String[] files = f.list();
        assert files != null;
        return files.length;
    }

    /*****************************************************
     * Delete
     *****************************************************/
    public static boolean deleteContent(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            assert files != null;
            for (File file : files) {
                if (!delete(file)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean delete(File f) {
        if (!f.exists()) {
            return true;
        }
        Log.i(TAG, "delete del=" + f);
        if (f.isDirectory()) {
            deleteContent(f);
        }
        return f.delete();
    }

    public static boolean delete(Iterable<File> files) {
        for (File f : files) {
            if (!delete(f)) {
                return false;
            }
        }
        return true;
    }

    public static boolean delete(File[] files) {
        for (File f : files) {
            if (!delete(f)) {
                return false;
            }
        }
        return true;
    }

    /*****************************************************
     * Create a file
     *****************************************************/
    public static boolean existsOrMake(File f) {
        return f.exists() || f.mkdirs();
    }

    /** @noinspection UnusedReturnValue*/
    public static boolean createFlagFile(File f) {
        try {
            //noinspection ResultOfMethodCallIgnored
            f.createNewFile();
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "createFlagFile file=" + f, ex);
            return false;
        }
    }

    /*****************************************************
     * Read a file
     *****************************************************/
    public static BufferedReader toBr(Reader r) {
        if (r instanceof BufferedReader) {
            return (BufferedReader) r;
        }
        return new BufferedReader(r);
    }

    public static BufferedReader toBr(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }
}
