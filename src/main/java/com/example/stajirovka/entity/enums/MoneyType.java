package com.example.stajirovka.entity.enums;

public enum MoneyType {
    CASH("CASH"),
    CASH_CURRENCY("CASH_CURRENCY"),
    BANK("BANK"),
    CARD("CARD");
    public final String value;

    MoneyType(String value) {
        this.value = value;
    }
}
