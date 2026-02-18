package com.ecom.model;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class OrderRequest {

	private String firstName;

	private String lastName;

	private String email;

	private String mobileNo;

	private String address;
	//cambio de city
	private String district;
	//cambio de state
	private String department;

	private String pincode;
	
	private String paymentType;

}
