package org.clarkproject.aioapi.api.obj.po;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
public class WalletPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amt;

    @Column(nullable = false)
    private String status;

    private LocalDateTime lastTxTime;
    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime lastFrozenTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmt() {
        return amt;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastTxTime() {
        return lastTxTime;
    }

    public void setLastTxTime(LocalDateTime lastTxTime) {
        this.lastTxTime = lastTxTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getLastFrozenTime() {
        return lastFrozenTime;
    }

    public void setLastFrozenTime(LocalDateTime lastFrozenTime) {
        this.lastFrozenTime = lastFrozenTime;
    }
}

