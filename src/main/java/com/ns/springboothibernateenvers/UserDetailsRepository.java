package com.ns.springboothibernateenvers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer>, RevisionRepository<UserDetails, Integer, Integer>, UserDetailsHistoryRepository {}
