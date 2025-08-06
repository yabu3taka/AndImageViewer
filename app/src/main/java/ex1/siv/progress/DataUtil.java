package ex1.siv.progress;

import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ex1.siv.storage.data.FolderInfo;

public class DataUtil {
    private DataUtil() {
    }

    public static String[] toUriListData(Collection<Uri> list) {
        ArrayList<String> uriList = new ArrayList<>();
        for (Uri uri : list) {
            uriList.add(uri.toString());
        }
        return uriList.toArray(new String[0]);
    }

    public static String[] toFolderInfoListData(Collection<FolderInfo> list) {
        ArrayList<String> uriList = new ArrayList<>();
        for (FolderInfo item : list) {
            uriList.add(item.toUri().toString());
        }
        return uriList.toArray(new String[0]);
    }

    public static List<Uri> toUriList(String[] list) {
        List<Uri> uriList = new ArrayList<>();
        if (list != null) {
            for (String uriStr : list) {
                uriList.add(Uri.parse(uriStr));
            }
        }
        return uriList;
    }

    public static File toFile(String str) {
        if (str == null) {
            return null;
        }
        return new File(str);
    }
}
