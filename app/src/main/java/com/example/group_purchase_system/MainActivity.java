package com.example.group_purchase_system;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity  {  // AppCompatActivity : 라지원 라이브러리에 포함되어있는 클래스

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView mPostRecyclerView;

    private PostAdapter mAdapter;
    private List<Post> mDatas;

    private FloatingActionButton MenuButton;        // 메뉴 선택 버튼
    private FloatingActionButton AddPost_Button;           // 게시글 추가 버튼
    private FloatingActionButton MyPost_Button;      // 내 게시글 보기 버튼
    private FloatingActionButton Search_Button;     // 검색 버튼
    private boolean isMenuOpen = false;     // 메뉴버튼 선택 여부

    private boolean AddPost_Click;     // 글쓰기 버튼 클릭여부
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

        TextView myTextView = findViewById(R.id.item_post_title);


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

        // 학과 선택 버튼
        Major_Category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPost_Click = false;      // 글쓰기 버튼 클릭 여부 전송

                // 선택한 항목의 정보를 Intent에 담아 Major_Category.Class를 시작
                Intent intent = new Intent(MainActivity.this, Major_Category.class);
                intent.putExtra("AddPost_Click", AddPost_Click);    // 학과 이름 전달

                Log.d(TAG, "글쓰기 버튼 클릭 여부 전달 : " + AddPost_Click);

                startActivity(intent);
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
                ActionButton();             // 버튼 동작 실행 메서드
            }
        });

        // 게시글 추가 버튼 이벤트 처리
        AddPost_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddPost_Click = true;      // 글쓰기 버튼 클릭 여부 전송

                // 선택한 항목의 정보를 Intent에 담아 Major_Category.Class를 시작
                Intent intent = new Intent(MainActivity.this, Major_Category.class);
                intent.putExtra("AddPost_Click", AddPost_Click);    // 학과 이름 전달

                Log.d(TAG, "글쓰기 버튼 클릭 여부 전달 : " + AddPost_Click);

                startActivity(intent);

            }
        });

        // 나의 게시글 버튼 이벤트 처리
        MyPost_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myStartActivity(My_Post.class);
            }
        });

        // 검색 버튼 이벤트 처리
        Search_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { Search_Dialog(); }
        });

        mPostRecyclerView = findViewById(R.id.main_recyclerview);
        mDatas = new ArrayList<>();

        mAdapter = new PostAdapter(mDatas);
        mPostRecyclerView.setAdapter(mAdapter);


    }
    private void myStartActivity(Class c) {    // 원하는 화면으로 이동하는 함수 (화면 이동 함수)
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    // 하단 알림창 출력하는 메서드
    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    // 게시글 리스트를 출력하는 메서드
    protected void onStart() {
        super.onStart();
        mDatas =new ArrayList<>();
        db.collection(Board_contents.post)
                .orderBy("timestamp", Query.Direction.DESCENDING) // "timestamp" 필드를 기준으로 내림차순 정렬 (최신순)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value != null) {
                            mDatas.clear();
                            for (QueryDocumentSnapshot snap : value) {
                                Map<String, Object> shot = snap.getData();
                                String name = String.valueOf(shot.get(Board_contents.name));
                                String title = String.valueOf(shot.get(Board_contents.title));
                                String contents = String.valueOf(shot.get(Board_contents.contents));
                                Post data = new Post(name, title, contents, name);
                                mDatas.add(data);
                            }
                            mAdapter = new PostAdapter(mDatas);
                            mPostRecyclerView.setAdapter(mAdapter);

                            mAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(Post post) {
                                    // 클릭된 아이템의 데이터를 가져옴
                                    String name = post.getName();

                                    // DetailActivity로 이동하는 Intent 설정
                                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                                    intent.putExtra("name", name);
                                    startActivity(intent); // DetailActivity 시작
                                }
                            });
                        }
                    }
                });
    }





    public void ActionButton() {
        Log.d(TAG, "isMenuOpen = " + isMenuOpen);
        if (isMenuOpen) {

        // 플로팅 액션 버튼 닫기 - 열려있는 플로팅 버튼 집어넣는 애니메이션

            ObjectAnimator addPostAnim = ObjectAnimator.ofFloat(AddPost_Button, "translationY", 0f);
            addPostAnim.start();
            ObjectAnimator myPostAnim = ObjectAnimator.ofFloat(MyPost_Button, "translationY", 0f);
            myPostAnim.start();
            ObjectAnimator searchAnim = ObjectAnimator.ofFloat(Search_Button, "translationY", 0f);
            searchAnim.start();

            MenuButton.setImageResource(R.drawable.baseline_menu_24);


        } else {        // 플로팅 액션 버튼 열기 - 닫혀있는 플로팅 버튼 꺼내는 애니메이션

            ObjectAnimator addPostAnim = ObjectAnimator.ofFloat(AddPost_Button, "translationY",  -160f);
            addPostAnim.start();
            ObjectAnimator myPostAnim = ObjectAnimator.ofFloat(MyPost_Button, "translationY",  -320f);
            myPostAnim.start();
            ObjectAnimator searchAnim = ObjectAnimator.ofFloat(Search_Button, "translationY",  -480f);
            searchAnim.start();

            // 메뉴 버튼 아이콘 변경
            MenuButton.setImageResource(R.drawable.baseline_close_24);
        }

        // 플로팅 버튼 상태 변경
            isMenuOpen = !isMenuOpen;
    }

    // 검색어를 입력받는 Dialog (대화상자)
    private void Search_Dialog() {

        startToast("게시글의 제목을 입력해주세요!");

        // Dialog Builder 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // Dialog 레이아웃 설정
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_search, null);
        builder.setView(view);


        EditText Search_EditText = view.findViewById(R.id.Search_name);         // 검색어 입력창
        Button OKButton = view.findViewById(R.id.Search_Ok_Button);            // 검색 버튼
        Button BackButton = view.findViewById(R.id.Search_Back_Button);        // 돌아가기 버튼

        // Dialog 생성
        AlertDialog alertDialog = builder.create();     // 객체 생성
        alertDialog.show();         // 사용자에게 보여주기

        // '검색' 버튼 클릭 이벤트
        OKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String query = Search_EditText.getText().toString();               // 입력받은 검색어를 저장

                if(TextUtils.isEmpty(query)) {              // 입력된 검색어가 없을 때
                    startToast("검색어를 입력해주세요!");
                } else {

                    // 선택한 항목의 정보를 Intent에 담아 Search_Result.Class (검색결과창)를 시작
                    Intent intent = new Intent(MainActivity.this, Search_Result.class);
                    intent.putExtra("query", query);    // 검색어 전달

                    Log.d(TAG, "전달한 검색어 : " + query);

                    startActivity(intent);
                }
            }
        });

        // 돌아가기 버튼 클릭 : 이전 화면으로 되돌아가기
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();      // Dialog창 닫기
            }
        });

    }

}
