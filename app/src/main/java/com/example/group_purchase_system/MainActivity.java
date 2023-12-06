package com.example.group_purchase_system;

import static com.google.common.collect.ComparisonChain.start;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.group_purchase_system.adapters.PostAdapter;
import com.example.group_purchase_system.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {  // AppCompatActivity : 라지원 라이브러리에 포함되어있는 클래스

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView mPostRecyclerView;

    private PostAdapter mAdapter;
    private List<Post> mDatas;

    private FloatingActionButton MenuButton;        // 메뉴 선택 버튼
    private FloatingActionButton Menu_XButton;      // 메뉴 선택 취소 버튼
    private FloatingActionButton AddPost_Button;           // 게시글 추가 버튼
    private FloatingActionButton MyPost_Button;      // 내 게시글 보기 버튼
    private FloatingActionButton Search_Button;     // 검색 버튼
    private boolean isMenuOpen;     // 메뉴버튼 선택 여부
    private static final String TAG = "MainActivity";     // TAG 추가
    private FirebaseAuth mAuth;

    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);     // 보여지는 화면

        // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {   // 로그인이 안되어있을 경우 (유저가 없을 경우)
            myStartActivity(LoginActivity.class);      // 로그인 창으로 이동
        } else {      // 로그인이 되어있을경우
            FirebaseFirestore db = FirebaseFirestore.getInstance();     // 데이터베이스 초기화

            DocumentReference docRef = db.collection("users").document(user.getUid());  // 사용자 고유식별자 구해서 불러오기
            //DocumentReference docRef = db.collection("user").document(name)

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

        // inflate된 레이아웃에서 버튼 찾아 초기화
       Button logoutButton = findViewById(R.id.logoutButton);       // 로그아웃 버튼
        Button Major_Category = findViewById(R.id.Major_Category);  // 학과 카테고리 버튼
        AddPost_Button = findViewById(R.id.AddPost_Button);         // 게시글 추가 버튼
        MyPost_Button = findViewById(R.id.MyPost_Button);           // 나의 게시글 보기 버튼
        Search_Button = findViewById(R.id.Search_Button);           // 검색 버튼
        MenuButton = findViewById(R.id.MenuButton);            // 메뉴 선택 버튼
        Menu_XButton = findViewById(R.id.Menu_XButton);        // 메뉴 선택 취소 버튼


        MenuCancleClick();    // 메뉴 선택하기 버튼 생성

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

        // 메뉴 선택하기 버튼 이벤트 처리 : 버튼 클릭했을 때 게시글 추가 & 내 게시글 보기 & 검색 버튼 나옴
        MenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMenuOpen =  false;    // 메뉴가 닫혀있는 상태
                //MenuClick();
                ActionButton(isMenuOpen);
            }
        });

        // 메뉴 선택 취소 버튼 이벤트 처리 : 버튼 클릭했을 때 게시글 추가 & 내 게시글 보기 & 검색 버튼 사라짐
        Menu_XButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMenuOpen =  true;    // 메뉴가 열려있는 상태
                //MenuCancleClick();
                ActionButton(isMenuOpen);
            }
        });

        mPostRecyclerView = findViewById(R.id.main_recyclerview);
        mDatas = new ArrayList<>();

        mAdapter = new PostAdapter(mDatas);
        mPostRecyclerView.setAdapter(mAdapter);

        // 게시글 추가
        //findViewById(R.id.main_post_edit).setOnClickListener(this);


    }
    private void myStartActivity(Class c) {    // 원하는 화면으로 이동하는 함수 (화면 이동 함수)
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    // 게시글 리스트를 출력하는 메서드
    protected void onStart() {
        super.onStart();
        mDatas =new ArrayList<>();
        db.collection(Board_contents.post)
                .orderBy(Board_contents.timestamp, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value != null) {
                            mDatas.clear();
                            for (QueryDocumentSnapshot snap : value) {
                                Map<String, Object> shot = snap.getData();
                                String name = String.valueOf(shot.get(MemberInfo.name));
                                String title = String.valueOf(shot.get(Board_contents.title));
                                String contents = String.valueOf(shot.get(Board_contents.contents));
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

    // 메뉴 선택하기 버튼 : 나머지 다른 옵션(게시글 추가, 내 게시글 보기) 버튼들이 나오도록 함.
    private void MenuClick() {
        MenuButton.setVisibility(View.GONE);
        Menu_XButton.setVisibility(View.VISIBLE);
        AddPost_Button.setVisibility(View.VISIBLE);
        MyPost_Button.setVisibility(View.VISIBLE);
        Search_Button.setVisibility(View.VISIBLE);
    }

    // 메뉴 선택 취소 버튼 : 다른 옵션이 사라지고 '메뉴 선택' 버튼만 나타남.
    private void MenuCancleClick() {
        MenuButton.setVisibility(View.VISIBLE);
        Menu_XButton.setVisibility(View.GONE);
        AddPost_Button.setVisibility(View.GONE);
        MyPost_Button.setVisibility(View.GONE);
        Search_Button.setVisibility(View.GONE);
    }

    private void ActionButton(boolean isMenuOpen) {
        // 플로팅 액션 버튼 닫기 - 열려있는 플로팅 버튼 집어넣는 애니메이션
        if (isMenuOpen) {
            MenuButton.setVisibility(View.VISIBLE);     // 메뉴 선택하기 버튼
            ObjectAnimator.ofFloat(AddPost_Button, "translationY", 0f).start();
            ObjectAnimator.ofFloat(MyPost_Button, "translationY", 0f).start();
            ObjectAnimator.ofFloat(Search_Button, "translationY", 0f).start();
        } else { // 플로팅 액션 버튼 열기 - 닫혀있는 플로팅 버튼 꺼내는 애니메이션

            Menu_XButton.setVisibility(View.VISIBLE);       // 메뉴 취소하기 버튼
            ObjectAnimator.ofFloat(AddPost_Button, "translationY", -360f).start();
            ObjectAnimator.ofFloat(MyPost_Button, "translationY", -180f).start();
            ObjectAnimator.ofFloat(Search_Button, "translationY", 0f, -90f).start();
        }
    }
}