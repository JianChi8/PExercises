package sfzd5.com.pexercises;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class RecordPlayer {
    private boolean isPlaying;
    int audioBufSize;
    AudioTrack player;

    byte[] bytes;
    int off1=0;

    public RecordPlayer() {
        this.isPlaying = false;
        audioBufSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    // 播放录音文件
    public void playRecordFile(byte[] bytes) {
        this.bytes = bytes;
        off1=0;
        if (player != null) {
            stop();
        }

        player = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBufSize,
                AudioTrack.MODE_STREAM);
        isPlaying = true;
        player.play();
        new Thread(runnable).start();
    }

    public boolean isPlaying(){
        return isPlaying;
    }



    public void stop(){
        isPlaying=false;
        player.stop();
        player.release();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while(isPlaying && off1<bytes.length){
                //这里会有延迟，因为上面使用的是MODE_STREAM模式
                player.write(bytes, off1, audioBufSize * 2);
                off1 +=audioBufSize*2;
            }
        }
    };

}