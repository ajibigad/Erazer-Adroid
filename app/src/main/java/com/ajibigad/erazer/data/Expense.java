package com.ajibigad.erazer.data;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Created by ajibigad on 29/07/2017.
 */

@Parcel
public class Expense {

    public enum STATE {PENDING, APPROVED, DECLINED, SETTLED}

    ;

    public enum PROOF_TYPE {EMAIL, IMAGE, TEXT}

    ;

    private long id;
    private String title;
    private String description;
    private Date dateAdded;
    private PROOF_TYPE proofType;
    private String proof;
    //    private String username;
    private User user;
    private double cost;
    private STATE state;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public PROOF_TYPE getProofType() {
        return proofType;
    }

    public void setProofType(PROOF_TYPE proofType) {
        this.proofType = proofType;
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }

}
