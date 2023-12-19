package com.example.group_purchase_system;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_purchase_system.adapters.PostAdapter;
import com.example.group_purchase_system.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class My_Post extends AppCompatActivity {
    private static final String TAG = "My_Post";     // TAG 추가

    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView mPostRecyclerView;
    private PostAdapter mAdapter;
    private List<Post> mDatas;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        // RecyclerView 초기화
        mPostRecyclerView = findViewById(R.id.my_post_recyclerview);
        mDatas = new ArrayList<>();
        mAdapter = new PostAdapter(mDatas);
        mPostRecyclerView.setAdapter(mAdapter);

        Search_MyPost();
    }

    // 나의 게시글을 찾는 메서드
    private void Search_MyPost() {
        CollectionReference PostRef = db.collection("post");

        if (user != null) {
            String userId = user.getUid();

            // 현재 사용자의 게시글만 검색
            Query query = PostRef.whereEqualTo("userId", userId);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mDatas.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String name = document.getString(Board_contents.name);
                        String title = document.getString(Board_contents.title);
                        String contents = document.getString(Board_contents.contents);
                        Post data = new Post(name, title, contents, name);
                        mDatas.add(data);
                    }

                    if (mDatas.isEmpty()) {
                        startToast("작성한 게시글이 없습니다.");
                    } else {
                        startToast(mDatas.size() + "개의 게시글이 검색되었습니다.");
                    }

                    mAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경 알리기
                } else {
                    Log.e(TAG, "검색 실패", task.getException());
                }
            });
        } else {
            // 사용자가 로그인되어 있지 않은 경우 예외 처리
            startToast("로그인이 필요합니다.");
        }
    }

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
