package com.example.recipeapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.recipeapp.R;
import com.example.recipeapp.fragments.AccountFragment;
import com.example.recipeapp.fragments.AddRecipeFragment;
import com.example.recipeapp.fragments.LikedFragment;
import com.example.recipeapp.fragments.RecipesFragment;
import com.example.recipeapp.models.Ingredient;
import com.example.recipeapp.models.Recipe;
import com.example.recipeapp.models.RecipeList;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private String userName;
    private String userId;
    private Boolean isLoading;
    public RecipeList myRecipeList;
    public ArrayList<String> chosenIngredientIds;
    public ArrayList<Ingredient> ingredients;
    public ArrayList<String> likedRecipeIds;
    public ArrayList<Recipe> onlineRecipes;
    public ArrayList<Recipe> totalRecipes;
    public ArrayList<Recipe> localRecipes;
    private ApiResponseListener mListener;
    private boolean isLocalRecipesLoaded;
    private boolean isOnlineRecipesLoaded;
    private boolean isLikedDataLoaded;
    private boolean isInitialLoad;
    private DatabaseReference ingredientsRef;
    private DatabaseReference usersRef;
    private DatabaseReference recipeRef;
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

        chosenIngredientIds = new ArrayList<>();
        ingredients = new ArrayList<>();
        onlineRecipes = new ArrayList<>();
        localRecipes = new ArrayList<>();
        likedRecipeIds = new ArrayList<>();
        totalRecipes = new ArrayList<>();

        isLocalRecipesLoaded = false;
        isOnlineRecipesLoaded = false;
        isLikedDataLoaded = false;
        isInitialLoad = false;

        fragmentManager = getSupportFragmentManager();

        FirebaseDatabase realTimeDb = FirebaseDatabase.getInstance();
        ingredientsRef = realTimeDb.getReference("ingredients");
        usersRef = realTimeDb.getReference("users").child(userId);
        recipeRef = realTimeDb.getReference("recipes");

        // When recipe list changes do something
        myRecipeList = new ViewModelProvider(this).get(RecipeList.class);
        myRecipeList.getRecipeList().observe(this, recipeList -> {

        });

        startLoading();
        getLikedRecipes();

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

    public interface ApiResponseListener {
        void onRecipesLoaded();

        void onLikedResponseFinished();
    }

    public void setApiResponseListener(ApiResponseListener listener) {
        this.mListener = listener;
    }

    // Call this method when the response finishes
    private void onOnlineRecipesResponseFinished() {
        isOnlineRecipesLoaded = true;

        if (isLocalRecipesLoaded && isLikedDataLoaded) {
            totalRecipes.clear();
            totalRecipes.addAll(localRecipes);
            totalRecipes.addAll(onlineRecipes);

            myRecipeList.setRecipes(totalRecipes);

            if (mListener != null) {
                stopLoading();
                mListener.onRecipesLoaded();
            }
            // Set the default fragment
            if (!isInitialLoad) {
                replaceFragment(new RecipesFragment(userName, userId));
                isInitialLoad = true;
            }
        }
    }

    private void onLocalRecipesResponseFinished() {
        isLocalRecipesLoaded = true;

        if (isOnlineRecipesLoaded && isLikedDataLoaded) {
            totalRecipes.clear();
            totalRecipes.addAll(localRecipes);
            totalRecipes.addAll(onlineRecipes);

            myRecipeList.setRecipes(totalRecipes);

            if (mListener != null) {
                mListener.onRecipesLoaded();
            }
            // Set the default fragment
            if (!isInitialLoad) {
                replaceFragment(new RecipesFragment(userName, userId));
                isInitialLoad = true;
            }
        }
    }

    private void onLikedResponseFinished() {
        isLikedDataLoaded = true;

        if (isOnlineRecipesLoaded && isLocalRecipesLoaded) {
            totalRecipes.clear();
            totalRecipes.addAll(localRecipes);
            totalRecipes.addAll(onlineRecipes);

            myRecipeList.setRecipes(totalRecipes);

            if (mListener != null) {
                mListener.onRecipesLoaded();
            }
            // Set the default fragment
            if (!isInitialLoad) {
                replaceFragment(new RecipesFragment(userName, userId));
                isInitialLoad = true;
            }
        }
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
    public void toggleIngredientIndex (Ingredient ingredient) {
        int currentIndex = ingredients.indexOf(ingredient);
        ingredients.get(currentIndex).toggleChecked();

        // Search for the index of the target item
        String id = ingredient.getId();
        int index = chosenIngredientIds.indexOf(id);

        if (index > -1) {
            chosenIngredientIds.remove(index); // Remove the index if it exists
        } else {
            chosenIngredientIds.add(id); // Add the index if it doesn't exist
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

    public void getOnlineRecipes() {
        if (!onlineRecipes.isEmpty()) {
            onOnlineRecipesResponseFinished();
        } else {
            startLoading();
            getData();
        }
    }

    private void getData() {
        isOnlineRecipesLoaded = false;
        String url = "https://recipe-api-xpp0.onrender.com/recipes";
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            parseResponseRecipes(response);
                        } catch (JSONException e) {
                            onOnlineRecipesResponseFinished();
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onOnlineRecipesResponseFinished();
            }
        });
        // Add the request to the RequestQueue
        queue.add(jsonArrayRequest);
    }

    private void parseResponseRecipes(JSONArray response) throws JSONException {
        // Loop through each recipe object in the array
        for (int i = 0; i < response.length(); i++) {
            JSONObject recipeJson = response.getJSONObject(i);

            // Parse recipe details
            String name = recipeJson.getString("name");
            String id = recipeJson.getString("id");
            String category = recipeJson.getString("category");
            String description = recipeJson.getString("description");
            String imageUrl = recipeJson.getString("imageUrl");
            String user = recipeJson.getString("user");

            // Parse ingredients array
            JSONArray ingredientsArray = recipeJson.getJSONArray("ingredients");
            ArrayList<Ingredient> ingredients = new ArrayList<>();
            for (int j = 0; j < ingredientsArray.length(); j++) {
                JSONObject ingredientJson = ingredientsArray.getJSONObject(j);
                String ingredientId = ingredientJson.getString("id");
                String ingredientName = ingredientJson.getString("name");
                String amount = ingredientJson.getString("amount");
                Ingredient ingredient = new Ingredient(ingredientName, amount, ingredientId);
                ingredients.add(ingredient);
            }

            // Create Recipe object
            Boolean isLiked = likedRecipeIds.contains(id);
            Recipe recipe = new Recipe(id, name, ingredients, description, imageUrl, isLiked, "all");

            onlineRecipes.add(recipe);
        }
        stopLoading();
        onOnlineRecipesResponseFinished();
    }

    public void getLikedRecipes () {
        isLikedDataLoaded = false;
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isLikedDataLoaded = false;
                userName = dataSnapshot.child("name").getValue(String.class);
                likedRecipeIds.clear();

                // Get the list of ingredients
                for (DataSnapshot likedRecipesSnapshot : dataSnapshot.child("like").getChildren()) {
                    String likedRecipeId = likedRecipesSnapshot.getKey();
                    likedRecipeIds.add(likedRecipeId);
                }
                onLikedResponseFinished();
                getRecipesData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                likedRecipeIds.clear();
                onLikedResponseFinished();
                getRecipesData();
            }
        });
    }

    public void getRecipesData () {
        isLocalRecipesLoaded = false;
        getOnlineRecipes();
        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isLocalRecipesLoaded = false;
                localRecipes.clear();

                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    // Get the recipe name
                    String recipeName = (String) recipeSnapshot.child("name").getValue();
                    String recipeId = recipeSnapshot.getKey();
                    System.out.println("Recipe Name: " + recipeName);

                    ArrayList<Ingredient> ingredients = new ArrayList<>();

                    // Get the list of ingredients
                    for (DataSnapshot ingredientSnapshot : recipeSnapshot.child("ingredients").getChildren()) {
                        // Get the name and amount of the ingredient
                        String ingredientId = ingredientSnapshot.child("id").getValue(String.class);
                        String ingredientName = ingredientSnapshot.child("name").getValue(String.class);
                        String ingredientAmount = ingredientSnapshot.child("amount").getValue(String.class);
                        System.out.println("Ingredient: " + ingredientName + ", Amount: " + ingredientAmount);
                        ingredients.add(new Ingredient(ingredientName, ingredientAmount, ingredientId));
                    }

                    // Get the description
                    String description = (String) recipeSnapshot.child("description").getValue();

                    // Get the image url
                    String imageUrl = null;
                    if ((String) recipeSnapshot.child("imageUrl").getValue() != null) {
                        imageUrl = (String) recipeSnapshot.child("imageUrl").getValue();
                    }

                    // Get the user
                    String user = (String) recipeSnapshot.child("user").getValue();

                    if (Objects.equals(user, "all") || Objects.equals(user, userId)) {
                        Boolean isLiked = likedRecipeIds.contains(recipeId);
                        localRecipes.add(new Recipe(recipeId, recipeName, ingredients, description, imageUrl, isLiked, userId));
                    }
                    System.out.println("description: " + description);
                }

                onLocalRecipesResponseFinished();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                stopLoading();
                onLocalRecipesResponseFinished();
            }
        });
    }
}