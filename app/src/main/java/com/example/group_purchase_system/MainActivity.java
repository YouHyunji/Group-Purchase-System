package com.example.group_purchase_system;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";     // TAG 추가
    private FirebaseAuth mAuth;     // FirebaseAuth 인스턴스 선언
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

       Button logoutButton = findViewById(R.id.logoutButton);  // 로그아웃 버튼

        // 로그아웃
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();   // 로그아웃하기
                myStartActivity(LoginActivity.class);   // 로그인 화면 이동
            }
        });
    }
    private void myStartActivity(Class c) {    // 원하는 화면으로 이동하는 함수 (화면 이동 함수)
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}