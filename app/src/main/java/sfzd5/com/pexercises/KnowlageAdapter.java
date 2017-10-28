package sfzd5.com.pexercises;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by fsp on 17-9-22.
 */

public class KnowlageAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    List<String> knowledges;
    List<String> filterKnowledges;

    public KnowlageAdapter(Context context){
        loadData();
        this.mInflater = LayoutInflater.from(context);
    }

    public void loadData(){
        DBHelper dbHelper = DBHelper.takeDBHelper();
        HashMap<String,List<String>> knowledgeMap = dbHelper.takeKnowledgeMap();
        String[] ss = new String[knowledgeMap.size()];
        ss = knowledgeMap.keySet().toArray(ss);
        knowledges = knowledgeMap.get(ss[0]);
        filterKnowledges = new LinkedList<>();
        setFilter("");
    }

    public void setFilter(String filter){
        filterKnowledges.clear();
        if(filter.isEmpty()){
            filterKnowledges.addAll(knowledges);
        } else {
            for(String s : knowledges){
                if(s.indexOf(filter)>=0){
                    filterKnowledges.add(s);
                }
            }
        }
    }

    @Override
    public int getCount() {
        return filterKnowledges.size();
    }

    @Override
    public Object getItem(int position) {
        return filterKnowledges.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        TextView txt = (TextView) convertView;
        txt.setText(filterKnowledges.get(position));
        return convertView;
    }
}
