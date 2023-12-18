package com.example.group_purchase_system.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_purchase_system.DetailActivity;
import com.example.group_purchase_system.MemberInfo;
import com.example.group_purchase_system.R;
import com.example.group_purchase_system.models.Post;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>{

    private List<Post> datas;
    private OnItemClickListener mListener; // mListener 선언

    public PostAdapter(List<Post> datas) {
        this.datas = datas;
    }


    // PostAdapter 클래스 내부에 클릭 이벤트 처리하는 인터페이스 추가
    public interface OnItemClickListener {
        void onItemClick(Post post);

    }

    // PostAdapter 클래스 내부에 클릭 이벤트 리스너를 설정하는 메서드 추가
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post data = datas.get(position);
        holder.title.setText(data.getTitle());
        holder.name.setText(data.getName());

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    int clickedPosition = holder.getAdapterPosition();
                    if (clickedPosition != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(datas.get(clickedPosition)); // 클릭된 아이템의 정보 전달

                        // 여기에서 DetailActivity.class를 시작합니다.
                        Context context = view.getContext();
                        Intent intent = new Intent(context, DetailActivity.class);

                        // DetailActivity에 데이터를 전달할 수 있습니다.
                        // 예를 들어, 포스트 ID를 DetailActivity로 보내는 경우
                        intent.putExtra("title", datas.get(clickedPosition).getTitle());
                        intent.putExtra("name", datas.get(clickedPosition).getName());

                        context.startActivity(intent);
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder{

        private TextView title;
        private TextView name;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.item_post_title);
            name = itemView.findViewById(R.id.item_post_name);

        }

    }
}
