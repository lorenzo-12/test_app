package com.example.test_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Exercise {

    public String name;
    public String category;

    public Exercise() {
        super();
        this.name = "dummy";
        this.category = "none";
    }

    public Exercise(String name){
        super();
        this.name = name;
    }

    public Exercise(String name, String category){
        this.name=name;
        this.category=category;
    }

    @Override
    public int hashCode() {
        return name.hashCode()+category.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj==null || !obj.getClass().equals(this.getClass())) return false;
        Exercise aux = (Exercise) obj;
        if( aux.name.equals(this.name) && aux.category.equals(this.category)) return true;
        else return false;
    }

    @NonNull
    @Override
    public String toString() {
        String res = "";
        String space = "\t\t";
        res += space+name+" "+category+"\n";
        return res;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
