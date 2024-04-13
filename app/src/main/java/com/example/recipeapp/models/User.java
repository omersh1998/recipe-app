package com.example.recipeapp.models;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class User {

    private String email;
    private String pass;
    private String phone;
    private String name;
    private String age;
    @Nullable
    private ArrayList<Ingredient> ingredients;

    public User(){

    }

    public User(String email, String phone, String name, String age) {
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.age = age;
    }
    public User(String email, String name, String age, String phone, @Nullable ArrayList<Ingredient> ingredients) {
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.age = age;
        this.ingredients = ingredients;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPass() {
        return pass;
    }

    public String getPhone() {
        return phone;
    }

    public String getAge() {
        return age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
