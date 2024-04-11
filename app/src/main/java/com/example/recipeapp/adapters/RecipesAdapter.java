package com.example.recipeapp.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipeapp.R;
import com.example.recipeapp.activities.HomeActivity;
import com.example.recipeapp.fragments.SingleRecipeFragment;
import com.example.recipeapp.models.Ingredient;
import com.example.recipeapp.models.Recipe;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder> {
    private final Context context;
    private ArrayList<Recipe> recipes;
    private DatabaseReference recipeRef;
    private DatabaseReference userRef;
    private String userId;

    public RecipesAdapter(Context context, ArrayList<Recipe> recipes, DatabaseReference recipeRef, DatabaseReference userRef, String userId) {
        this.context = context;
        this.recipes = recipes;
        this.recipeRef = recipeRef;
        this.userRef = userRef;
        this.userId = userId;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout here and return a new ViewHolder
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_item, parent, false);
        return new RecipeViewHolder(view, context, userRef, userId);
    }

    public void setRecipeList(ArrayList<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged(); // Notify RecyclerView that the dataset has changed
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe, this.context);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    // ViewHolder class definition
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView recipeNameTextView;
        private final ImageButton recipeImageView;
        private final ImageButton likeButton;
        private final DatabaseReference userRef;
        private final Context context;
        private final String userId;
        private Recipe recipe;

        public RecipeViewHolder(@NonNull View itemView, Context context, DatabaseReference userRef, String userId) {
            super(itemView);
            recipeNameTextView = itemView.findViewById(R.id.recipeNameTextView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
            likeButton = itemView.findViewById(R.id.likeButton);
            this.context = context;

            this.userRef = userRef;
            this.userId = userId;
        }
        public void bind(Recipe recipe, Context context) {
            this.recipe = recipe;
            recipeNameTextView.setText(recipe.getName());

            if (this.context instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) context;
                // Call the function from the activity
                homeActivity.loadImageToView(recipe, recipeImageView);
            }

//            Build string for ingredients
//            StringBuilder ingredientsBuilder = new StringBuilder();
//            for (Ingredient ingredient : recipe.getIngredients()) {
//                ingredientsBuilder.append(ingredient.getName()).append(": ").append(ingredient.getAmount()).append("\n");
//            }

            int iconId;
            if (recipe.getLiked()) {
                iconId = R.drawable.ic_like;
            }
            else {
                iconId = R.drawable.ic_liked_recipes;
            }

            // Set icon and padding
            likeButton.setImageResource(iconId);

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateLikeData(recipe, userId, !recipe.getLiked());
                }
            });

            recipeImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment();
                }
            });
        }

        private void replaceFragment () {
            if (this.context instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) context;
                homeActivity.replaceFragment(new SingleRecipeFragment(userId, this.recipe));
            }
        }

        public void updateLikeData (Recipe recipe, String userId, Boolean updateLikeTo) {
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
    }
}

