package sfzd5.com.pexercises;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.webkit.WebView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by fsp on 17-11-9.
 */

public class ShareHelper {
    WebView webView;
    Context context;
    PEApplication app;
    String postUrl;
    public ShareHelper(WebView webView, Context context, PEApplication app, String postUrl){
        this.webView = webView;
        this.context = context;
        this.app = app;
        this.postUrl = postUrl;
    }

    public void shareScreen(){
        WifiApHelper wifiApHelper = new WifiApHelper();
        String serverIp = wifiApHelper.getServer(context);
        if(serverIp==null){
            Snackbar.make(webView, "热点未连接", Snackbar.LENGTH_LONG).setAction("Action",null).show();
        } else {
            Bitmap longImage = cutWebView();
            File saveFile = saveImage(longImage);
            if(saveFile.exists())
                post_screen(saveFile, serverIp);
            else
                Snackbar.make(webView, "文件保存失败", Snackbar.LENGTH_LONG).setAction("Action",null).show();
        }
    }

    public void sharedWebViewImage(){
        Bitmap longImage = cutWebView();
        File saveFile = saveImage(longImage);

        if(saveFile.exists()) {
            post_file(saveFile);
            //分享
            shareSingleImage(saveFile, app.getSubject()+"每日一题");
        }
    }


    Bitmap cutWebView(){
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
        return longImage;
    }

    //分享单张图片
    public File saveImage(Bitmap bmp2) {
        File shareDir = StaticTools.getDiskShareDir(context);
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
        return svaeFile;
    }

    protected void post_screen(File file, String serverIp) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "show_image", fileBody)
                .build();

        Request request = new Request.Builder()
                .url("http://"+serverIp+":8964/upload.bin")
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

    //分享单张图片
    public void shareSingleImage(File svaeFile, String title) {


        Uri imageUri = Uri.fromFile(svaeFile);
        if (imageUri != null) {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(shareIntent, title));
        } else {
            Snackbar.make(webView, "分享出错", Snackbar.LENGTH_LONG).setAction("Action", null).show();
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

    protected void post_file(File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "head_image", fileBody)
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
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
