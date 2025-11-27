package es.udc.camisetas.backend.rest.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información pública de una camiseta")
public class ShirtDto {

    @Schema(
        description="Identificador de la camiseta",
        example="1"
    )
    private Long id;

    @Schema(
        description="Precio por el que el inversor compró la camiseta",
        example="7.5"
    )
    private BigDecimal purchasePrice;

    @Schema(
        description="Precio por el que se vendió la camiseta al cliente",
        example="12.5"
    )
    private BigDecimal salePrice;

    @Schema(
        description="Talla de la camiseta",
        example="L"
    )
    private String size;

    @Schema(
        description="Identificador del cliente que encargó la camiseta",
        example="1"
    )
    private Long customerId;

    @Schema(
        description="Instagram del cliente que encargó la camiseta",
        example="@lucass.perez"
    )
    private String customerInstagram;

    @Schema(
        description="En caso de que el cliente viniese de parte de un amigo, su identificador",
        example="1"
    )
    private Long convincingFriendId;

    @Schema(
        description="En caso de que el cliente viniese de parte de un amigo, su instagram",
        example="@sergio.suarezzz"
    )
    private String convincingFriendInstagram;

    @Schema(
        description="Identificador del inversor que asumió la compra de la camiseta",
        example="1"
    )
    private Long investorId;
    
    @Schema(
        description="Identificador del grupo de camisetas al que pertenece la camiseta",
        example="1"
    )
    private Long shirtTypeId;

    @Schema(
        description="Nombre del grupo de camisetas al que pertenece la camiseta",
        example="Grupo A"
    )
    private String shirtTypeName;

    @Schema(
        description="Booleano que indica si el cliente que encargó la camiseta ya la ha pagado"
    )
    private boolean bought;

    public ShirtDto(Long id, BigDecimal purchasePrice, BigDecimal salePrice, String size, Long customerId, Long convincingFriendId, String convincingFriendInstagram, String customerInstagram, Long investorId, Long shirtTypeId, String shirtTypeName, boolean bought) {
        this.id = id;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.size = size;
        this.customerId = customerId;
        this.convincingFriendId = convincingFriendId;
        this.convincingFriendInstagram = convincingFriendInstagram;
        this.customerInstagram = customerInstagram;
        this.investorId = investorId;
        this.shirtTypeId = shirtTypeId;
        this.shirtTypeName = shirtTypeName;
        this.bought = bought;
    }


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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerInstagram() {
        return customerInstagram;
    }

    public void setCustomerInstagram(String customerInstagram) {
        this.customerInstagram = customerInstagram;
    }

    public Long getConvincingFriendId() {
        return convincingFriendId;
    }

    public void setConvincingFriendId(Long convincingFriendId) {
        this.convincingFriendId = convincingFriendId;
    }

    public String getConvincingFriendInstagram() {
        return convincingFriendInstagram;
    }

    public void setConvincingFriendInstagram(String convincingFriendInstagram) {
        this.convincingFriendInstagram = convincingFriendInstagram;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }

    public Long getShirtTypeId() {
        return shirtTypeId;
    }

    public void setShirtTypeId(Long shirtTypeId) {
        this.shirtTypeId = shirtTypeId;
    }

    public String getShirtTypeName() {
        return shirtTypeName;
    }

    public void setShirtTypeName(String shirtTypeName) {
        this.shirtTypeName = shirtTypeName;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

}
