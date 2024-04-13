package com.example.recipeapp.models;

import androidx.annotation.Nullable;

public class Ingredient {
    private String name;
    @Nullable
    private String amount;
    @Nullable
    private String id;
    private Boolean isChecked;

    public Ingredient(String name, @Nullable String amount, @Nullable String id) {
        this.name = name;
        this.amount = amount;
        this.id = id;
        this.isChecked = false;
    }

    public Ingredient(@Nullable String id, String name) {
        this.name = name;
        this.id = id;
        this.isChecked = false;
    }

    public Ingredient(String name) {
        this.name = name;
        this.isChecked = false;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public void toggleChecked() {
        this.isChecked = !this.isChecked;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
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
