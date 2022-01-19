package com.ns.springboothibernateenvers;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class UserDetailsHistoryRepositoryImpl implements UserDetailsHistoryRepository {

	@PersistenceContext
	EntityManager entityManager;

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
