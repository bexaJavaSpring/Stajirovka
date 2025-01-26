package com.example.stajirovka.entity.enums;

public enum TransactionStatus {
  PREPAYMENT("PREPAYMENT"),
    PLANNED("PLANNED"),
    FINANCIAL_DEBT("FINANCIAL_DEBT"),
    LATER_PAYMENT("LATER_PAYMENT");

  public final String value;

  TransactionStatus(String value){
      this.value = value;
  }
}
