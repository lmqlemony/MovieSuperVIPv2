package com.lmqlemony.moviesupervip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private String movie, word,tops;
    private Elements elements;
    private ListView listView;
    private EditText editText;
    private Button button;
    private SharedPreferences sharedPreferences;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x0829) {
                ArrayList<String> data = new ArrayList<>();
                for(Element e :elements){
                    tops = e.select("img").attr("alt");
                    data.add(tops);
                }
                listView.setAdapter(new ArrayAdapter<String>(MainActivity.this,R.layout.item,data));
            } else if (msg.what == 0x0920) {
                Uri uri = Uri.parse("http://y.mt2t.com/lines?url=" + movie);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }else if(msg.what == 0x0000){
                Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        listView = findViewById(R.id.listView);
        sharedPreferences = getSharedPreferences("lmqwords", 0);
        editText.setText(sharedPreferences.getString("words", ""));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document docs = Jsoup.connect("http://list.iqiyi.com/www/1/-------------4-1-1-iqiyi--.html").get();
                    elements = docs.select("div.site-piclist_pic");
                } catch (IOException e) {
                    handler.obtainMessage(0x0000, "联网失败，[最新上映]加载不出").sendToTarget();
                    e.printStackTrace();
                }
                handler.obtainMessage(0x0829, elements).sendToTarget();
            }
        }).start();
//注释+
        //new row
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.requestFocus();
                word = editText.getText().toString().trim();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Document doc = Jsoup.connect("http://so.iqiyi.com/so/q_" + word + "?source=input&refersource=lib").get();
                            movie = doc.select("a.info_play_btn").attr("href");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        handler.obtainMessage(0x0920, movie).sendToTarget();
                    }
                }).start();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("words", editText.getText().toString());
                editor.commit();
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editText.selectAll();
                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editText.setText(((TextView)view).getText().toString());//括住强转！！！！
                listView.requestFocus();
            }
        });
    }
}
