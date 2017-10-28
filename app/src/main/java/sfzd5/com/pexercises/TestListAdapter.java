package sfzd5.com.pexercises;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by fsp on 17-9-21.
 */

public class TestListAdapter extends BaseAdapter {

    //创建数据库连接，获取数量，在创建视图时读取数据，尽量节约内存
    private LayoutInflater mInflater;
    List<TestQuestion> testQuestions;
    List<TestQuestion> filterTestQuestions;
    int curHard=-1;
    ShowState state = ShowState.nomal;

    public TestListAdapter(Context context, String key, boolean fromKey) {
        filterTestQuestions = new LinkedList<>();
        if(fromKey){
            state = ShowState.key;
            loadDataByKey(key);
        } else {
            state = Enum.valueOf(ShowState.class, key);
            loadData();
        }
        this.mInflater = LayoutInflater.from(context);
    }

    public void loadDataByKey(String key){
        DBHelper dbHelper = DBHelper.takeDBHelper();
        testQuestions = dbHelper.takeTestQuestionsByKnolage(key);
        setFilter(0);
    }
    public void loadData(){
        DBHelper dbHelper = DBHelper.takeDBHelper();
        if(state==ShowState.nomal){
            testQuestions = dbHelper.takeTestQuestions100();
        } else if(state==ShowState.hidden){
            testQuestions = dbHelper.takeTestQuestionsHidden();
        } else if(state==ShowState.shared){
            testQuestions = dbHelper.takeTestQuestionsShared();
        }
        setFilter(0);
    }


    public void setFilter(int hard){
        if(curHard==hard)
            return;
        filterTestQuestions.clear();
        if(hard==0){
            filterTestQuestions.addAll(testQuestions);
        } else {
            for(TestQuestion t : testQuestions){
                if(t.hard==hard)
                    filterTestQuestions.add(t);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filterTestQuestions.size();
    }

    @Override
    public Object getItem(int i) {
        return filterTestQuestions.get(i);
    }

    @Override
    public long getItemId(int i) {
        return filterTestQuestions.get(i).id;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        TestQuestion testQuestion = filterTestQuestions.get(i);
        convertView.setTag(testQuestion);

        TextView txt = (TextView) convertView;

        txt.setText((i+1)+"："+StaticTools.hard(testQuestion.hard)+" "+cleanHtml(testQuestion.question));
        return convertView;
    }

    private String cleanHtml(String html){
        html = html.replaceAll("<[^>]*>", "").replaceAll("\\s", "").replaceAll("&nbsp;", "");
        //if(html.length()>35) html = html.substring(0,35);
        return html;
    }

    public void remove(TestQuestion testQuestion) {
        testQuestions.remove(testQuestion);
        filterTestQuestions.remove(testQuestion);
        notifyDataSetChanged();
    }

    public void remove(int id) {
        for(int i=0; i<testQuestions.size(); i++){
            TestQuestion testQuestion = testQuestions.get(i);
            if(testQuestion.id==id){
                remove(testQuestion);
                break;
            }
        }
    }

    enum  ShowState{
        hidden, nomal, shared, key
    }
}
