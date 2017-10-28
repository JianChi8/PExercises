package sfzd5.com.pexercises;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SelectKnowlageActivity extends AppCompatActivity {

    ListView listView;
    EditText editText;
    KnowlageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_knowlage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(getString(R.string.title_activity_select_knowlage));

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                String knowlage = textView.getText().toString();
                knowlage = knowlage.substring(0, knowlage.indexOf("("));

                startMainActivity(knowlage, true);
            }
        });

        adapter = new KnowlageAdapter(this);
        listView.setAdapter(adapter);

        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new MyEditTextChangeListener());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.show_hidden){
            startMainActivity(TestListAdapter.ShowState.hidden.name(), false);
            return true;
        } else if(id == R.id.show_nomal){
            startMainActivity(TestListAdapter.ShowState.nomal.name(), false);
            return true;
        } else if(id == R.id.show_shared){
            startMainActivity(TestListAdapter.ShowState.shared.name(), false);
            return true;
        } else if(id == R.id.show_del_hidden){
            DBHelper.takeDBHelper().delHidden();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startMainActivity(String key, boolean fromKey){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fromKey", fromKey);
        intent.putExtra("key", key);
        startActivity(intent);
    }


    class MyEditTextChangeListener implements TextWatcher {
        /**
         * 编辑框的内容发生改变之前的回调方法
         */
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        /**
         * 编辑框的内容正在发生改变时的回调方法 >>用户正在输入
         * 我们可以在这里实时地 通过搜索匹配用户的输入
         */
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //adapter
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        /**
         * 编辑框的内容改变以后,用户没有继续输入时 的回调方法
         */
        @Override
        public void afterTextChanged(Editable s) {
            adapter.setFilter(s.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
