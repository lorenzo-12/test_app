package com.example.test_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Food {

    public String name;
    public String category;
    public int carb;
    public int prot;
    public int fat;
    public int cal;

    public Food() {
        super();
        this.name = "dummy";
        this.category = "none";
        this.carb = 0;
        this.prot = 0;
        this.fat = 0;
        this.cal = 0;
    }

    public Food(String name){
        super();
        this.name = name;
    }

    public Food(String name, String category, int carb, int prot, int fat, int cal){
        this.name = name;
        this.category = category;
        this.carb = carb;
        this.prot = prot;
        this.fat = fat;
        this.cal = cal;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj==null || !obj.getClass().equals(this.getClass())) return false;
        Food aux = (Food) obj;
        if( aux.name.equals(this.name)) return true;
        else return false;
    }

    @NonNull
    @Override
    public String toString() {
        String res = "";
        String space = "\t\t";
        res += space+name+" "+category+"\n";
        res += space+"c: "+String.valueOf(carb)+" p: "+String.valueOf(prot)+" f: "+String.valueOf(fat)+" c: "+String.valueOf(cal)+"\n";
        return res;
    }
    
}
