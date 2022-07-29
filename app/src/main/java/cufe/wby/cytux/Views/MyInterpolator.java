package cufe.wby.cytux.Views;

import android.animation.TimeInterpolator;

public class MyInterpolator implements TimeInterpolator {

    public double margin = 0.1;

    public MyInterpolator(double margin) {
        this.margin = margin;
    }

    @Override
    public float getInterpolation(float input) {
        float result;
        if (input <= margin) {
            result = (float) (2 * input - margin);
        } else if(input >= 1 - margin){
            result = (float) (2 * input - 1 + margin);
        } else {
            result = input;
        }

        return result;
    }
}