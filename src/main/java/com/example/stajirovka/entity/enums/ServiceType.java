package com.example.stajirovka.entity.enums;

public enum ServiceType {
    CREATING_WEBSITE("CREATING_WEBSITE"),
    CREATING_BOT("CREATING_BOT"),
    SMM("SMM"),
    LAUNCH_CONTEXTUAL_ADVERTISING("LAUNCH_CONTEXTUAL_ADVERTISING"),
    LAUNCH_TARGETING("LAUNCH_TARGETING"),
    BRENDING("BRENDING"),
    SEO("SEO"),
    OTHER("OTHER");

    public final String value;

    ServiceType(String value) {
        this.value = value;
    }
}
