package com.example.group_purchase_system;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class DetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView titleTextView, contentsTextView, likesTextView, viewsTextView, nameTextView, timestampTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();

        titleTextView = findViewById(R.id.titleTextView);
        contentsTextView = findViewById(R.id.contentsTextView);
        likesTextView = findViewById(R.id.likesTextView);
        viewsTextView = findViewById(R.id.viewsTextView);
        nameTextView = findViewById(R.id.nameTextView);
        timestampTextView = findViewById(R.id.timestampTextView);

        Button likeButton = findViewById(R.id.likeButton);
        Button backButton = findViewById(R.id.backToMainButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String uid = currentUser.getUid();

                    CollectionReference postsRef = db.collection("post");
                    Intent intent = getIntent();
                    if (intent != null && intent.hasExtra("title")) {
                        String receivedTitle = intent.getStringExtra("title");
                        postsRef.whereEqualTo("title", receivedTitle)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                long likes = document.getLong("likes");
                                                likes++;
                                                document.getReference().update("likes", likes);
                                                document.getReference().update("likedBy", FieldValue.arrayUnion(uid))
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("DetailActivity", "Likes updated");
                                                                    // 좋아요 수 업데이트 후 해당 값을 가져와 TextView에 표시
                                                                    document.getReference().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                DocumentSnapshot updatedDocument = task.getResult();
                                                                                if (updatedDocument.exists()) {
                                                                                    long updatedLikes = updatedDocument.getLong("likes");
                                                                                    // Likes 값 TextView에 표시
                                                                                    likesTextView.setText("Likes: " + updatedLikes);
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    Log.e("DetailActivity", "Error updating likes", task.getException());
                                                                }
                                                            }
                                                        });

                                                // 나머지 데이터 업데이트 및 표시 (생략)
                                            }
                                        } else {
                                            Log.e("DetailActivity", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                    }
                } else {
                    Log.d("DetailActivity", "User not logged in");
                }
            }
        });
    }
}