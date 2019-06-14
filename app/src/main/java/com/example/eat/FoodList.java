package com.example.eat;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eat.Database.Database;
import com.example.eat.Common.Common;
import com.example.eat.Interface.ItemClickListener;
import com.example.eat.Model.Food;
import com.example.eat.Model.Order;
import com.example.eat.ViewHolder.FoodViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;



public class FoodList extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    //FirebaseDatabase database;
    DatabaseReference foodList;
    String CategoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    //Chức năng tìm kiếm
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    SwipeRefreshLayout swipeRefreshLayout;
    //FaceBook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //Create Target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create Photo from Bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if(ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        //Init FaceBook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        foodList = FirebaseDatabase.getInstance().getReference("Food");
        //SwipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Nhận giá trị Intent gửi đi
                if(getIntent() != null){
                    CategoryId = getIntent().getStringExtra("CategoryId");
                }
                if(!CategoryId.isEmpty() && CategoryId !=null){
                    if(Common.isConnectedToInternet(getBaseContext())){
                        loadListFood(CategoryId);
                    }else{
                        Toast.makeText(FoodList.this,"Vui lòng kiểm tra kết nối Internet",Toast.LENGTH_LONG).show();
                        return;
                    }
                }

            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //Nhận giá trị Intent gửi đi
                if(getIntent() != null){
                    CategoryId = getIntent().getStringExtra("CategoryId");
                }
                if(!CategoryId.isEmpty() && CategoryId !=null){
                    if(Common.isConnectedToInternet(getBaseContext())){
                        loadListFood(CategoryId);
                    }else{
                        Toast.makeText(FoodList.this,"Vui lòng kiểm tra kết nối Internet",Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                //Tìm kiếm
                materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
                materialSearchBar.setHint("Nhập tên món ăn");
                //materialSearchBar.setSpeechMode(false);
                loadSuggest(); //Viết hàm để load gợi ý từ Firebase
                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //Khi người dùng nhập vào SearchBar danh sách gợi ý sẽ thay đổi theo.
                        List<String> suggest = new ArrayList<String>();
                        for(String search:suggestList){ // Vòng lặp suggetList
                            if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        //Khi Search Bar bị đóng
                        //Khôi phục adapter ban đầu
                        if(!enabled)
                            //searchAdapter.stopListening();
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        //Khi Search hoàn tất
                        //hiển thị kết quả của searchAdapter
                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });
        recyclerView = (RecyclerView)findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    private void startSearch(CharSequence text) {
        //Create query by name
        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());
        //Create options with query
        FirebaseRecyclerOptions<Food> foodoptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodoptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.food_name.setText(model.getName());
                viewHolder.food_price.setText(String.format("%s",model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image);
                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey());//gửi FoodId (key của Food) đến activity mới
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter); //Set adapter cho Recycler View của Search result
    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(CategoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Food item = postSnapshot.getValue(Food.class);
                    suggestList.add(item.getName()); //Thêm tên món ăn vào danh sách gợi ý

                }
                materialSearchBar.setLastSuggestions(suggestList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Hiển thị danh sách món
    private void loadListFood(String CategoryId) {
        //Create query by Category
        Query searchByName = foodList.orderByChild("menuId").equalTo(CategoryId);
        //Create options with query
        FirebaseRecyclerOptions<Food> foodoptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodoptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, final int position, @NonNull final Food model) {
                viewHolder.food_name.setText(model.getName());
                viewHolder.food_price.setText(String.format("%s",model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image);
                //Quick Cart
                    viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean isExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                            if (!isExists) {
                                new Database(getBaseContext()).addToCart(new Order(
                                        Common.currentUser.getPhone(),
                                        adapter.getRef(position).getKey(),
                                        model.getName(),
                                        "1",
                                        model.getPrice(),
                                        model.getImage(),
                                        ""
                                ));

                            }else{
                                new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(),adapter.getRef(position).getKey());
                            }
                            Toast.makeText(FoodList.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    });

                //Click to share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });
                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey());//gửi FoodId (key của Food) đến activity mới
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        // Thiết lập adapter
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!= null)
            adapter.startListening();

    }
        @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null) {
            adapter.stopListening();
        }
        if(searchAdapter !=null) {
            searchAdapter.stopListening();
        }
    }
}
