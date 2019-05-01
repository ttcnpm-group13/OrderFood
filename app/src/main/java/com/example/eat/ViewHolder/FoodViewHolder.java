package com.example.eat.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eat.Interface.ItemClickListener;
import com.example.eat.Model.Food;
import com.example.eat.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView food_name;
    public ImageView food_image;
    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(View itemView){
        super(itemView);
        food_name = (TextView)itemView.findViewById(R.id.menu_name);
        food_image = (ImageView)itemView.findViewById(R.id.menu_image);
        itemView.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
