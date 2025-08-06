package ex1.siv.storage.drive;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.api.services.drive.DriveScopes;

import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageConnector;
import ex1.siv.util.GoogleDriveAccess;

public class DriveStorageConnector implements StorageConnector {
    private final static String MY_SCOPE = DriveScopes.DRIVE_READONLY;

    public static GoogleSignInClient createGoogleSignInClient(Context context) {
        return GoogleDriveAccess.createGoogleSignInClient(context, MY_SCOPE);
    }

    @Override
    public Storage getStorage(@NonNull Context context) {
        GoogleDriveAccess d = GoogleDriveAccess.createInstance(context, MY_SCOPE);
        return new DriveStorage(d);
    }
}
