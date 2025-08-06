package ex1.siv.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import ex1.siv.R;
import ex1.siv.storage.exception.StorageException;

public class GoogleDriveAccess {
    private final static String TAG = GoogleDriveAccess.class.getSimpleName();

    private final Drive mDrive;

    private GoogleDriveAccess(Drive drive) {
        mDrive = drive;
    }

    public static GoogleSignInClient createGoogleSignInClient(Context context, String scope) {
        Log.i(TAG, "createGoogleSignInClient");
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(scope))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(context, gso);
    }

    public static GoogleDriveAccess createInstance(Context context, String scope) {
        Log.i(TAG, "createDriveService");

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account == null) {
            Log.e(TAG, "createDriveService Not Login");
            return null;
        }

        if (!GoogleSignIn.hasPermissions(account, new Scope(scope))) {
            Log.e(TAG, "createDriveService No permission");
            return null;
        }

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(scope)
        );
        credential.setSelectedAccount(account.getAccount());

        HttpTransport transport = new NetHttpTransport.Builder().build();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        Drive drive = new Drive.Builder(transport, jsonFactory, credential)
                .setApplicationName(context.getString(R.string.app_name))
                .build();
        return new GoogleDriveAccess(drive);
    }

    public static File getFileById(String fileId) {
        File file = new File();
        file.setId(fileId);
        return file;
    }

    public File getFileInfo(File folder, String name) throws IOException {
        Log.i(TAG, "getFileInfo N=" + name);

        if (folder == null) {
            folder = mDrive.files().get("root").execute();
            Log.i(TAG, "getFileInfo FolderId=root(" + folder.getId() + ") N=" + name);
        } else {
            Log.i(TAG, "getFileInfo FolderId=" + folder.getId() + " N=" + name);
        }

        Drive.Files.List request = mDrive.files().list()
                .setQ("name = '" + name + "' and '" + folder.getId() + "' in parents and trashed = false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .setPageSize(1);

        List<File> list = request.execute().getFiles();
        if (list.isEmpty()) {
            throw new StorageException("No Such imageFile/folder " + name);
        }
        return list.get(0);
    }

    private List<File> getFileListInternal(File file, boolean findFolder) throws IOException {
        Log.i(TAG, "getFileList Id=" + file.getId() + " findFolder=" + findFolder);

        String mimeCond;
        if (findFolder) {
            mimeCond = "mimeType = 'application/vnd.google-apps.folder'";
        } else {
            mimeCond = "mimeType != 'application/vnd.google-apps.folder'";
        }
        Drive.Files.List request = mDrive.files().list()
                .setQ(mimeCond + " and '" + file.getId() + "' in parents and trashed = false")
                .setSpaces("drive")
                .setFields("files(id, name, modifiedTime, size)")
                .setPageSize(1000);
        return request.execute().getFiles();
    }

    public List<File> getFileList(File file) throws IOException {
        return getFileListInternal(file, false);
    }

    public List<File> getFolderList(File file) throws IOException {
        return getFileListInternal(file, true);
    }

    public InputStream openFile(File file) throws IOException {
        try {
            Log.i(TAG, "openFile Start Id=" + file.getId());
            return mDrive.files().get(file.getId()).executeMediaAsInputStream();
        } finally {
            Log.i(TAG, "openFile End");
        }
    }

    public InputStream openFileInFolder(File folder, String name) throws IOException {
        Log.i(TAG, "openFileInFolder FolderId=" + folder.getId() + " N=" + name);
        File file = getFileInfo(folder, name);
        if (file == null) {
            return null;
        }
        return openFile(file);
    }

    public void downloadFile(File driveFile, java.io.File localFile) throws IOException {
        Log.i(TAG, "downloadFile Start Id=" + driveFile.getId());
        try (OutputStream outputStream = Files.newOutputStream(localFile.toPath())) {
            mDrive.files().get(driveFile.getId()).executeMediaAndDownloadTo(outputStream);
        }
        Log.i(TAG, "downloadFile End F=" + driveFile.getId());
    }

    private static final long SECOND_EPOCH = 1000L;

    public static long toSecondEpoch(DateTime time) {
        return (time.getValue() / SECOND_EPOCH) * SECOND_EPOCH;
    }
}
