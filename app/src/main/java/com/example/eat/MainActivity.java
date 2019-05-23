package com.example.eat;

import android.app.AlertDialog;

import android.content.Intent;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eat.Common.Common;
import com.example.eat.Model.User;
import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



import dmax.dialog.SpotsDialog;




public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 7171;
    Button  btnContinue;
    TextView txtChao,txtApp;
    FirebaseDatabase database;
    DatabaseReference users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountKit.initialize(this);
        setContentView(R.layout.activity_main);
        //printKeyHash();
        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");
        btnContinue = (Button)findViewById(R.id.btn_continue);
        //btndangky = (Button)findViewById(R.id.btndangky);
        txtChao = (TextView)findViewById(R.id.txtChao);
        txtApp = (TextView)findViewById(R.id.txtApp);
        Typeface face1 = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        txtChao.setTypeface(face1);
        txtApp.setTypeface(face1);

        //Paper.init(this);
        /*
        btnContinue1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent dangky = new Intent(MainActivity.this,DangKy.class);
                startActivity(dangky);


            }
        });
        */
        //Check Session Facebook Account Kit
        if(AccountKit.getCurrentAccessToken() !=null){
            //Create Dialog
            final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage("Vui lòng đợi").setCancelable(false).build();
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    //Copy code from exists user
                    users.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User localUser = dataSnapshot.getValue(User.class);
                                    //Copy code from LoginActivity
                                    Intent homeIntent = new Intent(MainActivity.this,Home.class);
                                    Common.currentUser = localUser;
                                    startActivity(homeIntent);
                                    waitingDialog.dismiss();
                                    finish();//Chuyển qua activity mới và hủy activity hiện tại
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        }
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent dangnhap = new Intent(MainActivity.this,Dangnhap.class);
                startActivity(dangnhap);
                */
                startLoginSystem();
            }
        });
        /*
        //Kiểm tra giá trị đã lưu nếu hợp lệ sẽ tự động đăng nhập
        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);
        if(user != null && pwd !=null){
            if(!user.isEmpty() && !pwd.isEmpty())
                login(user,pwd);
        }
        */
    }

    private void startLoginSystem() {
        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
        startActivityForResult(intent,REQUEST_CODE);
    }
    /*
    private void printKeyHash() {
        try{
            PackageInfo info = getPackageManager().getPackageInfo("com.example.eat", PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures){
                MessageDigest md =MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){

            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if(loginResult.getError() != null){
                Toast.makeText(this,""+loginResult.getError().getErrorType().getMessage(),Toast.LENGTH_SHORT ).show();
                return;
            }
            else if(loginResult.wasCancelled()){
                Toast.makeText(this,"Hủy",Toast.LENGTH_SHORT ).show();
                return;
            }else{
                if(loginResult.getAccessToken() != null){
                    //Show Dialog
                    final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage("Vui lòng đợi").setCancelable(false).build();
                    waitingDialog.show();
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String userPhone = account.getPhoneNumber().toString();
                            //Check if exits on Firebase Users
                            users.orderByKey().equalTo(userPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(!dataSnapshot.child(userPhone).exists()){//if not exists
                                                //We will create new user and login
                                                User newUser = new User();
                                                newUser.setPhone(userPhone);
                                                newUser.setName("");
                                                //Add to Firebase
                                                users.child(userPhone)
                                                        .setValue(newUser)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                    Toast.makeText(MainActivity.this,"Đăng kí thành công",Toast.LENGTH_SHORT).show();
                                                                //login
                                                                users.child(userPhone)
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                User localUser = dataSnapshot.getValue(User.class);
                                                                                //Copy code from LoginActivity
                                                                                Intent homeIntent = new Intent(MainActivity.this,Home.class);
                                                                                Common.currentUser = localUser;
                                                                                startActivity(homeIntent);
                                                                                waitingDialog.dismiss();
                                                                                finish();//Chuyển qua activity mới và hủy activity hiện tại
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                            }

                                                        });
                                            }
                                            else{ // if exists
                                                //login
                                                users.child(userPhone)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                User localUser = dataSnapshot.getValue(User.class);
                                                                //Copy code from LoginActivity
                                                                Intent homeIntent = new Intent(MainActivity.this,Home.class);
                                                                Common.currentUser = localUser;
                                                                startActivity(homeIntent);
                                                                waitingDialog.dismiss();
                                                                finish();//Chuyển qua activity mới và hủy activity hiện tại
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this,""+accountKitError.getErrorType().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }
}
