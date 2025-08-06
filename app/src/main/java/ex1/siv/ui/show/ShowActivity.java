package ex1.siv.ui.show;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ex1.siv.R;
import ex1.siv.area.AreaData;
import ex1.siv.area.matrix.AreaMatrixCreator;
import ex1.siv.area.matrix.AreaMatrixCreatorCenter;
import ex1.siv.area.matrix.AreaMatrixCreatorEmpty;
import ex1.siv.ui.MainIntentSetting;
import ex1.siv.ui.setting.CommonSetting;
import ex1.siv.storage.cursor.FileSetCursor;
import ex1.siv.storage.cursor.EmptyCursor;
import ex1.siv.storage.data.FileSet;
import ex1.siv.storage.data.FileSetList;
import ex1.siv.ui.StorageActivity;
import ex1.siv.ui.setting.NaviSetting;
import ex1.siv.ui.setting.SideListSetting;
import ex1.siv.util.ToggleMenu;

public class ShowActivity extends StorageActivity
        implements ViewSwitcher.ViewFactory,
        FileSetCursor.CursorChangeListener {
    private final static String TAG = ShowActivity.class.getSimpleName();

    private GestureDetector mGD;
    private ScaleGestureDetector mScaleGD;
    private ImageSwitcher mMainImage;

    private ScrollView mPhotoTextArea;

    private ActivityResultLauncher<Intent> mGetContentSetting;

    private ShowViewModel mShowViewModel;
    private NaviSetting mNaviSetting;
    private AreaMatrixCreator mMatrixCreator = new AreaMatrixCreatorEmpty();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        Log.i(TAG, "onCreate");

        // Image
        mMainImage = findViewById(R.id.MainImage);
        mMainImage.setFactory(this);
        mMainImage.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        mMainImage.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

        ViewTreeObserver observer = mMainImage.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        resetImageViewSize();
                        mMainImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        // Image Text
        mPhotoTextArea = findViewById(R.id.ContentArea);
        final TextView photoText = findViewById(R.id.PhotoText);

        // SideBar
        mSideNavi = findViewById(R.id.SideNavi);

        // List Type
        Spinner spinner = findViewById(R.id.TypeSpinner);
        spinner.setAdapter(new MySpinnerAdapter(this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mShowViewModel.setSideListType(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Image List
        mImageList = findViewById(R.id.ImageList);
        mImageList.setAdapter(new MyAdapter(this));
        mImageList.setOnItemClickListener((parent, view, position, id) -> {
            MyAdapter adapter = (MyAdapter) parent.getAdapter();
            if (mCursor.setCurrentFile(adapter.getItem(position), ShowActivity.this)) {
                mMainImage.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });

        // Image Info
        mImageFileText = findViewById(R.id.ImageFileText);

        // Event
        MyGestureListener listener = new MyGestureListener();
        mGD = new GestureDetector(this, listener);
        mScaleGD = new ScaleGestureDetector(this, listener);

        // Navigation Bar
        mNaviBar = findViewById(R.id.NaviBar);
        closeNaviBar();
        mNaviBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mCursor.setCurrentFile(progress, ShowActivity.this)) {
                    mMainImage.playSoundEffect(SoundEffectConstants.CLICK);
                }
            }
        });

        // Setting
        reloadSetting();

        // Activity Result
        mGetContentSetting = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> reloadSetting()
        );

        // setup ViewModel
        mShowViewModel = new ViewModelProvider(this, getViewModelFactory()).get(ShowViewModel.class);

        mShowViewModel.getFileSetList().observe(this, data -> {
            if (data.isDone()) {
                endLoading();
            }
            if (data.hasResult()) {
                setFileSetList(data.result);
            }
        });

        mShowViewModel.getCurrent().observe(this, data -> {
            if (mShowViewModel.loadBitmap(ShowActivity.this, data.fileSet)) {
                startLoading(data.fileSet);
            }
            mShowViewModel.loadBitmapText(ShowActivity.this, data.fileSet);
            mShowViewModel.prepareCache(data);
            updateListSelection(data.fileSet);
            setNaviBarPos(data.pos);
            setImageFile(data.fileSet);
        });

        mShowViewModel.getBitmap().observe(this, data -> {
            endLoading();
            setNextImage(data);
            nextTimer();
        });

        mShowViewModel.getText().observe(this, data -> {
            photoText.setText(data);
            mPhotoTextArea.scrollTo(0, 0);
        });

        mShowViewModel.getTextOpen().observe(this, data -> {
            mPhotoTextArea.setVisibility(data ? View.VISIBLE : View.GONE);
            invalidateOptionsMenu();
        });

        mShowViewModel.getSideList().observe(this, this::setSideList);

        // setup setting
        mShowViewModel.setScramble(MainIntentSetting.getScramble(getIntent()));

        // load file list
        endLoading();
        if (mShowViewModel.loadFileList(this, getTargetUri())) {
            startLoading(null);
        }

        // Menu
        closeMenu();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            return mGD.onTouchEvent(event);
        } else {
            return mScaleGD.onTouchEvent(event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGD = null;
        mScaleGD = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimerAndUpdateUI();
        mShowViewModel.saveFolderProp();
    }

    /*********************************************************************
     * Setting
     *********************************************************************/
    private void reloadSetting() {
        Log.i(TAG, "reloadSetting");

        CommonSetting p = new CommonSetting(this);
        mTimerMilliSec = p.getTimerTime();

        SideListSetting sideListSetting = p.getSideListSetting();
        sideListSetting.setupSideList(mSideNavi);

        mNaviSetting = p.getNaviSetting(new AreaData(mMainImage));
        mNaviSetting.setupTextZone(mPhotoTextArea);
    }

    /*********************************************************************
     * Menu
     *********************************************************************/
    private boolean isMenuOpened() {
        ActionBar actionBar = getSupportActionBar();
        return actionBar != null && actionBar.isShowing();
    }

    private void openMenu() {
        openSideList(false);
        openNaviBar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Log.i(TAG, "openMenu");
            actionBar.show();
        }

        stopTimerAndUpdateUI();
        invalidateOptionsMenu();
    }

    private void closeMenu() {
        closeSideList(false);
        closeNaviBar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Log.i(TAG, "closeMenu");
            actionBar.hide();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_show, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        ToggleMenu timerTm = new ToggleMenu(menu, R.id.menu_start, R.id.menu_stop);
        timerTm.setRunning(mTimerRunning);

        ToggleMenu sideListTm = new ToggleMenu(menu, R.id.menu_open_list, R.id.menu_close_list);
        sideListTm.setRunning(isSideListOpened());

        ToggleMenu textTm = new ToggleMenu(menu, R.id.menu_open_text, R.id.menu_close_text);
        textTm.setRunning(mShowViewModel.isTextOpened());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_start) {
            startTimerAndUpdateUI();
            return true;
        } else if (itemId == R.id.menu_stop) {
            stopTimerAndUpdateUI();
            return true;
        } else if (itemId == R.id.menu_open_list) {
            openSideList(true);
            return true;
        } else if (itemId == R.id.menu_close_list) {
            closeSideList(true);
            return true;
        } else if (itemId == R.id.menu_open_text) {
            openText();
            return true;
        } else if (itemId == R.id.menu_close_text) {
            closeText();
            return true;
        } else if (itemId == R.id.menu_rotate) {
            setRequestedOrientation(mShowViewModel.toggleOrientation());
            return true;
        } else if (itemId == R.id.menu_setting) {
            openSettingsActivity(mGetContentSetting);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*********************************************************************
     * Loading
     *********************************************************************/
    private void startLoading(FileSet fileSet) {
        findViewById(R.id.LoadingArea).setVisibility(View.VISIBLE);

        String text;
        if (fileSet == null) {
            text = "Loading\nList";
        } else {
            text = "Loading\nFile";
        }
        ((TextView) findViewById(R.id.ProgressText)).setText(text);
    }

    private void endLoading() {
        findViewById(R.id.LoadingArea).setVisibility(View.GONE);
    }

    /*********************************************************************
     * Image
     *********************************************************************/
    @Override
    public View makeView() {
        ImageView i = new ImageView(this);
        i.setBackgroundColor(0xFF000000);
        i.setScaleType(ImageView.ScaleType.MATRIX);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.MATCH_PARENT, ImageSwitcher.LayoutParams.MATCH_PARENT));
        return i;
    }

    private ImageView getImageView(View view) {
        return (ImageView) view;
    }

    private void setNextImage(Bitmap bitmap) {
        AreaMatrixCreator oldCreator = mMatrixCreator;

        AreaData viewArea = new AreaData(mMainImage);
        AreaData bitmapArea = new AreaData(bitmap);
        mMatrixCreator = new AreaMatrixCreatorCenter(viewArea, bitmapArea);
        Matrix matrix = mMatrixCreator.resetScale();

        if (oldCreator == null) {
            getImageView(mMainImage.getCurrentView()).setImageMatrix(matrix);
        } else {
            getImageView(mMainImage.getCurrentView()).setImageMatrix(oldCreator.resetScale());
        }
        getImageView(mMainImage.getNextView()).setImageMatrix(matrix);

        mMainImage.setImageDrawable(new BitmapDrawable(mMainImage.getResources(), bitmap));
    }

    private void resetImageViewSize() {
        reloadSetting();

        AreaData viewArea = new AreaData(mMainImage);
        mMatrixCreator = mMatrixCreator.createNewInstanceFromViewArea(viewArea);
        getImageView(mMainImage.getCurrentView()).setImageMatrix(mMatrixCreator.resetScale());
    }

    /*********************************************************************
     * List
     *********************************************************************/
    private FileSetCursor mCursor = new EmptyCursor();

    private Uri getTargetUri() {
        return getIntent().getData();
    }

    private void setFileSetList(FileSetList list) {
        Log.i(TAG, "setFileSetList C=" + list.count);
        setupNaviBar(list);
        mCursor = list.getCursor(mShowViewModel.initCurrent().fileSet);
        mShowViewModel.setSideListType(ShowViewModel.LIST_TYPE_INDEX);
    }

    @Override
    public boolean onPositionChanged(FileSet f, int pos) {
        return mShowViewModel.setCurrent(f, pos);
    }

    /*********************************************************************
     * Navi Bar
     *********************************************************************/
    private SeekBar mNaviBar;
    private TextView mImageFileText;

    private void setupNaviBar(FileSetList list) {
        mNaviBar.setMax(list.count - 1);
    }

    private void setNaviBarPos(int pos) {
        mNaviBar.setProgress(pos);
    }

    private void setImageFile(FileSet file) {
        mImageFileText.setText(file.imageFile.filename);
    }

    private void openNaviBar() {
        mNaviBar.setVisibility(View.VISIBLE);
        mImageFileText.setVisibility(View.VISIBLE);
    }

    private void closeNaviBar() {
        mNaviBar.setVisibility(View.GONE);
        mImageFileText.setVisibility(View.GONE);
    }

    /*********************************************************************
     * Side List
     *********************************************************************/
    private LinearLayout mSideNavi;
    private ListView mImageList;
    private boolean mSideListOpen = true;

    private boolean isSideListOpened() {
        return mSideNavi.getVisibility() != View.GONE;
    }

    private void openSideList(boolean fromMenuCommand) {
        if (!fromMenuCommand) {
            if (!mSideListOpen) {
                return;
            }
        }
        if (mShowViewModel.getSideList().getValue() == null) {
            return;
        }
        mSideListOpen = true;
        mSideNavi.setVisibility(View.VISIBLE);

        invalidateOptionsMenu();
    }

    private void closeSideList(boolean fromMenuCommand) {
        if (fromMenuCommand) {
            mSideListOpen = false;
        }
        mSideNavi.setVisibility(View.GONE);

        invalidateOptionsMenu();
    }

    private void setSideList(List<FileSet> list) {
        MyAdapter adapter = (MyAdapter) mImageList.getAdapter();
        adapter.clear();
        adapter.addAll(list);

        mSideNavi.getLayoutParams().width = AreaData.getListViewArea(this, adapter).getPreferableListViewItemWidth();

        updateListSelection(mCursor.getCurrentFile());
    }

    private void updateListSelection(FileSet f) {
        MyAdapter adapter = (MyAdapter) mImageList.getAdapter();
        int s = adapter.getPosition(f);
        if (s >= 0) {
            mImageList.setItemChecked(s, true);
        } else {
            int c = mImageList.getCheckedItemPosition();
            if (c < mImageList.getCount()) {
                mImageList.setItemChecked(c, false);
            }
        }
    }

    /*********************************************************************
     * Image Text
     *********************************************************************/
    private void openText() {
        mShowViewModel.setTextOpen(this, true);
    }

    private void closeText() {
        mShowViewModel.setTextOpen(this, false);
    }

    /*********************************************************************
     * Slide Show
     *********************************************************************/
    private long mTimerMilliSec = 1500L;
    private boolean mTimerRunning = false;

    private final Handler mTimerHandler = new Handler(Looper.getMainLooper());
    private final Runnable mTimerAction = () -> {
        if (mCursor.movePos(1, ShowActivity.this)) {
            if (!mCursor.hasNext()) {
                stopTimerAndUpdateUI();
            }
        } else {
            stopTimerAndUpdateUI();
        }
    };

    private void startTimer() {
        mTimerRunning = true;
        mTimerHandler.postDelayed(mTimerAction, mTimerMilliSec);
    }

    private void nextTimer() {
        if (mTimerRunning) {
            if (mCursor.hasNext()) {
                startTimer();
            } else {
                stopTimerAndUpdateUI();
            }
        }
    }

    private void startTimerAndUpdateUI() {
        if (mCursor.hasNext()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            startTimer();
        }
        invalidateOptionsMenu();
        closeMenu();
    }

    private void stopTimerAndUpdateUI() {
        if (mTimerRunning) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            mTimerHandler.removeCallbacks(mTimerAction);
            mTimerRunning = false;

            invalidateOptionsMenu();
        }
    }

    /*********************************************************************
     * Gesture Action
     *********************************************************************/
    private void setImageMatrix(Matrix matrix) {
        getImageView(mMainImage.getCurrentView()).setImageMatrix(matrix);
    }

    private void clickedImage() {
        mMainImage.playSoundEffect(SoundEffectConstants.CLICK);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener
            implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            if (!mMatrixCreator.isScaled()) {
                stopTimerAndUpdateUI();
                if (mNaviSetting.isNaviArea(e1) && mNaviSetting.isNaviArea(e2)) {
                    if (mCursor.movePos(mNaviSetting.getDirection(e1), ShowActivity.this)) {
                        clickedImage();
                    }
                } else {
                    int d = mNaviSetting.getDirectionByVelocity(velocityX, velocityY);
                    if (mCursor.movePos(d, ShowActivity.this)) {
                        clickedImage();
                    }
                }
            }
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            if (mNaviSetting.isNaviArea(e)) {
                stopTimerAndUpdateUI();
                if (mCursor.moveIndexPos(mNaviSetting.getDirection(e), ShowActivity.this)) {
                    clickedImage();
                }
            }
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            if (mNaviSetting.isNaviArea(e)) {
                stopTimerAndUpdateUI();
                if (mCursor.movePos(mNaviSetting.getDirection(e), ShowActivity.this)) {
                    clickedImage();
                }
            } else {
                if (isMenuOpened()) {
                    closeMenu();
                } else {
                    openMenu();
                }
            }
            return true;
        }

        public boolean onDoubleTap(@NonNull MotionEvent e) {
            stopTimerAndUpdateUI();
            if (mMatrixCreator.isScaled()) {
                setImageMatrix(mMatrixCreator.resetScale());
            } else {
                if (mNaviSetting.isNaviArea(e)) {
                    if (mCursor.movePos(mNaviSetting.getDirection(e), ShowActivity.this)) {
                        clickedImage();
                    }
                } else if (mMatrixCreator.isImageArea(e)) {
                    setImageMatrix(mMatrixCreator.commitScaleFactor(e, 1.5f));
                }
            }
            return true;
        }

        private boolean inBitmap = false;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            inBitmap = mMatrixCreator.isImageArea(e);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            if (mMatrixCreator.isScaled()) {
                if (inBitmap) {
                    stopTimerAndUpdateUI();
                    setImageMatrix(mMatrixCreator.move(-distanceX, -distanceY));
                }
            }
            return true;
        }

        private PointF focusPoint = null;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            stopTimerAndUpdateUI();
            PointF p = new PointF(detector.getFocusX(), detector.getFocusY());
            if (mMatrixCreator.isImageArea(p)) {
                focusPoint = p;
            }
            return true;
        }

        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            if (focusPoint != null) {
                setImageMatrix(mMatrixCreator.commitScaleFactor(focusPoint, detector.getScaleFactor()));
            }
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            focusPoint = null;
        }
    }

    /*********************************************************************
     * MyAdapter
     *********************************************************************/
    private static class MyAdapter extends ArrayAdapter<FileSet> {
        private MyAdapter(Context context) {
            super(context, R.layout.item_show);
        }
    }

    /*********************************************************************
     * MySpinnerAdapter
     *********************************************************************/
    private static class MySpinnerAdapter extends ArrayAdapter<String> {
        private MySpinnerAdapter(Context context) {
            super(context,
                    R.layout.item_spinner,
                    context.getResources().getStringArray(R.array.list_show_types));
        }
    }
}
