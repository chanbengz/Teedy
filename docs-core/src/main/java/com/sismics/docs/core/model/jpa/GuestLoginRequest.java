package com.sismics.docs.core.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "T_GUEST_LOGIN_REQUEST")
public class GuestLoginRequest {
    @Id
    @Column(name = "GLR_ID_C", length = 36)
    private String id;

    @Column(name = "GLR_TOKEN_C", nullable = false, length = 100)
    private String token;

    @Column(name = "GLR_IP_C", nullable = false, length = 45)
    private String ip;

    @Column(name = "GLR_TIMESTAMP_D", nullable = false)
    private Date timestamp;

    @Column(name = "GLR_STATUS_C", nullable = false, length = 20)
    private String status = "PENDING";

    public GuestLoginRequest() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date();
    }

    public GuestLoginRequest(String token, String ip) {
        this();
        this.token = token;
        this.ip = ip;
    }

    public String getId() {
        return id;
    }

    public GuestLoginRequest setId(String id) {
        this.id = id;
        return this;
    }

    public String getToken() {
        return token;
    }

    public GuestLoginRequest setToken(String token) {
        this.token = token;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public GuestLoginRequest setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public GuestLoginRequest setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public GuestLoginRequest setStatus(String status) {
        this.status = status;
        return this;
    }
}