package dev.superman.client.schema;

import java.util.Random;

public class User {
    private static final String GUEST_NAME = "Guest-";
    private String temporaryName;
    private String name;

    public User() {
        setTemporaryName(GUEST_NAME + new Random().nextInt(1000));
    }

    public String getTemporaryName() {
        return temporaryName;
    }
    
    public void setTemporaryName(String temporaryName) {
        this.temporaryName = temporaryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
