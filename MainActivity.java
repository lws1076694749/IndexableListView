package com.lenovo.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private ArrayList<String> mItems;
    private IndexableListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mItems = new ArrayList<String>();
        mItems.add("12345");
        mItems.add("A Diary of Wimpy");
        mItems.add("B is a good boy");
        mItems.add("C The Inspiring Words");
        mItems.add("D Stay hungry, stay foolish.");
        mItems.add("E Innovation distinguishes between a leader and a follower.");
        mItems.add("F Your time is limited, so don't waste it living someone else's life.");
        mItems.add("G Design is how it works.");
        mItems.add("H We're here to put a dent in the universe.");
        mItems.add("I We're here to put a dent in the universe.");
        mItems.add("J The only way to do great work is to love what you do. ");
        mItems.add("K Being the richest man in the cemetery doesn't matter to me");
        mItems.add("L I want to put a ding in the universe.");
        mItems.add("M Quality is more important than quantity.");
        mItems.add("N One home run is better than two doubles.");
        mItems.add("O Going to bed at night saying we've done something wonderful.");
        mItems.add("P Otherwise why else even be here?");

        Collections.sort(mItems);
        ContentAdapter adapter = new ContentAdapter(this, android.R.layout.simple_list_item_1, mItems);
        listView= (IndexableListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
    }

    private class ContentAdapter extends ArrayAdapter<String> implements SectionIndexer {
        private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public ContentAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public Object[] getSections() {
            String[] sections = new String[mSections.length()];
            for (int i = 0; i < mSections.length(); i++) {
                sections[i] = String.valueOf(mSections.charAt(i));
            }
            return sections;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            for (int i = sectionIndex; i >= 0; i--) {
                for (int j = 0; j < getCount(); j++) {
                    if (i == 0) {
                        for (int k = 0; k <= 9; k++) {
                            if (StringMatcher.match(String.valueOf(getItem(j).charAt(0)), String.valueOf(mSections.charAt(k)))) {
                                return j;
                            }
                        }
                    } else {
                        if (StringMatcher.match(String.valueOf(getItem(j).charAt(0)), String.valueOf(mSections.charAt(i)))) {
                            return j;
                        }
                    }
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }
    }
}
