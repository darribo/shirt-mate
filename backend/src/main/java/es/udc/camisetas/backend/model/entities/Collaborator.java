package es.udc.camisetas.backend.model.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Collaborator {

    private Long id;
    private String name;
    private BigDecimal profitPercentage;

    public Collaborator() {}

    public Collaborator(String name, BigDecimal profitPercentage) {
        this.name = name;
        this.profitPercentage = profitPercentage;
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

    public BigDecimal getProfitPercentage() {
        return profitPercentage;
    }

    public void setProfitPercentage(BigDecimal profitPercentage) {
        this.profitPercentage = profitPercentage;
    }

}
