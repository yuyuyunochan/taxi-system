package com.taxi.trip_service.entity;

import com.taxi.trip_service.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private DriverStatus status;
}