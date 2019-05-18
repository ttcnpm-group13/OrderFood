package com.example.eat.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.eat.Cart;
import com.example.eat.Database.Database;
import com.example.eat.Hientai.Hientai;
import com.example.eat.Interface.ItemClickListener;
import com.example.eat.Model.Order;
import com.example.eat.R;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;


class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener {
    public TextView name_MonDat, price_MonDat;
    public ElegantNumberButton btn_quantity;
    public ImageView cart_image;
    private ItemClickListener itemClickListener;

    public void setName_MonDat(TextView name_MonDat) {
        this.name_MonDat = name_MonDat;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        //ánh xạ
        name_MonDat = (TextView) itemView.findViewById(R.id.name_MonDat);
        price_MonDat = (TextView) itemView.findViewById(R.id.price_MonDat);
        btn_quantity = (ElegantNumberButton) itemView.findViewById(R.id.btn_quantity);
        cart_image = (ImageView)itemView.findViewById(R.id.cart_image);
        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Lựa chọn thao tác");
        menu.add(0, 0, getAdapterPosition(), Hientai.DELETE);
    }
}
public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {
    private List<Order> listData =new ArrayList<>();
    private Cart cart;//ngữ cảnh
    //private ViewGroup parent;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        //Công việc của LayoutInflater đọc xml layout file và chuyển đổi các thuộc tính của nó thành 1 View trong Java code
        LayoutInflater inflater = LayoutInflater.from(cart); //Tạo ra đối tượng LayoutInflater
        //ta có thể dùng phương thức inflate để chuyển đổi 1 xml layout file thành 1 View trong java
        View itemViem =inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(itemViem);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, final int i) {
        //Vẽ khung tròn hiển thị số lượng món đang đặt
       // TextDrawable drawable = (TextDrawable) TextDrawable.builder()
         //       .buildRound(""+listData.get(i).getQuantity(), Color.BLUE);
        //cartViewHolder.number_MonDat.setImageDrawable(drawable);
        Picasso.with(cart.getBaseContext())
                .load(listData.get(i).getImage())
                .resize(70,70)
                .centerCrop()
                .into(cartViewHolder.cart_image);
        cartViewHolder.btn_quantity.setNumber(listData.get(i).getQuantity());
        cartViewHolder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                    Order order =listData.get(i);
                    order.setQuantity(String.valueOf(newValue));
                    new Database(cart).updateCart(order);
                    //Update total price
                    int total = 0;
                    List<Order> orders = new Database(cart).getCarts();
                    for(Order item:orders)
                     total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                    cart.txtTongGia.setText(String.valueOf(total));
            }
        });
        //Locale locale = new Locale("en","EN");
        //NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(i).getPrice()))*(Integer.parseInt(listData.get(i).getQuantity()));
        //cartViewHolder.price_MonDat.setText(format.format(price));
        String total_price=String.valueOf(price);
        cartViewHolder.price_MonDat.setText(total_price);
        cartViewHolder.name_MonDat.setText(listData.get(i).getProductName());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

}
