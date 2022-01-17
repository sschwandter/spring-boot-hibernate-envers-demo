package com.ns.springboothibernateenvers;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureTestEntityManager
class SpringBootHibernateEnversApplicationTests {

    public static final int USER_ID = 1;
    @Autowired
    TestEntityManager entityManager;

    @Autowired
    UserDetailsRepository userRepository;

    @Test
    @Transactional
    void findByCurrentDateReturnsCurrentRevision() {

        final AuditReader auditReader = AuditReaderFactory.get(entityManager.getEntityManager());

        assertThat(entityManager, is(notNullValue()));
        UserDetails userDetails = new UserDetails(USER_ID, "NIRAJ", "SONAWANE");

        userRepository.save(userDetails);      // Create

        userDetails.setFirstName("Updated Name");
        userRepository.save(userDetails); // Update-1

        userDetails.setLastName("Updated Last name"); // Update-2
        userRepository.save(userDetails);

        userRepository.delete(userDetails);

        entityManager.getEntityManager().getTransaction().commit();

        // Das nur nebenbei... spring data jpa CRUD repos HABEN default methoden
        assertThat(userRepository.findAllByLastNameContains("Updated").size(), equalTo(1));

        final UserDetails currentUserDetails = auditReader.find(UserDetails.class, USER_ID, Date.from(Instant.now()));


        assertThat(currentUserDetails, is(nullValue()));

    }

    @Test
    @Transactional
    void ifCurrentEntryWasDeletedFindByCurrentDateReturnsNull() {

        final AuditReader auditReader = AuditReaderFactory.get(entityManager.getEntityManager());

        assertThat(entityManager, is(notNullValue()));
        UserDetails userDetails = new UserDetails(USER_ID, "NIRAJ", "SONAWANE");

        userRepository.save(userDetails);      // Create

        userDetails.setFirstName("Updated Name");
        userRepository.save(userDetails); // Update-1

        userDetails.setLastName("Updated Last name"); // Update-2
        userRepository.save(userDetails);

        userRepository.delete(userDetails);

        entityManager.getEntityManager().getTransaction().commit();


        final UserDetails currentUserDetails = auditReader.find(UserDetails.class, USER_ID, Date.from(Instant.now()));


        assertThat(currentUserDetails, is(nullValue()));

    }

}
