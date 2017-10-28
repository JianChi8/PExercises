package sfzd5.com.pexercises;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int WEB_VIEW_CODE = 206;

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(resultCode == RESULT_OK) {
            if (requestCode == WEB_VIEW_CODE){
                int id = intent.getIntExtra("id", -1);
                if(adapter.state==TestListAdapter.ShowState.nomal){
                    adapter.remove(id);
                } else if(adapter.state==TestListAdapter.ShowState.hidden){
                    adapter.remove(id);
                }
            }
        }
    }

    TestListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent intent = getIntent();
        String key = intent.getStringExtra("key");
        boolean fromKey = intent.getBooleanExtra("fromKey", true);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TestQuestion testQuestion = (TestQuestion) view.getTag();
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra("id", testQuestion.id);
                startActivityForResult(intent, WEB_VIEW_CODE);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final TestQuestion testQuestion = (TestQuestion) view.getTag();
                AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(MainActivity.this);
                normalDialog.setTitle("提示");
                normalDialog.setMessage("你确定要更改状态吗?");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(adapter.state==TestListAdapter.ShowState.shared){
                                    DBHelper.takeDBHelper().updateShared(testQuestion.id, !testQuestion.shared);
                                    adapter.remove(testQuestion);
                                } else if(adapter.state==TestListAdapter.ShowState.nomal || adapter.state==TestListAdapter.ShowState.key){
                                    DBHelper.takeDBHelper().updateShow(testQuestion.id, !testQuestion.show);
                                    adapter.remove(testQuestion);
                                } else if(adapter.state==TestListAdapter.ShowState.hidden){
                                    DBHelper.takeDBHelper().updateShow(testQuestion.id, !testQuestion.show);
                                    adapter.remove(testQuestion);
                                }
                            }
                        });
                normalDialog.setNegativeButton("关闭",null);
                // 显示
                normalDialog.show();

                return true;
            }
        });

        adapter = new TestListAdapter(this, key, fromKey);
        listView.setAdapter(adapter);

        if(fromKey){
            toolbar.setSubtitle(key);
        } else {
            if(adapter.state==TestListAdapter.ShowState.shared){
                toolbar.setSubtitle("已分享的物理习题");
            } else if(adapter.state==TestListAdapter.ShowState.nomal){
                toolbar.setSubtitle("随机100道物理习题");
            } else if(adapter.state==TestListAdapter.ShowState.hidden){
                toolbar.setSubtitle("被隐藏的物理习题题");
            }
        }

        Button b0 = (Button) findViewById(R.id.b0);
        Button b1 = (Button) findViewById(R.id.b1);
        Button b2 = (Button) findViewById(R.id.b2);
        Button b3 = (Button) findViewById(R.id.b3);
        Button b4 = (Button) findViewById(R.id.b4);
        Button b5 = (Button) findViewById(R.id.b5);
        b0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setFilter(0);
            }
        });
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setFilter(1);
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setFilter(2);
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setFilter(3);
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setFilter(4);
            }
        });
        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setFilter(5);
            }
        });
    }

}
