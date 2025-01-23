package com.example.stajirovka.entity;

import com.example.stajirovka.entity.enums.MoneyType;
import com.example.stajirovka.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private Double transactionFee;

    private LocalDate transactionDate;

    private String expenseCategory;

    private ServiceType serviceType;

    private TransactionStatus status;

    private String comment;
}
