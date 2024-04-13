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
    private ArrayList<Ingredient> filteredIngredients;
    private ArrayList<Boolean> checkedStates;
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

        this.checkedStates = new ArrayList<>();
        this.filteredIngredients = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            checkedStates.add(false); // Initialize all items as unchecked
        }
        this.filteredIngredients.addAll(ingredients);
    }

    // Filter method
    public void filter(String query) {
        filteredIngredients.clear();
        if (query.isEmpty()) {
            filteredIngredients.addAll(ingredients);
        } else {
            for (Ingredient ingredient : ingredients) {
                if (ingredient.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredIngredients.add(ingredient);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout here and return a new ViewHolder
        View view = LayoutInflater.from(context).inflate(R.layout.ingredient_item, parent, false);
        return new IngredientViewHolder(view, ingredientsRef, userRef, userId, ingredients);
    }

    public void setIngredientList(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged(); // Notify RecyclerView that the dataset has changed
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = filteredIngredients.get(position);
        boolean isIngredientChecked = checkedStates.get(ingredients.indexOf(ingredient));
        HomeActivity homeActivity = (HomeActivity) context;

        holder.bind(ingredient, context);

        // Set checkbox state
        holder.checkBox.setChecked(isIngredientChecked);

//        holder.checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
//            // Update list of ingredients based on checkbox state
//            checkedStates.set(ingredients.indexOf(ingredient), isChecked);
//            homeActivity.toggleIngredientIndex(ingredient);
//        });

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update list of ingredients based on checkbox state
                boolean isChecked = ((CheckBox) v).isChecked();
                checkedStates.set(ingredients.indexOf(ingredient), isChecked);
                homeActivity.toggleIngredientIndex(ingredient);
            }
        });
    }

    public String getEditTextText(int index) {
        // Search for the index of the target item
        Ingredient ingredient = ingredients.get(index);
        return ingredient.getAmount();
    }

    @Override
    public int getItemCount() {
        return filteredIngredients.size();
    }

    // ViewHolder class definition
    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        private final TextView ingredientNameTextView;
        private final EditText amountEditText;
        private CheckBox checkBox;
        private ArrayList<Ingredient> ingredients;
        private DatabaseReference ingredientsRef;
        private DatabaseReference userRef;
        private String userId;

        public IngredientViewHolder(@NonNull View itemView, DatabaseReference ingredientsRef, DatabaseReference userRef, String userId, ArrayList<Ingredient> ingredients) {
            super(itemView);
            ingredientNameTextView = itemView.findViewById(R.id.ingredientNameTextView);
            amountEditText = itemView.findViewById(R.id.amountEditText);
            checkBox = itemView.findViewById(R.id.ingredientCheckBox);
            this.ingredients = ingredients;
            this.ingredientsRef = ingredientsRef;
            this.userRef = userRef;
            this.userId = userId;
        }

        public void bind(Ingredient ingredient, Context context) {
            ingredientNameTextView.setText(ingredient.getName());
            String amount = ingredient.getAmount();
            if (amount != null) {
                amountEditText.setText(amount);
            }

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

