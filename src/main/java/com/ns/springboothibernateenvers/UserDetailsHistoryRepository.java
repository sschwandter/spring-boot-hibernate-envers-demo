package com.ns.springboothibernateenvers;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserDetailsHistoryRepository {

	Optional<UserDetails> getAtTimestamp(Integer id, Date date);

	List<UserDetails> getTechnicalHistory(Integer id);
}
