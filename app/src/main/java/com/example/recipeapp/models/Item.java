package com.example.recipeapp.models;

public class Item {
    private String name;
    private int amount;
    private int id_;

    public Item(String name, int amount, int id_) {
        this.name = name;
        this.amount = amount;
        this.id_ = id_;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
