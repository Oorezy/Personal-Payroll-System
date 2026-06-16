package com.introtech.authenticationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class Client {

    @Id
    private Long id;

    @Column(name = "client_name", unique = true)
    private String clientName;
}
