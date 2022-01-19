package com.ns.springboothibernateenvers;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;


@SpringBootApplication
public class SpringBootHibernateEnversApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootHibernateEnversApplication.class, args);
    }

    @Bean
    @Profile("initDb")
    ApplicationRunner init(UserDetailsRepository userRepository, AddressRepository addressRepository) {
        return (ApplicationArguments args) -> dataSetup(userRepository, addressRepository);
    }
    private void dataSetup(UserDetailsRepository userRepository, AddressRepository addressRepository) {

        UserDetails userDetails = new UserDetails(1, "NIRAJ", "SONAWANE", new Address(1, "Montecuccoliplatz"));

        addressRepository.save(new Address(1, "Montecuccoliplatz"));

        userRepository.save(userDetails);      // Create

        userDetails.setFirstName("Updated Name");
        userRepository.save(userDetails); // Update-1

        userDetails.setLastName("Updated Last name"); // Update-2
        userRepository.save(userDetails);

        addressRepository.save(new Address(1, "Updated address"));

        //
//        userRepository.delete(userDetails); // Delete
    }

}
