package com.example.recipeapp.fragments;

import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.DefaultItemAnimator;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.example.recipeapp.R;
        import com.example.recipeapp.activities.HomeActivity;
import com.example.recipeapp.adapters.IngredientListAdapter;
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
 * Use the {@link SingleRecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SingleRecipeFragment extends Fragment {
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private String userId;
    private Recipe recipe;
    private ImageView cover_image;
    private TextView recipeNameTextView;
    private TextView description_text_view;
    private ImageButton likeSingleButton;
    private RecyclerView recyclerViewIngredients;
    private IngredientListAdapter ingredientListAdapter;
    private TextView recipe_preparation;


    public SingleRecipeFragment(String userId, Recipe recipe) {
        this.recipe = recipe;
        this.userId = userId;
    }

    public static SingleRecipeFragment newInstance(String userId, Recipe recipe) {
        SingleRecipeFragment fragment = new SingleRecipeFragment(userId, recipe);
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
        View rootView = inflater.inflate(R.layout.single_recipe, container, false);

        cover_image = (ImageView) rootView.findViewById(R.id.cover_image);
        recipeNameTextView = (TextView) rootView.findViewById(R.id.recipeNameTextView);
        description_text_view = (TextView) rootView.findViewById(R.id.description_text_view);
        likeSingleButton = (ImageButton) rootView.findViewById(R.id.likeSingleButton);
        recipe_preparation = (TextView) rootView.findViewById(R.id.recipe_preparation);

        HomeActivity homeActivity = (HomeActivity) getActivity();
        homeActivity.loadImageToView(recipe, cover_image);

        recipeNameTextView.setText(recipe.getName());
        description_text_view.setText(recipe.getDescription());
        recipe_preparation.setText(recipe.getDescription());

        ingredientListAdapter = new IngredientListAdapter(getActivity(), this.recipe.getIngredients());

        recyclerViewIngredients = rootView.findViewById(R.id.ingredientsRecyclerView);
        recyclerViewIngredients = homeActivity.createRecycleView(recyclerViewIngredients);
        recyclerViewIngredients.setAdapter(ingredientListAdapter);

        setLikeButtonImage();

        likeSingleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recipe.setLiked(!recipe.getLiked());
                homeActivity.updateLikeData(recipe, userId, recipe.getLiked());
                setLikeButtonImage();
            }
        });

        return rootView;
    }

    private void setLikeButtonImage () {
        int iconId;
        if (recipe.getLiked()) {
            iconId = R.drawable.ic_like;
        }
        else {
            iconId = R.drawable.ic_liked_recipes;
        }

        // Set icon and padding
        likeSingleButton.setImageResource(iconId);
    }
}