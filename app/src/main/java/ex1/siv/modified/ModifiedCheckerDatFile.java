package ex1.siv.modified;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ex1.siv.util.ListUtil;

public class ModifiedCheckerDatFile implements ModifiedChecker {
    private final static String TAG = ModifiedCheckerDatFile.class.getSimpleName();

    private final static String DELIMITER = "\t";

    private final File folder;
    private final File datFile;
    private final HashMap<String, Long> modifiedMap = new HashMap<>();

    public ModifiedCheckerDatFile(File folder) {
        this.folder = folder;
        datFile = new File(folder, "modified.dat");
        loadModifiedMap();
    }

    private void loadModifiedMap() {
        Log.i(TAG, "loadModifiedMap start " + datFile);
        try (FileReader reader = new FileReader(datFile);
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] cols = line.split(DELIMITER, 2);
                    modifiedMap.put(cols[1], Long.parseLong(cols[0]));
                }
            }
            Log.i(TAG, "loadModifiedMap end " + modifiedMap.size());
        } catch (Exception ex) {
            Log.e(TAG, "loadModifiedMap", ex);
        }
    }

    private boolean writeModifiedMap() {
        Log.i(TAG, "writeModifiedMap start " + datFile);
        try (FileWriter writer = new FileWriter(datFile);
             BufferedWriter bw = new BufferedWriter(writer)) {
            for (Map.Entry<String, Long> entry : modifiedMap.entrySet()) {
                bw.write(entry.getValue() + DELIMITER + entry.getKey());
                bw.newLine();
            }
            Log.i(TAG, "writeModifiedMap end " + modifiedMap.size());
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "writeModifiedMap", ex);
            return false;
        }
    }

    @Override
    public boolean existCacheFile(File cacheFile, ModifiedFileInfo info) {
        String filename = cacheFile.getName();
        Long cacheModified = modifiedMap.get(filename);
        if (cacheModified == null) {
            return false;
        }
        return cacheModified == info.getModified();
    }

    @Override
    public boolean rememberFileInfo(File cacheFile, ModifiedFileInfo info) {
        Log.i(TAG, "setModifiedDate F=" + cacheFile + " M=" + info.getModified());
        modifiedMap.put(cacheFile.getName(), info.getModified());
        return writeModifiedMap();
    }

    @Override
    public void addNeedFile(Collection<String> needs) {
        needs.add(datFile.getName());
    }

    @Override
    public void cleanUp() {
        Log.i(TAG, "completeFolder start");
        String[] files = folder.list();
        if (ListUtil.isNullOrEmpty(files)) {
            return;
        }

        modifiedMap.keySet().retainAll(ListUtil.asSet(files));
        writeModifiedMap();
        Log.i(TAG, "completeFolder end");
    }
}
