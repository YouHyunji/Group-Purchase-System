package com.example.group_purchase_system;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.group_purchase_system.Board_contents;
import com.example.group_purchase_system.MemberInfo;
import com.example.group_purchase_system.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri imageUri;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String imageUrl;
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
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


    private void uploadImageToFirebaseStorage(Uri imageUri, String postId, Map<String, Object> data) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        data.put("imageUrl", imageUrl);

                        // 이미지 URL을 포함한 데이터를 Firestore에 저장합니다.
                        mStore.collection(Board_contents.post).document(postId).set(data, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(PostActivity.this, "게시물 작성에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.post_save_button) {
            if (mAuth.getCurrentUser() != null) {
                String postid = mStore.collection(Board_contents.post).document().getId();
                Map<String, Object> data = new HashMap<>();
                data.put(MemberInfo.name, mAuth.getCurrentUser().getUid());
                data.put(Board_contents.title, mTitle.getText().toString());
                data.put(Board_contents.contents, mContents.getText().toString());
                data.put(Board_contents.timestamp, FieldValue.serverTimestamp());
                data.put("views", 0);
                data.put("likes", 0);

                // 이미지 URL을 Firestore에 저장하기 위해 이미지 업로드를 호출하고, 그 결과를 Firestore에 저장하는 코드를 추가합니다.
                if (imageUri != null) {
                    uploadImageToFirebaseStorage(imageUri, postid, data);
                } else {
                    // 이미지를 선택하지 않은 경우
                    mStore.collection(Board_contents.post).document(postid).set(data, SetOptions.merge());
                    finish();
                }
            }
        }
    }
}