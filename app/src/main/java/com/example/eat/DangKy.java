package com.example.eat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.eat.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class DangKy extends AppCompatActivity {
    MaterialEditText edtten,edtsdt1,edtmk1,edtemail;
    Button btndangky1;
    //FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);
        edtten = (MaterialEditText)findViewById(R.id.edtten);
        edtsdt1 = (MaterialEditText)findViewById(R.id.edtsdt1);
        edtmk1 = (MaterialEditText)findViewById(R.id.edtmk1);
        btndangky1 = (Button)findViewById(R.id.btndangky1);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");
        //tao firebasrAuth
        //mAuth = FirebaseAuth.getInstance();
        //test setValue()
        //DatabaseReference admin;
        //admin = FirebaseDatabase.getInstance().getReference();
        //User user_admin = new User("Hoang Hieu","5422113");
        //admin.child("Admin").push().setValue(user_admin);

        btndangky1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog process = new ProgressDialog(DangKy.this);
                process.setMessage("Vui lòng đợi");
                process.show();
                //DangKyEmail();
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(edtsdt1.getText().toString().isEmpty()||edtten.getText().toString().isEmpty()||edtmk1.getText().toString().isEmpty()){
                            process.dismiss();
                            Toast.makeText(DangKy.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        }
                        //Kiểm tra số điện thoại đã có sẵn
                        else if(dataSnapshot.child(edtsdt1.getText().toString()).exists()){
                            process.dismiss();
                            Toast.makeText(DangKy.this, "Số điện thoại đã đăng ký", Toast.LENGTH_SHORT).show();
                        }
                        else{
                             process.dismiss();
                             User user = new User(edtten.getText().toString(),edtmk1.getText().toString());
                             table_user.child(edtsdt1.getText().toString()).setValue(user);
                             Toast.makeText(DangKy.this,"Đăng ký thành công",Toast.LENGTH_SHORT).show();
                             finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }
    /*
    private void DangKyEmail(){
        String email = edtemail.getText().toString();
        String password = edtmk1.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(DangKy.this,"Đăng ký với email thành công",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DangKy.this,"Lỗi",Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
    */
}
