package com.ns.springboothibernateenvers;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class UserDetailsHistoryRepository {

	@PersistenceContext
	EntityManager entityManager;

	final UserDetailsRepository userDetailsRepository;

	public UserDetailsHistoryRepository(UserDetailsRepository userDetailsRepository) {
		this.userDetailsRepository = userDetailsRepository;
	}

	public Optional<UserDetails> getCurrent(Integer id) {
		return userDetailsRepository.findById(id);
	}

	public Optional<UserDetails> getAtTimestamp(Integer id, Date date) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		return Optional.ofNullable(auditReader.find(UserDetails.class, id, date));
	}

	public List<UserDetails> getTechnicalHistory(Integer id) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);

		return auditReader.createQuery()
				.forRevisionsOfEntity(UserDetails.class, true, false)
				.add(AuditEntity.id().eq(id)).getResultList();
	}
}
