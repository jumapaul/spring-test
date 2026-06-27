package com.key_store_demo.springtest.customer;

public record CreateCustomerRequest(String name,
                                    String email,
                                    String address) {
}