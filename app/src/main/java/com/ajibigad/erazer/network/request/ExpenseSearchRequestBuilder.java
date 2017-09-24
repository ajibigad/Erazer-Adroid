package com.ajibigad.erazer.network.request;

import java.util.Date;

/**
 * Created by ajibigad on 29/07/2017.
 */

public class ExpenseSearchRequestBuilder {

    private String state;
    private Date dateAdded;
    private String sortBy;

    public ExpenseSearchRequestBuilder state(String state) {
        this.state = state;
        return this;
    }

    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.state != null || !this.state.isEmpty()) {
            stringBuilder.append("state").append("=").append(state);
        }
        return stringBuilder.toString();
    }
}
