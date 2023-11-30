package com.example.group_purchase_system;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group_purchase_system.adapters.PostAdapter;
import com.example.group_purchase_system.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {  // AppCompatActivity : 라지원 라이브러리에 포함되어있는 클래스

    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private RecyclerView mPostRecyclerView;

    private PostAdapter mAdapter;
    private List<Post> mDatas;

    private static final String TAG = "MainActivity";     // TAG 추가
    private FirebaseAuth mAuth;     // FirebaseAuth 인스턴스 선언
    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //  FirebaseAuth.getInstance() : 파이어 베이스의 인증 인스턴스 가저온다.이를 통해 파이어 베이스 인증시스템을 사용할 수 있다.
    // getCurrentUser() : 현재 로그인된 사용자를 가져옴 없으면 NULL
    @SuppressLint("MissingInflatedId")
    //Lint 경고를 무시하는 거임  MissingInflatedId는 아이디가 없을때의 경고이다.
    @Override // 재정의
    protected void onCreate(Bundle savedInstanceState) { //Bundle savedInstanceState : 엑티비티가 이전에 종료되었을때 다시가져올수있는 매개변수
        super.onCreate(savedInstanceState);         // AppCompatActivity 를 상속하고있다.
        // savedInstanceState : 이전 상태를 저장하고 있는 Bundle 객체 이전상태를 복구할때 사용
        setContentView(R.layout.activity_main);     // 보여지는 화면

        // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {   // 로그인이 안되어있을 경우 (유저가 없을 경우)
            myStartActivity(LoginActivity.class);      // 로그인 창으로 이동
        } else {      // 로그인이 되어있을경우
            FirebaseFirestore db = FirebaseFirestore.getInstance();     // 데이터베이스 초기화
            //FirebaseFirestore.getInstance() : 데이터베이스를 초기화한다.
            DocumentReference docRef = db.collection("users").document(user.getUid());  // 사용자 고유식별자 구해서 불러오기
            //DocumentReference docRef = db.collection("user").document(name)
            //특정문서를 참조하는 DocumentReference를 가져오는 부분
            //db는 앞서 생성한 Firestore 데이터베이스의 인스턴스를 가리킵니다.
            //collection("users")는 "users"라는 컬렉션(Collection)에 접근하는데, 컬렉션은 문서들의 그룹을 나타낸다.
            //document(user.getUid())는 해당 컬렉션 내에서 user.getUid()로 반환된 고유 식별자(UID)를 가진 특정 문서를 가리킵니다
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                //Firebase Firestore에서 문서를 가져오고 완료 리스너를 추가하는 부분입니다.
                //docRef.get(): docRef로 정의된 DocumentReference를 통해 해당 문서의 데이터를 가져오는 메서드입니다. 이 메서드를 호출하여 해당 문서의 데이터를 가져옵니다
                //addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { ... }): Firebase Firestore에서 데이터를 가져오는 작업이 완료되었을 때 처리해야 할 작업을 정의하는 부분입니다.
                //OnCompleteListener는 작업이 성공적으로 완료되었을 때 호출되는 콜백을 제공합니다.
                //DocumentSnapshot은 가져온 문서의 스냅샷을 나타냅니다. 이를 통해 해당 문서의 데이터에 접근할 수 있습니다.
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        //String name = (String) document.getData().get("name");
                        if (document.exists()) {        // 사용자에 대한 정보가 존재하면
                            String name = (String) document.getData().get("name");
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData().get("name"));
                            startToast(name + "님 환영합니다.");
                        } else {
                            Log.d(TAG, "No such document");
                            startToast("회원정보를 등록해주세요");
                            //myStartActivity(MemberinitActivity.class);      // 회원정보 등록 페이지(마이페이지)로 이동 ( 예정 )
                        }

                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }

        Button Major_Category = findViewById(R.id.Major_Category);  // 학과 카테고리 버튼
       Button logoutButton = findViewById(R.id.logoutButton);  // 로그아웃 버튼

        // 학과 선택 버튼
        Major_Category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myStartActivity(Major_Category.class);   // 학과 카테고리 이동
            }
        });


        // 로그아웃
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();   // 로그아웃하기
                myStartActivity(LoginActivity.class);   // 로그인 화면 이동
            }
        });


        mPostRecyclerView = findViewById(R.id.main_recyclerview);
        mDatas = new ArrayList<>();

        mAdapter = new PostAdapter(mDatas);
        mPostRecyclerView.setAdapter(mAdapter);

        findViewById(R.id.main_post_edit).setOnClickListener(this);


    }
    private void myStartActivity(Class c) {    // 원하는 화면으로 이동하는 함수 (화면 이동 함수)
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    protected void onStart() {
        super.onStart();
        mDatas =new ArrayList<>();
        mStore.collection(FirebaseID.post)
                .orderBy(FirebaseID.timestamp, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value != null) {
                            mDatas.clear();
                            for (QueryDocumentSnapshot snap : value) {
                                Map<String, Object> shot = snap.getData();
                                String name = String.valueOf(shot.get(MemberInfo.name));
                                String title = String.valueOf(shot.get(FirebaseID.title));
                                String contents = String.valueOf(shot.get(FirebaseID.contents));
                                Post data = new Post(name, title, contents);
                                mDatas.add(data);
                            }
                            mAdapter = new PostAdapter(mDatas);
                            mPostRecyclerView.setAdapter(mAdapter);
                        }
                    }
                });
    }
    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, PostActivity.class));
    }
}