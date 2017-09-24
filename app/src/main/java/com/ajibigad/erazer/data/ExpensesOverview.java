package com.ajibigad.erazer.data;

/**
 * Created by ajibigad on 29/07/2017.
 */

public class ExpensesOverview {
    private int count;
    private String state;

    public ExpensesOverview(int count, String state) {
        this.count = count;
        this.state = state;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
