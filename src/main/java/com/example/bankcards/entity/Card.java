package com.example.bankcards.entity;


import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.converter.CardStatusConverter;
import com.example.bankcards.entity.operations.Transaction;
import com.example.bankcards.util.CardNumberEncryptorConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Card extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_card")
    @SequenceGenerator(name = "sequence_card", sequenceName = "card_main_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "card_number")
    @Convert(converter = CardNumberEncryptorConverter.class)
    private String cardNumber;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private Customer customer;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "card_status")
    @Convert(converter = CardStatusConverter.class)
    private CardStatus status;

    @Column(name = "card_balance")
    private BigDecimal balance;

    @Column(name = "currency")
    private String currency;

    @OneToMany(mappedBy = "sourceCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> history;
}
