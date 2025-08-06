package ex1.siv.progress.data;

import android.net.Uri;

import androidx.work.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ex1.siv.progress.DataUtil;
import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.cache.CacheManager;
import ex1.siv.storage.data.FolderInfo;

public class ProgressAspectRestUri {
    private final static String FOLDER_URI = "folderUri";

    /*********************************************************************
     * Read data
     *********************************************************************/
    public static ProgressAspectRestUri create(ProgressData progressData) {
        if (progressData.data != null) {
            List<Uri> waitingUrl = DataUtil.toUriList(progressData.data.getStringArray(FOLDER_URI));
            return new ProgressAspectRestUri(waitingUrl);
        } else {
            return new ProgressAspectRestUri(new ArrayList<>());
        }
    }

    private final List<Uri> restUrl;

    private ProgressAspectRestUri(List<Uri> restUrl) {
        this.restUrl = restUrl;
    }

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    public boolean isRestUriEmpty() {
        if (restUrl == null) {
            return true;
        }
        return restUrl.isEmpty();
    }

    public List<FolderInfo> getWaitingFolderInfoList(StorageContext context, Storage storage) {
        if (restUrl == null) {
            return null;
        }
        if (restUrl.isEmpty()) {
            return null;
        }

        List<FolderInfo> ret = new ArrayList<>();
        for (Uri uri : restUrl) {
            ret.add(storage.getFromUri(context, uri));
        }
        return ret;
    }

    public boolean isVisibleFolder(FolderInfo folder, CacheManager cacheManager) {
        if (restUrl == null) {
            return true;
        }
        if (!cacheManager.hasCacheFolder(folder)) {
            return true;
        }
        return !restUrl.contains(folder.toUri());
    }

    /*********************************************************************
     * IBuilderAspect
     ********************************************************************/
    public static void keep(Data.Builder dataBuilder, ProgressData progressData) {
        String[] tmp = progressData.data.getStringArray(FOLDER_URI);
        if (tmp != null) {
            dataBuilder.putStringArray(FOLDER_URI, tmp);
        }
    }

    public static class AspectBuilder implements ProgressAspectBuilder {
        private final Set<Uri> restUriList = new HashSet<>();

        public void init(Collection<Uri> uriList) {
            restUriList.addAll(uriList);
        }

        public void done(Uri uri) {
            restUriList.remove(uri);
        }

        public void clear() {
            restUriList.clear();
        }

        private void setupInternal(Data.Builder builder) {
            builder.putStringArray(FOLDER_URI, DataUtil.toUriListData(restUriList));
        }

        @Override
        public void setupForProgress(Data.Builder builder) {
            setupInternal(builder);
        }

        @Override
        public void setupForResult(ProgressData.Status status, Data.Builder builder) {
            if (status == ProgressData.Status.Error) {
                setupInternal(builder);
            }
            if (status == ProgressData.Status.Cancel) {
                setupInternal(builder);
            }
        }
    }
}
