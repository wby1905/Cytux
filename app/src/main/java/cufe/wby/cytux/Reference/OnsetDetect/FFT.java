package cufe.wby.cytux.Reference.OnsetDetect;


/**
 * 快速傅里叶变换，用于对音乐转化至频域，代码参考外网。
 * https://www.ee.columbia.edu/~ronw/code/MEAPsoft/doc/html/FFT_8java-source.html
 */
public class FFT {

    int n, m;

    // Lookup tables. Only need to recompute when size of FFT changes.
    double[] cos;
    double[] sin;

    public FFT(int n) {
        this.n = n;
        this.m = (int) (Math.log(n) / Math.log(2));

        // Make sure n is a power of 2
        if (n != (1 << m))
            throw new RuntimeException("FFT length must be power of 2");

        // precompute tables
        cos = new double[n / 2];
        sin = new double[n / 2];

        for (int i = 0; i < n / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / n);
            sin[i] = Math.sin(-2 * Math.PI * i / n);
        }

    }


    //x是实部，y是虚部
    public void fft(double[] x, double[] y) {
        int i, j, k, n1, n2, a;
        double c, s, t1, t2;

        //n=16;
        // Bit-reverse
        j = 0;
        n2 = n / 2;//8
        for (i = 1; i < n - 1; i++) {
            n1 = n2;//8
            while (j >= n1) {
                j = j - n1;//0 4 0 4 0
                n1 = n1 / 2;//4 4 4 4 4
            }
            j = j + n1;//8 4 12 8 4 12 8 4 12

            if (i < j) {// i j，1 8,2 4,3 12,  4 8,5 4,6 12,  7 8,8 4,9 12  ,,10 8,11 4,12 12,  13 8,14 4,
                // i j交换，1 8,2 4,3 12,  4 8, ,6 12,  7 8, ,9 12  ,, , , ,   , , ;     8, 4, 12, 1, 5, 3, 2, 7, 6, 10, 11, 9, 13, 14, 15, 16,
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;
        //m=4
        for (i = 0; i < m; i++) {
            n1 = n2;//1
            n2 = n2 + n2;//2
            a = 0;

            for (j = 0; j < n1; j++) {
                c = cos[a];
                s = sin[a];
                a += 1 << (m - i - 1);

                for (k = j; k < n; k = k + n2) {
                    t1 = c * x[k + n1] - s * y[k + n1];
                    t2 = s * x[k + n1] + c * y[k + n1];
                    x[k + n1] = x[k] - t1;
                    y[k + n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }
    }
}
