package sfzd5.com.pexercises;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    PEApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean needPermission = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                needPermission = true;
            }
        }

        if(needPermission){
            AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
            normalDialog.setTitle("警告");
            normalDialog.setMessage("请在应用权限管理中确认读写文件权限");
            normalDialog.setPositiveButton("确定",null);
            normalDialog.show();
        } else {
            app = (PEApplication) getApplication();

            if (app.dbInitSuccess) {
                if (app.isSetSubject()) {
                    initList();
                } else {
                    selectSubject();
                }
            } else {
                showNoDBFileDialog();
            }
        }
    }

    private void showNoDBFileDialog(){
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        normalDialog.setTitle("警告");
        normalDialog.setMessage("请将数据文件拷贝到根目录文件夹：" + PEApplication.dbDirPath);
        normalDialog.setPositiveButton("确定",null);
        normalDialog.show();
    }

    private void initList(){
        String subject = app.getSubject();
        if(app.subjectDBs.contains(subject)) {
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String subject = app.getSubject();
                    if(app.subjectDBs.contains(subject)) {
                        TextView textView = (TextView) view;
                        String dbName = textView.getText().toString();
                        if(app.initDbHelper(dbName)){
                            Intent intent = new Intent(StartActivity.this, SelectKnowlageActivity.class);
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder normalDialog = new AlertDialog.Builder(StartActivity.this);
                            normalDialog.setTitle("警告");
                            normalDialog.setMessage("未找到数据文件：" + dbName);
                            normalDialog.setPositiveButton("确定",null);
                            normalDialog.show();
                        }
                    } else {
                        Toast.makeText(StartActivity.this, "未发现"+app.getSubject()+"数据库，请设置并重启APP", Toast.LENGTH_LONG);
                    }
                }
            });

            listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, app.dbNames));

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setSubtitle(subject);

        } else {
            showNoDBFileDialog();
        }

    }

    private void selectSubject(){
        String[] a = new String[app.subjectMap.keySet().size()];
        final String[] items = app.subjectMap.values().toArray(a);
        AlertDialog.Builder listDialog = new AlertDialog.Builder(this);
        listDialog.setTitle("设置科目");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String subject = items[which];
                app.checkDbFile();
                app.setSubject(subject);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initList();
                        Toast.makeText(StartActivity.this, "已设置学科："+app.getSubject(), Toast.LENGTH_LONG);
                    }
                });
            }
        });
        listDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.set_subject){
            selectSubject();
            return true;
        } else if (id == R.id.show_shared){
            Intent intent = new Intent(this, MrytActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


}
