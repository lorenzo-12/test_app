package com.example.test_app;

import androidx.annotation.Nullable;

public class Steps {

    public String date;
    public int step;

    public Steps() {
        super();
        this.step = 0;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj==null || !obj.getClass().equals(this.getClass())) return false;
        Steps aux = (Steps) obj;
        if( aux.date.equals(this.date) ) return true;
        else return false;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}
