package com.example.recipeapp.models;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Recipe {
    private String name;
    private ArrayList<Ingredient> ingredients;
    private String description;
    private String id;
    @Nullable
    private String user;
    @Nullable
    private String imageUrl;

    @Nullable
    public String getUser() {
        return user;
    }

    public void setUser(@Nullable String user) {
        this.user = user;
    }

    private Boolean isLiked;

    public Recipe(String id, String name, ArrayList<Ingredient> ingredients, String description, @Nullable String imageUrl, Boolean isLiked) {
        this.name = name;
        this.id = id;
        this.ingredients = ingredients;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isLiked = isLiked;
    }

    public Recipe(String name, ArrayList<Ingredient> ingredients, String description, @Nullable String imageUrl, @Nullable String userId) {
        this.name = name;
        this.id = null;
        this.ingredients = ingredients;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isLiked = null;
        this.user = userId;
    }

    public Boolean getLiked() {
        return isLiked;
    }

    public void setLiked(Boolean liked) {
        isLiked = liked;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@Nullable String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }
    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}
