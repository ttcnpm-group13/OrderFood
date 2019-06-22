package com.example.eat;


import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eat.Database.Database;
import com.example.eat.Common.Common;
import com.example.eat.Helper.RecyclerItemTouchHelper;
import com.example.eat.Interface.RecyclerItemTouchHelperListener;
import com.example.eat.Model.MyResponse;
import com.example.eat.Model.Notification;
import com.example.eat.Model.Order;
import com.example.eat.Model.Request;
import com.example.eat.Model.Sender;
import com.example.eat.Model.Token;
import com.example.eat.Remote.APIService;
import com.example.eat.ViewHolder.CartAdapter;
import com.example.eat.ViewHolder.CartViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.SnackBar;

import java.util.ArrayList;

import java.util.List;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity implements RecyclerItemTouchHelperListener {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests;
     public TextView txtTongGia;
    FButton btnDatMon;
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;
    String address,comment;
    /*
    PlacesClient placesClient;
    Place shippingAddress;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS);
    AutocompleteSupportFragment autocompleteSupportFragment;
*/
    RelativeLayout rootLayout;
    APIService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Note: add this code before setContentView method
        setContentView(R.layout.activity_cart);
        // init service
        mService = Common.getFCMService();

        rootLayout=(RelativeLayout)findViewById(R.id.rootLayout);
        //Khởi tạo Fire base cho các yêu cầu đơn hàng
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        //Khởi tạo RecyclerView
        recyclerView=(RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);//các Item có cùng chiều cao và độ rộng thì ta có thể tối ưu hiệu năng
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //Swipe to delete cart
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);

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
        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);
        final MaterialEditText edtAddress = (MaterialEditText)order_address_comment.findViewById(R.id.edtAddress);
        final MaterialEditText edtComment = (MaterialEditText)order_address_comment.findViewById(R.id.edtComment);
        final CheckBox rdiHomeAddress = (CheckBox) order_address_comment.findViewById(R.id.rdiShipToHome);
        //Event for Checkbox
        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(Common.currentUser.getHomeAddress() !=null){
                        address = Common.currentUser.getHomeAddress();
                        edtAddress.setText(""+address);
                    }
                    else{
                        Toast.makeText(Cart.this,"Vui lòng cập nhật địa chỉ của bạn",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    edtAddress.setText("");
                }
            }
        });
        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!rdiHomeAddress.isChecked()){
                    if(edtAddress != null){
                        address = edtAddress.getText().toString();
                    }
                    else{
                        Toast.makeText(Cart.this,"Vui lòng nhập địa chỉ hoặc chọn địa chỉ của bạn",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if(TextUtils.isEmpty(address)){
                    Toast.makeText(Cart.this,"Vui lòng nhập địa chỉ hoặc chọn địa chỉ của bạn",Toast.LENGTH_SHORT).show();
                    return;
                }
                comment= edtComment.getText().toString();
                //Tạo mới một yêu cầu
                Request request = new Request( // tạo mới đối tượng request
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        address,
                        txtTongGia.getText().toString(),
                        "0",
                        cart,
                        comment
                );
                //Gửi lên Firebase
                //Sử dụng System.CurentTimeMilli đến key
                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);

                //Xóa giỏ hàng
                new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                sendNotoficationOrder(order_number);
                /*
                //Remove Fragment
                getSupportFragmentManager().beginTransaction()
                        .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
                 */
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

    private void sendNotoficationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("serverToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Token serverToken = postSnapshot.getValue(Token.class);
                    Notification notification = new Notification("Bạn có đơn hàng mới " + order_number, "Thông báo");
                    Sender content = new Sender(serverToken.getToken(), notification);
                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(Cart.this,"Đơn hàng đã được gửi đi",Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(Cart.this,"Lỗi !",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*
    private void initPlaces() {
        Places.initialize(this,getString(R.string.places_api_key));
        placesClient = Places.createClient(this);

    }

    private void setupPlaceAutocomplete() {
        autocompleteSupportFragment =(AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //hide search icon before fragment
        autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_search_button).setVisibility(View.GONE);
        //set hint for autocomplete EditText
        ((EditText)autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_search_input)).setHint("Nhập địa chỉ");
        //set Text size
        ((EditText)autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_search_input)).setTextSize(20);

        autocompleteSupportFragment.setPlaceFields(placeFields);
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                shippingAddress = place;
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(Cart.this,""+status.getStatusMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }
*/
    private void loadOrder() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());//lấy thông tin từ database
        adapter =new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();// cập nhật giao diện khi dữ liệu thay đổi
        recyclerView.setAdapter(adapter);
        //Tính tổng giá
        int total = 0;
        for(Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
            txtTongGia.setText(String.valueOf(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE)){
            deleteCart(item.getOrder());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteCart( int position) {
        //Xóa 1 món ăn ở trong giỏ hàng List<Order>
        cart.remove(position);
        //Xóa các dữ liệu cũ trong SQlite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        //Cập nhật lại dữ liệu mới
        for (Order item:cart){
            new Database(this).addToCart(item);
        }
        //refresh
        loadOrder();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof CartViewHolder){
            String name =((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();
            final Order deleteItem =((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex =viewHolder.getAdapterPosition();
            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());
            //Update total price
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for(Order item:orders)
                total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
           txtTongGia.setText(String.valueOf(total));
            //Make Snackbar
            Snackbar snackbar =Snackbar.make(rootLayout,name +" đã xóa khỏi giỏ hàng!",Snackbar.LENGTH_LONG);
            snackbar.setAction("Khôi Phục", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);
                    //Update total price
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for(Order item:orders)
                        total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                    txtTongGia.setText(String.valueOf(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
