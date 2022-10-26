package com.example.test_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public class User {

    public String username;
    public String password;
    public int sex;
    public int weight;
    public int height;
    public int carb_goal;
    public int prot_goal;
    public int fat_goal;
    public int cal_goal;
    public String question;

    //for each user we want to keep track of the list of all his/her foods and exercises
    //and we want to keep track of his/her diary
    public HashMap<String,Food> food_list;
    public HashMap<String,Exercise> exercise_list;
    public HashMap<String,Day> diary;


    public User() {
        super();
        username = "dummy";
        password = "none";
        food_list = new HashMap<String,Food>();
        exercise_list = new HashMap<String,Exercise>();
        diary = new HashMap<String,Day>();
        weight = 0;
        height = 0;
        carb_goal = 0;
        prot_goal = 0;
        fat_goal = 0;
        cal_goal = 0;
        question = "none";
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj==null || !obj.getClass().equals(this.getClass())) return false;
        User aux = (User) obj;
        if(aux.username.equals(this.username) ) return true;
        else return false;
    }

    @NonNull
    @Override
    public String toString() {
        String res = "";
        res += username+" "+password+" "+question+"\n";
        res += String.valueOf(weight)+" "+String.valueOf(height)+" "+String.valueOf(cal_goal)+" "+String.valueOf(prot_goal)+" "+String.valueOf(fat_goal)+" "+String.valueOf(cal_goal)+"\n";
        res += "foods:\n";
        for(String key : food_list.keySet()){
            res += food_list.get(key).toString();
        }
        res += "exercises:\n";
        for(String key : exercise_list.keySet()){
            res += exercise_list.get(key).toString();
        }
        res += "diary:\n";
        for(String key : diary.keySet()){
            res += diary.get(key).toString();
        }
        return res;
    }

    public void setGoals(int carb, int prot, int fat, int cal){
        this.carb_goal = carb;
        this.prot_goal = prot;
        this.fat_goal = fat;
        this.cal_goal = cal;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HashMap<String, Food> getFood_list() {
        return food_list;
    }

    public void setFood_list(HashMap<String, Food> food_list) {
        this.food_list = food_list;
    }

    public HashMap<String, Exercise> getExercise_list() {
        return exercise_list;
    }

    public void setExercise_list(HashMap<String, Exercise> exercise_list) {
        this.exercise_list = exercise_list;
    }


}









































