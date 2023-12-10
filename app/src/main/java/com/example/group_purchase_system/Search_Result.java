package com.example.group_purchase_system;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group_purchase_system.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Search_Result extends AppCompatActivity {
    private static final String TAG = "Search_Result";     // TAG 추가

    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String Query;  // 받아온 검색어

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_category);

        // Intent에서 데이터 받아오기
        Query = getIntent().getStringExtra("query");  // 검색어


    }

    // 사용자가 검색한 게시글을 찾는 메서드
    private void Search_Post(String query_title) {


        CollectionReference PostRef = db.collection("post");        // 'post' 컬렉션 접근
        // DocumentReference userDocRef = userRef.document(user.getUid());

        Query query = PostRef.whereEqualTo("title", query_title);     // 검색한 내용과 일치하는 문서 찾기

        query.get().addOnCompleteListener(task -> {
            if (task.getResult().isEmpty()) {           // 쿼리 결과가 없으면
                startToast("해당하는 게시글이 없습니다.");
                Log.d(TAG, "Search_Post 메서드 : 게시글이 존재하지않음");
            } else {
                // 쿼리 결과가 있는 경우
                Log.d(TAG, "Search_Post 메서드 : 게시글 찾음");

                // 쿼리 결과 출력하기 ( 수정 중 )
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String Id = document.getId();    // 아이디 구하기

                            }
                        }
                    }
                });
            }
        });
    }

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}