package com.example.recipeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.activities.HomeActivity;
import com.example.recipeapp.adapters.RecipesAdapter;
import com.example.recipeapp.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LikedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LikedFragment extends Fragment {

    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private String userName;
    private String userId;
    private ArrayList<Recipe> likedRecipes;
    private RecipesAdapter recipesAdapter;
    private DatabaseReference userRef;
    private DatabaseReference recipeRef;
    private HomeActivity homeActivity;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;

    public LikedFragment(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
    }

    public static LikedFragment newInstance(String userName, String userId) {
        LikedFragment fragment = new LikedFragment(userName, userId);
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
        recyclerView = rootView.findViewById(R.id.liked_recipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return recyclerView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.liked_recipes, container, false);

        homeActivity = (HomeActivity) getActivity();

        FirebaseDatabase realTimeDb = FirebaseDatabase.getInstance();
        userRef = realTimeDb.getReference("users").child(userId);

        likedRecipes = new ArrayList<>();
        homeActivity.likedRecipeIds = new ArrayList<>();

        homeActivity.getLikedRecipes();

        recyclerView = createRecycleView(rootView);

//        getMyRecipes(null);

        homeActivity.myRecipeList.getRecipeList().observe(getViewLifecycleOwner(), newArray -> {
            getMyRecipes(newArray);
        });

        return rootView;
    }

    public void getMyRecipes (@Nullable ArrayList<Recipe> recipesToCheck) {
        // Start getting recipes data
        FirebaseDatabase realTimeDb = FirebaseDatabase.getInstance();
        recipeRef = realTimeDb.getReference("recipes");
        likedRecipes = new ArrayList<>();

        ArrayList<Recipe> recipesToUse;
        if (recipesToCheck != null) {
            recipesToUse = recipesToCheck;
        } else {
            recipesToUse = homeActivity.totalRecipes;
        }

        for (Recipe recipe : recipesToUse) {
            if (recipe.getLiked()) {
                likedRecipes.add(recipe);
            }
        }

        // Start loading screen
        homeActivity.stopLoading();

        // Initialize or update the adapter
        if (recipesAdapter == null) {
            recipesAdapter = new RecipesAdapter(getActivity(), likedRecipes, recipeRef, userRef, userId);
            recyclerView.setAdapter(recipesAdapter);
        } else {
            recipesAdapter.setRecipeList(likedRecipes);
        }

//        recipeRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                recipes.clear();
//
//                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
//                    // Get the user
//                    String user = (String) recipeSnapshot.child("user").getValue();
//                    String recipeId = recipeSnapshot.getKey();
//                    boolean isLiked = likedRecipeIds.contains(recipeId);
//
//                    // Only continue if the recipe is the current user's recipe
//                    if ((Objects.equals(user, "all") || Objects.equals(user, userId)) && isLiked) {
//                        // Get the recipe name
//                        String recipeName = (String) recipeSnapshot.child("name").getValue();
//                        System.out.println("Recipe Name: " + recipeName);
//
//                        ArrayList<Ingredient> ingredients = new ArrayList<>();
//
//                        // Get the list of ingredients
//                        for (DataSnapshot ingredientSnapshot : recipeSnapshot.child("ingredients").getChildren()) {
//                            String ingredientId = ingredientSnapshot.getKey();
//                            String ingredientName = ingredientSnapshot.child("name").getValue(String.class);
//                            String ingredientAmount = ingredientSnapshot.child("amount").getValue(String.class);
//                            System.out.println("Ingredient: " + ingredientName + ", Amount: " + ingredientAmount);
//                            ingredients.add(new Ingredient(ingredientName, ingredientAmount, ingredientId));
//                        }
//
//                        // Get the description
//                        String description = (String) recipeSnapshot.child("description").getValue();
//
//                        // Get the image url
//                        String imageUrl = null;
//                        if ((String) recipeSnapshot.child("imageUrl").getValue() != null) {
//                            imageUrl = (String) recipeSnapshot.child("imageUrl").getValue();
//                        }
//
//                        recipes.add(new Recipe(recipeId, recipeName, ingredients, description, imageUrl, true, user));
//                    }
//                }
//
//                homeActivity.stopLoading();
//
//                // Initialize or update the adapter
//                if (recipesAdapter == null) {
//                    recipesAdapter = new RecipesAdapter(getActivity(), recipes, recipeRef, userRef, userId);
//                    recyclerView.setAdapter(recipesAdapter);
//                } else {
//                    recipesAdapter.setRecipeList(recipes);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
    }
}