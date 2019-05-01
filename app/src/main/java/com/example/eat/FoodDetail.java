package com.example.eat;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.eat.Database.Database;
import com.example.eat.Model.Food;
import com.example.eat.Model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FoodDetail extends AppCompatActivity {
    TextView food_name,food_price,food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    ElegantNumberButton numberButton;
    String foodId = "";
    FirebaseDatabase database;
    DatabaseReference foods;
    Food currentFood;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        //Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Food");
        //ánh xạ
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (FloatingActionButton)findViewById(R.id.btnCart);
        //Tạo sự kiện click cho button thêm hàng vào giỏ
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice()
                ));
                Toast.makeText(FoodDetail.this,"Đã thêm hàng vào giỏ",Toast.LENGTH_SHORT).show();
            }
        });
        food_name = (TextView)findViewById(R.id.food_name);
        food_description=(TextView)findViewById(R.id.food_description);
        food_price=(TextView)findViewById(R.id.food_price);
        food_image = (ImageView)findViewById(R.id.image_food);
        collapsingToolbarLayout =(CollapsingToolbarLayout)findViewById(R.id.collapsing);

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        //Nhận THông tin Food từ Intent gửi đến
        if(getIntent() != null){
            foodId= getIntent().getStringExtra("FoodId");
        }
        if(!foodId.isEmpty() && foodId !=null){
            getDetailFood(foodId);
        }

    }
    //Hiển thông thông tin chi tiết món
    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // đối tượng currentFood lấy dữ liệu từ database
                currentFood= dataSnapshot.getValue(Food.class);
                //Thiết lập ánh
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice());
                food_description.setText(currentFood.getDescription());
                food_name.setText(currentFood.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
