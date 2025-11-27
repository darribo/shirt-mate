package es.udc.camisetas.backend.model.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Customer {

    private Long id;
    private String instagram;
    private Customer convincingFriend;
    private Set<Customer> convincedFriends;

    public Customer() {}

    public Customer(String instagram) {
        this.instagram = instagram;
        this.convincingFriend = null;
        this.convincedFriends = new HashSet<>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstagram() {
        return instagram;
    }
    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="convincingFriendId")
    public Customer getConvincingFriend() {
        return convincingFriend;
    }

    public void setConvincingFriend(Customer convincingFriend) {
        this.convincingFriend = convincingFriend;
    }

    @OneToMany(mappedBy = "convincingFriend")
    public Set<Customer> getConvincedFriends() {
        return convincedFriends;
    }

    public void setConvincedFriends(Set<Customer> convincedFriends) {
        this.convincedFriends = convincedFriends;
    }

    // Función para añadir un amigo "convencido"
    public void addConvincedFriend(Customer convincedFriend) {
        convincedFriend.setConvincingFriend(this);
        this.convincedFriends.add(convincedFriend);
    }

    public void deleteConvincedFriend(Customer convincedFriend) {
        convincedFriend.setConvincingFriend(null);
        this.convincedFriends.remove(convincedFriend);
    }

    // Se cambia el convencedor de la instancia actual a nulo y se quita la instancia actual del conjunto de convencidos del convencedor
    public void deleteConvincingFriend(Customer convincingFriend) {
        this.convincingFriend = null;
        Set<Customer> convincedFriends = convincingFriend.getConvincedFriends();
        convincedFriends.remove(this);
        convincingFriend.setConvincedFriends(convincedFriends);

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(getId(), customer.getId()) && Objects.equals(getInstagram(), customer.getInstagram()) && Objects.equals(getConvincingFriend(), customer.getConvincingFriend());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getInstagram(), getConvincingFriend());
    }
}
