package com.example.test_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Day {

    public String date;
    public int or;
    public String food_name;
    public int quantity;
    public String exercise_name;
    public int set;
    public int rep;

    public Day() {
        super();
        this.date = "00-00-00";
        this.or = 0;
        this.food_name = "none";
        this.quantity = 0;
        this.exercise_name = "none";
        this.set = 0;
        this.rep = 0;
    }

    public Day(String date){
        super();
        this.date = date;
    }

    @Override
    public int hashCode() {
        return date.hashCode()+food_name.hashCode()+exercise_name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj==null || !obj.getClass().equals(this.getClass())) return false;
        Day aux = (Day) obj;
        if(aux.date.equals(this.date) && food_name.equals(this.food_name)) return true;
        if(aux.date.equals(this.date) && exercise_name.equals(this.exercise_name)) return true;
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        String res = "";
        String space = "\t\t";
        res += space+date+"\n";
        if(or==0) {
            res += space+food_name+" q"+String.valueOf(quantity)+"\n";
        }
        else{
            res += space+exercise_name+" s: "+String.valueOf(set)+" r: "+String.valueOf(rep)+"\n";
        }
        return res;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getOr() {
        return or;
    }

    public void setOr(int or) {
        this.or = or;
    }

    public String getFood_name() {
        return food_name;
    }

    public void setFood_name(String food_name) {
        this.food_name = food_name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getExercise_name() {
        return exercise_name;
    }

    public void setExercise_name(String exercise_name) {
        this.exercise_name = exercise_name;
    }

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
    }

    public int getRep() {
        return rep;
    }

    public void setRep(int rep) {
        this.rep = rep;
    }
}
