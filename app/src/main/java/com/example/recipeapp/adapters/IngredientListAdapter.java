package com.example.recipeapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.models.Ingredient;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class IngredientListAdapter extends RecyclerView.Adapter<IngredientListAdapter.IngredientListViewHolder> {
    private final Context context;
    private ArrayList<Ingredient> ingredients;

    public IngredientListAdapter(Context context, ArrayList<Ingredient> ingredients) {
        this.context = context;
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout here and return a new ViewHolder
        View view = LayoutInflater.from(context).inflate(R.layout.custom_ingredient_item, parent, false);
        return new IngredientListViewHolder(view, ingredients);
    }

    public void setIngredientList(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged(); // Notify RecyclerView that the dataset has changed
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientListViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    // ViewHolder class definition
    public static class IngredientListViewHolder extends RecyclerView.ViewHolder {
        private final TextView ingredientNameTextView;
        private final TextView ingredientAmount;
        private ArrayList<Ingredient> ingredients;

        public IngredientListViewHolder(@NonNull View itemView, ArrayList<Ingredient> ingredients) {
            super(itemView);
            ingredientNameTextView = itemView.findViewById(R.id.ingredientName);
            ingredientAmount = itemView.findViewById(R.id.ingredientAmount);
            this.ingredients = ingredients;
        }

        public void bind(Ingredient ingredient) {
            ingredientNameTextView.setText(ingredient.getName());
            ingredientAmount.setText(ingredient.getAmount());
        }
    }
}