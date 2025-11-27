package es.udc.camisetas.backend.model.entities;

import es.udc.camisetas.backend.model.enums.Size;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Shirt {

    private Long id;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Size size;
    private ShirtType ShirtType;
    private Collaborator investor;
    private Customer customer;

    public Shirt() {}

    public Shirt(BigDecimal purchasePrice, Size size, ShirtType shirtType, Collaborator investor, Customer customer) {
        this.purchasePrice = purchasePrice;
        this.salePrice = null;
        this.size = size;
        this.ShirtType = shirtType;
        this.investor = investor;
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

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    @Enumerated(EnumType.STRING)
    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shirtTypeId")
    public ShirtType getShirtType() {
        return ShirtType;
    }

    public void setShirtType(ShirtType shirtType) {
        this.ShirtType = shirtType;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "investorId")
    public Collaborator getInvestor() {
        return investor;
    }

    public void setInvestor(Collaborator investor) {
        this.investor = investor;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId")
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Transient
    public BigDecimal getProfit() {
        if(salePrice == null)
            return purchasePrice.multiply(new BigDecimal(-1));
        else
            return salePrice.subtract(purchasePrice);
    }

    @Transient
    public boolean isPurchased() {
        return salePrice != null;
    }
}
