package com.introtech.introtechservice.common;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseEntity implements Model, Serializable {

    @Id
    @SequenceGenerator(name = "base_sequence", initialValue = 10000, allocationSize = 1)
    @GeneratedValue(generator = "base_sequence")
    private Long id;
}
