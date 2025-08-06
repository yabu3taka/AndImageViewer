package ex1.siv.storage.cache;

import androidx.annotation.NonNull;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import ex1.siv.storage.task.ResultCallback;

/**
 * キャッシュのダウンロード処理を依頼する
 * {@link CacheAction}, {@link CacheActionReply} を渡して依頼する。
 */
public class CacheRequester {
    private final static String TAG = CacheRequester.class.getSimpleName();

    /*********************************************************************
     * Request
     *********************************************************************/
    private final Set<String> mDoneIdSet = new HashSet<>();

    public <R> void requestLoad(@NonNull CacheActionReply<R> action, ResultCallback<R> c) {
        mDoneIdSet.add(action.getId());

        start();
        sendLoadRequestMsg(action, c);
    }

    public void requestCache(@NonNull CacheAction action) {
        String id = action.getId();
        if (mDoneIdSet.contains(id)) {
            Log.i(TAG, "requestCache contains F=" + action.file);
            return;
        }

        if (action.existCache()) {
            Log.i(TAG, "requestCache existCache F=" + action.file);
            mDoneIdSet.add(id);
            return;
        }

        if (isStarted()) {
            sendCacheRequestMsg(action);
        }
    }

    public void stopAllCacheRequest() {
        CacheAction.removeAllCacheRequestMessages(mHandler);
    }

    public void cancelAction(CacheAction action) {
        if (mHandler == null) {
            Log.e(TAG, "cancelAction No Handler");
            return;
        }
        Log.i(TAG, "cancelAction F=" + action.file);
        action.cancelMe(mHandler);
    }

    private <R> void sendLoadRequestMsg(CacheActionReply<R> action, ResultCallback<R> c) {
        Log.i(TAG, "sendLoadRequestMsg F=" + action.file);
        Message mess = action.makeCacheRequestMessage(false);
        action.setCallback(c);
        mess.replyTo = mReplyManager.messenger;
        mHandler.sendMessageAtFrontOfQueue(mess);
    }

    private void sendCacheRequestMsg(CacheAction action) {
        Log.i(TAG, "sendCacheRequestMsg F=" + action.file);
        Message mess = action.makeCacheRequestMessage(true);
        mHandler.sendMessage(mess);
    }

    /*********************************************************************
     * Download Thread
     *********************************************************************/
    private CacheReplyManager mReplyManager = null;
    private HandlerThread mHandlerThread = null;
    private Handler mHandler = null;

    public boolean isStarted() {
        return mHandlerThread != null;
    }

    private void start() {
        if (!isStarted()) {
            Log.i(TAG, "start");
            mHandlerThread = new HandlerThread("loader");
            mHandlerThread.start();
            mHandler = Handler.createAsync(mHandlerThread.getLooper(), new CacheHandlerCallback());

            mReplyManager = new CacheReplyManager();
        }
    }

    public void pause() {
        Log.i(TAG, "pause");
    }

    public void stop() {
        if (mHandlerThread != null) {
            Log.i(TAG, "stop");
            CacheAction.removeAllMessages(mHandler);
            mHandler = null;

            mHandlerThread.quit();
            mHandlerThread = null;

            mReplyManager.stop();
            mReplyManager = null;
        }
    }

    private static class CacheHandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            CacheAction action = (CacheAction) msg.obj;
            Log.i(TAG, "CacheHandlerCallback F=" + action.file);
            action.downloadCache();
            try {
                if (msg.replyTo != null) {
                    Log.i(TAG, "CacheHandlerCallback replyTo F=" + action.file);
                    Message replyMess = Message.obtain();
                    replyMess.what = msg.what;
                    replyMess.obj = action;
                    msg.replyTo.send(replyMess);
                }
            } catch (Exception ex) {
                Log.e(TAG, "CacheHandlerCallback Ex", ex);
            }
            return false;
        }
    }

    /*********************************************************************
     * Reply Thread
     *********************************************************************/
    private static class CacheReplyManager {
        private final Handler mHandler;
        private final Messenger messenger;

        CacheReplyManager() {
            mHandler = new Handler(Looper.getMainLooper(), new ReplyHandlerCallback());
            messenger = new Messenger(mHandler);
        }

        void stop() {
            CacheAction.removeAllMessages(mHandler);
        }
    }

    private static class ReplyHandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            CacheAction action = (CacheAction) msg.obj;
            Log.i(TAG, "ReplyHandlerCallback F=" + action.file);
            if (!action.isCanceled()) {
                action.doCallback();
            }
            return false;
        }
    }
}
