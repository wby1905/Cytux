package cufe.wby.cytux.Reference.OnsetDetect;

import java.util.ArrayList;
import java.util.List;

/**
 * 参考
 * https://www.badlogicgames.com/wordpress/?p=187
 * https://blog.csdn.net/qq_43533416/article/details/105633236?utm_medium=distribute.pc_relevant.none-task-blog-utm_term-3&spm=1001.2101.3001.4242
 */
public class Sampler {

    public static void handleData(int[] data, ArrayList<Integer> allTime, long musicTime, int window, int window_size, double multiplier) {

        // 这里的谱通量也就是窗口的强度大小（音量）
        double[] dataFFT;//频谱
        List<Double> spectralFlux = new ArrayList<>();//每个样本窗口的频谱通量

        List<Double> threshold = new ArrayList<>();//每个样本窗口的阈值 debug用


        int len = data.length;

        int m = len / window;//样本窗口数

        dataFFT = new double[len];
        double[] dataFFTVariation = new double[len - m];


        FFT fft = new FFT(window);//本地类

        double[] re = new double[window];//实部
        double[] im = new double[window];//虚部，为0

        for (int k = 0; k < m; k++) {
            //处理每个样本窗口
            for (int i = 0; i < window; i++) {
                re[i] = data[k * window + i];
                im[i] = 0;
            }
            fft.fft(re, im);//每个样本窗口离散傅里叶变换后的fft,赋值到了re实部和im虚部里面

            double flux = 0;//flux是样本窗口里本次采样频谱与上个频谱之差，所有频谱的差的正值和为该样本窗口的频谱通量
            for (int i = 0; i < window; i++) {
                dataFFT[k * window + i] = re[i];//把实部值添加到频谱数组里面

                if (i > 0) {
                    int v = k * window + i - 1 - k;
                    dataFFTVariation[v] = dataFFT[k * window + i] - dataFFT[k * window + i - 1];
                    double value = dataFFTVariation[v];
                    flux += value < 0 ? 0 : value;
                }
            }
            spectralFlux.add(flux);
        }

        //阈值
        int delete = 50;
        for (int i = 0; i < spectralFlux.size() - delete; i++) {//去除最后结尾异变的值
            int start = Math.max(0, i - window_size);//防止开头、结尾溢出
            int end = Math.min(spectralFlux.size() - delete - 1, i + window_size);
            double mean = 0;
            for (int j = start; j <= end; j++) {
                mean += spectralFlux.get(j);
            }
            mean /= (end - start);

            threshold.add(mean * multiplier);//每个样本窗口与前后THRESHOLD_WINDOW_SIZE 10个样本窗口频谱通量的均值为该样本窗口的阈值

            if (mean * multiplier <= spectralFlux.get(i)) {
                int time = (int) (i * window / (len * 1.0) * musicTime);
                if ((allTime.size() > 0 && (time - allTime.get(allTime.size() - 1)) > 100) || allTime.size() < 1)
                    allTime.add(time);
            }
        }
        allTime.subList(0, 3).clear();
    }
}

