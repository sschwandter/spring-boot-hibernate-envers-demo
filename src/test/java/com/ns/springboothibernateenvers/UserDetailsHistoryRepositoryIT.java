package com.ns.springboothibernateenvers;

import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
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

/**
 * TODO: cleanup after each test. ATM you can only run a single test reliably!
 */

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

	@BeforeEach
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

	// Try the following: create userdetails with a certain address, then update the address. When I retrieve the original
	// userdetails - will it still have the old address? I sure hope so :)

	@Test
	@Disabled // This fails! Is this expected behaviour? Arguably yes. But how can I retrieve a version of the userdetails that points to the old address revision?
	void editReferredAddressUserDetailsStillPointsToOldAddress() {

		UserDetails userDetails = initTestData();

		final Address originalAddress = template.execute(status -> addressRepository.getById(1));


		template.executeWithoutResult(transactionStatus -> addressRepository.save(new Address(1, "Updated address")));

		template.executeWithoutResult(__ -> AuditReaderFactory.get(
				entityManager.getEntityManager()).getRevisions(Address.class, 1)
				.forEach(System.out::println));

		template.executeWithoutResult(__ -> {
			final UserDetails detailsFromDb = userRepository.getById(USER_ID);
			// FAILS!
			assertThat(detailsFromDb.getAddress().getAddress(), equalTo("Updated address"));
		});
	}

	@Test
	void doIUnderstandTheWayEnversWorksYet() {

		UserDetails userDetails = initTestData();

		final Address originalAddress = template.execute(status -> addressRepository.getById(1));


		template.executeWithoutResult(transactionStatus -> addressRepository.save(new Address(1, "Updated address")));

		template.executeWithoutResult(__ -> AuditReaderFactory.get(
						entityManager.getEntityManager()).getRevisions(Address.class, 1)
				.forEach(System.out::println));

		// Interesting, when going via the envers api, I get the old address for all 3 revisions
		template.executeWithoutResult(__ -> {
			userRepository.getTechnicalHistory(1).forEach(System.out::println);


			// When I do not go through envers, of course I get the address that's currently in the main table - which
			// is the updated one.
			System.out.println(userRepository.getById(1));

//			final Optional<UserDetails> detailsFromDb = userRepository.getAtTimestamp(1, Date.from(Instant.now().minus(70, ChronoUnit.MILLIS)));
//			assertThat(detailsFromDb.get().getAddress().getAddress(), equalTo("Montecuccoliplatz"));

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
