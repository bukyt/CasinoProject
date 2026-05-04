package com.casino.userservice;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.casino.userservice.users.model.ContactDetails;
import com.casino.userservice.users.model.PlayerProfile;
import com.casino.userservice.users.model.Preferences;
import com.casino.userservice.users.model.ProfileStatus;
import com.casino.userservice.users.repository.PlayerProfileRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadProfilesData(PlayerProfileRepository playerProfileRepository) {
        return args -> {
            if (playerProfileRepository.count() > 0) {
                return;
            }

            PlayerProfile profile1 = new PlayerProfile();
            profile1.setPlayerProfileId("pp-01");
            profile1.setAccountId("acc-01");
            profile1.setFullName("Demo Player One");
            profile1.setDateOfBirth(LocalDate.of(1995, 5, 10));
            profile1.setStatus(ProfileStatus.ACTIVE);
            profile1.setContactDetails(new ContactDetails("player1@example.com", "+3725000001", "Tallinn"));
            profile1.setPreferences(new Preferences("en", "EUR"));
            playerProfileRepository.save(profile1);

            PlayerProfile profile2 = new PlayerProfile();
            profile2.setPlayerProfileId("pp-02");
            profile2.setAccountId("acc-02");
            profile2.setFullName("Demo Player Two");
            profile2.setDateOfBirth(LocalDate.of(1998, 8, 21));
            profile2.setStatus(ProfileStatus.ACTIVE);
            profile2.setContactDetails(new ContactDetails("player2@example.com", "+3725000002", "Tartu"));
            profile2.setPreferences(new Preferences("et", "EUR"));
            playerProfileRepository.save(profile2);
        };
    }
}
