package cufe.wby.cytux.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

import cufe.wby.cytux.R;

public class SoundPoolUtil {
    private static SoundPoolUtil soundPoolUtil;
    private final SoundPool soundPool;
    public int[] ids = new int[3];

    //单例模式
    public static SoundPoolUtil getInstance(Context context) {
        if (soundPoolUtil == null)
            soundPoolUtil = new SoundPoolUtil(context);
        return soundPoolUtil;
    }

    @SuppressLint("NewApi")//这里初始化SoundPool的方法是安卓5.0以后提供的新方式
    private SoundPoolUtil(Context context) {
        soundPool = new SoundPool.Builder().build();
        //加载音频文件
        ids[0] = soundPool.load(context, R.raw.button_fx, 1);
        ids[2] = soundPool.load(context, R.raw.fx, 1);
    }

    public void play(int number) {
        Log.d("SOUND_FX", "number " + number);
        /*
         * 播放音频
         * params说明：
         * //左耳道音量【0~1】
         * //右耳道音量【0~1】
         * //播放优先级【0表示最低优先级】
         * //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
         * //播放速度【1是正常，范围从0~2】
         */
        soundPool.play(number, 1, 1, 0, 0, 1);
    }
}