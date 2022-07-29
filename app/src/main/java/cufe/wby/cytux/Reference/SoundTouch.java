package net.surina.soundtouch; //这里包名不能改变，改了jni会出错

public final class SoundTouch {
    // Native interface function that returns SoundTouch version string.
    // This invokes the native c++ routine defined in "soundtouch-jni.cpp".
    public native static String getVersionString();

    private native static long newInstance();

    private native void deleteInstance(long handle);

    public native final float getMyBPM(String inputFile);

    long handle;


    public SoundTouch() {
        handle = newInstance();
    }


    public void close() {
        deleteInstance(handle);
        handle = 0;
    }


    // Load the native library upon startup
    static {
        System.loadLibrary("soundtouch");
    }
}
