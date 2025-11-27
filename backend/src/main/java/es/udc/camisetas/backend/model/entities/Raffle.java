package es.udc.camisetas.backend.model.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Raffle {

    private Long id;
    private BigDecimal participationPrice;
    private String description;
    private ShirtType shirtType;
    private Set<Level> battlePass;

    public Raffle() {}

    public Raffle(BigDecimal participationPrice, String description, ShirtType shirtType) {
        this.participationPrice = participationPrice;
        this.description = description;
        this.shirtType = shirtType;
        this.battlePass = new HashSet<>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getParticipationPrice() {
        return participationPrice;
    }

    public void setParticipationPrice(BigDecimal participationPrice) {
        this.participationPrice = participationPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="shirtTypeId")
    public ShirtType getShirtType() {
        return shirtType;
    }

    public void setShirtType(ShirtType shirtType) {
        this.shirtType = shirtType;
    }

    @OneToMany(mappedBy = "raffle")
    public Set<Level> getBattlePass() {
        return battlePass;
    }

    public void setBattlePass(Set<Level> battlePass) {
        this.battlePass = battlePass;
    }

    public void addLevel(Level level) {
        this.battlePass.add(level);
        level.setRaffle(this);
    }

    public void removeLevel(Level level) {
        this.battlePass.remove(level);
        level.setRaffle(null);
    }
}
