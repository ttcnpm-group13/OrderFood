package com.example.eat;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eat.Database.Database;
import com.example.eat.Hientai.Hientai;
import com.example.eat.Model.Order;
import com.example.eat.Model.Request;
import com.example.eat.ViewHolder.CartAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests;
    TextView txtTongGia;
    FButton btnDatMon;
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Note: add this code before setContentView method
        setContentView(R.layout.activity_cart);
        //Khởi tạo Fire base cho các yêu cầu đơn hàng
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        //Khởi tạo RecyclerView
        recyclerView=(RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);//các Item có cùng chiều cao và độ rộng thì ta có thể tối ưu hiệu năng
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //Ánh xạ
        txtTongGia = (TextView)findViewById(R.id.txtTongGia);
        btnDatMon = (FButton)findViewById(R.id.btndatmon);
        // Tạo sự kiện click cho button đặt món
        btnDatMon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cart.size() > 0) {
                    showAlertDialog();
                }
                else{
                    Toast.makeText(Cart.this,"Giỏ hàng của bạn đang trống !",Toast.LENGTH_SHORT).show();
                }

            }
        });
        loadOrder();
    }
    private void showAlertDialog(){//Thiết lập AlertDialog để nhận địa chỉ
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Bước cuối cùng ");
        alertDialog.setMessage("Nhập địa chỉ nhận hàng :");
        final EditText edtAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams lP= new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtAddress.setLayoutParams(lP);
        alertDialog.setView(edtAddress);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Tạo mới một yêu cầu
                Request request = new Request( // tạo mới đối tượng request
                        Hientai.currentUser.getPhone(),
                        Hientai.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTongGia.getText().toString(),
                        cart
                );
                //Gửi lên Firebase
                //Sử dụng System.CurentTimeMilli đến key
                requests.child(String.valueOf(System.currentTimeMillis()))
                        .setValue(request);
                //Xóa giỏ hàng
                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this,"Đơn hàng đã được gửi đi",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        alertDialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
    private void loadOrder() {
        cart = new Database(this).getCarts();//lấy thông tin từ database
        adapter =new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();// cập nhật giao diện khi dữ liệu thay đổi
        recyclerView.setAdapter(adapter);

        int total = 0;
        for(Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
            txtTongGia.setText(String.valueOf(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Hientai.DELETE)){
            deleteCart(item.getOrder());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteCart( int position) {
        //Xóa 1 món ăn ở trong giỏ hàng List<Order>
        cart.remove(position);
        //Xóa các dữ liệu cũ trong SQlite
        new Database(this).cleanCart();
        //Cập nhật lại dữ liệu mới
        for (Order item:cart){
            new Database(this).addToCart(item);
        }
        //refresh
        loadOrder();
    }
}
