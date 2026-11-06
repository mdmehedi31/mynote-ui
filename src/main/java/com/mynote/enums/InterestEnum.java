package com.mynote.enums;

public enum InterestEnum {


    JAVA("Java Backend"),
    SPRING_BOOT("Spring Boot"),
    CLOUD_COMPUTING("Cloud Computing"),
    AI_ML("Artificial Intelligence"),
    WEB_DEVELOPMENT("Web Development"),
    DATABASE_DESIGN("Database Design");

    private final String displayName;

    InterestEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


