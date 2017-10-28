package sfzd5.com.pexercises;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.qssq666.audio.AudioManager;

public class DataEncodeThread {
    private final AudioManager libMp3Lame;
    private byte[] mMp3Buffer;
    private FileOutputStream mFileOutputStream;

    /**
     * Constructor
     *
     * @param file       file
     * @param bufferSize bufferSize
     * @throws FileNotFoundException file not found
     */
    public DataEncodeThread(File file, int bufferSize, AudioManager libMp3Lame) {
        this.libMp3Lame = libMp3Lame;
        try {
            this.mFileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mMp3Buffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25))];
    }

    /**
     * Flush all data left in lame buffer to file
     */
    public void flushAndRelease() {
        //将MP3结尾信息写入buffer中
        final int flushResult = libMp3Lame.flush(mMp3Buffer);
        if (flushResult > 0) {
            try {
                mFileOutputStream.write(mMp3Buffer, 0, flushResult);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mFileOutputStream != null) {
                    try {
                        mFileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                libMp3Lame.close();
            }
        }
    }

    public void encodeData(short[] rawData, int readSize) {
        int encodedSize = libMp3Lame.encode(rawData, rawData, readSize, mMp3Buffer);
        if (encodedSize > 0) {
            try {
                mFileOutputStream.write(mMp3Buffer, 0, encodedSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
