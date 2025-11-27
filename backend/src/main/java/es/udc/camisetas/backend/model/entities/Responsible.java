package es.udc.camisetas.backend.model.entities;

import jakarta.persistence.*;

@Entity
public class Responsible {

    private Long id;
    private String name;
    private String surname;
    private String phoneNumber;
    private Customer customer;

    public Responsible() {}

    public Responsible(String name, String surname, String phoneNumber, Customer customer) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.customer = customer;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customerId")
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
