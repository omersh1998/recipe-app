package com.example.recipeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.activities.HomeActivity;
import com.example.recipeapp.adapters.RecipesAdapter;
import com.example.recipeapp.models.Ingredient;
import com.example.recipeapp.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private String userName;
    private String userId;
    private ArrayList<Recipe> recipes;
    private RecipesAdapter recipesAdapter;
    private DatabaseReference userRef;
    private DatabaseReference recipeRef;
    private ArrayList<String> likedRecipes;
    private HomeActivity homeActivity;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;


    public AccountFragment(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
    }

    public static AccountFragment newInstance(String userName, String userId) {
        AccountFragment fragment = new AccountFragment(userName, userId);
        Bundle args = new Bundle();
        args.putString(USER_NAME, userName);
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString(USER_NAME);
            userId = getArguments().getString(USER_ID);
        }
        mAuth = FirebaseAuth.getInstance();
    }

    public RecyclerView createRecycleView (View rootView) {
        recyclerView = rootView.findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return recyclerView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.account, container, false);

        TextView nameView = (TextView) rootView.findViewById(R.id.userNameTextView);
        nameView.setText("Hello " + userName + "!");

        FirebaseDatabase realTimeDb = FirebaseDatabase.getInstance();
        userRef = realTimeDb.getReference("users").child(userId);

        recipes = new ArrayList<>();
        likedRecipes = new ArrayList<>();

        recyclerView = createRecycleView(rootView);

        homeActivity = (HomeActivity) getActivity();
        homeActivity.startLoading();

        getData();

        return rootView;
    }

    // Set the user's name and liked recipes
    public void getData () {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("name").getValue(String.class);
                likedRecipes.clear();

                // Get the list of ingredients
                for (DataSnapshot likedRecipesSnapshot : dataSnapshot.child("like").getChildren()) {
                    String likedRecipeId = likedRecipesSnapshot.getKey();
                    likedRecipes.add(likedRecipeId);
                }

                getMyRecipes();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                likedRecipes.clear();
                getMyRecipes();
            }
        });
    }

    public void getMyRecipes () {
        // Start getting recipes data
        FirebaseDatabase realTimeDb = FirebaseDatabase.getInstance();
        recipeRef = realTimeDb.getReference("recipes");
        recipes = new ArrayList<>();

        // Start loading screen
        homeActivity.startLoading();

        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipes.clear();

                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    // Get the user
                    String user = (String) recipeSnapshot.child("user").getValue();

                    // Only continue if the recipe is the current user's recipe
                    if (Objects.equals(user, userId)) {
                        // Get the recipe name
                        String recipeName = (String) recipeSnapshot.child("name").getValue();
                        String recipeId = recipeSnapshot.getKey();
                        System.out.println("Recipe Name: " + recipeName);

                        ArrayList<Ingredient> ingredients = new ArrayList<>();

                        // Get the list of ingredients
                        for (DataSnapshot ingredientSnapshot : recipeSnapshot.child("ingredients").getChildren()) {
                            String ingredientId = ingredientSnapshot.getKey();
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

                        Boolean isLiked = likedRecipes.contains(recipeId);
                        recipes.add(new Recipe(recipeId, recipeName, ingredients, description, imageUrl, isLiked, userId));
                    }
                }

                homeActivity.stopLoading();

                // Initialize or update the adapter
                if (recipesAdapter == null) {
                    recipesAdapter = new RecipesAdapter(getActivity(), recipes, recipeRef, userRef, userId);
                    recyclerView.setAdapter(recipesAdapter);
                } else {
                    recipesAdapter.setRecipeList(recipes);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                homeActivity.stopLoading();
            }
        });
    }
}