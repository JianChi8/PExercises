package sfzd5.com.pexercises;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MrytActivity extends AppCompatActivity {

    ListView listView;
    OkHttpClient client;
    TestJson testJson;
    LayoutInflater mInflater;

    PEApplication app;

    String xkUrlPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mryt);
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

        //http://wx.circuits.top/mryt/json.php
        //{"pageCount":2,"list":[{"id":40,"d":"2017-10-22","knowledge":"欧姆定律"},{"id":39,"d":"2017-10-21","knowledge":"欧姆定律"},{"id":38,"d":"2017-10-20","knowledge":"欧姆定律"},{"id":37,"d":"2017-10-19","knowledge":"欧姆定律"},{"id":17,"d":"2017-10-03","knowledge":"电压电阻单元测试"}]}
        mInflater = LayoutInflater.from(this);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Test test = (Test) view.getTag();
                Intent intent = new Intent(MrytActivity.this, VoiceRecActivity.class);
                intent.putExtra("Test", JSON.toJSONString(test));
                startActivity(intent);
            }
        });

        client = new OkHttpClient();

        downJson();
    }

    void downJson(){
        //创建一个Request
        Request request = new Request.Builder().url("http://wx.circuits.top/mryt"+xkUrlPath+"/json.php").build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Snackbar.make(listView, "获取数据出错，点击重试", Snackbar.LENGTH_LONG).setAction("Action", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downJson();
                }
            }).show();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String json = response.body().string();
            testJson = JSON.parseObject(json, TestJson.class);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(new MrytListAdapter());
                }
            });
        }
    };

    class MrytListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return testJson.list.size();
        }

        @Override
        public Object getItem(int position) {
            return testJson.list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return testJson.list.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            }
            Test test = testJson.list.get(position);
            convertView.setTag(test);

            TextView txt = (TextView) convertView;

            txt.setText(test.d+" "+test.knowledge);
            return convertView;
        }
    }
}
