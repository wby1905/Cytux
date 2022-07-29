package cufe.wby.cytux;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.surina.soundtouch.SoundTouch;

import java.io.File;
import java.util.ArrayList;

import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import cufe.wby.cytux.Class.Music;
import cufe.wby.cytux.Reference.AndroidAudioConverter;
import cufe.wby.cytux.Reference.OnsetDetect.Sampler;
import cufe.wby.cytux.Reference.WaveAccess.WaveFileReader;
import cufe.wby.cytux.Utils.BaseActivity;
import cufe.wby.cytux.Utils.SoundPoolUtil;

/**
 * 目前已知bug：音乐文件太大会导致OOM
 */
public class Configurations extends BaseActivity {
    TextView textViewConsole = null; //控制台，用于输出处理信息
    StringBuilder consoleText = new StringBuilder();
    Music music;
    File music_file;
    ProgressBar progressBar;

    RadioGroup difficulty, size, soundFx; // 选择
    int diff = 0, si = 0, fx = 0; //对应的选项

    ProcessMusic task;

    //生成谱面的配置
    int window = 1024; //以1024个采样为一个窗口 样本窗口越大，可能超过阈值的越多
    int windowSize = 20; //阈值左右各取多少个样本窗口的频谱通量 越慢的歌要的越少，size越大，块数越多
    double multiplier = 1.5; //阈值的加权值
    ArrayList<Integer> allTiming = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurations);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        textViewConsole = findViewById(R.id.process);
        progressBar = findViewById(R.id.progressBar);

        difficulty = findViewById(R.id.difficulty);
        size = findViewById(R.id.size);
        soundFx = findViewById(R.id.soundFx);

        //配置选择框
        RadioGroup.OnCheckedChangeListener onCheckedChangeListener = (group, checkedId) -> {
            if (difficulty.equals(group)) {
                if (checkedId == R.id.easy) diff = 0;
                else diff = 1;
            } else if (size.equals(group)) {
                if (checkedId == R.id.small) si = 0;
                else si = 1;
            } else if (soundFx.equals(group)) {
                if (checkedId == R.id.no_fx) fx = 0;
                else fx = 1;
            }
        };
        difficulty.setOnCheckedChangeListener(onCheckedChangeListener);
        size.setOnCheckedChangeListener(onCheckedChangeListener);
        soundFx.setOnCheckedChangeListener(onCheckedChangeListener);

        // 检查 SoundTouch 版本
        checkLibVersion();

        //获取音乐
        music = (Music) getIntent().getSerializableExtra("Music");
        assert music != null;
        music_file = new File(music.getPath());

        // 配置按钮
        Button startGame = findViewById(R.id.start_game);

        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                music_file = new File(convertedFile.getPath());
                appendToConsole("转换格式成功！");
                allTiming = new ArrayList<>();
                task.execute(music);
            }

            @Override
            public void onFailure(Exception error) {
                error.printStackTrace();
                appendToConsole("转换格式失败！");
            }
        };

        startGame.setOnClickListener(v -> {
            //将音乐转换为wav格式
            SoundPoolUtil sp = SoundPoolUtil.getInstance(this);
            sp.play(sp.ids[0]);
            if (!music_file.getName().contains(".wav")) {
                appendToConsole("正在转换格式至WAV...");
                requestPermission();
                AndroidAudioConverter.with(this)
                        .setFile(music_file)
                        .setFormat(AudioFormat.WAV)
                        .setCallback(callback)
                        .convert(this);
            } else {
                appendToConsole("当前已经是WAV格式");
                allTiming = new ArrayList<>();
                task.execute(music);
            }
            progressBar.setProgress(33, true);

        });

        task = new ProcessMusic();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //确保内存不会泄露
        if (task != null && !task.isCancelled() && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
            task = null;
        }
    }

    /**
     * 后台对音乐进行处理，检测BPM以及生成游戏谱面,多线程处理
     */
    private class ProcessMusic extends AsyncTask<Music, Integer, Void> {
        /// 处理函数，注意需要提前转化为 wav 格式
        public void processing(Music m) {

            if (isCancelled()) return;

            // 调用 SoundTouch 库计算BPM
            appendToConsole("计算bpm中...");
            SoundTouch st = new SoundTouch();
            Log.i("SoundTouch", "process file " + music_file.getName());
            long startTime = System.currentTimeMillis();
            float bpm = st.getMyBPM(music_file.getPath());
            music.setBpm(bpm);
            st.close();
            st = null;
            Log.i("SoundTouch", "bpm " + bpm);
            if (bpm == 0) {
                music.setBpm(200);
                appendToConsole("BPM检测失败！假定为 200");
            }

            if (isCancelled()) return;
            // 生成谱面
            appendToConsole("生成谱面中...");
            getTiming();
            appendToConsole("处理完成!BPM为：" + bpm + "节奏点数量为：" + allTiming.size());
            progressBar.setProgress(100, true);

            if (isCancelled()) return;

            long endTime = System.currentTimeMillis();
            float duration = (endTime - startTime) * 0.001f;
            appendToConsole("总耗时：" + duration + 's');


            Intent intent = new Intent(Configurations.this, GamePlay.class);
            intent.putExtra("TIMING", allTiming);
            intent.putExtra("SIZE", si);
            intent.putExtra("FX", fx);
            intent.putExtra("Music", music);
            startActivity(intent);
        }

        @Override
        protected Void doInBackground(Music... music) {
            processing(music[0]);
            return null;
        }


    }

    // 生成游戏谱面
    public void getTiming() {
        String path = music_file.getPath();
        int[] data;
        WaveFileReader reader = new WaveFileReader(path);
        int[][] data_DUAL = reader.getData();
        data = new int[data_DUAL[0].length];
        for (int i = 0; i < data.length; i++) {
            int tmp = 0;
            for (int[] ints : data_DUAL) {
                tmp += ints[i];
            }
            data[i] = tmp / data_DUAL.length;
        }

        if (diff == 0) {
            windowSize = (int) (10 + music.getBpm() / 100);
            multiplier = 1.5 + 10 / music.getBpm();

        } else {
            windowSize = (int) (10 + music.getBpm() / 100);
            multiplier = 1.2 + 10 / music.getBpm();
        }
        Sampler.handleData(data, allTiming, music.getDuration(),
                window, windowSize, multiplier);

        Log.i("Sampler", "time " + music.getDuration() + "allTiming.length " + allTiming.size());
        music_file.delete();
    }


    /// 输出到控制台中
    public void appendToConsole(final String text) {
        // run on UI thread to avoid conflicts
        runOnUiThread(() -> {
            consoleText.append(text);
            consoleText.append("\n");
            textViewConsole.setText(consoleText);
            ScrollView scrollView = findViewById(R.id.scrollView1);
            scrollView.fullScroll(View.FOCUS_DOWN);

        });
    }

    /// 输出 SoundTouch 版本
    protected void checkLibVersion() {
        String ver = SoundTouch.getVersionString();
        appendToConsole("SoundTouch native library version = " + ver);
    }

    /**
     * 请求读写权限
     */
    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "申请权限", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }


}