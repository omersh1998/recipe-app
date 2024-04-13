package com.example.recipeapp.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class RecipeList extends ViewModel {
    private final MutableLiveData<ArrayList<Recipe>> recipeList = new MutableLiveData<ArrayList<Recipe>>();
    public void clearRecipes() {
        recipeList.setValue(new ArrayList<>());
    }
    public void setRecipes(ArrayList<Recipe> recipesToAppend) {
        recipeList.setValue(recipesToAppend);
    }
    public LiveData<ArrayList<Recipe>> getRecipeList() {
        return recipeList;
    }
}
