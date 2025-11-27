package es.udc.camisetas.backend.model.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class ShirtType {

    private Long id;
    private String name;
    private String image;
    private BigDecimal baseSalesPrice;
    private String description;
    private int freeShirtPeople;
    private Responsible responsible;

    public ShirtType() {}

    public ShirtType(String name, String image, BigDecimal baseSalesPrice, String description, int freeShirtPeople, Responsible responsible) {
        this.name = name;
        this.image = image;
        this.baseSalesPrice = baseSalesPrice;
        this.description = description;
        this.freeShirtPeople = freeShirtPeople;
        this.responsible = responsible;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public BigDecimal getBaseSalesPrice() {
        return baseSalesPrice;
    }

    public void setBaseSalesPrice(BigDecimal baseSalesPrice) {
        this.baseSalesPrice = baseSalesPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFreeShirtPeople() {
        return freeShirtPeople;
    }

    public void setFreeShirtPeople(int freeTShirtPeople) {
        this.freeShirtPeople = freeTShirtPeople;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsibleId")
    public Responsible getResponsible() {
        return responsible;
    }

    public void setResponsible(Responsible responsible) {
        this.responsible = responsible;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShirtType that = (ShirtType) o;
        return getFreeShirtPeople() == that.getFreeShirtPeople() && Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getImage(), that.getImage()) && Objects.equals(getBaseSalesPrice(), that.getBaseSalesPrice()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getResponsible(), that.getResponsible());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getImage(), getBaseSalesPrice(), getDescription(), getFreeShirtPeople(), getResponsible());
    }
}
