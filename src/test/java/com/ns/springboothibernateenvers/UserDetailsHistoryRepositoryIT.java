package com.ns.springboothibernateenvers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureTestEntityManager
public class UserDetailsHistoryRepositoryIT {

	public static final int USER_ID = 1;
	@Autowired
	TestEntityManager entityManager;

	@Autowired
	UserDetailsRepository userRepository;

	@Autowired
	AddressRepository addressRepository;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	private TransactionTemplate template;

	@BeforeAll
	public void transactionTemplate() {
		template = new TransactionTemplate(platformTransactionManager);
	}

	@Test
	void findAllRevisionsReturnsListOfAllRevisions() {

		initTestData();

		template.executeWithoutResult(__ -> {
			final List<UserDetails> allUserDetails = userRepository.getTechnicalHistory(USER_ID);

			assertThat(allUserDetails.size(), equalTo(3));
			assertThat(allUserDetails, containsInAnyOrder(new UserDetails(USER_ID, "NIRAJ", "SONAWANE", getSampleAddress1()),
					new UserDetails(USER_ID, "Updated Name", "SONAWANE", getSampleAddress1()),
					new UserDetails(USER_ID, "Updated Name", "Updated Last name", getSampleAddress1())));
		});
	}

	private Address getSampleAddress1() {
		return new Address(1, "Montecuccoliplatz");
	}

	@Test
	void findByCurrentDateReturnsCurrentRevision() {

		initTestData();

		template.executeWithoutResult(__ -> {
			final Optional<UserDetails> currentUserDetails = userRepository.getAtTimestamp(USER_ID, Date.from(Instant.now()));
			assertThat(currentUserDetails.isPresent(), is(true));
		});
	}

	@Test
	void ifCurrentEntryWasDeletedFindByCurrentDateReturnsEmptyResult() {

		UserDetails userDetails = initTestData();

		delete(userDetails);

		template.executeWithoutResult(__ ->  {
				final Optional<UserDetails> currentUserDetails = userRepository.getAtTimestamp(USER_ID, Date.from(Instant.now()));
				assertThat(currentUserDetails.isPresent(), is(false));
		});
	}

	private UserDetails initTestData() {
		UserDetails userDetails = new UserDetails(USER_ID, "NIRAJ", "SONAWANE", getSampleAddress1());
		initialCommit();
		firstUpdate(userDetails);
		secondUpdate(userDetails);
		return userDetails;
	}

	@Transactional
	void delete(UserDetails userDetails) {
		userRepository.delete(userDetails);
	}
	@Transactional
	void secondUpdate(UserDetails userDetails) {
		userDetails.setLastName("Updated Last name"); // Update-2
		userRepository.save(userDetails);
	}

	@Transactional
	void firstUpdate(UserDetails userDetails) {
		userDetails.setFirstName("Updated Name");
		userRepository.save(userDetails); // Update-1
	}

	@Transactional
	void initialCommit() {
		addressRepository.save(getSampleAddress1());
		UserDetails userDetails = new UserDetails(USER_ID, "NIRAJ", "SONAWANE", getSampleAddress1());
		userRepository.save(userDetails);      // Create
	}
}
