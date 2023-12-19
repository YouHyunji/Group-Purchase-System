package com.example.group_purchase_system;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class Major_Category extends AppCompatActivity {
    private static final String TAG = "Major_Category";     // TAG 추가

    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListView listView;
    private boolean AddPost_Click;      // 게시글 추가버튼 클릭여부

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_category);

        startToast("학과를 선택하세요.");

        // Intent에서 데이터 받아오기
        AddPost_Click = getIntent().getBooleanExtra("AddPost_Click", AddPost_Click);         // 게시글 추가 버튼 클릭

        // 리스트뷰와 어댑터 초기화
        listView = (ListView) findViewById(R.id.List);

        // 카테고리 목록 불러오기
        CategoryLoad();

    }

    // 학과 리스트를 출력하기위한 커스텀 어댑터
    public class MajorAdapter extends ArrayAdapter<StorageReference> {
        private static final String TAG = "MajorAdapter";
        private final Context context;
        private final List<StorageReference> major;       // 학과 정보(데이터)를 담고 있는 리스트


        public MajorAdapter(Context context, List<StorageReference> major) {     // 생성자, 멤버변수 초기화
            super(context, R.layout.list_item, major);
            this.context = context;
            this.major = major;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Item의 현재 위치 구하기 ( 참조 변수 )
            StorageReference MajorRef = major.get(position);

            // 각 아이템 뷰에 해당하는 XML 파일을 inflate ( XML 파일 -> 실제 뷰 객체 )
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            // 리스트 내용
            TextView Major_nameView = (TextView) convertView.findViewById(R.id.item_name);

            // 학과 이름 설정
            Major_nameView.setText(MajorRef.getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {        // 과목 리스트 아이템 클릭 이벤트

                        // 클릭한 아이템의 정보를 가져옴 : 아이템 이름
                        String selected_Major = MajorRef.getName();

                            // 선택한 항목의 정보를 Intent에 담아 Object_Category.Class를 시작
                            Intent intent = new Intent(Major_Category.this, Object_Category.class);
                            intent.putExtra("selected_Major", selected_Major);    // 학과 이름 전달
                            intent.putExtra("AddPost_Click", AddPost_Click);    // 글쓰기 버튼 선택 여부 전달

                            Log.d(TAG, "전달한 학과 이름 : " + selected_Major);
                            Log.d(TAG, "글쓰기 버튼 클릭 여부 전달 : " + AddPost_Click);

                            startActivity(intent);
                }
            });

            return convertView;
        }
    }

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // 카테고리 불러오는 메소드
    private void CategoryLoad() {
        CollectionReference CategoryRef = db.collection("Major");   //  학과 컬렉션 참조

        CategoryRef.get()  // 컬렉션 전체 데이터 가져오기
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {      // 불러오기 성공
                            List<StorageReference> Major_List = new ArrayList<>(); // StorageReference 리스트를 담을 리스트

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String major_name = (String) document.get("major_name");

                                // StorageReference를 생성하여 리스트에 추가
                                if (major_name != null) {     // null 처리
                                    Major_List.add(FirebaseStorage.getInstance().getReference().child(major_name));
                                } else {  startToast("학과 불러오기 실패" + "\n 다시 시도해 주세요.");}
                            }

                            // 어댑터를 생성하고 리스트뷰에 설정
                            MajorAdapter adapter = new MajorAdapter(Major_Category.this, Major_List);
                            listView.setAdapter(adapter);

                            // 어댑터에 데이터가 변경되었음을 알려줌
                            adapter.notifyDataSetChanged();
                            //startToast("학과 불러오기 성공");
                            Log.d(TAG,"학과 불러오기 성공");
                        } else {
                            Log.d(TAG, "학과 불러오기 실패 : Error getting documents: ", task.getException());
                            startToast("학과 불러오기 실패" + "\n 다시 시도해 주세요.");
                        }
                    }
                });
    }
}
