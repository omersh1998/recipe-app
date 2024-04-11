package com.example.recipeapp.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.activities.HomeActivity;
import com.example.recipeapp.models.Ingredient;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder> {
    private final Context context;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Ingredient> chosenIngredients;
    private DatabaseReference ingredientsRef;
    private DatabaseReference userRef;
    private String userId;

    public IngredientsAdapter(Context context, ArrayList<Ingredient> ingredients, DatabaseReference ingredientsRef, DatabaseReference userRef, String userId) {
        this.context = context;
        this.ingredients = ingredients;
        this.ingredientsRef = ingredientsRef;
        this.userRef = userRef;
        this.userId = userId;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout here and return a new ViewHolder
        View view = LayoutInflater.from(context).inflate(R.layout.ingredient_item, parent, false);
        return new IngredientViewHolder(view, ingredientsRef, userRef, userId);
    }

    public void setIngredientList(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged(); // Notify RecyclerView that the dataset has changed
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient, position, context);
    }

    public String getEditTextText(int position) {
        Ingredient ingredient = ingredients.get(position);
        return ingredient.getAmount();
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    // ViewHolder class definition
    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        private final TextView ingredientNameTextView;
        private final EditText amountEditText;
        private CheckBox checkBox;
        private DatabaseReference ingredientsRef;
        private DatabaseReference userRef;
        private String userId;

        public IngredientViewHolder(@NonNull View itemView, DatabaseReference ingredientsRef, DatabaseReference userRef, String userId) {
            super(itemView);
            ingredientNameTextView = itemView.findViewById(R.id.ingredientNameTextView);
            amountEditText = itemView.findViewById(R.id.amountEditText);
            checkBox = itemView.findViewById(R.id.ingredientCheckBox);
            this.ingredientsRef = ingredientsRef;
            this.userRef = userRef;
            this.userId = userId;
        }

        public void bind(Ingredient ingredient, int position, Context context) {
            HomeActivity homeActivity = (HomeActivity) context;
            ingredientNameTextView.setText(ingredient.getName());
            String amount = ingredient.getAmount();
            if (amount != null) {
                amountEditText.setText(amount);
            }

            checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                // Update list of ingredients based on checkbox state
                homeActivity.toggleIngredientIndex(position);
            });

            // Add a TextWatcher to update the Ingredient object as the user types
            amountEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ingredient.setAmount(editable.toString());
                }
            });
        }
    }
}

