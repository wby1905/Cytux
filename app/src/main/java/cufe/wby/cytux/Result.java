package cufe.wby.cytux;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import cufe.wby.cytux.Utils.BaseActivity;

public class Result extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        TextView maxCombo, finalScore, count, name, rank;
        maxCombo = findViewById(R.id.max_combo);
        finalScore = findViewById(R.id.final_score);
        count = findViewById(R.id.count);
        name = findViewById(R.id.name);
        rank = findViewById(R.id.rank);

        Intent intent = getIntent();
        finalScore.setText(intent.getStringExtra("SCORE"));
        maxCombo.setText(String.valueOf(intent.getIntExtra("MAXCOMBO", 0)));
        name.setText(intent.getStringExtra("NAME"));

        int perfect, good, miss, total;
        perfect = intent.getIntExtra("PERFECT", 0);
        good = intent.getIntExtra("GOOD", 0);
        miss = intent.getIntExtra("MISS", 0);
        total = intent.getIntExtra("TOTAL", 0);

        String s = String.format("%d\t\t\t\t\t%d\t\t\t\t\t%d", perfect, good, miss);
        count.setText(s);

        if (total == 1000000) {
            s = "RANK\nMM";
            rank.setTextColor(Color.GREEN);
        } else if (total > 900000) {
            s = "RANK\nS";
            rank.setTextColor(Color.YELLOW);
        } else if (total > 850000) {
            s = "RANK\nA";
            rank.setTextColor(Color.YELLOW);
        } else if(total > 800000) {
            s = "RANK\nB";
            rank.setTextColor(getResources().getColor(R.color.teal_200, getTheme()));
        } else if(total > 700000) {
            s = "RANK\nC";
            rank.setTextColor(getResources().getColor(R.color.teal_200, getTheme()));
        } else {
            s = "FAILED";
            rank.setTextColor(Color.RED);
        }
        rank.setText(s);

        Button back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent;
                intent = new Intent("force_offline");
                sendBroadcast(intent);
            }
        });
    }

    /**
     * 按返回键时清除全部
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
}