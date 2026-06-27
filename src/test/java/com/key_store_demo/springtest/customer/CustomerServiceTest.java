package com.key_store_demo.springtest.customer;

import com.key_store_demo.springtest.exception.CustomerEmailUnavailableException;
import com.key_store_demo.springtest.exception.CustomerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    CustomerService underTest;

    @Mock
    CustomerRepository customerRepository;

    @Captor
    ArgumentCaptor<Customer> customerArgumentCaptor;

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerRepository);
    }

    @Test
    void shouldGetAllCustomers() {
        underTest.getCustomers();

        verify(customerRepository).findAll();
    }

    @Test
    void shouldThrowNotFoundWhenGivenInvalidIdWhileGetCustomerById() {
        Long id = 1L;

        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                underTest.getCustomerById(id))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer with id " + id + " doesn't found");
    }

    @Test
    void shouldGetCustomerById() {
        //Given
        Long id = 1L;
        String name = "leon";
        String email = "leon@gmail.com";
        String address = "UK";

        Customer customer = new Customer(
                id, name, email, address
        );

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        //When
        Customer customerById = underTest.getCustomerById(id);

        //Then
        assertThat(customerById.getId()).isEqualTo(id);
        assertThat(customerById.getName()).isEqualTo(name);
        assertThat(customerById.getEmail()).isEqualTo(email);
        assertThat(customerById.getAddress()).isEqualTo(address);
    }

    @Test
    void shouldCreateCustomer() {
        //given
        CreateCustomerRequest request = new CreateCustomerRequest(
                "leon",
                "leon@gmail.com",
                "US"
        );
        //when
        underTest.createCustomer(request);
        //then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer customerCaptured = customerArgumentCaptor.getValue();

        //verify the value passed here is same as what we pass in the service
        assertEquals(customerCaptured.getName(), request.name());
        assertEquals(customerCaptured.getEmail(), request.email());
        assertEquals(customerCaptured.getAddress(), request.address());
    }

    @Test
    void shouldNotCreateCustomerAndThrowExceptionWhenCustomerFindByEmailIsPresent() {
        //given
        CreateCustomerRequest request = new CreateCustomerRequest(
                "leon",
                "leon@gmail.com",
                "US"
        );

        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(new Customer())); //Makes our find by email to true.
        //when
        //then
        assertThatThrownBy(() ->
                underTest.createCustomer(request))
                .isInstanceOf(CustomerEmailUnavailableException.class)
                .hasMessage("The email " + request.email() + " unavailable.");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenIdNotFound() {
        //Given
        Long id = 1L;
        String name = "leon";
        String email = "leon@gmail.com";
        String address = "US";

        when(customerRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                underTest.updateCustomer(id, name, email, address))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer with id " + 1L + " doesn't found");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldOnlyUpdateCustomerName() {
        //Given
        Long id = 1L;
        Customer customer = new Customer(
                id,
                "leon",
                "leon@gmail.com",
                "US"
        );
        String newName = "juma";
        when(customerRepository.findById(id))
                .thenReturn(Optional.of(customer));

        //when
        underTest.updateCustomer(id, newName, null, null);

        //then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(newName);
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getAddress()).isEqualTo(customer.getAddress());
    }

    @Test
    void shouldThrowEmailUnavailableWhenGivenEmailAlreadyPresentedWhileUpdateCustomer() {
        //Given
        long id = 5L;
        Customer customer = Customer.create(
                id,
                "leon",
                "leon@gmail.com",
                "US"
        );
        String newEmail = "leonaldo@gmail.com";

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        when(customerRepository.findByEmail(newEmail)).thenReturn(Optional.of(new Customer()));

        assertThatThrownBy(() ->
                underTest.updateCustomer(id, null, newEmail, null))
                .isInstanceOf(CustomerEmailUnavailableException.class)
                .hasMessage("The email " + newEmail + " unavailable to update");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldOnlyUpdateEmail() {
        Long id = 1L;
        Customer customer = new Customer(
                id,
                "leon",
                "leon@gmail.com",
                "US"
        );
        String newEmail = "juma@gmail.com";

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        //when
        underTest.updateCustomer(id, null, newEmail, null);

        //then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(newEmail);
        assertThat(capturedCustomer.getAddress()).isEqualTo(customer.getAddress());
    }

    @Test
    void shouldOnlyUpdateAddress() {
        //Given
        Long id = 1L;
        Customer customer = new Customer(
                id,
                "leon",
                "leon@gmail.com",
                "Uk"
        );

        String newAddress = "US";
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        //when
        underTest.updateCustomer(id, null, null, newAddress);

        //then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getAddress()).isEqualTo(newAddress);
    }

    @Test
    void updateAllAttributes() {

        //Given
        Long id = 1L;
        Customer customer = new Customer(
                id,
                "leon",
                "leon@gmail.com",
                "US"
        );

        String newName = "newName";
        String email = "new@gmail.com";
        String address = "UK";

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        //When
        underTest.updateCustomer(id, newName, email, address);

        //Then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(newName);
        assertThat(capturedCustomer.getEmail()).isEqualTo(email);
        assertThat(capturedCustomer.getAddress()).isEqualTo(address);
    }

    @Test
    void shouldThrowExceptionWhenIdDoesNotExist() {
        Long id = 1L;
        //Given
        when(customerRepository.existsById(id)).thenReturn(false);
        assertThatThrownBy(() ->
                underTest.deleteCustomer(id))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer with id " + id + " doesn't exist.");

        verify(customerRepository, never()).deleteById(any());
    }

    @Test
    void shouldDeleteCustomer() {
        Long id = 1L;
        when(customerRepository.existsById(id)).thenReturn(true);

        underTest.deleteCustomer(id);
        verify(customerRepository).deleteById(id);
    }
}