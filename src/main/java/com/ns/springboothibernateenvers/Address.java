package com.ns.springboothibernateenvers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@Audited
@AllArgsConstructor
@NoArgsConstructor
public class Address {

	@Id
	private Integer addressId;

	String address;
}
