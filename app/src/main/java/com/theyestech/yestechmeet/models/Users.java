package com.theyestech.yestechmeet.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Users implements Parcelable {
    private String id;
    private String username;
    private String name;
    private String email;
    private String profilePhoto;
    private String state;
    private String status;
    private String search;
    private String token;
    private String fcm_token;


    public Users(String id, String username, String name, String email, String profilePhoto, String state, String status, String search,String token,String fcm_token) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.profilePhoto = profilePhoto;
        this.state = state;
        this.status = status;
        this.search = search;
        this.token = token;
        this.fcm_token = fcm_token;
    }

    public Users() {
    }

    protected Users(Parcel in) {
        id = in.readString();
        username = in.readString();
        name = in.readString();
        email = in.readString();
        profilePhoto = in.readString();
        state = in.readString();
        status = in.readString();
        search = in.readString();
        token = in.readString();
        fcm_token = in.readString();
    }

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFcm_token() {
        return fcm_token;
    }

    public void setFcm_token(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(profilePhoto);
        dest.writeString(state);
        dest.writeString(status);
        dest.writeString(search);
        dest.writeString(token);
        dest.writeString(fcm_token);
    }
}
