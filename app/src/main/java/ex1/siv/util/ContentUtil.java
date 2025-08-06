package ex1.siv.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.google.common.base.Strings;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/** @noinspection unused*/
public class ContentUtil {
    private final static String TAG = ContentUtil.class.getSimpleName();

    private ContentUtil() {
    }

    public static InputStream openInputStream(ContentResolver cr, Uri uri) throws FileNotFoundException {
        return cr.openInputStream(uri);
    }

    public static InputStream openInputStream(Context c, DocumentFile df) throws FileNotFoundException {
        return openInputStream(c.getContentResolver(), df.getUri());
    }

    /*********************************************************************
     * listing
     *********************************************************************/
    public interface Appender {
        void addContentItem(ContentItem item);

        boolean isFinished();
    }

    public static class ContentItem {
        private final Uri parentUri;
        private final String id;

        public final String name;
        private Uri documentUri;
        public final String mime;
        public final long lastModified;
        private DocumentFile cache;

        public ContentItem(Uri parentUri, String id, String name, String mime, long lastModified) {
            this.parentUri = parentUri;
            this.id = id;

            this.name = name;
            this.mime = mime;
            this.lastModified = lastModified;
            this.documentUri = null;
            this.cache = null;
        }

        public ContentItem(DocumentFile docFile) {
            this.parentUri = null;
            this.id = null;

            this.name = docFile.getName();
            if (docFile.isDirectory()) {
                this.mime = DocumentsContract.Document.MIME_TYPE_DIR;
            } else {
                this.mime = "file";
            }
            this.lastModified = docFile.lastModified();
            this.documentUri = docFile.getUri();
            this.cache = docFile;
        }

        public Uri getDocumentUri() {
            if (this.documentUri == null) {
                this.documentUri = DocumentsContract.buildDocumentUriUsingTree(parentUri, id);
            }
            return this.documentUri;
        }

        public DocumentFile getDocumentFile(Context context) {
            if (cache == null) {
                getDocumentUri();
                if (isDirectory()) {
                    cache = DocumentFile.fromTreeUri(context, documentUri);
                } else {
                    cache = DocumentFile.fromSingleUri(context, documentUri);
                }
                assert cache != null;
            }
            return cache;
        }

        public boolean isDirectory() {
            return DocumentsContract.Document.MIME_TYPE_DIR.equals(this.mime);
        }

        public DocumentFile getParentDocumentFile(Context context) {
            if (parentUri != null) {
                return DocumentFile.fromTreeUri(context, parentUri);
            } else {
                return null;
            }
        }
    }

    public static boolean listing(Context context, DocumentFile dirFile,
                                  Appender appender, boolean folder) {
        Uri parentUri = dirFile.getUri();
        Log.d(TAG, "listing URI=" + parentUri);

        ContentResolver resolver = context.getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, DocumentsContract.getDocumentId(parentUri));
        String[] param = new String[]{
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        };

        // String selection = DocumentsContract.Document.COLUMN_MIME_TYPE + " = ?";
        // String[] selectionArgs = new String[]{DocumentsContract.Document.MIME_TYPE_DIR};

        try (Cursor c = resolver.query(childrenUri, param, null, null, null)) {
            Log.d(TAG, "listing ITERATING");
            assert c != null;

            while (c.moveToNext()) {
                String documentId = c.getString(0);
                String documentName = c.getString(1);
                String documentMime = c.getString(2);
                long documentLastModified = c.getLong(3);
                ContentItem item = new ContentItem(parentUri, documentId, documentName, documentMime, documentLastModified);
                if (!folder || item.isDirectory()) {
                    appender.addContentItem(item);
                }
                if (appender.isFinished()) {
                    break;
                }
            }

            Log.d(TAG, "listing END");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
            return false;
        }
    }

    /*********************************************************************
     * Get item list
     *********************************************************************/
    public interface Converter<T> {
        T conv(ContentItem item);
    }

    private static class MyArrayList<T> implements Appender {
        private final Converter<T> conv;
        private final ArrayList<T> list = new ArrayList<>();

        private MyArrayList(Converter<T> conv) {
            this.conv = conv;
        }

        @Override
        public void addContentItem(ContentItem item) {
            T obj = conv.conv(item);
            if (obj != null) {
                list.add(obj);
            }
        }

        @Override
        public boolean isFinished() {
            return false;
        }
    }

    public static <T> List<T> listFile(Context context, DocumentFile dirFile, Converter<T> conv, boolean folder) {
        MyArrayList<T> appender = new MyArrayList<>(conv);
        if (listing(context, dirFile, appender, folder)) {
            return appender.list;
        }
        return null;
    }

    public static <T> List<T> listFile(Context context, DocumentFile dirFile, Converter<T> conv) {
        return listFile(context, dirFile, conv, false);
    }

    /*********************************************************************
     * Find a file
     *********************************************************************/
    public interface FileFinder {
        void addTarget(String filename);

        DocumentFileMap findFile();
    }

    private static class MyFindFile implements Appender, FileFinder {
        private final Context context;
        private final DocumentFile dirFile;
        private final HashSet<String> wanted = new HashSet<>();

        private DocumentFileMap result;

        private MyFindFile(Context context, DocumentFile dirFile) {
            this.context = context;
            this.dirFile = dirFile;
        }

        public void addTarget(String filename) {
            if (!Strings.isNullOrEmpty(filename)) {
                wanted.add(filename);
            }
        }

        public DocumentFileMap findFile() {
            result = new DocumentFileMap();
            if (listing(context, dirFile, this, false)) {
                return result;
            }
            return null;
        }

        @Override
        public void addContentItem(ContentItem item) {
            if (wanted.contains(item.name)) {
                wanted.remove(item.name);
                result.map.put(item.name, item.getDocumentFile(context));
            }
        }

        @Override
        public boolean isFinished() {
            return this.wanted.isEmpty();
        }
    }

    public static class DocumentFileMap {
        private final HashMap<String, DocumentFile> map = new HashMap<>();

        private DocumentFileMap() {
        }

        public DocumentFile get(String name) {
            return map.get(name);
        }

        public DocumentFile getFile(String name) {
            DocumentFile file = get(name);
            if (file == null) {
                return null;
            }
            if (!file.isFile()) {
                return null;
            }
            return file;
        }
    }

    public static FileFinder getFinder(Context context, DocumentFile dirFile) {
        return new MyFindFile(context, dirFile);
    }

    public static DocumentFile findFile(Context context, DocumentFile dirFile, String name) {
        FileFinder finder = getFinder(context, dirFile);
        finder.addTarget(name);
        DocumentFileMap map = finder.findFile();
        if (map == null) {
            return null;
        }
        return map.getFile(name);
    }
}
