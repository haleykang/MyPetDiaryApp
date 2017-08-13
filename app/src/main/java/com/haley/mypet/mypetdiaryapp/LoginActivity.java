package com.haley.mypet.mypetdiaryapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// 로그인 페이지
public class LoginActivity extends AppCompatActivity {

    // 1. 변수 선언
    private static final String TAG = "LoginActivity";
    // 1) 로그인 진행상황 알려줄 다이얼로그
    // private ProgressDialog -> deprecated
    private ProgressBar progressBar;
    // 2) 서버에서 다운받은 메세지 저장 변수
    private String json;

    // 3) 로그인 성공 시 아이디 저장 변수
    public String id;
    // public String pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = (ProgressBar) findViewById(R.id.pb);


    } // end of onCreate

    // 2. 스레드 - 서버에서 데이터 다운 받는 역할
    // 모바일에서 입력한 아이디와 비밀번호를 서버에 전달 해서 결과 받아옴
    class LoginThread extends Thread {

        // 변수
        private String id;
        private String pw;

        // 생성
        LoginThread(String id, String pw) {

            this.id = id;
            this.pw = pw;
            Log.v(TAG, "LoginThread 생성자");

        }

        // run() 함수 - 서버와 연결
        @Override
        public void run() {
            Log.v(TAG, "LoginThread - run()");

            // 1) 서버 요청 주소
            String addr = "http://192.168.25.46:8080/mypet/androidlogin?";
            // 2) 파라미터로 넘길 아이디와 비밀번호 추가
            addr += "id=" + id;
            addr += "&pw=" + pw;
            Log.v(TAG, "요청 주소 : " + addr);
            // 3) Buffered Reader로 한 줄 씩 읽어온 데이터를 추가해서 저장할 StringBuilder
            StringBuilder html = new StringBuilder();
            try {
                // 4) URL 생성
                URL url = new URL(addr);
                Log.v(TAG, "URL 생성");
                // 5) 연결 요청 - openConnection()
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Log.v(TAG, "openConnection()");

                if (con != null) {
                    // 연결 시간 설정
                    con.setConnectTimeout(10000);
                    // 캐시에서 값 가져오지 않도록
                    con.setUseCaches(false);
                    Log.v(TAG, "if(con != null)");

                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.v(TAG, "연결 성공");
                        // 서버에서 전달한 메세지 읽어올 BufferedReader 객체
                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        while (true) {
                            String line = br.readLine();
                            if (line == null) break;
                            html.append(line + "\n");
                        }
                        br.close();

                        // 6) Json 변수에 html 값 저장
                        LoginActivity.this.json = html.toString();
                        Log.v(TAG, "서버 메세지 : " + json);
                        // 7) 핸들러 실행
                        handler.sendEmptyMessage(0);
                    }
                    // 8) 연결 종료
                    con.disconnect();
                    Log.v(TAG, "연결 종료");

                }


            } catch (Exception e) {
                Log.e(TAG, "연결 오류 : " + e.getMessage());
            }


        }
    } // end of Thread

    // 3. 핸들러 - 스레드에서 데이터를 다운 받은 후 다운 받은 데이터를 파싱해서 출력
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 프로그레스 바 없앰
            progressBar.setVisibility(View.INVISIBLE);

            Log.v(TAG, "핸들러 시작");
            try {
                if (json != null) {
                    // {"login":{"id":"kjj8032","pw":"1234","profile":null,"nickname":"강남이언니"}}
                    // JSON 파싱해서 저장
                    JSONObject login = new JSONObject(json);
                    JSONObject user = login.getJSONObject("result"); // result 키에 저장되어있는 값 가져오기
                    Log.v(TAG, "로그인 성공");
                    // 아이디 저장 -> intent로 넘기기
                    LoginActivity.this.id = user.getString("id");
                    Log.v(TAG, "아이디 : " + LoginActivity.this.id);

                    // ListActivity로 이동
                    Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                    intent.putExtra("id", LoginActivity.this.id);
                    startActivity(intent);
                    //startActivity(overridePendingTransitionnew Intent(LoginActivity.this, ListActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }

            } catch (Exception e) {
                Log.v(TAG, "로그인 실패");
                // 로그인 정보가 맞지 않음 -> 로그인 실패
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                // 아이디 초기화
                LoginActivity.this.id = "";
                // LoginActivity.this.pw = "";

            }
        }
    }; // end of Handler


    // 4. 로그인 버튼 클릭 처리
    public void onLoginClick(View v) {
        Log.v(TAG, "로그인 버튼 클릭");

        // 사용자가 EditText에 입력한 값 가져오기
        EditText inputId = (EditText) findViewById(R.id.id_et);
        EditText inputPw = (EditText) findViewById(R.id.pw_et);

        String tempId = inputId.getText().toString();
        String tempPw = inputPw.getText().toString();
        Log.v(TAG, "사용자가 입력한 ID : " + tempId + ", PW : " + tempPw);

        // 프로그레스 바 보이게
        progressBar.setVisibility(View.VISIBLE);

        // 스레드 시작
        LoginThread th = new LoginThread(tempId, tempPw);
        th.start();


    }


}
