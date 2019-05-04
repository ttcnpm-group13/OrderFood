package com.example.eat.ViewHolder;


import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.eat.Hientai.Hientai;
import com.example.eat.Interface.ItemClickListener;
import com.example.eat.Model.Order;
import com.example.eat.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener {
    public TextView name_MonDat, price_MonDat;
    public ImageView number_MonDat;
    private ItemClickListener itemClickListener;

    public void setName_MonDat(TextView name_MonDat) {
        this.name_MonDat = name_MonDat;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        //ánh xạ
        name_MonDat = (TextView) itemView.findViewById(R.id.name_MonDat);
        price_MonDat = (TextView) itemView.findViewById(R.id.price_MonDat);
        number_MonDat = (ImageView) itemView.findViewById(R.id.number_MonDat);
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
    private Context context;//ngữ cảnh
    private ViewGroup parent;

    public CartAdapter(List<Order> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Công việc của LayoutInflater đọc xml layout file và chuyển đổi các thuộc tính của nó thành 1 View trong Java code
        LayoutInflater inflater = LayoutInflater.from(context); //Tạo ra đối tượng LayoutInflater
        //ta có thể dùng phương thức inflate để chuyển đổi 1 xml layout file thành 1 View trong java
        View itemViem =inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(itemViem);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, int i) {
        //Vẽ khung tròn hiển thị số lượng món đang đặt
        TextDrawable drawable = (TextDrawable) TextDrawable.builder()
                .buildRound(""+listData.get(i).getQuantity(), Color.BLUE);
        cartViewHolder.number_MonDat.setImageDrawable(drawable);

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
