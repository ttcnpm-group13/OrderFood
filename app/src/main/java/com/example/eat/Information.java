package com.example.eat;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eat.Common.Common;
import com.example.eat.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;

public class Information extends AppCompatActivity {
    TextView txtPhone,txtName,txtAddress;
    FButton btn_nameUpdate,btn_addressUpdate;
    FirebaseDatabase database;
    DatabaseReference user_db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        //Anh xa
        txtName=(TextView)findViewById(R.id.txtName);
        txtPhone=(TextView)findViewById(R.id.txtPhone);
        txtAddress=(TextView)findViewById(R.id.txtAddress);
        btn_nameUpdate = (FButton)findViewById(R.id.btn_nameUpdate);
        btn_addressUpdate = (FButton)findViewById(R.id.btn_addressUpdate);
        database = FirebaseDatabase.getInstance();
        user_db = database.getReference("User");

        //Event for button
        btn_nameUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Information.this);
                alertDialog.setTitle("Cập nhật tên");
                alertDialog.setMessage("Vui lòng điển đủ thông tin");
                LayoutInflater inflater = LayoutInflater.from(Information.this);
                View layout_name = inflater.inflate(R.layout.update_name_layout,null);
                final MaterialEditText edtName =(MaterialEditText)layout_name.findViewById(R.id.edtName);
                alertDialog.setView(layout_name);
                alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Information.this).build();
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
                                            Toast.makeText(Information.this,"Tên đã cập nhật",Toast.LENGTH_SHORT).show();
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
        });
        btn_addressUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Information.this);
                alertDialog.setTitle("Cập nhật địa chỉ");
                alertDialog.setMessage("Vui lòng điển đủ thông tin");
                LayoutInflater inflater = LayoutInflater.from(Information.this);
                View layout_address = inflater.inflate(R.layout.home_address_layout,null);
                final MaterialEditText edtHomeAddress =(MaterialEditText)layout_address.findViewById(R.id.edtHomeAddress);
                alertDialog.setView(layout_address);
                alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Information.this).build();
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
                                            Toast.makeText(Information.this,"Địa chỉ đã cập nhật",Toast.LENGTH_SHORT).show();
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
        });
        user_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.child(Common.currentUser.getPhone()).getValue(User.class);
                Common.currentUser=user;
                txtName.setText("Tên: "+ Common.currentUser.getName());
                txtPhone.setText("SĐT: "+ Common.currentUser.getPhone());
                txtAddress.setText("ĐC: "+ Common.currentUser.getHomeAddress());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
