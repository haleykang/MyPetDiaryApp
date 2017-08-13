package com.haley.mypet.mypetdiaryapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.haley.mypet.mypetdiaryapp.domain.Diary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// 일기 모아보기
public class ListActivity extends AppCompatActivity {

    private static final String TAG = "ListActivity";
    // 1. 변수 선언
    // 1) 서버에서 가져온 데이터를 저장할 List
    private List<Diary> list;
    // 2) 리스트 뷰
    private ListView listView;
    //    // 3) ArrayAdapter
//    private ArrayAdapter<String> adapter;
//    // 4) 서버에서 가져온 데이터 중 화면에 출력할 값만 저장하는 ArrayList
//    private List<String> arrayList = new ArrayList<>();
    // 5) 진행 상황 출력 Progress Bar
    private ProgressBar pb;
    // 6) 서버에서 다운 받은 문자열을 저장할 변수
    private String json;
    // 7) 로그인 액티비티에서 가져온 id 저장할 변수
    private String id;
    // 8) 리스트 없을 때 출력할 텍스트 뷰
    private TextView nolist;


    // 커스텀 어댑터 사용을 위해
    // private Diary diary;
    private ArrayList<Diary> mArrayList = new ArrayList<Diary>();
    private CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // 아이디 가져와서 저장
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        Log.v(TAG, "아이디 : " + id);

        // 아이디 가져오기
        pb = (ProgressBar) findViewById(R.id.progress);
        nolist = (TextView) findViewById(R.id.nolist_tv);
        listView = (ListView) findViewById(R.id.list_view);

        // 리스트 객체 생성
        list = new ArrayList<>();

        // 어댑터 생성
        customAdapter = new CustomAdapter(mArrayList, ListActivity.this);
        listView.setAdapter(customAdapter);

        // 리스트 뷰 설정 추가
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // listView.setDivider(new ColorDrawable(Color.RED));
        listView.setDividerHeight(2);

        // 리스트 뷰에서 항목을 클릭 했을 때 상세보기 페이지로 이동
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // list에서 선택한 인덱스의 데이터를 저장
                Diary diary = list.get(i);
                // 저장된 값을 인텐트에 저장 후 상세보기 화면으로 이동
                // -> 이 떄 Diary 클래스는 Serialzible 되어 있어야 함
                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                intent.putExtra("diary", diary);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


            }
        });

        // 스레드 실행
        ListThread th = new ListThread();
        th.start();


    }


    // 2. 스레드
    class ListThread extends Thread {
        @Override
        public void run() {
            // 1) 요청 주소
            String addr = "http://192.168.25.46:8080/mypet/androidlist?";
            addr += "id=" + id;
            Log.v(TAG, "요청주소 : " + addr);

            // 2) StringBuilder
            StringBuilder html = new StringBuilder();

            try {
                // 3) URL
                URL url = new URL(addr);

                // 4) HttlURLConnection 연결 요청
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Log.v(TAG, "openConnection()");
                if (con != null) {
                    // 연결 시간
                    con.setConnectTimeout(10000);
                    // 캐시에서 안가져오도록
                    con.setUseCaches(false);

                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.v(TAG, "연결 성공");
                        // (1) BufferedReader로 데이터 읽어오기
                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        while (true) {
                            String line = br.readLine();
                            if (line == null) break;
                            html.append(line + "\n");
                        }
                        // (2) br close()
                        br.close();
                        // (3) 가져온 값 json 변수에 저장
                        json = html.toString();
                        // (4) 핸들러 실행
                        handler.sendEmptyMessage(0);
                    }
                    // (5) 연결 끊기
                    con.disconnect();
                }

            } catch (Exception e) {
                Log.e(TAG, "연결 오류 : ", e);
            }

        }
    } // end of Thread


    // 3. 핸들러 - 서버에서 다운 받은 데이터 파싱해서 출력
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 프로그레스바 중지
            pb.setVisibility(View.GONE);
            Log.v(TAG, "핸들러 시작");
            try {
                if (json != null) {
                    Log.v(TAG, "json != null");

                    /*
                     * {"result":[{"no":108,"title":"안녕하세요","content":"sdf","image":
		             * "default_image.jpg","readcnt":16,"regdate":"2017-08-10","id":"kjj8032",
		             * "ckshare":"true"}]}
		             */
                    // (1) JSON 데이터 파싱
                    JSONObject data = new JSONObject(json);
                    JSONArray result = data.getJSONArray("result"); // result 키에 저장된 array가져오기
                    // (2) result에 저장된 각각의 값 저장
                    for (int i = 0; i < result.length(); ++i) {
                        // result 배열에서 i위치에 있는 값 저장
                        JSONObject temp = result.getJSONObject(i);
                        // 가져온 데이터를 Diary에 저장
                        Diary diary = new Diary();
                        diary.setNo(temp.getInt("no"));
                        diary.setTitle(temp.getString("title"));
                        diary.setContent(temp.getString("content"));
                        diary.setImage(temp.getString("image"));
                        diary.setReadcnt(temp.getInt("readcnt"));
                        diary.setRegdate(temp.getString("regdate"));
                        Log.v(TAG, "regDate : " + temp.getString("regdate"));
                        diary.setId(temp.getString("id"));
                        diary.setCkshare(temp.getString("ckshare"));


                        // 리스트에 추가
                        list.add(diary);
                        // 목록보기 화면에 출력
                        mArrayList.add(diary);

                    }
                    // 어댑터에 변화 내용 있음을 알림
                    customAdapter.notifyDataSetChanged();

                }

            } catch (Exception e) {
                // 예외 상황 - 입력된 게시글이 없을 때
                nolist.setVisibility(View.VISIBLE);
                Log.v(TAG, "핸들러 예외", e);
            }


        }
    }; // end of Handle

    ///// 커스텀 리스트 뷰

    private class CustomAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<Diary> arrayList;
        private Context context;

        // 1. 생성자
        public CustomAdapter(ArrayList<Diary> arrayList, Context context) {
            this.inflater = LayoutInflater.from(context);
            this.arrayList = arrayList;
            this.context = context;
            Log.v(TAG, "CustomAdapter 생성자");
        }

        // 2. ArrayList get set
//        public ArrayList<Diary> getArrayList() {
//            return arrayList;
//        }
//
//        public void setArrayList(ArrayList<Diary> arrayList) {
//            this.arrayList = arrayList;
//        }


        // 리스트에서 보여줄 데이터의 갯수
        @Override
        public int getCount() {
            return arrayList.size();
        }

        // 리스트에서 i 위치에 있는 값 리턴
        @Override
        public Object getItem(int i) {
            return arrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            // 리스트 아이템이 새로 추가될 경우 view의 값은 null
            if (view == null) {
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.list_item, null);
                // 아이템 리스트뷰의 고유주소 저장
                holder.titleTv = (TextView) view.findViewById(R.id.title_tv);
                holder.dateTv = (TextView) view.findViewById(R.id.regdate_tv);


                // 뷰에 holder 저장
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            // ArrayList 포지션에 맞는 데이터 가져오기
            Diary d = arrayList.get(i);

            // 리스트 아이템에 테이블에 입력된 값 출력
            holder.titleTv.setText(d.getTitle());
            holder.dateTv.setText("작성일 : " + d.getRegdate());

            return view;
        }
    }

    private class ViewHolder {

        TextView titleTv = null;
        TextView dateTv = null;

    }


}
