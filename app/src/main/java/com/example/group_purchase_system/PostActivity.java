package com.example.group_purchase_system;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

// 글쓰기창 액티비티
public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostActivity";     // TAG 추가
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText mTitle, mContents;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mTitle = findViewById(R.id.post_title_edit);
        mContents = findViewById(R.id.post_content_edit);

        findViewById(R.id.post_save_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(mAuth.getCurrentUser() != null) {
            String postid = db.collection(Board_contents.post).document().getId();

            CollectionReference userRef = db.collection("users");        // 사용자 정보 접근
            DocumentReference userDocRef = userRef.document(user.getUid());

            // 현재 사용자의 'name' 필드의 값 가져오기
            userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {      // 문서 찾기 성공
                    if (documentSnapshot.exists()) {        // 문서가 있으면
                        String userName = documentSnapshot.getString("name");   // 'name' 필드의 값을 가져와서 사용

                        // 데이터 삽입 로직
                        Map<String, Object> data = new HashMap<>();
                        data.put("userId",user.getUid());
                        data.put(MemberInfo.name, userName);
                        data.put(Board_contents.title, mTitle.getText().toString());
                        data.put(Board_contents.contents, mContents.getText().toString());
                        data.put(Board_contents.timestamp, FieldValue.serverTimestamp());
                        db.collection(Board_contents.post).document(postid).set(data, SetOptions.merge());
                        finish();

                    } else {
                        // 해당 문서가 존재하지 않을 경우의 처리
                        Log.d(TAG, "문서가 존재하지 않습니다.");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {       // 문서 찾기 실패
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "문서 읽기 실패", e);
                }
            });

        }
    }
}