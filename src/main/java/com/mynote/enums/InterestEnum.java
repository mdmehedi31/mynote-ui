package com.mynote.enums;


public enum InterestEnum {
    JAVA("Java Backend"),
    SPRING_BOOT("Spring Boot"),
    CLOUD_COMPUTING("Cloud Computing"),
    AI_ML("Artificial Intelligence"),
    WEB_DEVELOPMENT("Web Development"),
    DATABASE_DESIGN("Database Design");

    private final String label;

    InterestEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static InterestEnum fromLabel(String label) {
        for (InterestEnum interest : values()) {
            if (interest.label.equalsIgnoreCase(label) || interest.name().equalsIgnoreCase(label)) {
                return interest;
            }
        }
        return null;
    }
}
