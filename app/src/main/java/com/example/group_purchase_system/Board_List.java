package com.example.group_purchase_system;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
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


public class Board_List extends AppCompatActivity {
    private static final String TAG = "Board_Category";     // TAG 추가

    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListView listView;
    private String selected_Major;      // 학과
    private String selected_Object;     // 물품

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_category);

        // Intent에서 데이터 받아오기
        selected_Major = getIntent().getStringExtra("selected_Major");  // 학과 정보
        selected_Object = getIntent().getStringExtra("selected_Object");  // 물품 정보

        // 리스트뷰와 어댑터 초기화
        listView = (ListView) findViewById(R.id.List);

    }

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // 카테고리 불러오는 메소드 ( 수정 필요 )
    private void CategoryLoad() {
        CollectionReference CategoryRef = db.collection("Major")   // Major 컬렉션 접근
                .document(selected_Major)           // 이전에 선택한 학과 정보
                .collection("Object");

        CategoryRef.get()  // 컬렉션 전체 데이터 가져오기
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {      // 불러오기 성공
                            List<StorageReference> Object_List = new ArrayList<>(); // StorageReference 리스트를 담을 리스트

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String object_name = (String) document.get("object");

                                // StorageReference를 생성하여 리스트에 추가
                                if (object_name != null) {     // null 처리
                                    Object_List.add(FirebaseStorage.getInstance().getReference().child(object_name));
                                } else {  startToast("물품 목록 불러오기 실패" + "\n다시 시도해 주세요.");}
                            }

                            // 어댑터를 생성하고 리스트뷰에 설정
                            Object_Category.ObjectAdapter adapter = new Object_Category.ObjectAdapter(Object_Category.this, Object_List);
                            listView.setAdapter(adapter);

                            // 어댑터에 데이터가 변경되었음을 알려줌
                            adapter.notifyDataSetChanged();
                            //startToast("물품 목록 불러오기 성공");
                            Log.d(TAG,"물품 목록 불러오기 성공");
                        } else {
                            Log.d(TAG, "물품 목록 불러오기 실패 : Error getting documents: ", task.getException());
                            startToast("물품 목록 불러오기 실패" + "\n 다시 시도해 주세요.");
                        }
                    }
                });
    }
}