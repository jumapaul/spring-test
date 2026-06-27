package com.key_store_demo.springtest.customer;

import com.key_store_demo.springtest.AbstractTestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest extends AbstractTestContainer {


    @Autowired
    CustomerRepository underTest;

    @BeforeEach
    void setUp() {
        String email = "abc@gmail.com";

        Customer customer = Customer.create(
                "abc",
                email,
                "US"
        );

        underTest.save(customer);
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void shouldReturnCustomerWhenFindByEmail() {

        //When
        Optional<Customer> customerByEmail = underTest.findByEmail("abc@gmail.com");

        //Then
        assertTrue(customerByEmail.isPresent());
    }

    @Test
    void shouldNotReturnCustomerWhenFindByEmail() {

        Optional<Customer> customerEmail = underTest.findByEmail("def@gmail.com");

        assertFalse(customerEmail.isPresent());
    }
}