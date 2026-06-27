package com.key_store_demo.springtest.customer;

import com.key_store_demo.springtest.AbstractTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.test.autoconfigure.AutoConfigureRestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestClient
class CustomerControllerTest extends AbstractTestContainer {

    @LocalServerPort
    private int port;

    private RestClient restClient;
    String uri = "/api/v1/customers";

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }


    @Test
    void shouldCreateCustomer() {
        //Given
        CreateCustomerRequest customerRequest = new CreateCustomerRequest(
                "Leon",
                "leon@gmail.com",
                "US"
        );

        //when & then
        ResponseEntity<Void> bodilessEntity = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(customerRequest)
                .retrieve()
                .toBodilessEntity();

        assertThat(bodilessEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<List<Customer>> response = restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Customer customerCreated = Objects.requireNonNull(response.getBody())
                .stream()
                .filter(c -> c.getEmail().equals(customerRequest.email()))
                .findFirst()
                .orElseThrow();

        assertThat(customerCreated.getName()).isEqualTo(customerRequest.name());
        assertThat(customerCreated.getEmail()).isEqualTo(customerRequest.email());
        assertThat(customerCreated.getAddress()).isEqualTo(customerRequest.address());
    }


    @Test
    void updateCustomer() {
        //Given
        CreateCustomerRequest request = new CreateCustomerRequest(
                "james",
                "james@gmail.com",
                "US"
        );

        String name = "Paul";
        String email = "juma@gmail.com";
        String address = "UK";

        ResponseEntity<Void> createdCustomer = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        assertThat(createdCustomer.getStatusCode()).isEqualTo(HttpStatus.OK);

        //GetAllCustomers
        ResponseEntity<List<Customer>> allCustomers = restClient.get().uri(uri).retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        Long id = Objects.requireNonNull(allCustomers.getBody())
                .stream()
                .filter(c -> c.getEmail().equals(request.email()))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        assertThat(createdCustomer.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> updateCustomerById = restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(uri + "/" + id)
                        .queryParam("name", name)
                        .queryParam("email", email)
                        .queryParam("address", address)
                        .build()
                )
                .retrieve()
                .toBodilessEntity();

        assertThat(updateCustomerById.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Get customer By id
        ResponseEntity<Customer> customerById = restClient.get().uri(uri + "/" + id)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        assertThat(customerById.getStatusCode()).isEqualTo(HttpStatus.OK);

        Customer updateCustomer = Objects.requireNonNull(customerById.getBody());

        assertThat(updateCustomer.getEmail()).isEqualTo(email);
        assertThat(updateCustomer.getName()).isEqualTo(name);
        assertThat(updateCustomer.getAddress()).isEqualTo(address);
    }

    @Test
    void deleteCustomer() {
        //Given
        CreateCustomerRequest request = new CreateCustomerRequest(
                "leon",
                "leon@gmail.com",
                "Us"
        );

        //then
        ResponseEntity<Customer> response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(Customer.class);

        assertThat(response.getStatusCode().equals(HttpStatus.OK));

        //Get all Customers
        ResponseEntity<List<Customer>> allCustomer = restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        Long id = Objects.requireNonNull(allCustomer.getBody())
                .stream()
                .filter(c -> c.getEmail().equals(request.email()))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        ResponseEntity<Void> bodilessEntity = restClient.delete()
                .uri(uri + "/" + id)
                .retrieve()
                .toBodilessEntity();

        assertThat(bodilessEntity.getStatusCode().is2xxSuccessful()).isTrue();

        assertThatThrownBy(() ->
                restClient.get()
                        .uri(uri + "/" + id)
                        .retrieve()
                        .toEntity(Customer.class)
        )
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}