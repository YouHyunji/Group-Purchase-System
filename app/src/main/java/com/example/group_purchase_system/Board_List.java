package com.example.group_purchase_system;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_purchase_system.R;
import com.example.group_purchase_system.adapters.PostAdapter;
import com.example.group_purchase_system.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class Board_List extends AppCompatActivity {
    private static final String TAG = "Board_List";     // TAG 추가

    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FloatingActionButton MenuButton;        // 메뉴 선택 버튼
    private FloatingActionButton AddPost_Button;           // 게시글 추가 버튼
    private FloatingActionButton MyPost_Button;      // 내 게시글 보기 버튼
    private FloatingActionButton Search_Button;     // 검색 버튼
    private boolean isMenuOpen = false;     // 메뉴버튼 선택 여부

    private RecyclerView mPostRecyclerView;
    private PostAdapter mAdapter;
    private List<Post> mDatas;
    private String selected_Major;  // 받아온 과목
    private String selected_Object;  // 받아온 물품
    private boolean AddPost_Click;      // 게시글 추가버튼 클릭여부

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_post);

        // Intent에서 데이터 받아오기

        selected_Major = getIntent().getStringExtra("selected_Major");  // 학과 정보
        selected_Object = getIntent().getStringExtra("selected_Object");  // 물품 정보
        AddPost_Click = getIntent().getBooleanExtra("AddPost_Click", AddPost_Click);         // 게시글 추가 버튼 클릭

        // RecyclerView 초기화
        mPostRecyclerView = findViewById(R.id.search_recyclerview);
        mDatas = new ArrayList<>();
        mAdapter = new PostAdapter(mDatas);
        mPostRecyclerView.setAdapter(mAdapter);


        // inflate된 레이아웃에서 버튼 찾아 초기화
        AddPost_Button = findViewById(R.id.AddPost_Button);         // 게시글 추가 버튼
        MyPost_Button = findViewById(R.id.MyPost_Button);           // 나의 게시글 보기 버튼
        Search_Button = findViewById(R.id.Search_Button);           // 검색 버튼
        MenuButton = findViewById(R.id.MenuButton);            // 메뉴 선택 버튼

        TextView Major_name = findViewById(R.id.Major_ShowText);       // 상단바 학과명
        TextView Object_name = findViewById(R.id.Object_ShowText);       // 상단바 물품명

        // 상단바 이름 출력
        Major_name.setText(selected_Major);
        Object_name.setText(selected_Object);

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
                Intent intent = new Intent(Board_List.this, Major_Category.class);
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
                startToast("나의 게시글 보기로 이동");
            }
        });

        // 검색 버튼 이벤트 처리
        Search_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { Search_Dialog(); }
        });
    }


    // 검색어를 입력받는 Dialog (대화상자)
    private void Search_Dialog() {

        startToast("게시글의 제목을 입력해주세요!");

        // Dialog Builder 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(Board_List.this);

        // Dialog 레이아웃 설정
        View view = LayoutInflater.from(Board_List.this).inflate(R.layout.dialog_search, null);
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
                    Intent intent = new Intent(Board_List.this, Search_Result.class);
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

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

    private void myStartActivity(Class c) {    // 원하는 화면으로 이동하는 함수 (화면 이동 함수)
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }
}
