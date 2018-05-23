package org.wiyi.indexablelistview.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.wiyi.indexablelistview.IndexableExpandableListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private IndexableExpandableListView listView ;
    private MyAdapter adapter ;
    private ArrayList<String> charactes ;
    private List<ArrayList<String>> data ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData() ;
    }

    private void initView() {
        listView = (IndexableExpandableListView) findViewById(R.id.indexer);
    }

    private void initData() {
        charactes = new ArrayList<>(26) ;
        data = new ArrayList<>() ;
        char c = 'A' ;
        for (int i=c;i<='Z';i++) {
            ArrayList<String> contact = new ArrayList<>(10) ;
            for (int j=0;j<10;j++) {
                contact.add(String.valueOf((char)i) + (j + 1)) ;
            }
            data.add(contact) ;
            charactes.add(String.valueOf((char)i)) ;
        }

        adapter = new MyAdapter(this) ;
        adapter.setData(charactes,data);

        listView.setIndexableAdapter(adapter);

        int len = adapter.getGroupCount() ;
        for (int i=0;i<len;i++) {
            listView.expandGroup(i) ;
        }
    }
}
