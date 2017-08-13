package com.haley.mypet.mypetdiaryapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.haley.mypet.mypetdiaryapp.domain.Diary;

import java.io.InputStream;
import java.net.URL;

// 상세보기
public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private ImageView mImage;
    private TextView mTitle;
    private TextView mContent;
    private TextView mReadcnt;
    private TextView mRegdate;

    private Diary mDiary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1) 이전 액티비티에서 보낸 데이터 가져오기
        Intent intent = getIntent();
        // 2) mDiary에 가져온 데이터 저장
        mDiary = (Diary) intent.getSerializableExtra("diary");
        // 3) xml 고유 주소 저장
        mImage = (ImageView) findViewById(R.id.image_view);
        mTitle = (TextView) findViewById(R.id.title_tv);
        mContent = (TextView) findViewById(R.id.content_tv);
        mReadcnt = (TextView) findViewById(R.id.readcnt_tv);
        mRegdate = (TextView) findViewById(R.id.date_tv);
        // 4) xml에 출력
        mTitle.setText(mDiary.getTitle());
        mContent.setText(mDiary.getContent());
        mReadcnt.setText(String.valueOf(mDiary.getReadcnt()));
        mRegdate.setText(mDiary.getRegdate());
        // 5) 스레드 시작
        DetailThread th = new DetailThread();
        th.start();

    }

    // 1. 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 스레드에서 보낸 이미지를 비트 형태로 받아와서 저장
            Bitmap bit = (Bitmap) msg.obj;
            Log.v(TAG, "핸들러 : " + bit.toString());

            if (bit == null) {
                Toast.makeText(DetailActivity.this, "이미지를 가져오지 못했습니다 ", Toast.LENGTH_SHORT).show();

            } else {
                mImage.setImageBitmap(bit);
            }
        }
    };

    // 2. 스레드 - 서버와 연결해서 특정 no의 이미지를 다운
    class DetailThread extends Thread {
        @Override
        public void run() {
            try {

                // 1) 업로드된 이미지를 저장하는 폴더의 url 저장 (Diary 객체에 저장된 image 주소 가져옴)
                String addr = "http://192.168.25.46:8080/mypet/diaryimage/" + mDiary.getImage();
                Log.v(TAG,"addr : " + addr);


                // 2) OpenStrean
                InputStream is = new URL(addr).openStream();

                // 3) 비트맵 파일로 가져옴
                Bitmap bit = BitmapFactory.decodeStream(is);
                Log.v(TAG, "가져온 이미지 : " + bit.toString());
                is.close();

                // 4) 핸들러에 비트맵 전달
                Message msg = handler.obtainMessage();
                msg.obj = bit;


                // 5) 핸들러 실행
                handler.sendMessage(msg);


            } catch (Exception e) {
                Log.v(TAG, "연결 오류 : ", e);
            }

        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
