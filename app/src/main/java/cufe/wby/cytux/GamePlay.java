package cufe.wby.cytux;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import cufe.wby.cytux.Class.Music;
import cufe.wby.cytux.Utils.BaseActivity;
import cufe.wby.cytux.Utils.SoundPoolUtil;
import cufe.wby.cytux.Views.BPMLine;
import cufe.wby.cytux.Views.Note;

public class GamePlay extends BaseActivity {

    ArrayList<Integer> allTiming;
    int perfect, good, miss;
    int size, fx, now = 0, nowY;
    Music music;
    MediaPlayer mediaPlayer = new MediaPlayer();

    RelativeLayout relativeLayout;

    TextView comboNow, scoreNow;
    int combo = 0, comboWeight, totalScore = 0, maxCombo = 0;

    BPMLine bpmLine;
    double time;
    ObjectAnimator lineAnim;

    Button pause;
    boolean state = false;

    int WIDTH, HEIGHT;

    SoundPoolUtil sp = SoundPoolUtil.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        bpmLine = findViewById(R.id.bpmLine);
        relativeLayout = findViewById(R.id.game_play);
        comboNow = findViewById(R.id.combo);
        scoreNow = findViewById(R.id.score);
        pause = findViewById(R.id.pause);

        // 获取屏幕宽高
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        WIDTH = point.x;
        HEIGHT = point.y;
        Log.i("INFO", "width: " + WIDTH + " height: " + HEIGHT);

        getData();

        initLine();

        initMedia();

        initStateButton();

        lineAnim.addUpdateListener(animation -> {
            int Y = (int) animation.getAnimatedValue();
            boolean reverse = nowY > Y;
            int mu = reverse ? -1 : 1;
            double v = HEIGHT / time;
//            Log.i("SPEED", " "+HEIGHT / time);

            while (true)
            {if (mediaPlayer != null) {
                if(now < allTiming.size())
                if (allTiming.get(now) <= mediaPlayer.getCurrentPosition()  - 500) {
                        int X = (int) (300 + Math.random() * (WIDTH - 600));
                        Y = (int) (Y + 500 * v * mu);
                        if (Y < 150) {
                            int delta = 150 - Y;
                            Y = 150 + delta;
                        } else if (Y > HEIGHT - 150) {
                            int delta = Y - HEIGHT + 150;
                            Y = HEIGHT - 150 - delta;
                        }
                        Note note = new Note(GamePlay.this, size, Y, X, allTiming.get(now), now);

                        Log.i("UPDATED", "time " + mediaPlayer.getCurrentPosition() + " pos" + Y + " Timing " + allTiming.get(now));
                        if (note.flag == 0) {
                            now += 1;
                            continue;
                        }
                        note.setOnClickListener(v1 -> {
                            if (state) return;
                            if(fx==1) sp.play(sp.ids[2]);
                            int delay = Math.abs(note.timing - mediaPlayer.getCurrentPosition() + 1500);
                            String s = "MISS";
                            if (delay < 250) {
                                perfect += 1;
                                s = "PERFECT";
                                combo += 1;
                                comboNow.setTextColor(Color.YELLOW);
                            } else if (delay < 400) {
                                good += 1;
                                combo += 1;
                                s = "GOOD";
                                comboNow.setTextColor(getResources().getColor(R.color.teal_200, getTheme()));
                            } else {
                                miss += 1;
                                combo = 0;
                                comboNow.setTextColor(Color.WHITE);
                            }
                            s = combo + "x\n" + s;
                            comboNow.setText(s);
                            maxCombo = Math.max(combo, maxCombo);
                            Log.i("Clicked", s + delay);

                            note.flag = 0;

                            totalScore = (int) ((perfect + 0.6 * good) / allTiming.size() * 900000);
                            totalScore += combo * comboWeight;
                            s = String.format("%07d", totalScore);

                            scoreNow.setText(s);
                            relativeLayout.removeView(note);
                        });
                        note.start(relativeLayout);
                        now += 1;
                    } else break;
                } else break;
            break;
            }
            nowY = Y;


        });

        lineAnim.start();
        //播放音频
        //播放音频
        new Thread(() -> {
            try {
                Thread.sleep((long) (time));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
        }).start();

        mediaPlayer.setOnCompletionListener(mp -> {
            Intent intent = new Intent(GamePlay.this, Result.class);
            intent.putExtra("NAME", music.getName().substring(0, music.getName().lastIndexOf(".")));
            intent.putExtra("PERFECT", perfect);
            intent.putExtra("GOOD", good);
            intent.putExtra("MISS", miss);
            intent.putExtra("SCORE", scoreNow.getText());
            intent.putExtra("TOTAL", totalScore);
            intent.putExtra("MAXCOMBO", maxCombo);
            mediaPlayer = null;
            startActivity(intent);
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("MISS");
        MISSReceiver receiver = new MISSReceiver();
        registerReceiver(receiver, intentFilter);
    }


    private void getData() {
        Intent intent = getIntent();
        music = (Music) intent.getSerializableExtra("Music");
        size = intent.getIntExtra("SIZE", 0);
        fx = intent.getIntExtra("FX", 0);
        allTiming = intent.getIntegerArrayListExtra("TIMING");
        assert allTiming != null;
        comboWeight = allTiming.size() * (allTiming.size() - 1) / 2;
        comboWeight = 100000 / comboWeight;
    }

    private void initMedia() {
        try {
            mediaPlayer.setDataSource(music.getPath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            Toast.makeText(this, "no music file found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void initLine() {
        // 线速计算
        time = music.getBpm();
        time = 60 / time; //  一个节奏用时多少秒
        time = 4 * time; // 保证一次扫描之内v至少有4个节奏点
        time *= 1000; // 转换为ms
        time = (int) time;

        lineAnim = ObjectAnimator.ofInt(bpmLine, "lineY", 150, HEIGHT - 150);
        lineAnim.setDuration((int) time);
        lineAnim.setInterpolator(new LinearInterpolator());
        lineAnim.setRepeatCount(ValueAnimator.INFINITE);
        lineAnim.setRepeatMode(ValueAnimator.REVERSE);
        Log.i("BPMLINE", "time " + time);
    }

    private void initStateButton() {

        pause.setOnClickListener(v -> {
            if (!state) {//true
                pause.setText("▶");
                state = true;
                if (mediaPlayer != null) mediaPlayer.pause();
                lineAnim.pause();
                Intent intent = new Intent("PAUSE");
                intent.putExtra("PAUSE", true);
                sendBroadcast(intent);

            } else {
                pause.setText("| |");
                state = false;
                Intent intent = new Intent("PAUSE");
                intent.putExtra("PAUSE", false);
                sendBroadcast(intent);
                if (mediaPlayer != null) mediaPlayer.start();
                lineAnim.resume();

            }
        });
    }

    /**
     * 暂停播放歌曲
     */
    @Override
    protected void onPause() {
        super.onPause();
        state = false;
        pause.performClick();

    }


    /**
     * 按返回键时清除
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            finish();
            Intent intent;
            intent = new Intent("force_offline");
            sendBroadcast(intent);
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    class MISSReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            combo = 0;
            miss += 1;
            String s = combo + "\n" + "MISS";
            comboNow.setTextColor(Color.WHITE);
            comboNow.setText(s);
        }
    }

}