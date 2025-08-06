package ex1.siv.storage.favorite;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Set;

import ex1.siv.storage.StorageContext;

public class SimpleFavoriteSaver implements FavoriteSaver {
    private final static String TAG = SimpleFavoriteSaver.class.getSimpleName();

    private final File file;

    public SimpleFavoriteSaver(StorageContext context) {
        file = new File(context.getMyContext().getFilesDir(), "favorite.txt");
    }

    @Override
    public void saveFavorite(Set<String> map) {
        Log.i(TAG, "save start " + file);
        try (FileWriter writer = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(writer)) {
            for (String folder : map) {
                bw.write(folder);
                bw.newLine();
            }
            Log.i(TAG, "save end " + map.size());
        } catch (Exception ex) {
            Log.e(TAG, "save", ex);
        }
    }

    @Override
    public void loadFavorite(Set<String> map) {
        Log.i(TAG, "load start " + file);
        try (FileReader reader = new FileReader(file);
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    map.add(line);
                }
            }
            Log.i(TAG, "load end " + map.size());
        } catch (Exception ex) {
            Log.e(TAG, "load", ex);
        }
    }
}
