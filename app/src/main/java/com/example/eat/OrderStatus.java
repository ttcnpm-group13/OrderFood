package com.example.eat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eat.Hientai.Hientai;
import com.example.eat.Interface.ItemClickListener;
import com.example.eat.Model.Request;
import com.example.eat.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OrderStatus extends AppCompatActivity {
    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    //Firebase
    FirebaseDatabase database;
    DatabaseReference requests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        // Khởi tạo database
        database  = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        loadOrders(Hientai.currentUser.getPhone());

    }

    private void loadOrders(String phone) {
        Query getOrderByUser = requests.orderByChild("phone")
                .equalTo(phone);
        FirebaseRecyclerOptions<Request> orderOptions = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(getOrderByUser,Request.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(orderOptions) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int position, @NonNull Request model) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_layout,parent,false);
                return new OrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter != null) {
            adapter.stopListening();
        }
    }

    private String convertCodeToStatus(String status){
        if(status.equals("0"))
            return "Đang xử lí";
        else if (status.equals("1"))
            return "Đang giao";
        else return "Đã giao";
    }
}
