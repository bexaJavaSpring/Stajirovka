package com.example.stajirovka.entity;

import com.example.stajirovka.entity.enums.MoneyType;
import com.example.stajirovka.entity.enums.ServiceType;
import com.example.stajirovka.entity.enums.TransactionStatus;
import com.example.stajirovka.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(value = EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(value = EnumType.STRING)
    private MoneyType moneyType;

    @Column(name = "transaction_fee")
    private Double transactionFee;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "expense_category")
    private String expenseCategory;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "comment")
    private String comment;

    @Column(name = "view_url")
    private String viewUrl;
}
