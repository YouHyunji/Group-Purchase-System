package com.example.group_purchase_system;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Date;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.util.CustomClassMapper;

public class DetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView titleTextView, contentsTextView, likesTextView, viewsTextView, nameTextView, timestampTextView;

    private void startToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        //메뉴로 돌아가는 버튼
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });



        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("title")) {
            String title = intent.getStringExtra("title");
            String contents = intent.getStringExtra("contents");
            String name = intent.getStringExtra("name");
            long likes = intent.getLongExtra("likes", 0);
            long timestamp = intent.getLongExtra("timestamp", 0);
            long views2 = intent.getLongExtra("views", 0);


            titleTextView.setText("Title: " + title);
            contentsTextView.setText("Contents: " + contents);
            likesTextView.setText("Likes: " + likes);
            nameTextView.setText("Name: " + name);
            timestampTextView.setText("Timestamp: " + timestamp);
            viewsTextView.setText("Views: " + views2);



            // 파이어베이스의 'views' 값을 업데이트
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference postsRef = db.collection("post");
            String receivedTitle = intent.getStringExtra("title");

            postsRef.whereEqualTo("title", receivedTitle)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    // 파이어베이스의 필드 값을 업데이트
                                    long views2 = document.getLong("views");
                                    long likes = document.getLong("likes");

                                    views2++;
                                    long finalViews = views2;
                                    document.getReference().update("likes", likes);
                                    document.getReference().update("views", finalViews)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("DetailActivity", "Views updated");
                                                        // 업데이트 완료 후 파이어베이스의 데이터를 다시 불러와 TextView에 표시
                                                        document.getReference().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot updatedDocument = task.getResult();
                                                                    if (updatedDocument.exists()) {
                                                                        long updatedViews = updatedDocument.getLong("views");
                                                                        // Views 값 TextView에 표시
                                                                        viewsTextView.setText("Views: " + updatedViews);
                                                                        long updatedLikes = updatedDocument.getLong("likes");
                                                                        // Likes 값 TextView에 표시
                                                                        likesTextView.setText("Likes: " + updatedLikes);


                                                                        Timestamp timestamp = document.getTimestamp("timestamp");
                                                                        if (timestamp != null) {
                                                                            Date date = timestamp.toDate(); // 여기서 toDate() 메소드를 사용하여 Date 객체로 변환
                                                                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                                                            String formattedDate = dateFormat.format(date);
                                                                            timestampTextView.setText("Timestamp: " + formattedDate);
                                                                            // 이제 'date' 변수에는 'timestamp'의 날짜 정보가 포함된 Date 객체가 저장됨
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Log.e("DetailActivity", "Error updating views", task.getException());
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Log.e("DetailActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }


        // 클릭한 title 값 가져오기
        String clickedTitle = getIntent().getStringExtra("title");
//이미지 출력하는 부분
// Firestore의 'post' 컬렉션에서 클릭한 title에 해당하는 문서 가져오기
        CollectionReference postsRef = FirebaseFirestore.getInstance().collection("post");
        postsRef.whereEqualTo("title", clickedTitle)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                // 해당 문서에서 imageUrl 필드 가져오기
                                String imageUrl = document.getString("imageUrl");
                                ImageView imageView = findViewById(R.id.imageView);
                                // imageUrl을 사용하여 이미지 로딩 라이브러리(Glide, Picasso 등)를 이용해 이미지 표시하기
                                Glide.with(getApplicationContext())
                                        .load(imageUrl)
                                        .into(imageView); // imageView는 이미지를 표시할 ImageView

                            }

                        } else {
                            Log.e("DetailActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });


        //버튼 클릭하면 조회수 증가/혹은 이미 눌렀다는 표시 하는부분
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String uid = currentUser.getUid();

                    CollectionReference postsRef = db.collection("post");
                    if (intent != null && intent.hasExtra("title")) {
                        String receivedTitle = intent.getStringExtra("title");
                        postsRef.whereEqualTo("title", receivedTitle)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                List<String> likedBy = (List<String>) document.get("likedBy");
                                                long likes = document.getLong("likes");
                                                if (likedBy != null && likedBy.contains(uid)) {
                                                    startToast("이미 좋아요를 눌렀습니다.");
                                                    // 이미 좋아요를 눌렀으므로 중복 처리
                                                }else {
                                                    likes++;
                                                }
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
                                                                    startToast("좋아요는 한 번만 가능합니다");
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