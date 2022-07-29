package cufe.wby.cytux.Views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatButton;

import cufe.wby.cytux.R;

/**
 * 打击点
 */
public class Note extends AppCompatButton {
    int centerY, centerX;
    RelativeLayout.LayoutParams params;
    AnimatorSet noteAnim = new AnimatorSet();
    ObjectAnimator decay;

    Context context;

    public int flag = 1, timing, idx;

    public Note(Context context, int size, int centerY, int centerX, int timing, int idx) {
        super(context);
        this.centerY = centerY;
        this.centerX = centerX;
        this.context = context;
        this.timing = timing;
        this.idx = idx;
        this.setTranslationZ(1000f / (float) (timing + 100));

        this.setBackgroundResource(R.drawable.note);

        if (size == 0) {
            params = new RelativeLayout.LayoutParams(250, 250);
            size = 125;
        }
        if (size == 1) {
            params = new RelativeLayout.LayoutParams(300, 300);
            size = 150;
        }
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (centerY - size < 0) {
            flag = 0;
            return;
        }
        params.topMargin = centerY - size;
        params.leftMargin = centerX;

        this.setLayoutParams(params);

        initAnim();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("PAUSE");
        PauseReceiver receiver = new PauseReceiver();
        context.registerReceiver(receiver, intentFilter);
    }

    private void initAnim() {
        ObjectAnimator scaleX, scaleY, alpha, Z;
        Z = ObjectAnimator.ofFloat(this, "translationZ", this.getTranslationZ(), this.getTranslationZ() + 1);

        scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.5f, 1);
        scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.5f, 1);
        alpha = ObjectAnimator.ofFloat(this, "alpha", 0.2f, 1);

        noteAnim.play(scaleX).with(scaleY).with(alpha).with(Z);
        noteAnim.setDuration(500);
        noteAnim.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public void start(RelativeLayout relativeLayout) {
        Note note = this;
        note.setId(View.generateViewId());

        noteAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                relativeLayout.addView(note);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (note.flag != 0) {
                    decay = ObjectAnimator.ofFloat(note, "alpha", 1, 0);
                    decay.setDuration(200);
                    decay.setStartDelay(300);
                    decay.setInterpolator(new AccelerateInterpolator());
                    decay.start();
                    decay.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (note.flag != 0) {
                                Intent intent = new Intent("MISS");
                                context.sendBroadcast(intent);
                            }
                            relativeLayout.removeView(note);
                        }
                    });
                }
            }
        });

        noteAnim.start();
    }

    class PauseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(flag==0) return;
            if (intent.getBooleanExtra("PAUSE", false)) {
                if(noteAnim!=null)
                if (noteAnim.isRunning()) noteAnim.pause();
                if(decay!=null)
                if(decay.isStarted()) decay.pause();
            }
            else{
                if(noteAnim!=null)
                if(noteAnim.isPaused()) noteAnim.resume();
                if(decay!=null)
                if(decay.isPaused()) decay.resume();
            }
        }
    }

}
