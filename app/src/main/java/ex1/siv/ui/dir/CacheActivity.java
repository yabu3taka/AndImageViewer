package ex1.siv.ui.dir;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import ex1.siv.R;
import ex1.siv.dialog.InputDialogFragment;
import ex1.siv.dialog.YesNoDialogFragment;
import ex1.siv.file.FileController;
import ex1.siv.progress.data.ProgressSecondary;
import ex1.siv.progress.data.ProgressSingle;
import ex1.siv.progress.format.ProgressFormatSimple;
import ex1.siv.progress.observer.ProgressAreaObserver;
import ex1.siv.progress.observer.ProgressBarObserver;
import ex1.siv.progress.observer.ProgressMessObserver;
import ex1.siv.progress.observer.ProgressPlayerObserver;
import ex1.siv.storage.cache.CacheBatchWorker;
import ex1.siv.storage.cache.CacheFolderInfo;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.worker.FolderImgBatchWorker;
import ex1.siv.ui.StorageActivity;
import ex1.siv.util.CheckableList;
import ex1.siv.util.ShowUtil;
import ex1.siv.util.UserInterfaceUtil;

public class CacheActivity extends StorageActivity
        implements YesNoDialogFragment.OnAnswerListener,
        InputDialogFragment.OnAnswerListener {
    private final static String TAG = CacheActivity.class.getSimpleName();

    private final static int QUESTION_STOP = 1;
    private final static int QUESTION_RENAME = 2;
    private final static int QUESTION_CLEAR = 3;

    private RecyclerView mTargetListsView;
    private Spinner mFilterSpinner;

    private DirViewModel mDirViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache);

        // Folder List
        mTargetListsView = findViewById(R.id.TargetList);
        mTargetListsView.setLayoutManager(new LinearLayoutManager(this));
        mTargetListsView.setAdapter(new CacheActivity.MyAdapter(this));

        DividerItemDecoration deco = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        deco.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.divider)));
        mTargetListsView.addItemDecoration(deco);

        // Cache Type
        mFilterSpinner = findViewById(R.id.FilterSpinner);
        mFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setupAdapterList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Player Control
        ProgressAreaObserver ob = new ProgressAreaObserver(this, R.id.ProgressArea);
        ob.addObserver(new ProgressPlayerObserver()
                .addPlayButton(this, R.id.RestartButton)
                .addStopButton(this, R.id.StopButton));
        ob.addObserver(new ProgressBarObserver(this, R.id.ProgressBar));
        ob.addObserver(new ProgressMessObserver(this, R.id.ProgressText, new MyProgressFormat(this)));
        ob.addObserver(progressData -> {
            if (progressData.completed()) {
                setupAdapterList();
                invalidateOptionsMenu();
            }
        });
        CacheBatchWorker.getController(this).setupObserver(this, ob);

        findViewById(R.id.RestartButton).setOnClickListener(v -> startDownload());
        findViewById(R.id.StopButton).setOnClickListener(v -> stopDownload());

        // Checkbox Control
        findViewById(R.id.CheckAllButton).setOnClickListener(v -> checkTargetAll());
        findViewById(R.id.UncheckAlButton).setOnClickListener(v -> uncheckTargetAll());

        // setup ViewModel
        mDirViewModel = new ViewModelProvider(this, getViewModelFactory()).get(DirViewModel.class);
        mDirViewModel.getFolderList().observe(this, this::setServerFolderList);

        mDirViewModel.loadFolderList(this, getTargetUri());

        //
        int spinnerId = mDirViewModel.isCacheOk() ? R.array.list_cache_types : R.array.list_direct_types;
        mFilterSpinner.setAdapter(new MySpinnerAdapter(this, spinnerId));
    }

    private Uri getTargetUri() {
        return getIntent().getData();
    }

    /*********************************************************************
     * Menu
     *********************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cache, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mDirViewModel.isCacheOk()) {
            boolean startOk = !mAdapterData.isEmptyChecked();
            boolean retryOk = !CacheBatchWorker.getRestUri(this).isRestUriEmpty();

            if (!CacheBatchWorker.getController(this).canStart()) {
                startOk = false;
                retryOk = false;
            }

            menu.findItem(R.id.menu_cache_check).setVisible(startOk);
            menu.findItem(R.id.menu_cache_sync).setVisible(startOk);
            menu.findItem(R.id.menu_cache_delete).setVisible(startOk);

            menu.findItem(R.id.menu_cache_retry).setVisible(retryOk);
        } else {
            boolean startOk = !mAdapterData.isEmptyChecked();
            boolean retryOk = !FolderImgBatchWorker.getRestUri(this).isRestUriEmpty();

            if (!FolderImgBatchWorker.getController(this).canStart()) {
                startOk = false;
                retryOk = false;
            }

            menu.findItem(R.id.menu_cache_check).setVisible(false);
            menu.findItem(R.id.menu_cache_sync).setVisible(startOk);
            menu.findItem(R.id.menu_cache_delete).setVisible(startOk);

            menu.findItem(R.id.menu_cache_retry).setVisible(retryOk);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_cache_check) {
            startCacheCheck();
            return true;
        } else if (itemId == R.id.menu_cache_sync) {
            if (mDirViewModel.isCacheOk()) {
                startDownload();
            } else {
                startImageSync();
            }
            return true;
        } else if (itemId == R.id.menu_cache_retry) {
            checkRetryTarget();
            return true;
        } else if (itemId == R.id.menu_cache_delete) {
            setClearTargetsByCheckBox();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*********************************************************************
     * Make Adapter
     *********************************************************************/
    private List<FolderInfo> mServerFolderList;

    private void setServerFolderList(List<FolderInfo> list) {
        Log.i(TAG, "setServerFolderList");
        mServerFolderList = list;
        setupAdapterList();
    }

    private List<FolderInfo> getFolderInfoListForCache() {
        int type = mFilterSpinner.getSelectedItemPosition();
        Log.i(TAG, "getFolderInfoListForCache T=" + type);

        List<CacheFolderInfo> cacheList = mDirViewModel.getCacheFolderList();

        switch (type) {
            case 1: // チェック済み
                return new ArrayList<>(mAdapterData.filterByChecked(cacheList, mCacheFolderInfoKey));
            case 2: // 不完全
                List<FolderInfo> list = new ArrayList<>();
                for (CacheFolderInfo folder : cacheList) {
                    if (!folder.isDone()) {
                        list.add(folder);
                    }
                }
                return list;
            default: // 全て
                return new ArrayList<>(cacheList);
        }
    }

    private List<FolderInfo> getFolderInfoListForDirect() {
        int type = mFilterSpinner.getSelectedItemPosition();
        Log.i(TAG, "getFolderInfoListForDirect T=" + type);
        if (type == 1) { // チェック済み
            return mAdapterData.filterByChecked(mServerFolderList);
        }
        // 全て
        return new ArrayList<>(mServerFolderList);
    }

    private void reloadAdapterListAfterChange() {
        if (mDirViewModel.isCacheOk()) {
            setupAdapterList();
        } else {
            mDirViewModel.forceReload();
            mDirViewModel.loadFolderList(this, getTargetUri());
        }
    }

    private void setupAdapterList() {
        List<FolderInfo> list;
        if (mDirViewModel.isCacheOk()) {
            list = getFolderInfoListForCache();
        } else {
            list = getFolderInfoListForDirect();
        }
        list.sort((lhs, rhs) -> lhs.filename.compareToIgnoreCase(rhs.filename));
        mAdapterData.replaceList(list);

        updateAllAdapter();
    }

    private void updateAllAdapter() {
        RecyclerView.Adapter<?> adapter = mTargetListsView.getAdapter();
        assert adapter != null;
        adapter.notifyDataSetChanged();
    }

    /*********************************************************************
     * Adapter Item List
     *********************************************************************/
    private final CheckableList<FolderInfo> mAdapterData = new CheckableList<>(item -> item.filename);
    private final CheckableList.KeyFactory<CacheFolderInfo> mCacheFolderInfoKey = item -> item.filename;

    private void updateCheckChangedUI() {
        invalidateOptionsMenu();
    }

    private void checkTargetAll() {
        mAdapterData.checkAll();
        updateAllAdapter();
        updateCheckChangedUI();
    }

    private void uncheckTargetAll() {
        mAdapterData.uncheckAll();
        updateAllAdapter();
        updateCheckChangedUI();
    }

    private void checkRetryTarget() {
        mAdapterData.uncheckAll();
        List<FolderInfo> folderList = CacheBatchWorker.getRestUri(this).getWaitingFolderInfoList(this, mDirViewModel.getStorage());
        if (folderList == null) {
            return;
        }

        mAdapterData.setChecked(folderList, true);

        updateAllAdapter();
        updateCheckChangedUI();
    }

    /*********************************************************************
     * Image Sync
     *********************************************************************/
    private void startImageSync() {
        List<FolderInfo> folderList = mAdapterData.getCheckedItemList();
        FolderImgBatchWorker.startMe(this, folderList);
    }

    private void stopImageSyncActually() {
        FolderImgBatchWorker.getController(this).cancelMe();
    }

    /*********************************************************************
     * Cache Download
     *********************************************************************/
    private List<FolderInfo> getDownloadFolderList() {
        return mAdapterData.filterByChecked(mServerFolderList);
    }

    private void startDownload() {
        List<FolderInfo> folderList = getDownloadFolderList();
        CacheBatchWorker.startMe(this, mDirViewModel.getCacheManager(), folderList);
    }

    private void startCacheCheck() {
        List<FolderInfo> folderList = getDownloadFolderList();
        CacheBatchWorker.startCheck(this, mDirViewModel.getCacheManager(), folderList);
    }

    private void stopDownload() {
        YesNoDialogFragment.Builder builder = YesNoDialogFragment.builder(QUESTION_STOP,
                getString(R.string.cache_title),
                getString(R.string.cache_stop_mess));
        builder.setOk(getString(android.R.string.ok));
        builder.setCancel(getString(android.R.string.cancel));
        builder.create().show(getSupportFragmentManager(), "dialog");
    }

    private void stopDownloadActually() {
        CacheBatchWorker.getController(this).cancelMe();
    }

    /*********************************************************************
     * Rename
     *********************************************************************/
    private FileController mRenameTarget;

    private void setRenameTarget(FileController targetFile) {
        Log.i(TAG, "setRenameTarget F=" + targetFile.getName());
        mRenameTarget = targetFile;

        InputDialogFragment.Builder builder = InputDialogFragment.builder(QUESTION_RENAME,
                getString(R.string.rename_title));
        builder.setOk(getString(android.R.string.ok));
        builder.setCancel(getString(android.R.string.cancel));
        builder.setRenameTarget(targetFile);
        builder.create().show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean validateInput(Context c, Bundle args, String str) {
        if (!InputDialogFragment.validateNewName(c, args, str)) {
            return false;
        }
        FileController parentFile = mRenameTarget.getParent();
        if (parentFile != null) {
            Log.i(TAG, "validateInput check dup");
            //noinspection RedundantIfStatement
            if (!InputDialogFragment.validateDuplication(c, parentFile, str)) {
                return false;
            }
        }
        return true;
    }

    private void renameTargetActually(String newName) {
        Log.i(TAG, "renameTargetActually F=" + newName);
        if (!mRenameTarget.rename(newName)) {
            ShowUtil.showInternalError(this, "Rename Failed");
            return;
        }
        reloadAdapterListAfterChange();
    }

    /*********************************************************************
     * Clear
     *********************************************************************/
    private List<FileController> mClearTarget;

    private void setClearTarget(FileController targetFile) {
        Log.i(TAG, "setClearTarget F=" + targetFile.getName());
        mClearTarget = new ArrayList<>();
        mClearTarget.add(targetFile);
        questionClear();
    }

    private void questionClear() {
        YesNoDialogFragment.Builder builder;
        if (mDirViewModel.isCacheOk()) {
            builder = YesNoDialogFragment.builder(QUESTION_CLEAR,
                    getString(R.string.cache_title),
                    getString(R.string.cache_clear_mess));
        } else {
            builder = YesNoDialogFragment.builder(QUESTION_CLEAR,
                    getString(R.string.cache_direct_title),
                    getString(R.string.cache_direct_clear_mess));
        }
        builder.setOk(getString(android.R.string.ok));
        builder.setCancel(getString(android.R.string.cancel));
        builder.create().show(getSupportFragmentManager(), "dialog");
    }

    private void setClearTargetsByCheckBox() {
        mClearTarget = new ArrayList<>();
        for (FolderInfo targetInfo : mAdapterData.getCheckedItemList()) {
            mClearTarget.add(targetInfo.toFileController(this));
        }
        if (mClearTarget.isEmpty()) {
            return;
        }

        questionClear();
    }

    private void clearTargetActually() {
        boolean delMe = true;
        //noinspection RedundantIfStatement
        if (mDirViewModel.isCacheOk()) {
            delMe = false;
        }
        Log.i(TAG, "clearTargetActually delMe=" + delMe);

        for (FileController fc : mClearTarget) {
            if (!fc.delete(delMe)) {
                ShowUtil.showInternalError(this, "Delete Failed");
                return;
            }
        }
        reloadAdapterListAfterChange();
    }

    /*********************************************************************
     * OnAnswerListener
     *********************************************************************/
    @Override
    public void onAnswerClick(int questionId, int which) {
        if (questionId == QUESTION_STOP) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (mDirViewModel.isCacheOk()) {
                    stopDownloadActually();
                } else {
                    stopImageSyncActually();
                }
            }
        } else if (questionId == QUESTION_CLEAR) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                clearTargetActually();
            }
        }
    }

    @Override
    public void onAnswerClick(int questionId, int which, String str) {
        if (questionId == QUESTION_RENAME) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                renameTargetActually(str);
            }
        }
    }

    /*********************************************************************
     * Adapter Action
     *********************************************************************/
    public void onItemClick(View v, int pos) {
        FolderInfo targetInfo = mAdapterData.get(pos);
        int itemId = v.getId();
        if (itemId == R.id.EditButton) {
            setRenameTarget(targetInfo.toFileController(this));
        } else if (itemId == R.id.ClearButton) {
            setClearTarget(targetInfo.toFileController(this));
        }
    }

    /**
     * @noinspection unused
     */
    public void onItemCheckedChanged(CompoundButton buttonView, boolean isChecked, int pos) {
        FolderInfo targetInfo = mAdapterData.get(pos);
        mAdapterData.setChecked(targetInfo, isChecked);
        updateCheckChangedUI();
    }

    /*********************************************************************
     * MyAdapter
     *********************************************************************/
    private static class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        public final CheckBox targetCheck;
        public final ImageView doneIcon;
        public final ImageView exIcon;
        public final ImageButton clearButton;
        public final ImageButton editButton;

        private final CacheActivity listener;

        public MyViewHolder(View itemView, CacheActivity listener) {
            super(itemView);
            targetCheck = itemView.findViewById(R.id.TargetCheck);
            doneIcon = itemView.findViewById(R.id.DoneIcon);
            exIcon = itemView.findViewById(R.id.ExIcon);
            clearButton = itemView.findViewById(R.id.ClearButton);
            editButton = itemView.findViewById(R.id.EditButton);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, getAdapterPosition());
        }

        @Override
        public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            listener.onItemCheckedChanged(v, isChecked, getAdapterPosition());
        }

        public void setupEvent() {
            clearButton.setOnClickListener(this);
            editButton.setOnClickListener(this);
            targetCheck.setOnCheckedChangeListener(this);
        }

        public void clearEvent() {
            clearButton.setOnClickListener(null);
            editButton.setOnClickListener(null);
            targetCheck.setOnCheckedChangeListener(null);
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private final CacheActivity context;

        private MyAdapter(CacheActivity context) {
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cache, parent, false);
            return new MyViewHolder(inflate, context);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.clearEvent();

            FolderInfo folder = context.mAdapterData.get(position);
            holder.targetCheck.setText(folder.filename);
            holder.targetCheck.setChecked(context.mAdapterData.isChecked(folder));

            if (folder instanceof CacheFolderInfo) {
                UserInterfaceUtil.setGone(holder.doneIcon, !((CacheFolderInfo) folder).isDone());
            } else {
                UserInterfaceUtil.setGone(holder.doneIcon, true);
            }

            holder.exIcon.setVisibility(View.GONE);

            holder.setupEvent();
        }

        @Override
        public int getItemCount() {
            return context.mAdapterData.getItemCount();
        }
    }

    /*********************************************************************
     * MySpinnerAdapter
     *********************************************************************/
    private static class MySpinnerAdapter extends ArrayAdapter<String> {
        private MySpinnerAdapter(Context context, int id) {
            super(context,
                    R.layout.item_spinner,
                    context.getResources().getStringArray(id));
        }
    }

    /*********************************************************************
     * MyProgressFormat
     *********************************************************************/
    private static class MyProgressFormat extends ProgressFormatSimple {
        private final String textDone;

        public MyProgressFormat(Context c) {
            textDone = c.getString(R.string.text_done);
            setupStatusMessage(c);

            setupWhatName(CacheBatchWorker.WHAT_CACHE_DL, "Cache Download");
            setupWhatName(CacheBatchWorker.WHAT_CACHE_CHECK, "Cache Check");
            setupWhatName(FolderImgBatchWorker.WHAT_FOLDER_IMT, "Image Sync");
        }

        @Override
        protected String formatSecondary(ProgressSecondary data) {
            return String.format(Locale.getDefault(), "%s/%s " + textDone + " (%d/%d)",
                    data.target, data.secondaryTarget,
                    data.secondaryCurrent, data.secondarySize);
        }

        @Override
        protected String formatPrimary(ProgressSingle data) {
            return data.target + textDone;
        }
    }
}
