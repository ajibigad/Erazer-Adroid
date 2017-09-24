package com.ajibigad.erazer.data;

import io.realm.RealmObject;

/**
 * Created by ajibigad on 14/08/2017.
 */

public class Role extends RealmObject {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
