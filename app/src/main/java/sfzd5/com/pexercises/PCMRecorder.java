package sfzd5.com.pexercises;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by fsp on 17-10-24.
 */

public class PCMRecorder {
    private AudioRecord recorder;
    //录音源
    private static int audioSource = MediaRecorder.AudioSource.MIC;
    //录音的采样频率
    private static int audioRate = 44100;
    //录音的声道，单声道
    private static int audioChannel = AudioFormat.CHANNEL_IN_MONO;
    //量化的深度
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //缓存的大小
    private static int bufferSize = AudioRecord.getMinBufferSize(audioRate, audioChannel, audioFormat);
    //记录播放状态
    private boolean isRecording = false;
    //数字信号数组
    private byte[] noteArray;
    //文件输出流
    private ByteArrayOutputStream os;
    //录音线程
    private Thread recThread;
    //结束事件
    private Over over;

    public PCMRecorder(Over over) {
        this.over = over;
    }

    public boolean startRecord() {

        if (recorder != null) {
            recorder.stop();
            recorder.release();
        }

        if(os!=null) {
            os.reset();
        } else {
            os = new ByteArrayOutputStream();
        }

        recorder = new AudioRecord(audioSource, audioRate, audioChannel, audioFormat, bufferSize);

        isRecording = true;

        recorder.startRecording();

        // 开启音频文件写入线程
        recThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeData();
            }
        });

        recThread.start();

        return false;

    }

    public boolean stopRecord() {
        isRecording = false;
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        return false;
    }

    public boolean isRecordIng() {
        return isRecording;
    }

    public byte[] getBytes() {
        return os.toByteArray();
    }

    //将数据写入文件夹,文件的写入没有做优化
    public void writeData() {
        noteArray = new byte[bufferSize];

        while (isRecording == true) {
            int recordSize = recorder.read(noteArray, 0, bufferSize);
            if (recordSize > 0) {
                try {
                    os.write(noteArray);
                } catch (IOException e) {

                }
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {

            }
        }
        if(over!=null)
            over.over();
    }
}
