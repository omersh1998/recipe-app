package com.example.recipeapp.models;

import androidx.annotation.Nullable;

public class Ingredient {
    private String name;
    @Nullable
    private String amount;

    public Ingredient(String name, @Nullable String amount) {
        this.name = name;
        this.amount = amount;
    }

    public Ingredient(String name) {
        this.name = name;
        this.amount = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getAmount() {
        return amount;
    }

    public void setAmount(@Nullable String amount) {
        this.amount = amount;
    }
}
