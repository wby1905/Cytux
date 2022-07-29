package cufe.wby.cytux.Utils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import cufe.wby.cytux.R;

/**
 * 用于使用后台线程播放背景音乐
 */
public class MusicPlayer extends Service {
    public static final String APP_TAG = "Cytux";
    public static final String ACTION_MUSIC_PLAY = "cufe.wby.Utils.action.ACTION_MUSIC_PLAY";
    public static final String ACTION_MUSIC_PAUSE = "cufe.wby.Utils.action.ACTION_MUSIC_PAUSE";
    private MediaPlayer mediaPlayer;
    private String path_now; // 表示当前歌曲的路径
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        private ServiceHandler(Looper looper) {
            super(looper);
        }

        //播放  暂停
        @Override
        public void handleMessage(@NonNull Message msg) {
            Intent intent = (Intent) msg.obj;
            String path = intent.getStringExtra("path");
            if (ACTION_MUSIC_PLAY.equals(intent.getAction())) {
                if (mediaPlayer != null && path != null && !path.equals(path_now)) {
                    path_now = path;
                    Log.i("MusicPlayer", path);
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(path);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                if (mediaPlayer != null && path == null) {
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer = MediaPlayer.create(MusicPlayer.this, R.raw.main_menu);//create包含prepare()
                    mediaPlayer.setLooping(true);
                }

                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(MusicPlayer.this, R.raw.main_menu);//create包含prepare()
                    mediaPlayer.setLooping(true);
                }
                mediaPlayer.start();//播放或恢复播放
            } else if (ACTION_MUSIC_PAUSE.equals(intent.getAction())) {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("MusicPlayer");
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(APP_TAG, " MusicPlayer " + intent.getAction());
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.v(APP_TAG, " BgmService onDestroy");
        super.onDestroy();
        mServiceLooper.quit();
        //mediaPlayer非常消耗资源
        if (mediaPlayer != null) mediaPlayer.release();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
