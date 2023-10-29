package main.java;

public class User {
    private final String username;
    private final String userId;
    private final String type;

    public User(String username, String userId, String type) {
        this.username = username;
        this.userId = userId;
        this.type = type;
    }

    public String getUsername(){
        return this.username;
    }

    public String getUserId(){
        return this.userId;
    }

    public String getUserType(){ return this.type; }
}
