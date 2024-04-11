package com.example.recipeapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.recipeapp.R;
import com.example.recipeapp.fragments.RecipesFragment;
import com.example.recipeapp.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
    }

    public void loginUser (String userId) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("userId", userId);
        getUserData(userId)
                .thenAccept(userName -> {
                    intent.putExtra("userName", userName);
                    startActivity(intent);
                })
                .exceptionally(throwable -> {
                    System.out.println("Could not get username: " + throwable.getMessage());
                    startActivity(intent);
                    return null;
                });

    }

    public void addData(String uid) {

        EditText user = findViewById(R.id.email);
        EditText phone = findViewById(R.id.phone);
        EditText name = findViewById(R.id.name);
        EditText age = findViewById(R.id.age);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(uid);

        User user1 = new User(user.getText().toString(), phone.getText().toString(), name.getText().toString(), age.getText().toString());

        myRef.setValue(user1);
    }

    public CompletableFuture<String> getUserData(String userId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("name").getValue(String.class);
                future.complete(userName);
                System.out.println("The read omer: " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        return future;
    }
}