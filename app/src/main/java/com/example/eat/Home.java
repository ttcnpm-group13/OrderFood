package com.example.eat;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.eat.Common.Common;
import com.example.eat.Database.Database;
import com.example.eat.Interface.ItemClickListener;
import com.example.eat.Model.Banner;
import com.example.eat.Model.Category;
import com.example.eat.ViewHolder.MenuViewHolder;
import com.facebook.accountkit.AccountKit;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;



public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView txtuserName;
    DatabaseReference category;
    RecyclerView list_menu;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter ;
    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;
    //Slider
    HashMap<String,String> image_list;
    SliderLayout mSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext())){
                    Load_menu();
                }else{
                    Toast.makeText(getBaseContext(),"Vui lòng kiểm tra kết nối Internet",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        //Defult, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext())){
                    Load_menu();
                }else{
                    Toast.makeText(getBaseContext(),"Vui lòng kiểm tra kết nối Internet",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        //khai bao du lieu tu database
        category= FirebaseDatabase.getInstance().getReference("Category");
        //Paper.init(this);

        fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            //Tạo sự kiện click để xem hóa đơn
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this,Cart.class);
                startActivity(cartIntent);
            }
        });
        fab.setCount(new Database (this).getCountCart(Common.currentUser.getPhone()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Hiển thị tên user trên navigation
        View headerview = navigationView.getHeaderView(0);
        txtuserName= (TextView)headerview.findViewById(R.id.txtuserName);
        txtuserName.setText(Common.currentUser.getName());
        //Tải lên danh sách món
        list_menu = (RecyclerView)findViewById(R.id.recycler_menu);
        list_menu.setHasFixedSize(true);
        LinearLayoutManager layoutManager;
        //layoutManager = new LinearLayoutManager(this);
        //list_menu.setLayoutManager(layoutManager);
        list_menu.setLayoutManager(new GridLayoutManager(this, 2));
        //Set up slider
        setupSlider();
    }

    private void setupSlider() {
        mSlider= (SliderLayout)findViewById(R.id.slider);
        image_list = new HashMap<>();
        final DatabaseReference banner = FirebaseDatabase.getInstance().getReference("Banner");
        banner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Banner banner = postSnapShot.getValue(Banner.class);
                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());
                }
                for(String key:image_list.keySet()){
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];
                    //Create Slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView.description(nameOfFood).image(image_list.get(key)).setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this,FoodDetail.class);
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });
                            //Add Extra bundle
                            textSliderView.bundle(new Bundle());
                            textSliderView.getBundle().putString("FoodId",idOfFood);
                            mSlider.addSlider(textSliderView);
                            //Remove Event after finish
                            banner.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);
    }


    // lấy giữ liệu từ Firebase kết nối với recycler View
    private void Load_menu() {
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();
         adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
             @Override
             protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                 viewHolder.txtMenuName.setText(model.getName());
                 Picasso.with(getBaseContext()).load(model.getImage())
                         .into(viewHolder.imageView);
                 final Category clickItem = model;
                 viewHolder.setItemClickListener(new ItemClickListener() {
                     @Override
                     public void onClick(View view, int position, boolean isLongClick) {
                         //Lấy CategoryId và gửi nó đến Activity mới
                         Intent foodList = new Intent(Home.this, FoodList.class);
                         //Vì Category là key nên ta lấy key của dữ liệu trên
                         foodList.putExtra("CategoryId", adapter.getRef(position).getKey());//Dữ liệu "CategoryId" lấy cùng của Intent
                         startActivity(foodList);
                     }
                 });
             }

             @NonNull
             @Override
             public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                 View itemView = LayoutInflater.from(parent.getContext())
                         .inflate(R.layout.menu_food,parent,false);
                 return new MenuViewHolder(itemView);
             }
         };
         adapter.startListening();
        list_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }
    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database (this).getCountCart(Common.currentUser.getPhone()));
        if(adapter != null)
            adapter.startListening();
        mSlider.startAutoCycle();
    }


        @Override
    protected void onStop() {
        super.onStop();
        if(adapter != null) {
            adapter.stopListening();
        }
        mSlider.stopAutoCycle();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //refresh menu
        if(item.getItemId() == R.id.menu_search){
            startActivity(new Intent(Home.this,SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            Intent infoIntent = new Intent(Home.this,Information.class);
            startActivity(infoIntent);
        } else if (id == R.id.nav_donhang) {
            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);
        } else if (id == R.id.nav_datmon) {
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);
        } else if (id == R.id.nav_dangxuat) {
            //Xóa user và password đã ghi nhớ
            //Paper.book().destroy();
            //Logout
            /*
            Intent signIn = new Intent(Home.this,Dangnhap.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(signIn);
            */
            AccountKit.logOut();
            Intent intent = new Intent(Home.this,MainActivity.class);
            startActivity(intent);
        }else if(id == R.id.nav_update_name){
            updateName();
        }else if(id == R.id.nav_home_address){
            updateHomeAddress();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateHomeAddress() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Cập nhật địa chỉ");
        alertDialog.setMessage("Vui lòng điển đủ thông tin");
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_address = inflater.inflate(R.layout.home_address_layout,null);
        final MaterialEditText edtHomeAddress =(MaterialEditText)layout_address.findViewById(R.id.edtHomeAddress);
        alertDialog.setView(layout_address);
        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Home.this).build();
                waitingDialog.show();
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());
                //Update address
                Map<String,Object> update_address = new HashMap<>();
                update_address.put("homeAddress",edtHomeAddress.getText().toString());
                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(update_address)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //dismiss dialog
                                waitingDialog.dismiss();
                                if(task.isSuccessful()){
                                    Toast.makeText(Home.this,"Địa chỉ đã cập nhật",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

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

    private void updateName() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Cập nhật tên");
        alertDialog.setMessage("Vui lòng điển đủ thông tin");
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_name = inflater.inflate(R.layout.update_name_layout,null);
        final MaterialEditText edtName =(MaterialEditText)layout_name.findViewById(R.id.edtName);
        alertDialog.setView(layout_name);
        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Home.this).build();
                waitingDialog.show();
                Common.currentUser.setName(edtName.getText().toString());
                //Update name
                Map<String,Object> update_name = new HashMap<>();
                update_name.put("name",edtName.getText().toString());
                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(update_name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //dismiss dialog
                                waitingDialog.dismiss();
                                if(task.isSuccessful()){
                                    Toast.makeText(Home.this,"Tên đã cập nhật",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

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
}
