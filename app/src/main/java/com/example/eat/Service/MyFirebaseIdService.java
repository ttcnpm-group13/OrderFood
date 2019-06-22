package com.example.eat.Service;

import com.example.eat.Common.Common;
import com.example.eat.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefresh = FirebaseInstanceId.getInstance().getToken();
        if (Common.currentUser != null)
            updateTokenToFirebase(tokenRefresh);
    }

    private void updateTokenToFirebase(String tokenRefresh) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh, false);
        tokens.child(Common.currentUser.getPhone()).setValue(token);
    }
}
