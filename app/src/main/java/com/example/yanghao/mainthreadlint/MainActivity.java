package com.example.yanghao.mainthreadlint;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
public class MainActivity extends AppCompatActivity {

    Button but;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        Log.d("tag","hello,world");
        but=(Button)findViewById(R.id.Button01);

        but.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String[] contentResult = new String[1];
                contentResult[0] =loadContentFromSDCard("歌词.txt");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            File f=new File("/sdcard/ebook/"+"歌词.txt");
                            int length=(int)f.length();
                            byte[] buff=new byte[length];
                            FileInputStream fis=new FileInputStream(f);
                            fis.read(buff);
                            fis.close();
                        }catch(Exception e){

                        }
                    }
                }).start();

                EditText etContent=(EditText)findViewById(R.id.EditText01);
                etContent.setText(contentResult[0]);

            }
        });
    }

    public String loadContentFromSDCard(String fileName){

        String content=null;
        try{
            File f=new File("/sdcard/ebook/"+fileName);
            int length=(int)f.length();
            byte[] buff=new byte[length];
            FileInputStream fis=new FileInputStream(f);
            fis.read(buff);
            fis.close();
            content=new String(buff,"UTF-8");
        }catch(Exception e){
        }
        return content;
    }

    class MyThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                File f=new File("/sdcard/ebook/"+"歌词.txt");
                int length=(int)f.length();
                byte[] buff=new byte[length];
                FileInputStream fis=new FileInputStream(f);
                fis.read(buff);
                fis.close();
            }catch(Exception e){

            }
        }
    }

}
