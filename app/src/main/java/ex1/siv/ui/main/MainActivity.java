package ex1.siv.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import ex1.siv.storage.exception.StorageStrIdException;
import ex1.siv.storage.drive.DriveStorageConnector;
import ex1.siv.storage.task.ResultData;
import ex1.siv.ui.dir.DirActivity;
import ex1.siv.ui.StorageActivity;
import ex1.siv.R;

import ex1.siv.ui.MainIntentSetting;
import ex1.siv.ui.setting.StorageFolderSetting;
import ex1.siv.storage.StorageType;
import ex1.siv.storage.data.TopFolderInfo;
import ex1.siv.util.ShowUtil;

public class MainActivity extends StorageActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private MainViewModel mMainViewModel;

    private ActivityResultLauncher<Intent> mGetContentSignIn;
    private ActivityResultLauncher<Intent> mGetContentFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Read Setting
        StorageFolderSetting p = new StorageFolderSetting(this);
        ((EditText) findViewById(R.id.LocalFolderText)).setText(p.getLocalTopDir());
        ((EditText) findViewById(R.id.DriveFolderText)).setText(p.getDriveTopDir());

        // Storage Type Spinner
        Spinner spinner = findViewById(R.id.StorageSpinner);
        int storageType = p.getStorageType();
        spinner.setSelection(storageType);
        changeStorageType(storageType);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                changeStorageType(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // GoogleSignIn
        mGetContentSignIn = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleSignInResult);

        findViewById(R.id.SignInButton).setOnClickListener(v -> {
            GoogleSignInClient c = DriveStorageConnector.createGoogleSignInClient(MainActivity.this);
            mGetContentSignIn.launch(c.getSignInIntent());
        });

        // Folder selection
        mGetContentFile = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        onFileSelect(result.getData());
                    }
                });

        findViewById(R.id.SelectFolderButton).setOnClickListener(v -> openFileSelectDialog());
        findViewById(R.id.SelectCheckButton).setOnClickListener(v -> checkDirSetting());

        // Go Next
        findViewById(R.id.OkButton).setOnClickListener(v -> openDirActivity());
        ((EditText) findViewById(R.id.PasswordText)).setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                openDirActivity();
            }
            return false;
        });

        // setup ViewModel
        mMainViewModel = new ViewModelProvider(this, getViewModelFactory()).get(MainViewModel.class);
        mMainViewModel.getTopFolder().observe(this, this::setTopFolder);

        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            requestAccountPermission();
            return;
        }
        doMainProcess();
    }

    private void doMainProcess() {
        Log.i(TAG, "doMainProcess");
        updateSignInUI(GoogleSignIn.getLastSignedInAccount(this));

        MainDirData mdd = new MainDirData(this);
        if (mdd.hasDirString()) {
            if (!mMainViewModel.isLoaded(mdd)) {
                checkDirSetting();
            }
        }
    }

    /*********************************************************************
     * Google Account
     *********************************************************************/
    private void handleSignInResult(ActivityResult result) {
        try {
            Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            updateSignInUI(completedTask.getResult(ApiException.class));
        } catch (ApiException e) {
            Log.w(TAG, "handleSignInResult: failed code=" + e.getStatusCode());
            updateSignInUI(null);
        }
    }

    private void updateSignInUI(GoogleSignInAccount a) {
        String email = "";
        if (a != null) {
            email = a.getEmail();
        }
        ((TextView) findViewById(R.id.DriveUserText)).setText(email);
    }

    /*********************************************************************
     * Permission
     *********************************************************************/
    private final static int REQUEST_CODE_ACCOUNT_PERMISSION = 0;

    private void requestAccountPermission() {
        String type = Manifest.permission.GET_ACCOUNTS;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, type)) {
            ShowUtil.showUserError(this, R.string.perm_request_account);
        }
        ActivityCompat.requestPermissions(this, new String[]{type}, REQUEST_CODE_ACCOUNT_PERMISSION);
    }

    private void openPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCOUNT_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult:GRANTED");
                doMainProcess();
            } else {
                Log.e(TAG, "onRequestPermissionsResult:DENIED");
                String type = Manifest.permission.GET_ACCOUNTS;
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, type)) {
                    ShowUtil.showUserError(this, R.string.perm_request_account);
                    ActivityCompat.requestPermissions(this, new String[]{type}, REQUEST_CODE_ACCOUNT_PERMISSION);
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.perm_error_title)
                            .setMessage(R.string.perm_self_account)
                            .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> openPermissionSettings())
                            .create()
                            .show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /*********************************************************************
     * Menu
     *********************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_setting) {
            openSettingsActivity(null);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*********************************************************************
     * Folder Setting
     *********************************************************************/
    public void onFileSelect(Intent intent) {
        Log.i(TAG, "onFileSelect " + intent.getData());
        Uri uri = intent.getData();
        if (uri != null) {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        String text = uri == null ? "" : uri.toString();
        ((EditText) findViewById(R.id.LocalFolderText)).setText(text);
    }

    private void changeStorageType(int type) {
        if (type == StorageType.STORAGE_LOCAL) {
            findViewById(R.id.LocalGroup).setVisibility(View.VISIBLE);
            findViewById(R.id.DriveGroup).setVisibility(View.GONE);
        } else {
            findViewById(R.id.LocalGroup).setVisibility(View.GONE);
            findViewById(R.id.DriveGroup).setVisibility(View.VISIBLE);
        }
    }

    private void openFileSelectDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        mGetContentFile.launch(intent);
    }

    /*********************************************************************
     * Check Folder Setting
     *********************************************************************/
    private TopFolderInfo mTopFolder = null;

    private void printDirCheckResult(String mess) {
        ((TextView) findViewById(R.id.SelectCheckText)).setText(mess);
    }

    private void checkDirSetting() {
        Log.i(TAG, "checkDirSetting start");

        MainDirData mdd = new MainDirData(this);
        if (!mdd.hasDirString()) {
            printDirCheckResult(getString(R.string.fcheck_empty));
            return;
        }

        mMainViewModel.loadTopFolder(this, mdd);
    }

    private void setTopFolder(ResultData<TopFolderInfo> data) {
        Log.i(TAG, "setTopFolder L=" + data.loading);
        mTopFolder = data.result;

        if (data.loading) {
            printDirCheckResult(getString(R.string.fcheck_doing));
        } else if (data.hasResult()) {
            if (mMainViewModel.isLoaded(new MainDirData(this))) {
                printDirCheckResult(getString(R.string.fcheck_done));
            } else {
                printDirCheckResult(getString(R.string.fcheck_retry));
            }
        } else {
            assert data.exception != null;
            printDirCheckResult(data.exception.getMessage());
        }
    }

    /*********************************************************************
     * To DirAct
     *********************************************************************/
    private void openDirActivity() {
        TopFolderInfo topFolder = mTopFolder;
        if (topFolder == null) {
            ShowUtil.showUserError(this, R.string.fcheck_dir);
            return;
        }

        MainDirData current = new MainDirData(this);
        if (!mMainViewModel.isLoaded(current)) {
            ShowUtil.showUserError(this, R.string.fcheck_retry);
            return;
        }

        EditText passText = findViewById(R.id.PasswordText);
        try {
            topFolder.checkPassword(passText.getText().toString());
        } catch (StorageStrIdException ex) {
            ShowUtil.showUserError(this, ex.strId);
            return;
        } catch (Exception ex) {
            ShowUtil.showException(this, ex);
            return;
        }

        passText.setText("");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(passText.getWindowToken(), 0);
        }

        SharedPreferences.Editor e = new StorageFolderSetting(this).me().edit();
        StorageFolderSetting.setStorageType(e, current.type);
        StorageFolderSetting.setDriveTopDir(e, current.driveTopDir);
        StorageFolderSetting.setLocalTopDir(e, current.localTopDir);
        e.apply();

        Intent intent = new Intent(this, DirActivity.class);
        intent.setData(topFolder.topFolder.toUri());
        MainIntentSetting.setScramble(intent, topFolder.getScramble());
        startActivity(intent);
    }
}
