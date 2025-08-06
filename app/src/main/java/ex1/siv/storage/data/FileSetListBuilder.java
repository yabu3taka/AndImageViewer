package ex1.siv.storage.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ex1.siv.storage.exception.StorageException;
import ex1.siv.util.BitmapUtil;
import ex1.siv.util.ContentUtil;
import ex1.siv.util.FileUtil;

public class FileSetListBuilder<T extends FileInfo> implements ContentUtil.Appender {
    public final static String INDEX_FILE = "index.dat";

    private final HashMap<String, T> mImageMap = new HashMap<>(100);
    private final HashMap<String, T> mTextMap = new HashMap<>();
    private final HashSet<String> mIndexSet = new HashSet<>();
    private final HashMap<String, T> mMiscMap = new HashMap<>();
    private FolderSetting mSetting = new FolderSetting();

    public FileSetListBuilder() {
    }

    public void add(T file) {
        String filename = file.filename;
        String basename = FileUtil.getBasename(filename);
        if (BitmapUtil.isPhotoFile(filename)) {
            mImageMap.put(basename, file);
        } else if (BitmapUtil.isTextFile(filename)) {
            mTextMap.put(basename, file);
        } else {
            mMiscMap.put(filename.toLowerCase(), file);
        }
    }

    public boolean isEmpty() {
        return mImageMap.isEmpty();
    }

    /*********************************************************************
     * Read from ContentUtil.listing
     *********************************************************************/
    private ContentUtil.Converter<T> mConv;

    public FileSetListBuilder(ContentUtil.Converter<T> c) {
        this.mConv = c;
    }

    @Override
    public void addContentItem(ContentUtil.ContentItem item) {
        add(mConv.conv(item));
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    /*********************************************************************
     * Read setting file
     *********************************************************************/
    public T getMiscFile(String filename) {
        return mMiscMap.get(filename.toLowerCase());
    }

    public interface SettingFileOpener<T2 extends FileInfo> {
        InputStream openSettingFile(T2 file) throws Exception;
    }

    private Reader getReader(SettingFileOpener<T> fac, String filename) throws Exception {
        T file = getMiscFile(filename);
        if (file == null) {
            return null;
        }
        InputStream inputStream = fac.openSettingFile(file);
        if (inputStream == null) {
            return null;
        }
        return new InputStreamReader(inputStream);
    }

    public void loadIndex(SettingFileOpener<T> fac) {
        try (Reader reader = getReader(fac, INDEX_FILE)) {
            if (reader != null) {
                loadIndex(reader);
            }
        } catch (Exception ex) {
            throw new StorageException("Failed to read index");
        }
    }

    private void loadIndex(Reader reader) throws Exception {
        try (BufferedReader br = FileUtil.toBr(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String basename = FileUtil.getBasename(line);
                    mIndexSet.add(basename.toLowerCase());
                }
            }
        }
    }

    public void loadSetting(SettingFileOpener<T> fac) {
        try (Reader reader = getReader(fac, FolderSetting.SETTING_FILE)) {
            mSetting = FolderSetting.create(reader);
        } catch (Exception ex) {
            throw new StorageException("Failed to read setting");
        }
    }

    /*********************************************************************
     * Create list
     *********************************************************************/
    private List<String> sortedKeys() {
        List<String> ret = new ArrayList<>(mImageMap.keySet());
        ret.sort(String::compareToIgnoreCase);
        return ret;
    }

    public FileSetList createFileSetList() {
        List<FileSet> ret = new ArrayList<>();
        for (String key : sortedKeys()) {
            boolean indexed = mIndexSet.contains(key.toLowerCase());

            T imgFile = mImageMap.get(key);
            assert imgFile != null;

            if (mSetting.blankFlag) {
                if (indexed) {
                    if (!ret.isEmpty()) {
                        ret.add(new FileSet(new FileInfoEmpty(imgFile)));
                    }
                }
            }
            ret.add(new FileSet(imgFile, mTextMap.get(key), indexed));
        }

        boolean hasIndex = !mIndexSet.isEmpty();
        boolean hasText = !mTextMap.isEmpty();
        return new FileSetList(ret, hasIndex, hasText);
    }

    public FileInfoList createFileInfoList() {
        List<FileSet> ret = new ArrayList<>();
        for (String key : sortedKeys()) {
            ret.add(new FileSet(mImageMap.get(key), mTextMap.get(key), false));
        }
        return new FileInfoList(ret, new HashMap<>(mMiscMap));
    }
}
