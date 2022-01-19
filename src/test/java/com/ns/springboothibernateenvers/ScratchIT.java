package com.ns.springboothibernateenvers;

import org.hibernate.envers.AuditReader;
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

import javax.persistence.EntityManager;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@AutoConfigureTestEntityManager
public class ScratchIT {

	public static final int ADDRESS_ID = 1;
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
	@Disabled
	void minimalTest() {
		UserDetails currUserDetails = userRepository.getById(1);
		assertThat(currUserDetails, equalTo(new UserDetails(1, "Updated Name", "Updated Last name", new Address(1, "Montecuccoliplatz"))));

		AuditReader auditReader = AuditReaderFactory.get(entityManager.getEntityManager());

		final UserDetails originalUserDetails = auditReader.find(UserDetails.class, 1, 2);
		assertThat(originalUserDetails, equalTo(new UserDetails(1, "NIRAJ", "SONAWANE", new Address(1, "Montecuccoliplatz"))));
	}

	@Test
		//@Disabled
	void mininimalSetup() {

		final Address montecuccoliplatz = new Address(ADDRESS_ID, "Montecuccoliplatz");

		UserDetails stefan = new UserDetails(USER_ID, "Stefan", "Schwandter", montecuccoliplatz);

		// Need to write initial data in one transaction ...
		template.execute(status -> {
			addressRepository.save(montecuccoliplatz);
			Address fromDb = addressRepository.getById(ADDRESS_ID);
			assertThat(fromDb, equalTo(montecuccoliplatz));
//			return 0;
//		});

//		template.execute(status ->
//		{
			userRepository.save(stefan);

			UserDetails userDetailsFromDb = userRepository.getById(USER_ID);

			assertThat(userDetailsFromDb, equalTo(stefan));
			return 0;
		});

		// Correct first name - need to write in separate transaction so that envers creates new revision
		UserDetails stefanCorrected = new UserDetails(USER_ID, "Stefan Mario", "Schwandter", montecuccoliplatz);
		template.execute(status -> {
			userRepository.save(stefanCorrected);
			UserDetails userDetailsFromDbAfterCorrection = userRepository.getById(USER_ID);
			assertThat(userDetailsFromDbAfterCorrection, equalTo(new UserDetails(USER_ID, "Stefan Mario", "Schwandter", montecuccoliplatz)));
			return 0;
		});

		template.execute(status -> {
			final EntityManager entityManager = this.entityManager.getEntityManager();
			final AuditReader auditReader = AuditReaderFactory.get(entityManager);
			final List<Number> revisions = auditReader.getRevisions(UserDetails.class, 1);
//			return 0;
//		});

			// But we want the older version
//		template.execute(status -> {
			final UserDetails previousRevison = auditReader.find(UserDetails.class, USER_ID, 1);
			assertThat(previousRevison, equalTo(stefan));
			return 0;
		});

	}

}
