package com.users.groupservice.model;

import java.util.List;

public class Group {

    private int id;
    private List<Object> accounts;

    public Group() {
    }

    public Group(int galleryId) {
        this.id = galleryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Object> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Object> images) {
        this.accounts = images;
    }

}
