package com.ajibigad.erazer.data;

import org.parceler.Parcel;
import org.parceler.Transient;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by ajibigad on 29/07/2017.
 */

@Parcel
public class User extends RealmObject {

    @PrimaryKey
    private String username;

    @Required
    private String token;

    private boolean admin;

    @Transient
    private RealmList<Role> roles;

    public Boolean isAdmin() {
        return admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public RealmList<Role> getRoles() {
        return roles;
    }

    public void setRoles(RealmList<Role> roles) {
        this.roles = roles;
    }
}
