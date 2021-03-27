package com.autod.locshare.model;

public class Request<T> {
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private T data;
    private  BrowseUser user;

    public BrowseUser getUser() {
        return user;
    }

    public void setUser(BrowseUser user) {
        this.user = user;
    }

}
