package ex1.siv.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class CheckableList<T> {
    private final static String TAG = CheckableList.class.getSimpleName();

    public interface KeyFactory<T2> {
        String getKey(T2 item);
    }

    private List<T> mList = new ArrayList<>();
    private final KeyFactory<T> mKeyFactory;

    public CheckableList(KeyFactory<T> f) {
        mKeyFactory = f;
    }

    /*****************************************************
     * List
     *****************************************************/
    public void replaceList(List<T> list) {
        mList = list;
    }

    public int getItemCount() {
        return mList.size();
    }

    public T get(int position) {
        return mList.get(position);
    }

    /*****************************************************
     * Checked
     *****************************************************/
    private final HashSet<String> mCheckedMap = new HashSet<>();

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    public boolean isEmptyChecked() {
        return mCheckedMap.isEmpty();
    }

    private void setCheckedInternal(String key, boolean isChecked) {
        if (isChecked) {
            mCheckedMap.add(key);
        } else {
            mCheckedMap.remove(key);
        }
    }

    public void setChecked(T item, boolean isChecked) {
        setCheckedInternal(mKeyFactory.getKey(item), isChecked);
    }

    public void setChecked(Iterable<T> items, boolean isChecked) {
        for (T item : items) {
            setChecked(item, isChecked);
        }
    }

    public boolean isCheckedInternal(String key) {
        return mCheckedMap.contains(key);
    }

    public boolean isChecked(T item) {
        return isCheckedInternal(mKeyFactory.getKey(item));
    }

    public <T2> List<T2> filterByChecked(Iterable<T2> items, KeyFactory<T2> f) {
        List<T2> ret = new ArrayList<>();
        for (T2 item : items) {
            if (isCheckedInternal(f.getKey(item))) {
                ret.add(item);
            }
        }
        return ret;
    }

    public List<T> filterByChecked(Iterable<T> items) {
        return filterByChecked(items, mKeyFactory);
    }

    public void toggleChecked(T item) {
        setChecked(item, !isChecked(item));
    }

    public void checkAll() {
        for (T item : mList) {
            setChecked(item, true);
        }
    }

    public void uncheckAll() {
        mCheckedMap.clear();
    }

    public List<T> getCheckedItemList() {
        List<T> itemList = new ArrayList<>();
        for (T item : mList) {
            if (isChecked(item)) {
                itemList.add(item);
            }
        }
        return itemList;
    }

    public void importChecked(Function<T, Boolean> a) {
        uncheckAll();
        for (T item : mList) {
            setChecked(item, a.apply(item));
        }
        Log.i(TAG, "importChecked N=" + mCheckedMap.size());
    }

    /*****************************************************
     * Selected
     *****************************************************/
    private String mSelectedKey = "";

    public void setSelected(T item) {
        if (item == null) {
            mSelectedKey = "";
        } else {
            mSelectedKey = mKeyFactory.getKey(item);
        }
    }

    /** @noinspection unused*/
    public boolean isSelected(T item) {
        return Objects.equals(mSelectedKey, mKeyFactory.getKey(item));
    }

    /*****************************************************
     * Status
     *****************************************************/
    private Status getStatusInternal(String key) {
        if (Objects.equals(key, mSelectedKey)) {
            return Status.SELECTED;
        }
        if (isCheckedInternal(key)) {
            return Status.CHECKED;
        }
        return Status.NONE;
    }

    public Status getStatus(T item) {
        return getStatusInternal(mKeyFactory.getKey(item));
    }

    public enum Status {
        NONE,
        SELECTED,
        CHECKED
    }

    /*****************************************************
     * Changed
     *****************************************************/
    public KeyStatus getCurrentKeyStatus() {
        return new KeyStatus(mCheckedMap, mSelectedKey);
    }

    public List<Integer> getDifferentPos(KeyStatus orig) {
        HashSet<String> hash = orig.diff(this);
        List<Integer> ret = new ArrayList<>();
        for (int i = 0; i < mList.size(); ++i) {
            if (hash.contains(mKeyFactory.getKey(mList.get(i)))) {
                ret.add(i);
            }
        }
        return ret;
    }

    public static class KeyStatus {
        private final HashSet<String> mCheckedMap;
        private final String mSelectedKey;

        KeyStatus(HashSet<String> checked, String selected) {
            mCheckedMap = new HashSet<>(checked);
            mSelectedKey = selected;
        }

        HashSet<String> diff(CheckableList<?> current) {
            Log.i(TAG, "diff start");
            HashSet<String> ret = new HashSet<>();
            for (String key : mCheckedMap) {
                if (!current.mCheckedMap.contains(key)) {
                    ret.add(key);
                }
            }
            for (String key : current.mCheckedMap) {
                if (!mCheckedMap.contains(key)) {
                    ret.add(key);
                }
            }
            if (!Objects.equals(mSelectedKey, current.mSelectedKey)) {
                Log.i(TAG, "diff selected");
                ret.add(mSelectedKey);
                ret.add(current.mSelectedKey);
            }
            return ret;
        }
    }
}
