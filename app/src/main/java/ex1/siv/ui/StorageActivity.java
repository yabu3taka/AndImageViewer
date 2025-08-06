package ex1.siv.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import ex1.siv.storage.StorageContext;
import ex1.siv.storage.cache.CacheRequester;
import ex1.siv.ui.setting.SettingsActivity;

public abstract class StorageActivity extends AppCompatActivity implements StorageContext {
    private final static String TAG = StorageActivity.class.getSimpleName();

    /*********************************************************************
     * For StorageContext
     *********************************************************************/
    private final CacheRequester cacheRequester = new CacheRequester();

    public Context getMyContext(){
        return this;
    }

    @Override
    public CacheRequester getCacheRequester() {
        return cacheRequester;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        cacheRequester.pause();
        Log.i(TAG, "onPause End");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        cacheRequester.stop();
        Log.i(TAG, "onStop End");
    }

    /*********************************************************************
     * For Util
     *********************************************************************/
    protected void openSettingsActivity(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(this, SettingsActivity.class);
        if (launcher == null) {
            startActivity(intent);
        } else {
            launcher.launch(intent);
        }
    }

    protected ViewModelProvider.Factory getViewModelFactory() {
        return new ViewModelProvider.AndroidViewModelFactory(getApplication());
    }
}
