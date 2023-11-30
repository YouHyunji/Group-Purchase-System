package com.example.group_purchase_system;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

// 글쓰기창 액티비티
public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
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
            String postid = mStore.collection(Board_contents.post).document().getId();
            Map<String, Object> data = new HashMap<>();
            data.put(MemberInfo.name, mAuth.getCurrentUser().getUid());
            data.put(Board_contents.title, mTitle.getText().toString());
            data.put(Board_contents.contents, mContents.getText().toString());
            data.put(Board_contents.timestamp, FieldValue.serverTimestamp());
            mStore.collection(Board_contents.post).document(postid).set(data, SetOptions.merge());
            finish();
        }
    }
}