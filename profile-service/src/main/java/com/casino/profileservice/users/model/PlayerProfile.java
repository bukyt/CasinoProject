package com.casino.profileservice.users.model;

import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_profiles")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerProfile {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id; // This is the Integer (1, 2, 3...) we need

    @Column(name = "player_profile_id", nullable = false, unique = true)
    private String playerProfileId; // This is the UUID String

    @Column(nullable = false, unique = true)
    private String accountId; // Auth UUID

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus status;

    @Embedded
    private ContactDetails contactDetails;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "language", column = @Column(name = "pref_language")),
            @AttributeOverride(name = "currency", column = @Column(name = "pref_currency"))
    })
    private Preferences preferences;
}
