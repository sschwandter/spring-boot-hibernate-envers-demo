package com.ns.springboothibernateenvers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@AutoConfigureTestEntityManager
public class UserDetailsHistoryRepositoryIT {

	public static final int USER_ID = 1;
	@Autowired
	TestEntityManager entityManager;

	@Autowired
	UserDetailsRepository userRepository;

	@Autowired
	UserDetailsHistoryRepository userDetailsHistoryRepository;
	@Test
	@Transactional
	void findAllRevisionsReturnsListOfAllRevisions() {

		UserDetails userDetails = new UserDetails(USER_ID, "NIRAJ", "SONAWANE");
		userRepository.save(userDetails);      // Create

		userDetails.setFirstName("Updated Name");
		userRepository.save(userDetails); // Update-1

		userDetails.setLastName("Updated Last name"); // Update-2
		userRepository.save(userDetails);

		entityManager.getEntityManager().getTransaction().commit();


		final List<UserDetails> allUserDetails = userDetailsHistoryRepository.getTechnicalHistory(USER_ID);


		assertThat(allUserDetails.size(), equalTo(3));
		assertThat(allUserDetails, containsInAnyOrder(new UserDetails(USER_ID, "NIRAJ", "SONAWANE"),
				new UserDetails(USER_ID, "Updated Name", "SONAWANE"),
				new UserDetails(USER_ID, "Updated Name", "Updated Last name")));

	}

	@Test
	@Transactional
	void findByCurrentDateReturnsCurrentRevision() {

		assertThat(entityManager, is(notNullValue()));
		UserDetails userDetails = new UserDetails(USER_ID, "NIRAJ", "SONAWANE");

		userRepository.save(userDetails);      // Create

		userDetails.setFirstName("Updated Name");
		userRepository.save(userDetails); // Update-1

		userDetails.setLastName("Updated Last name"); // Update-2
		userRepository.save(userDetails);

		entityManager.getEntityManager().getTransaction().commit();

		final Optional<UserDetails> currentUserDetails = userDetailsHistoryRepository.getAtTimestamp(USER_ID, Date.from(Instant.now()));

		assertThat(currentUserDetails.isPresent(), is(true));
	}

	@Test
	@Transactional
	void ifCurrentEntryWasDeletedFindByCurrentDateReturnsEmptyResult() {

		assertThat(entityManager, is(notNullValue()));
		UserDetails userDetails = new UserDetails(USER_ID, "NIRAJ", "SONAWANE");

		userRepository.save(userDetails);      // Create

		userDetails.setFirstName("Updated Name");
		userRepository.save(userDetails); // Update-1

		userDetails.setLastName("Updated Last name"); // Update-2
		userRepository.save(userDetails);

		userRepository.delete(userDetails);

		entityManager.getEntityManager().getTransaction().commit();

		final Optional<UserDetails> currentUserDetails = userDetailsHistoryRepository.getAtTimestamp(USER_ID, Date.from(Instant.now()));

		assertThat(currentUserDetails.isPresent(), is(false));
	}
}
