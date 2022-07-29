package cufe.wby.cytux.Utils;

import android.app.Application;
import android.util.Log;

import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cufe.wby.cytux.Reference.AndroidAudioConverter;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 需要预先加载ffmpeg库

        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
                Log.i("AUDIO CONVERTER", "done");
            }
            @Override
            public void onFailure(Exception error) {
                // FFmpeg is not supported by device
                error.printStackTrace();
            }
        });
    }


}
