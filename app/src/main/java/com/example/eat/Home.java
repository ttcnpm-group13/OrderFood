package com.example.eat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.example.eat.Database.Database;
import com.example.eat.Hientai.Hientai;
import com.example.eat.Interface.ItemClickListener;
import com.example.eat.Model.Category;
import com.example.eat.Model.Order;
import com.example.eat.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView txtuserName;
    DatabaseReference category;
    RecyclerView list_menu;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter ;
    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;
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
                if (Hientai.isConnectedToInternet(getBaseContext())){
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
                if (Hientai.isConnectedToInternet(getBaseContext())){
                    Load_menu();
                }else{
                    Toast.makeText(getBaseContext(),"Vui lòng kiểm tra kết nối Internet",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        //khai bao du lieu tu database
        category= FirebaseDatabase.getInstance().getReference("Category");
        Paper.init(this);

        fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            //Tạo sự kiện click để xem hóa đơn
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this,Cart.class);
                startActivity(cartIntent);
            }
        });
        fab.setCount(new Database (this).getCountCart());

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
        txtuserName.setText(Hientai.currentUser.getName());
        //Tải lên danh sách món
        list_menu = (RecyclerView)findViewById(R.id.recycler_menu);
        list_menu.setHasFixedSize(true);
        LinearLayoutManager layoutManager;
        //layoutManager = new LinearLayoutManager(this);
        //list_menu.setLayoutManager(layoutManager);
        list_menu.setLayoutManager(new GridLayoutManager(this, 2));
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
        fab.setCount(new Database (this).getCountCart());
        if(adapter != null)
            adapter.startListening();
    }

        @Override
    protected void onStop() {
        super.onStop();
        if(adapter != null) {
            adapter.stopListening();
        }
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
        if(item.getItemId() == R.id.refresh){
            Load_menu();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_donhang) {
            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);
        } else if (id == R.id.nav_datmon) {
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);
        } else if (id == R.id.nav_dangxuat) {
            //Xóa user và password đã ghi nhớ
            Paper.book().destroy();
            //Logout
            Intent signIn = new Intent(Home.this,Dangnhap.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(signIn);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
