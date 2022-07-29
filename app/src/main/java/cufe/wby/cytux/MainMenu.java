package cufe.wby.cytux;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import cufe.wby.cytux.Utils.MusicPlayer;
import cufe.wby.cytux.Utils.SoundPoolUtil;

public class MainMenu extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        SoundPoolUtil sp = SoundPoolUtil.getInstance(this);

        setContentView(R.layout.activity_main_menu);
        Button start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.play(sp.ids[0]);
                Intent intent_1 = new Intent(MainMenu.this, Choosing.class);
                startActivity(intent_1);
            }
        });

        Button exit = findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.play(sp.ids[0]);
                finish();
                System.exit(0);
            }
        });

        requestPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(intent == null)
        intent = new Intent(this, MusicPlayer.class);
        String action = MusicPlayer.ACTION_MUSIC_PLAY;
        intent.setAction(action);
        this.startService(intent);
    }

    // 结束
    @Override
    protected void onPause() {
        super.onPause();
        if(intent == null)
        intent = new Intent(this, MusicPlayer.class);
        String action = MusicPlayer.ACTION_MUSIC_PAUSE;
        intent.setAction(action);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MusicPlayer.class));
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