package com.example.recipeapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.recipeapp.R;
import com.example.recipeapp.fragments.AccountFragment;
import com.example.recipeapp.fragments.AddRecipeFragment;
import com.example.recipeapp.fragments.LikedFragment;
import com.example.recipeapp.fragments.RecipesFragment;
import com.example.recipeapp.models.Ingredient;
import com.example.recipeapp.models.Recipe;
import com.example.recipeapp.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HomeActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private String userName;
    private String userId;
    private Boolean isLoading;
    public ArrayList<Integer> chosenIngredientIndexes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        isLoading = false;

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            userName = intent.getStringExtra("userName");
        }

        chosenIngredientIndexes = new ArrayList<>();

        fragmentManager = getSupportFragmentManager();

        // Set the default fragment
        replaceFragment(new RecipesFragment(userName, userId));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.action_recipes) {
                    selectedFragment = new RecipesFragment(userName, userId);
                }
                if (itemId == R.id.action_account) {
                    selectedFragment = new AccountFragment(userName, userId);
                }
                if (itemId == R.id.action_add_recipe) {
                    selectedFragment = new AddRecipeFragment(userName, userId);
                }
                if (itemId == R.id.action_liked_recipes) {
                    selectedFragment = new LikedFragment(userName, userId);
                }

                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                }

                return true;
            }
        });
    }

    public void replaceFragment (Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flFragment, fragment);
        transaction.commit();
    }

    public void startLoading() {
        if (!isLoading) {
            findViewById(R.id.loading).setVisibility(View.VISIBLE);
            isLoading = true;
        }
    }

    public void stopLoading() {
        if (isLoading) {
            findViewById(R.id.loading).setVisibility(View.GONE);
            isLoading = false;
        }
    }

    public void loadImageToView (Recipe recipe, ImageView recipeImageView) {
        // Set the recipe image if available
        String imageUrl = recipe.getImageUrl();
        // Set the recipe image here
        Glide.with(this /* Context */)
                .load(imageUrl)
                .placeholder(R.drawable.photo_placeholder_foreground)
                .error(R.drawable.photo_placeholder_foreground)
                .into(recipeImageView);
    }

    public RecyclerView createRecycleView (RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return recyclerView;
    }

    public void updateLikeData(Recipe recipe, String userId, Boolean updateLikeTo) {
        FirebaseDatabase realTimeDb = FirebaseDatabase.getInstance();
        DatabaseReference userRef = realTimeDb.getReference("users").child(userId);

        // Create a HashMap to hold the updates
        Map<String, Object> updates = new HashMap<>();

        String isUpdateOrDelete = null;
        if (updateLikeTo) {
            isUpdateOrDelete = recipe.getName();
        }
        updates.put("like/" + recipe.getId(), isUpdateOrDelete);
        // updates.put("key2/nested_key1", "new_value2");
        // updates.put("key3/nested_key2", "new_value3");

        // Update the data in the database
        userRef.updateChildren(updates);
    }

    // Method to toggle an ingredient index in the chosenIngredientIndexes array
    public void toggleIngredientIndex (int index) {
        if (chosenIngredientIndexes.contains(index)) {
            chosenIngredientIndexes.remove(Integer.valueOf(index)); // Remove the index if it exists
        } else {
            chosenIngredientIndexes.add(index); // Add the index if it doesn't exist
        }
    }

    public void addNewIngredient(Ingredient ingredient) {
        FirebaseDatabase realTimeDb = FirebaseDatabase.getInstance();
        DatabaseReference ingredientsRef = realTimeDb.getReference("ingredients");

        String ingredientName = ingredient.getName();

        Context context = this;

        // Check if the ingredient name already exists in the database
        ingredientsRef.orderByValue().equalTo(ingredientName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ingredient name already exists
                    // Inform the user that the ingredient name is taken
                    Toast.makeText(context, "Ingredient already exists!", Toast.LENGTH_SHORT).show();
                } else {
                    // Ingredient name is available, add it to the database
                    String newIngredientId = ingredientsRef.push().getKey();
                    ingredientsRef.child(newIngredientId).setValue(ingredientName);
                    // Inform the user that the ingredient has been added successfully
                    Toast.makeText(context, "Ingredient added successfully!", Toast.LENGTH_SHORT).show();
                    replaceFragment(new AddRecipeFragment(userName, userId));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Toast.makeText(context, "Error updating the db", Toast.LENGTH_SHORT).show();
            }
        });
    }
}