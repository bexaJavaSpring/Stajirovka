package com.example.stajirovka.entity.enums;

public enum TransactionType {
    INCOME("INCOME"),
    EXPENSE("EXPENSE"),
    MOVED("MOVED");

    public final String value;

    TransactionType(String value){
        this.value = value;
    }

}
