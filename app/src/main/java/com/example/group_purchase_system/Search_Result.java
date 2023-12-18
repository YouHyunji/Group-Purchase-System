package com.example.group_purchase_system;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_purchase_system.R;
import com.example.group_purchase_system.adapters.PostAdapter;
import com.example.group_purchase_system.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Search_Result extends AppCompatActivity {
    private static final String TAG = "Search_Result";     // TAG 추가

    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView mPostRecyclerView;
    private PostAdapter mAdapter;
    private List<Post> mDatas;
    private String Query_title;  // 받아온 검색어

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // Intent에서 데이터 받아오기
        Query_title = getIntent().getStringExtra("query");  // 검색어

        // RecyclerView 초기화
        mPostRecyclerView = findViewById(R.id.search_result_view);
        mDatas = new ArrayList<>();
        mAdapter = new PostAdapter(mDatas);
        mPostRecyclerView.setAdapter(mAdapter);

        Search_Post(Query_title);
    }

    // 사용자가 검색한 게시글을 찾는 메서드
    private void Search_Post(String query_title) {
        CollectionReference PostRef = db.collection("post");

        // 'title' 필드 (제목)에 검색어가 포함된 게시글 검색
        Query query = PostRef.whereEqualTo("title", query_title);

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
                    startToast("해당하는 게시글이 없습니다.");
                } else {
                    startToast(mDatas.size() + "개의 게시글이 검색되었습니다.");
                }

                mAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경 알리기
            } else {
                Log.e(TAG, "검색 실패", task.getException());
            }
        });
    }

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}