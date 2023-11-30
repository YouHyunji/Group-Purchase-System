package com.example.group_purchase_system;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Object_Category extends AppCompatActivity {
    private static final String TAG = "Object_Category";     // TAG 추가

    // 현재 로그인 되어있는지 확인 ( 현재 사용자 불러오기 )
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListView listView;
    private String selected_Major;  // 학과 정보

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_category);

        startToast("물품을 선택하세요.");

        // Intent에서 데이터 받아오기
        selected_Major = getIntent().getStringExtra("selected_Major");  // 학과 정보

        // 리스트뷰와 어댑터 초기화
        listView = (ListView) findViewById(R.id.List);

        // 카테고리 목록 불러오기
        CategoryLoad();

    }

    // 물품 리스트를 출력하기위한 커스텀 어댑터
    public class ObjectAdapter extends ArrayAdapter<StorageReference> {
        private static final String TAG = "ObjectAdapter";
        private final Context context;
        private final List<StorageReference> object;       // 물품 정보(데이터)를 담고 있는 리스트


        public ObjectAdapter(Context context, List<StorageReference> object) {     // 생성자, 멤버변수 초기화
            super(context, R.layout.list_item, object);
            this.context = context;
            this.object = object;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Item의 현재 위치 구하기 ( 참조 변수 )
            StorageReference ObjectRef = object.get(position);

            // 각 아이템 뷰에 해당하는 XML 파일을 inflate ( XML 파일 -> 실제 뷰 객체 )
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            // 리스트 내용
            TextView Object_nameView = (TextView) convertView.findViewById(R.id.item_name);

            // 물품 이름 설정
            Object_nameView.setText(ObjectRef.getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {        // 과목 리스트 아이템 클릭 이벤트

                    // 클릭한 아이템의 정보를 가져옴 : 아이템 이름
                    String selected_Object = ObjectRef.getName();

                    // 선택한 항목의 정보를 Intent에 담아 Board_List.Class를 시작
                    Intent intent = new Intent(Object_Category.this, Board_List.class);
                    intent.putExtra("selected_Major", selected_Major);    // 학과 이름 전달
                    intent.putExtra("selected_Object", selected_Object);    // 물품 이름 전달

                    Log.d(TAG, "전달한 학과 이름 : " + selected_Major);
                    Log.d(TAG, "전달한 물품 이름 : " + selected_Object);

                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

    private void startToast(String msg) {     // Toast 띄우는 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // 카테고리 불러오는 메소드
    private void CategoryLoad() {
        CollectionReference CategoryRef = db.collection("Major")   // Major 컬렉션 접근
                .document(selected_Major)           // 이전에 선택한 학과 정보
                .collection("Object");

        CategoryRef.get()  // 컬렉션 전체 데이터 가져오기
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {      // 불러오기 성공
                            List<StorageReference> Object_List = new ArrayList<>(); // StorageReference 리스트를 담을 리스트

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String object_name = (String) document.get("object");

                                // StorageReference를 생성하여 리스트에 추가
                                if (object_name != null) {     // null 처리
                                    Object_List.add(FirebaseStorage.getInstance().getReference().child(object_name));
                                } else {  startToast("물품 목록 불러오기 실패" + "\n다시 시도해 주세요.");}
                            }

                            // 어댑터를 생성하고 리스트뷰에 설정
                            ObjectAdapter adapter = new ObjectAdapter(Object_Category.this, Object_List);
                            listView.setAdapter(adapter);

                            // 어댑터에 데이터가 변경되었음을 알려줌
                            adapter.notifyDataSetChanged();
                            //startToast("물품 목록 불러오기 성공");
                            Log.d(TAG,"물품 목록 불러오기 성공");
                        } else {
                            Log.d(TAG, "물품 목록 불러오기 실패 : Error getting documents: ", task.getException());
                            startToast("물품 목록 불러오기 실패" + "\n 다시 시도해 주세요.");
                        }
                    }
                });
    }
}
