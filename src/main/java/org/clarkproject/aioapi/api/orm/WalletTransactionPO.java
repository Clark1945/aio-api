package org.clarkproject.aioapi.api.orm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "wallet_transaction")
public class WalletTransactionPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletPO wallet;

    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime completeTime;

    @Column(nullable = false)
    private String txType;

    private Long receiver;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amt;

    private String transactionStatus;

    @Column(nullable = false)
    private UUID transactionId = UUID.randomUUID();

    @Column(precision = 19, scale = 2)
    private BigDecimal fee;

    private String description;

}

