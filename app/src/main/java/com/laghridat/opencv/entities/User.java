package com.laghridat.opencv.entities;

public class User {
    protected String user_name;
    protected String password;
    protected String firstName;
    protected String lastName;

public User(String userName, String password){
    this.user_name=userName;
    this.password=password;
}
public User(String userName, String password, String firstName, String lastName){
        this.user_name=userName;
        this.password=password;
        this.firstName=firstName;
        this.lastName=lastName;
}
}
