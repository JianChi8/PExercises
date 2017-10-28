package sfzd5.com.pexercises;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import cn.qssq666.audio.AudioManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class VoiceRecActivity extends AppCompatActivity {

    /**
     * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
     */
    private static final int DEFAULT_SAMPLING_RATE = 44100;//模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 下面是对此的封装
     * private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
     */
    private static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;
    //======================Lame Default Settings=====================
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;
    /**
     * 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
     */
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;
    /**
     * Encoded bit rate. MP3 file will be encoded with bit rate 32kbps
     */
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;

    ImageView imageView;
    Button bt_rec;
    Button bt_save;
    Button bt_upload;
    Button bt_listen;
    Test test;
    RecordPlayer player;
    OkHttpClient client;

    File shareDir;
    File mp3File;
    PCMRecorder recorder;
    int mBufferSize;
    byte[] bytes;
    short[] mPCMBuffer;

    PEApplication app;
    String xkUrlPath;

    private DataEncodeThread mEncodeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_rec);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (PEApplication) getApplication();
        if(app.getSubject().equals("物理")) {
            xkUrlPath = "";
        } else if(app.getSubject().equals("数学")) {
            xkUrlPath = "sx";
        } else if(app.getSubject().equals("化学")) {
            xkUrlPath = "hx";
        }

        recorder=new PCMRecorder(pcmOver);


        test = JSON.parseObject(getIntent().getStringExtra("Test"), Test.class);

        imageView = (ImageView) findViewById(R.id.imageView);
        bt_rec = (Button) findViewById(R.id.bt_rec);
        bt_upload = (Button) findViewById(R.id.bt_upload);
        bt_save = (Button) findViewById(R.id.bt_save);
        bt_listen = (Button) findViewById(R.id.bt_listen);

        client = new OkHttpClient();

        loadPng();

        shareDir = StaticTools.getDiskShareDir(this);
        if (!shareDir.exists()) {
            shareDir.mkdir();
        }
        mp3File = new File(shareDir, "tmp.mp3");
        if(mp3File.exists())
            mp3File.delete();

        player = new RecordPlayer();

        bt_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recorder.isRecordIng()){
                    recorder.stopRecord();
                    bt_rec.setText("录音");
                } else {
                    recorder.startRecord();
                    bt_save.setEnabled(false);
                    bt_rec.setText("停止");
                }
            }
        });

        bt_listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bytes!=null)
                    player.playRecordFile(bytes);
                else
                    Snackbar.make(imageView, "请先录音", Snackbar.LENGTH_LONG).setAction("Action",null).show();
            }
        });

        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bytes!=null) {
                    short[] sa = new short[bytes.length / 2];
                    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sa);
                    int p = 0;
                    for(int i=0; i< sa.length; i++){
                        mPCMBuffer[p] = sa[i];
                        p++;
                        if (p >= mBufferSize) {
                            mEncodeThread.encodeData(mPCMBuffer, p);
                            p=0;
                        }
                    }

                    if(p>0){
                        mEncodeThread.encodeData(mPCMBuffer, p);
                    }

                    bytes = null;
                    Snackbar.make(imageView, "已保存", Snackbar.LENGTH_LONG).setAction("Action",null).show();
                } else {
                    Snackbar.make(imageView, "请先录音", Snackbar.LENGTH_LONG).setAction("Action",null).show();
                }
            }
        });

        bt_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post_file();
            }
        });

        mBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat());
        mPCMBuffer = new short[mBufferSize];
        AudioManager libMp3Lame = new AudioManager();
        libMp3Lame.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE, DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
        mEncodeThread = new DataEncodeThread(mp3File, mBufferSize, libMp3Lame);
    }

    void loadPng(){
        //创建一个Request
        Request request = new Request.Builder().url("http://wx.circuits.top/mryt"+xkUrlPath+"/upload/"+test.id+".png").build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Snackbar.make(imageView, "加载图片失败", Snackbar.LENGTH_LONG).setAction("Action",null).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //得到从网上获取资源，转换成我们想要的类型
                byte[] Picture_bt = response.body().bytes();
                //通过handler更新UI
                Message message = handler.obtainMessage();
                message.obj = Picture_bt;
                handler.sendMessage(message);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_voice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.answer) {
            if(test.dbid>0){
                Intent intent = new Intent(VoiceRecActivity.this, AnswerActivity.class);
                intent.putExtra("dbid", test.dbid);
                intent.putExtra("dbname", test.dbname);
                intent.putExtra("testid", test.id);
                startActivity(intent);
            } else {
                Toast.makeText(this, "未找到答案", Toast.LENGTH_LONG);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //通过message，拿到字节数组
            byte[] Picture = (byte[]) msg.obj;
            //使用BitmapFactory工厂，把字节数组转化为bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);
            //通过imageview，设置图片
            imageView.setImageBitmap(bitmap);
        }
    };

    Over pcmOver = new Over() {
        @Override
        public void over() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bt_save.setEnabled(true);
                    bt_upload.setEnabled(true);
                    bytes = recorder.getBytes();
                }
            });
        }
    };

    protected void post_file() {
        mEncodeThread.flushAndRelease();

        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), mp3File);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "mp3file", fileBody)
                .build();

        Request request = new Request.Builder()
                .url("http://www.circuits.top/mryt"+xkUrlPath+"/upamr.php?id=" + test.id)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Snackbar.make(imageView, "上传出错", Snackbar.LENGTH_LONG).setAction("Action",null).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Snackbar.make(imageView, "上传完成", Snackbar.LENGTH_LONG).setAction("Action",null).show();
            }
        });
    }

/*    public File mergeMp4() throws Exception {
        Movie finalMovie = new Movie();
        List<Track> audioTracks = new ArrayList<>();
        for (int i = 0; i < mp4List.size(); i++) {
            File tempfile = new File(shareDir, mp4List.get(i) + ".3gp");
            if(tempfile.exists()) {
                Movie movie = MovieCreator.build(tempfile.getAbsolutePath());
                for (Track track : movie.getTracks()) {
                    if (track.getHandler().equals("soun")) {
                        audioTracks.add(track);
                    }
                }
            }
        }

        finalMovie.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        Container container = new DefaultMp4Builder().build(finalMovie);
        File mergedFile = new File(shareDir, "merged.3gp");

        if(mergedFile.exists())
            mergedFile.delete();

        FileChannel fc = new RandomAccessFile(mergedFile, "rw").getChannel();
        container.writeContainer(fc);
        fc.close();
        return mergedFile;
    }*/
}
