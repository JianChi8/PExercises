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
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    ShareHelper shareHelper;
    OkHttpClient mOkHttpClient;
    File dbFile;

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
        dbFile = new File(dbDir, dbFileName);

        DBHelper dbHelper = new DBHelper(dbFile);
        //查询 数据库，生成html
        testQuestion = dbHelper.takeTestQuestion(dbid);
        dbHelper.close();

        template = StaticTools.readRawAllText(this, R.raw.answertemplate);

        webView = (WebView) findViewById(R.id.webView);

        if(testQuestion.answer.isEmpty()){
            down_answer_img();
        } else {
            showHtml();
        }

        String dir = "";
        if (app.getSubject().equals("物理")) {
            dir ="";
        } else if (app.getSubject().equals("数学")) {
            dir ="sx";
        } else if (app.getSubject().equals("化学")) {
            dir ="hx";
        }

        shareHelper = new ShareHelper(webView, this, app, "http://www.circuits.top/mryt"+dir+"/up.php?testid=" + String.valueOf(testid));
    }

    void showHtml(){
        String html = template.replace("<content>", img2Base64(testQuestion));
        webView.loadData(html, "text/html; charset=UTF-8", null);
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
            shareHelper.sharedWebViewImage();

            Intent intent = new Intent();
            intent.putExtra("id", testQuestion.id);
            setResult(RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
            //finish();
            return true;
        } else if(id == R.id.screen){
            shareHelper.shareScreen();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
    下载图片
    **/
    void down_answer_img(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mOkHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                String aurl = "http://" + domain + ".cooco.net.cn/answerdetail/" + testQuestion.answer_id + "/";
                Pattern p = Pattern.compile("<img[^>]*>");
                Pattern psrc = Pattern.compile("src=['\"]([^'\"]*)['\"]");

                Request request = new Request.Builder().url(aurl).build();
                Call call = mOkHttpClient.newCall(request);
                try {
                    Response response = call.execute();
                    if(response.isSuccessful()) {
                        String html = response.body().string();
                        if (html.isEmpty()) {
                            testQuestion.answer = "答案略";
                        } else {
                            DBHelper dbHelper = new DBHelper(dbFile);

                            testQuestion.answer = html;
                            Matcher m = p.matcher(html);
                            int j = 0;
                            while (m.find()) {
                                String img = m.group();
                                Matcher src = psrc.matcher(img);
                                if (src.find()) {
                                    String url = src.group(1);
                                    if (url.startsWith("/"))
                                        url = "http://" + domain + ".cooco.net.cn" + url;//http://czwl.cooco.net.cn/files/down/test/2016/03/25/20/2016032520323732298210.files/image043.gif
                                    else if (url.startsWith("http")) {

                                    } else {
                                        continue;
                                    }

                                    String picname = testQuestion.id + "_" + j + url.substring(url.length() - 4);
                                    System.out.println(picname);

                                    try {
                                        Request prequest = new Request.Builder().url(url).build();
                                        Call pcall = mOkHttpClient.newCall(prequest);
                                        Response presponse = pcall.execute();
                                        if(presponse.isSuccessful()){
                                            InputStream is = null;
                                            byte[] buf = new byte[2048];
                                            int len = 0;
                                            ByteArrayOutputStream bos=new ByteArrayOutputStream();
                                            try {
                                                is = presponse.body().byteStream();
                                                while ((len = is.read(buf)) != -1) {
                                                    bos.write(buf, 0, len);
                                                }
                                                bos.flush();
                                                dbHelper.insertAnswerPic(picname, bos.toByteArray());

                                            } catch (Exception e) {
                                            } finally {
                                                try {
                                                    if (is != null)
                                                        is.close();
                                                } catch (IOException e) {
                                                }
                                                try {
                                                    if (bos != null)
                                                        bos.close();
                                                } catch (IOException e) {
                                                }
                                            }
                                        }
                                    } catch (Exception e) {

                                    }

                                    j++;
                                }
                            }

                            dbHelper.updateAnswer(testQuestion);
                            dbHelper.close();

                        }
                        //显示答案

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showHtml();
                            }
                        });

                    } else {
                        //返回获取错误
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(webView, "获取答案失败", Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public String img2Base64(TestQuestion testQuestion) {
        Pattern p = Pattern.compile("<img[^>]*>");
        Pattern psrc = Pattern.compile("src=['\"]([^'\"]*)['\"]");

        StringBuilder stringBuilder = new StringBuilder();
        int pstart = 0;

        DBHelper dbHelper =new DBHelper(dbFile);

        Matcher m = p.matcher(testQuestion.question);
        int j = 0;
        while (m.find()) {
            stringBuilder.append(testQuestion.question.substring(pstart, m.start()));
            pstart = m.end();

            String img = m.group();
            Matcher src = psrc.matcher(img);
            if (src.find()) {
                String url = src.group(1);
                String fileName = testQuestion.id + "_" + j + url.substring(url.length() - 4);
                byte[] bytes = dbHelper.takeAnswerPic(fileName);
                if (bytes!=null) {
                    stringBuilder.append(img.substring(0, src.start(1)));
                    stringBuilder.append(imageToBase64Head(fileName, bytes));
                    stringBuilder.append(img.substring(src.end(1)));
                } else {
                    stringBuilder.append(img);
                }

                j++;
            } else {
                stringBuilder.append(img);
            }
        }
        stringBuilder.append(testQuestion.question.substring(pstart));

        return stringBuilder.toString();
    }


    public static String imageToBase64Head(String imgFile, byte[] bytes){
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        String type = imgFile.substring(imgFile.length()-3,imgFile.length());
        //为编码添加头文件字符串
        String head = "data:image/"+type+";base64,";
        return head + Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
