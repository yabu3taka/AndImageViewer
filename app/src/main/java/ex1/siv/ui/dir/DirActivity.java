package ex1.siv.ui.dir;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ex1.siv.storage.data.FolderImgAndText;
import ex1.siv.ui.StorageActivity;
import ex1.siv.R;
import ex1.siv.ui.show.ShowActivity;
import ex1.siv.dialog.YesNoDialogFragment;
import ex1.siv.ui.MainIntentSetting;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.cache.CacheBatchWorker;
import ex1.siv.util.CheckableList;
import ex1.siv.util.ClosingTimer;
import ex1.siv.util.ShowUtil;
import ex1.siv.util.ToggleMenu;
import ex1.siv.util.UserInterfaceUtil;

public class DirActivity extends StorageActivity
        implements YesNoDialogFragment.OnAnswerListener {
    private final static String TAG = DirActivity.class.getSimpleName();

    private final static int QUESTION_CACHE = 0;

    private RecyclerView mGridView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ActivityResultLauncher<Intent> mGetContentCache;

    private DirViewModel mDirViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir);

        // Folder List
        mGridView = findViewById(R.id.DirList);
        mGridView.setLayoutManager(new GridLayoutManager(this, 2));
        mGridView.setAdapter(new MyAdapter(this));

        // Reload
        mSwipeRefreshLayout = findViewById(R.id.DirListOuter);
        mSwipeRefreshLayout.setOnRefreshListener(this::reloadFolderList);

        // Hide ContentArea
        View contentTextView = findViewById(R.id.ContentImage);
        contentTextView.setClickable(true);
        contentTextView.setOnClickListener(v -> {
            updateSelectedItem(null);
            hideFolderInfo();
        });

        // Activity Result
        mGetContentCache = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> reloadFolderList()
        );

        // setup ViewModel
        mDirViewModel = new ViewModelProvider(this, getViewModelFactory()).get(DirViewModel.class);
        mDirViewModel.getFolderList().observe(this, this::setFolderList);
        mDirViewModel.getFolderInfo().observe(this, this::setFolderInfo);
        mDirViewModel.setScramble(MainIntentSetting.getScramble(getIntent()));

        mDirViewModel.loadFolderList(this, getTargetUri());

        // back key
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            private final ClosingTimer timer = new ClosingTimer(5000);

            @Override
            public void handleOnBackPressed() {
                if (revertFavoriteEdit()) {
                    return;
                }
                if (!timer.isClosingOk()) {
                    ShowUtil.showUserInfo(DirActivity.this, "閉じるためにもう一度押す");
                    return;
                }
                finish();
            }
        });
    }

    private Uri getTargetUri() {
        return getIntent().getData();
    }

    private void updateWindowUI() {
        int titleId = R.string.title_activity_dir;
        if (mFavoriteEditMode) {
            titleId = R.string.title_activity_dir_edit_fav;
        }
        setTitle(titleId);
        invalidateOptionsMenu();
    }

    /*********************************************************************
     * Menu
     *********************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dir, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean cacheDownloading = false;
        if (mDirViewModel.isCacheOk()) {
            cacheDownloading = !CacheBatchWorker.getController(this).canStart();
        }

        ToggleMenu tm = new ToggleMenu(menu, R.id.menu_dir_edit, R.id.menu_dir_commit);
        if (cacheDownloading) {
            tm.setAllOff();
            menu.findItem(R.id.menu_cache).setVisible(true);
        } else {
            tm.setRunning(mFavoriteEditMode);
            menu.findItem(R.id.menu_cache).setVisible(!mFavoriteEditMode);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_dir_edit) {
            startFavoriteEdit();
            return true;
        } else if (itemId == R.id.menu_dir_commit) {
            commitFavoriteEdit();
            return true;
        } else if (itemId == R.id.menu_setting) {
            openSettingsActivity(null);
            return true;
        } else if (itemId == R.id.menu_cache) {
            openDownloaderActivity();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void openDownloaderActivity() {
        Intent intent = new Intent(this, CacheActivity.class);
        intent.setData(getTargetUri());
        mGetContentCache.launch(intent);
    }

    /*********************************************************************
     * Edit Favorite
     *********************************************************************/
    private boolean mFavoriteEditMode = false;

    private void startFavoriteEdit() {
        mFavoriteEditMode = true;
        updateSelectedItem(null);
        hideFolderInfo();
        updateWindowUI();
    }

    private void commitFavoriteEdit() {
        YesNoDialogFragment.Builder builder = YesNoDialogFragment.builder(QUESTION_CACHE,
                getString(R.string.fav_title),
                getString(R.string.fav_mess));
        builder.setOk(getString(android.R.string.ok));
        builder.setCancel(getString(android.R.string.cancel));
        builder.create().show(getSupportFragmentManager(), "dialog");
    }

    private void commitFavoriteEditActually() {
        List<FolderInfo> folderList = mAdapterData.getCheckedItemList();
        mDirViewModel.getFavoriteManager().replaceFavorite(folderList);
        mFavoriteEditMode = false;
    }

    private boolean revertFavoriteEdit() {
        boolean currentMode = mFavoriteEditMode;
        mFavoriteEditMode = false;
        if (currentMode) {
            updateWindowUI();
            updateListMark();
        }
        return currentMode;
    }

    /*********************************************************************
     * OnAnswerListener
     *********************************************************************/
    @Override
    public void onAnswerClick(int questionId, int which) {
        if (questionId == QUESTION_CACHE) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    commitFavoriteEditActually();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    revertFavoriteEdit();
                    break;
            }
            updateWindowUI();
        }
    }

    /*********************************************************************
     * FolderInfo
     *********************************************************************/
    private void setFolderInfo(FolderImgAndText info) {
        Log.i(TAG, "setFolderInfo");
        if (info.hasInfo()) {
            UserInterfaceUtil.setGone(this, R.id.ContentArea, false);
            String text = info.text.title + System.lineSeparator() + info.text.comment;
            ((TextView) findViewById(R.id.ContentText)).setText(text);
            ((ImageView) findViewById(R.id.ContentImage)).setImageBitmap(info.bitmap);
        } else {
            hideFolderInfo();
        }
    }

    private void hideFolderInfo() {
        Log.i(TAG, "hideFolderInfo");
        UserInterfaceUtil.setGone(this, R.id.ContentArea, true);
    }

    /*********************************************************************
     * List
     *********************************************************************/
    private final CheckableList<FolderInfo> mAdapterData = new CheckableList<>(item -> item.filename);

    private void reloadFolderList() {
        mDirViewModel.forceReload();
        mDirViewModel.loadFolderList(DirActivity.this, getTargetUri());
    }

    private void setFolderList(List<FolderInfo> list) {
        Log.i(TAG, "setFolderList");

        List<FolderInfo> allList = mDirViewModel.appendCacheFolderInfo(list);
        allList.sort((lhs, rhs) -> lhs.filename.compareToIgnoreCase(rhs.filename));
        mAdapterData.replaceList(allList);

        mSwipeRefreshLayout.setRefreshing(false);

        updateListMark();
    }

    private void updateListMark() {
        Log.i(TAG, "updateListMark");
        mAdapterData.importChecked(folderInfo -> mDirViewModel.getFavoriteManager().isFavorite(folderInfo));
        RecyclerView.Adapter<?> adapter = mGridView.getAdapter();
        assert adapter != null;
        adapter.notifyDataSetChanged();
    }

    private void updateSelectedItem(FolderInfo target) {
        CheckableList.KeyStatus orig = mAdapterData.getCurrentKeyStatus();
        mAdapterData.setSelected(target);
        List<Integer> posList = mAdapterData.getDifferentPos(orig);
        RecyclerView.Adapter<?> adapter = mGridView.getAdapter();
        assert adapter != null;
        for (int p : posList) {
            adapter.notifyItemChanged(p);
        }
    }

    public void onItemClick(View v, int position) {
        FolderInfo target = mAdapterData.get(position);
        assert target != null;
        Log.i(TAG, "onItemClick Uri=" + target.toUri());

        if (mFavoriteEditMode) {
            Log.i(TAG, "onItemClick toggleChecked");
            mAdapterData.toggleChecked(target);
            RecyclerView.Adapter<?> adapter = mGridView.getAdapter();
            assert adapter != null;
            adapter.notifyItemChanged(position);
            mDirViewModel.loadFolderInfo(this, target);
        } else {
            int itemId = v.getId();
            if (itemId == R.id.ItemButton) {
                boolean openable = true;
                if (mDirViewModel.isCacheOk()) {
                    openable = CacheBatchWorker.getRestUri(this).isVisibleFolder(target, mDirViewModel.getCacheManager());
                }
                if (openable) {
                    Log.i(TAG, "onItemClick open");
                    Intent intent = new Intent(this, ShowActivity.class);
                    intent.setData(target.toUri());
                    MainIntentSetting.copy(intent, this);
                    startActivity(intent);
                } else {
                    ShowUtil.showUserError(this, R.string.cache_dl_running);
                }
            } else {
                Log.i(TAG, "onItemClick loadFolderInfo");
                updateSelectedItem(target);
                mDirViewModel.loadFolderInfo(this, target);
            }
        }
    }

    /*********************************************************************
     * MyAdapter (RecyclerView)
     *********************************************************************/
    private static class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final View mainArea;
        public final Button showButton;
        public final TextView mainText;

        private final DirActivity listener;

        public MyViewHolder(View itemView, DirActivity listener) {
            super(itemView);
            mainArea = itemView.findViewById(R.id.ItemArea);
            mainText = itemView.findViewById(android.R.id.text1);
            showButton = itemView.findViewById(R.id.ItemButton);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, getAdapterPosition());
        }

        public void setupEvent() {
            mainText.setOnClickListener(this);
            showButton.setOnClickListener(this);
        }

        public void clearEvent() {
            mainText.setOnClickListener(null);
            showButton.setOnClickListener(null);
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<DirActivity.MyViewHolder> {
        private final DirActivity context;

        private MyAdapter(DirActivity context) {
            this.context = context;
        }

        @NonNull
        @Override
        public DirActivity.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dir, parent, false);
            return new DirActivity.MyViewHolder(inflate, context);
        }

        @Override
        public void onBindViewHolder(@NonNull DirActivity.MyViewHolder holder, int position) {
            holder.clearEvent();

            FolderInfo folder = context.mAdapterData.get(position);
            holder.mainText.setText(folder.filename);

            int colorId = R.color.bgcolor_normal;
            switch (context.mAdapterData.getStatus(folder)) {
                case NONE:
                    break;
                case CHECKED:
                    colorId = R.color.bgcolor_dir_on;
                    break;
                case SELECTED:
                    colorId = R.color.bgcolor_select;
                    break;
            }

            int colorInt = context.getResources().getColor(colorId, context.getTheme());
            ColorStateList csl = ColorStateList.valueOf(colorInt);
            holder.mainArea.setBackgroundTintList(csl);

            holder.setupEvent();
        }

        @Override
        public int getItemCount() {
            return context.mAdapterData.getItemCount();
        }
    }
}
