package sfzd5.com.pexercises;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnswerActivity extends AppCompatActivity {

    static {//webView长截图分享在api21以上需要处理
        if(Build.VERSION.SDK_INT >= 21){
            WebView.enableSlowWholeDocumentDraw();
        }
    }

    WebView webView;
    TestQuestion testQuestion;
    String dateStr;
    PEApplication app;
    String template;
    String domain;
    String answer="";
    int testid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (PEApplication) getApplication();

        domain = "czwl";
        if(app.getSubject().equals("物理")) {
            domain = "czwl";
        } else if(app.getSubject().equals("数学")) {
            domain = "czsx";
        } else if(app.getSubject().equals("化学")) {
            domain = "czhx";
        }

        Intent intent = getIntent();
        int dbid = intent.getIntExtra("dbid", 1);
        testid = intent.getIntExtra("testid", 1);
        String dbname = intent.getStringExtra("dbname");


        File dbDir = app.getSubjectDbDir();
        String dbFileName = app.name2DbFile.get(dbname);
        File dbFile = new File(dbDir, dbFileName);

        DBHelper dbHelper = new DBHelper(dbFile);
        //查询 数据库，生成html
        testQuestion = dbHelper.takeTestQuestion(dbid);
        dbHelper.close();

        template = StaticTools.readRawAllText(this, R.raw.answertemplate);

        webView = (WebView) findViewById(R.id.webView);

        String url = "http://" + domain + ".cooco.net.cn/answerdetail/" + testQuestion.answer_id + "/";
        OkHttpClient mOkHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Snackbar.make(webView, "获取答案失败", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    answer = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String html = template.replace("<content>", answer);
                        webView.loadDataWithBaseURL("http://"+domain+".cooco.net.cn/", html, "text/html; charset=UTF-8", "UTF-8", null);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_answer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.shared) {
            if(testid<0) {
                Snackbar.make(webView, "分享试题后才能分享答案", Snackbar.LENGTH_LONG).show();
                return true;
            }

            // WebView 生成长图，也就是超过一屏的图片，代码中的 longImage 就是最后生成的长图
            webView.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
            webView.setDrawingCacheEnabled(true);
            webView.buildDrawingCache();
            Bitmap longImage = Bitmap.createBitmap(webView.getMeasuredWidth(), webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(longImage);  // 画布的宽高和 WebView 的网页保持一致
            Paint paint = new Paint();
            canvas.drawBitmap(longImage, 0, webView.getMeasuredHeight(), paint);
            webView.draw(canvas);

            //分享
            shareSingleImage(longImage, app.getSubject()+"每日一题");

            Intent intent = new Intent();
            intent.putExtra("id", testQuestion.id);
            setResult(RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
            //finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //分享单张图片
    public void shareSingleImage(Bitmap bmp2, String title) {
        File shareDir = StaticTools.getDiskShareDir(this);
        if (!shareDir.exists()) {
            shareDir.mkdir();
        }

        bmp2 = cleanBoard(bmp2);

        String fileName = String.valueOf((new Date()).getTime()) + ".png";
        File svaeFile = new File(shareDir, fileName);

        try {
            FileOutputStream out = new FileOutputStream(svaeFile);
            bmp2.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(svaeFile.exists()){
            if(app.getSubject().equals("物理")) {
                post_file(svaeFile, "");
            } else if(app.getSubject().equals("数学")) {
                post_file(svaeFile, "sx");
            } else if(app.getSubject().equals("化学")) {
                post_file(svaeFile, "hx");
            }


            Uri imageUri = Uri.fromFile(svaeFile);
            if (imageUri != null) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(shareIntent, title));
            } else {
                Snackbar.make(webView, "分享出错", Snackbar.LENGTH_LONG).setAction("Action",null).show();
            }
        }

    }


    Bitmap cleanBoard(Bitmap bmp2){
        int h;
        int[] pixs = new int[bmp2.getWidth()];
        for(h = bmp2.getHeight()-1; h>0; h--){
            bmp2.getPixels(pixs, 0, pixs.length, 0, h, pixs.length, 1);
            if(hasBlack(pixs)){
                break;
            }
        }

        Bitmap bmp = Bitmap.createBitmap(bmp2, 0, 0, bmp2.getWidth(), h+5, null,false);
        return bmp;

    }

    boolean hasBlack(int[] pixs){
        for(int x = 0; x<pixs.length; x++){
            int p = pixs[x];
            p = p & 0x00FFFFFF;
            if(p!=0x00FFFFFF){
                return true;
            }
        }
        return false;
    }



    protected void post_file(File file, String dir) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "head_image", fileBody)
                .build();

        Request request = new Request.Builder()
                .url("http://www.circuits.top/mryt"+dir+"/up.php?testid=" + String.valueOf(testid))
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Snackbar.make(webView, "上传失败", Snackbar.LENGTH_LONG).setAction("Action",null).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Snackbar.make(webView, "上传完成", Snackbar.LENGTH_LONG).setAction("Action",null).show();
            }
        });
    }
}
