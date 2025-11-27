package es.udc.camisetas.backend.model.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class Level {
    private Long id;
    private String levelDescription;
    private BigDecimal price; //Precio que nos tenemos que gastar
    private int necessaryParticipants; //Número de participantes que lo activa
    private Customer winner;
    private Raffle raffle;

    public Level() {}

    public Level(String levelDescription, BigDecimal price, int necessaryParticipants) {
        this.levelDescription = levelDescription;
        this.price = price;
        this.necessaryParticipants = necessaryParticipants;
        this.winner = null;
        this.raffle = null;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLevelDescription() {
        return levelDescription;
    }

    public void setLevelDescription(String description) {
        this.levelDescription = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getNecessaryParticipants() {
        return necessaryParticipants;
    }

    public void setNecessaryParticipants(int necessaryParticipants) {
        this.necessaryParticipants = necessaryParticipants;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="winnerId")
    public Customer getWinner() {
        return winner;
    }

    public void setWinner(Customer winner) {
        this.winner = winner;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="raffleId")
    public Raffle getRaffle() {
        return raffle;
    }

    public void setRaffle(Raffle raffle) {
        this.raffle = raffle;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Level level = (Level) o;
        return getNecessaryParticipants() == level.getNecessaryParticipants() && Objects.equals(getId(), level.getId()) && Objects.equals(getLevelDescription(), level.getLevelDescription()) && Objects.equals(getPrice(), level.getPrice()) && Objects.equals(getWinner(), level.getWinner()) && Objects.equals(getRaffle(), level.getRaffle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLevelDescription(), getPrice(), getNecessaryParticipants(), getWinner(), getRaffle());
    }
}
