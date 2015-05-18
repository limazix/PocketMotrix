package edu.cmu.pocketsphinx;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;

import java.io.IOException;

/**
 * Created by limazix on 13/05/15.
 */
public class MicrophoneAudioSrc extends AbsAudioSrc {

    private static final String TAG = MicrophoneAudioSrc.class.getName();

    private boolean skipBuffer = true;
    private AudioRecord recorder;

    public MicrophoneAudioSrc(int sampleRate) throws IOException {
        super(sampleRate);
        recorder = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION, getSampleRate(),
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, getBufferSize() * 2);

        if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                recorder.release();
                throw new IOException(
                        "Failed to initialize recorder. Microphone might be already in use.");
        }
    }

    @Override
    public synchronized void readBuffer() {
        short[] buffer = new short[getBufferSize()];

        if(!skipBuffer && recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
            int nread = recorder.read(buffer, 0, buffer.length);

            Bundle b = new Bundle();
            b.putInt(SpeechRecognizerConstantsUtil.BUNDLE_AUDIO_NREAD, nread);
            b.putShortArray(SpeechRecognizerConstantsUtil.BUNDLE_AUDIO_BUFFER, buffer);

            getAudioQueue().add(b);

        } else skipBuffer = false;

    }

    @Override
    public void startRecorder() {
        recorder.startRecording();
        if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
            stopRecorder();
            IOException ioe = new IOException(
                    "Failed to start recording. Microphone might be already in use.");
        }

    }

    @Override
    public void stopRecorder() {
        recorder.stop();
    }

    public void shutDown() {
        recorder.release();
    }
}
