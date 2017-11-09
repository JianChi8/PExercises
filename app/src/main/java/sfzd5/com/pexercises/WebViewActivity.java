package sfzd5.com.pexercises;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class WebViewActivity extends AppCompatActivity {

    static {//webView长截图分享在api21以上需要处理
        if(Build.VERSION.SDK_INT >= 21){
            WebView.enableSlowWholeDocumentDraw();
        }
    }

    WebView webView;
    TestQuestion testQuestion;
    String dateStr;
    DBHelper dbHelper;
    PEApplication app;
    int testid = -1;

    ShareHelper shareHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (PEApplication) getApplication();

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 1);

        dbHelper = DBHelper.takeDBHelper();
        //查询 数据库，生成html
        testQuestion = dbHelper.takeTestQuestion(id);
        String template = StaticTools.readRawAllText(this, R.raw.template);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        dateStr = formatter.format(date);
        String html = template.replace("<date>", dateStr+app.getSubject()+"题").replace("<content>", img2Base64(testQuestion));

        webView = (WebView) findViewById(R.id.webView);
        webView.loadData(html, "text/html; charset=UTF-8", null);
        //webView.loadData(html, "html", "utf-8");

        String dir = "";
        if (app.getSubject().equals("物理")) {
            dir ="";
        } else if (app.getSubject().equals("数学")) {
            dir ="sx";
        } else if (app.getSubject().equals("化学")) {
            dir ="hx";
        }
        String postUrl = "http://www.circuits.top/mryt"+dir+"/up.php?d=" + date + "&knowledge=" + testQuestion.knowledge + "&dbid=" + String.valueOf(testQuestion.id) + "&dbname=" + app.dbFile2Name.get(dbHelper.getDBFileName());

        shareHelper = new ShareHelper(webView, this, app, postUrl);
    }

    public String img2Base64(TestQuestion testQuestion) {
        Pattern p = Pattern.compile("<img[^>]*>");
        Pattern psrc = Pattern.compile("src=['\"]([^'\"]*)['\"]");

        StringBuilder stringBuilder = new StringBuilder();
        int pstart = 0;

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
                byte[] bytes = dbHelper.takePic(fileName);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_view, menu);
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
            shareHelper.sharedWebViewImage();

            Intent intent = new Intent();
            intent.putExtra("id", testQuestion.id);
            setResult(RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle

            //更新数据库
            dbHelper.updateShared(testQuestion.id, true);

            //finish();
            return true;
        } else if(id == R.id.answer) {
            Intent intent = new Intent(WebViewActivity.this, AnswerActivity.class);
            intent.putExtra("dbid", testQuestion.id);
            intent.putExtra("dbname", app.dbFile2Name.get(dbHelper.getDBFileName()));
            intent.putExtra("testid", testid);
            startActivity(intent);
        } else if(id == R.id.screen){
            shareHelper.shareScreen();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

}
