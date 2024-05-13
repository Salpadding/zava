package com.github.zava.test.mybatis;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class City {
    @Id
    @Column(nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long countryId;

    @Column(nullable = false, columnDefinition = "varchar(255) default ''")
    private String name;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP()")
    private LocalDateTime createdAt;
}
