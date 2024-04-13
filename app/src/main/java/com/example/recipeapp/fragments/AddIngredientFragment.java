package com.example.recipeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.activities.HomeActivity;
import com.example.recipeapp.adapters.IngredientListAdapter;
import com.example.recipeapp.models.Ingredient;
import com.example.recipeapp.models.Recipe;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddIngredientFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddIngredientFragment extends Fragment {
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private String userId;
    private EditText ingredientName;
    private Button addIngredientButton;

    public AddIngredientFragment(String userId) {
        this.userId = userId;
    }

    public static AddIngredientFragment newInstance(String userId) {
        AddIngredientFragment fragment = new AddIngredientFragment(userId);
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_ingredient, container, false);

        HomeActivity homeActivity = (HomeActivity) getActivity();
        ingredientName = rootView.findViewById(R.id.ingredientNameEditText);
        addIngredientButton = rootView.findViewById(R.id.submitIngredientButton);

        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeActivity.addNewIngredient(new Ingredient(ingredientName.getText().toString()));
            }
        });

        return rootView;
    }
}