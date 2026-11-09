package com.danzo.multi_currency_expense_tracker.expense;

import com.danzo.multi_currency_expense_tracker.auth.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expense",indexes = @Index(columnList = "user_id, spent_on"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private Users user;

    @Column(name = "description",nullable = false)
    private String description;

    @Column(name = "amount_cents",nullable = false)
    private long amountCents;

    @Column(name = "currency",nullable = false,length = 3)
    private String currency;

    @Column(name = "amount_eur_cents",nullable = false)
    private long amountEurCents;

    @Column(name = "rate_used",precision = 18,scale = 8,nullable = false)
    private BigDecimal rateUsed;

    @Column(name = "spent_on",nullable = false)
    private LocalDate spentOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "category",nullable = false)
    private Category category;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;



    public Expense(Users user, String description, long amountCents, String currency,
                   long amountEurCents, BigDecimal rateUsed, LocalDate spentOn, Category category) {
        this.user = user;
        this.description = description;
        this.amountCents = amountCents;
        this.currency = currency;
        this.amountEurCents = amountEurCents;
        this.rateUsed = rateUsed;
        this.spentOn = spentOn;
        this.category = category;
    }

}
