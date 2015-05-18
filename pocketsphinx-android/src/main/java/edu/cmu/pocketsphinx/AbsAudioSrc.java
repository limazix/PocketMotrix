package edu.cmu.pocketsphinx;

import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by limazix on 13/05/15.
 */
public abstract class AbsAudioSrc implements Runnable {

    private static final long SLEEP_IN_SECONDS = 100;
    private static final String TAG = AbsAudioSrc.class.getName();
    private boolean stop = false;
    private int sampleRate;
    private int bufferSize;

    private LinkedBlockingQueue<Bundle> audioQueue = new LinkedBlockingQueue<Bundle>();

    public AbsAudioSrc(int sampleRate) {
        this.sampleRate = sampleRate;
        bufferSize = Math.round(sampleRate * SpeechRecognizerConstantsUtil.BUFFER_SIZE_SECONDS);
    }

    @Override
    public void run()  {
        startRecorder();
        Log.i(TAG, "Start Recorder");
        while (!stop) {
            try {
                readBuffer();
                Log.i(TAG, "Buffer Read");
                Thread.sleep(SLEEP_IN_SECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        Log.i(TAG, "Stop Recorder");
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public synchronized LinkedBlockingQueue<Bundle> getAudioQueue() {
        return audioQueue;
    }

    public void setAudioQueue(LinkedBlockingQueue<Bundle> audioQueue) {
        this.audioQueue = audioQueue;
    }

    public synchronized void stop() {
        this.stop = true;
    }

    public synchronized void reset() {
        this.stop = false;
    }

    public abstract void readBuffer();

    public abstract void startRecorder();

    public abstract void stopRecorder();
}
