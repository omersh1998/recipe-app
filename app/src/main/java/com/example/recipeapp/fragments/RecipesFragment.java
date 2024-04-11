package com.example.recipeapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.recipeapp.R;
import com.example.recipeapp.activities.HomeActivity;
import com.example.recipeapp.adapters.RecipesAdapter;
import com.example.recipeapp.models.Ingredient;
import com.example.recipeapp.models.Recipe;
import com.example.recipeapp.models.User;
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
 * Use the {@link RecipesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView nameView;
    private DatabaseReference userRef;
    private DatabaseReference recipeRef;
    private RecyclerView recyclerView;
    private ArrayList<Recipe> recipes;
    private String userName;
    private String userId;
    private ArrayList<String> likedRecipes;
    private HomeActivity homeActivity;
    private RecipesAdapter recipesAdapter;

    public RecipesFragment(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
        Bundle args = new Bundle();
        args.putString(USER_NAME, userName);
        args.putString(USER_ID, userId);
        this.setArguments(args);
    }

    public static RecipesFragment newInstance(String userName, String userId) {
        RecipesFragment fragment = new RecipesFragment(userName, userId);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipes, container, false);

        // Get user data from DB
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = mAuth.getCurrentUser();
        if (fbUser != null) {
            userId = fbUser.getUid();
            String email = fbUser.getEmail();
        }

        // Start getting recipes data
        FirebaseDatabase realTimeDb = FirebaseDatabase.getInstance();
        userRef = realTimeDb.getReference("users").child(userId);
        recipeRef = realTimeDb.getReference("recipes");

        recipes = new ArrayList<>();
        likedRecipes = new ArrayList<>();

        nameView = rootView.findViewById(R.id.textViewUserName);

        homeActivity = (HomeActivity) getActivity();
        homeActivity.startLoading();

        recyclerView = rootView.findViewById(R.id.res);
        recyclerView = homeActivity.createRecycleView(recyclerView);

        getUserData();

        // Inflate the layout for this fragment
        return rootView;
    }

    // Set the user's name and liked recipes
    public void getUserData () {
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

                getRecipesData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                getRecipesData();
            }
        });
    }

    public void getRecipesData () {
        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipes.clear();

                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    // Get the recipe name
                    String recipeName = (String) recipeSnapshot.child("name").getValue();
                    String recipeId = recipeSnapshot.getKey();
                    System.out.println("Recipe Name: " + recipeName);

                    ArrayList<Ingredient> ingredients = new ArrayList<>();

                    // Get the list of ingredients
                    for (DataSnapshot ingredientSnapshot : recipeSnapshot.child("ingredients").getChildren()) {
                        // Get the name and amount of the ingredient
                        String ingredientName = ingredientSnapshot.child("name").getValue(String.class);
                        String ingredientAmount = ingredientSnapshot.child("amount").getValue(String.class);
                        System.out.println("Ingredient: " + ingredientName + ", Amount: " + ingredientAmount);
                        ingredients.add(new Ingredient(ingredientName, ingredientAmount));
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
                        Boolean isLiked = likedRecipes.contains(recipeId);
                        recipes.add(new Recipe(recipeId, recipeName, ingredients, description, imageUrl, isLiked));
                    }
                    System.out.println("description: " + description);
                }

                homeActivity.stopLoading();

                // Initialize or update the adapter
                if (recipesAdapter == null) {
                    recipesAdapter = new RecipesAdapter(getActivity(), recipes, recipeRef, userRef, userId);
                    recyclerView.setAdapter(recipesAdapter);
                } else {
                    recipesAdapter.setRecipeList(recipes);
                }

                nameView.setText("Welcome " + userName + "!");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                homeActivity.stopLoading();
            }
        });
    }
}