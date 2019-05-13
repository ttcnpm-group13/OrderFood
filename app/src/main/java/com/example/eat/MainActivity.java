package com.example.eat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eat.Hientai.Hientai;
import com.example.eat.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends AppCompatActivity {
    Button  btndangnhap,btndangky;
    TextView txtChao,txtApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btndangnhap = (Button)findViewById(R.id.btndangnhap);
        btndangky = (Button)findViewById(R.id.btndangky);
        txtChao = (TextView)findViewById(R.id.txtChao);
        txtApp = (TextView)findViewById(R.id.txtApp);
        Typeface face1 = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        txtChao.setTypeface(face1);
        txtApp.setTypeface(face1);

        Paper.init(this);
        btndangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dangky = new Intent(MainActivity.this,DangKy.class);
                startActivity(dangky);
            }
        });
        btndangnhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dangnhap = new Intent(MainActivity.this,Dangnhap.class);
                startActivity(dangnhap);
            }
        });
        //Kiểm tra giá trị đã lưu nếu hợp lệ sẽ tự động đăng nhập
        String user = Paper.book().read(Hientai.USER_KEY);
        String pwd = Paper.book().read(Hientai.PWD_KEY);
        if(user != null && pwd !=null){
            if(!user.isEmpty() && !pwd.isEmpty())
                login(user,pwd);
        }
    }
    private void login(final String phone, final String pwd) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");
        if(Hientai.isConnectedToInternet(getBaseContext())){
            final ProgressDialog process = new ProgressDialog(MainActivity.this);
            process.setMessage("Vui lòng đợi");
            process.show();
            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //Kiểm tra người dùng có tồn tại trong database
                    if (dataSnapshot.child(phone).exists()) {
                        // Lấy thông tin người dùng
                        process.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone); // set Phone cho người dùng
                        /*
                        if(user.getPassword()==null ||pwd.isEmpty()){
                            Toast.makeText(MainActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        }
                        */
                        if (user.getPassword().equals(pwd)) {
                            Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            Intent homeIntent = new Intent(MainActivity.this,Home.class);
                            Hientai.currentUser = user;
                            startActivity(homeIntent);
                            finish();//Chuyển qua activity mới và hủy activity hiện tại

                        } else {
                            Toast.makeText(MainActivity.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        process.dismiss();
                        Toast.makeText(MainActivity.this,"Người dùng không tồn tại",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else{
            Toast.makeText(MainActivity.this,"Vui lòng kiểm tra kết nối Internet",Toast.LENGTH_LONG).show();
            return;
        }
    }
}
