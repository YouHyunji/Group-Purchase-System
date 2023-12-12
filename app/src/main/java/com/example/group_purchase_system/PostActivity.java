package com.example.group_purchase_system;


import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;


import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostActivity";     // TAG 추가
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();     // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )

    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri imageUri;

    private String imageUrl;

    private EditText mTitle, mContents;
    private View.OnClickListener saveImageToFirestore;
    private Object postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mTitle = findViewById(R.id.post_title_edit);
        mContents = findViewById(R.id.post_content_edit);

        findViewById(R.id.post_save_button).setOnClickListener(this);

        findViewById(R.id.button_select_image).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }


    // FireStore에 이미지 업로드
    private void uploadImageToFirebaseStorage(Uri imageUri, String postId, Map<String, Object> data) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        data.put("imageUrl", imageUrl);

                        // 이미지 URL을 포함한 데이터를 Storage에 저장
                        db.collection(Board_contents.post).document(postId).set(data, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    startToast("게시물 작성 실패 + \n + 다시 시도해 주세요");
                                    Log.e(TAG, "게시물 작성 실패", e);
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    // 업로드 실패 시 처리할 내용
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }



    // 리스너 호출 함수 (클릭 함수) : 게시글 정보를 FireStore에 업로드
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.post_save_button) {
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
                            data.put("userId", user.getUid());
                            data.put(MemberInfo.name, userName);
                            data.put(Board_contents.title, mTitle.getText().toString());
                            data.put(Board_contents.contents, mContents.getText().toString());
                            data.put(Board_contents.timestamp, FieldValue.serverTimestamp());
                            data.put("views", 0);
                            data.put("likes", 0);

                            // 이미지 URL을 Firestore에 저장하기 위해 이미지 업로드를 호출하고, 그 결과를 Firestore에 저장
                            if (imageUri != null) {
                                uploadImageToFirebaseStorage(imageUri, postid, data);
                            } else {
                                // 이미지를 선택하지 않은 경우
                                db.collection(Board_contents.post).document(postid).set(data, SetOptions.merge());
                                 startToast("이미지를 선택해주세요!");
                                // finish();            // 이미지 선택이 필수일 경우, 지울 것.
                            }

                        } else {
                            // 해당 문서가 존재하지 않을 경우의 처리
                            Log.d(TAG, "문서가 존재하지 않습니다.");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {       //  사용자 정보 문서 찾기 실패
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "사용자 정보 문서 읽기 실패", e);
                    }
                });

            }

        }
    }

    // 하단 알림창 출력하는 메서드
    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}