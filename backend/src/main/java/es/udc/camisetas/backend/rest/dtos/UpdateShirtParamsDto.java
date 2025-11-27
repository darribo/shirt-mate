package es.udc.camisetas.backend.rest.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Parámetros necesarios para actualizar una camiseta")
public class UpdateShirtParamsDto {

    @Schema(
        description="Instagram del cliente que encargó la camiseta",
        example="@lucas.perezz"
    )
    private String instagram;

    @Schema(
        description="Precio que pagó el inversor que compró la camiseta",
        example="7.5"
    )
    private BigDecimal purchasePrice;

    @Schema(
        description="Precio que pagó el cliente",
        example = "12.5"
    )
    private BigDecimal salePrice;
    
    @Schema(
        description="Identificador del grupo de camisetas al que pertenece la camiseta",
        example="1"
    )
    private Long shirtTypeId;

    @Schema(
        description="Identificador del inversor que compró la camiseta",
        example="1"
    )
    private Long investorId;

    @Schema(
        description="Talla de la camiseta",
        example="L"
    )
    private String size;

    @Schema(
        description="En caso de que el cliente venga de parte de un amigo, su identificador",
        example="1"
    )
    private Long convincingFriendId;

    @Schema(
        description="Booleano que indica si el cliente es el responsable de su grupo de camisetas"
    )
    private boolean isResponsible;

    @Schema(
        description="Identificador como responsable de la camiseta",
        example="1"
    )
    private Long responsibleId;

    @Schema(
        description="Nombre como responsable de la camiseta",
        example="Lucas"
    )
    private String name;

    @Schema(
        description="Apellido como responsable de la camiseta",
        example="Pérez"
    )
    private String surname;

    @Schema(
        description="Teléfono del responsable de la camiseta",
        example="612234567"
    )
    private String phoneNumber;

    public UpdateShirtParamsDto(String instagram, BigDecimal purchasePrice, BigDecimal salePrice, Long shirtTypeId, Long investorId, String size, Long convincingFriendId, boolean isResponsible, Long responsibleId, String name, String surname, String phoneNumber) {
        this.instagram = instagram;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.shirtTypeId = shirtTypeId;
        this.investorId = investorId;
        this.size = size;
        this.convincingFriendId = convincingFriendId;
        this.isResponsible = isResponsible;
        this.responsibleId = responsibleId;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
    }


    @AssertTrue(message = "El nombre del responsable debe ser obligatorio")
    public boolean isNameValidIfResponsible() {
        if (isResponsible) {
            return name != null && !name.trim().isEmpty();
        }
        return true;
    }

    @NotNull
    @Pattern(regexp = "^@.*", message = "El instagram debe empezar con @")
    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    @NotNull
    @Min(value=0)
    @DecimalMax(value = "999999999.99")
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    @Min(value=0)
    @DecimalMax(value = "999999999.99")
    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    @NotNull
    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }

    @NotNull
    public Long getShirtTypeId() {
        return shirtTypeId;
    }

    public void setShirtTypeId(Long shirtTypeId) {
        this.shirtTypeId = shirtTypeId;
    }

    @NotNull
    @Pattern(regexp = "XS|S|M|L|XL", message = "Talla inválida")
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Long getConvincingFriendId() {
        return convincingFriendId;
    }

    public void setConvincingFriendId(Long convincingFriendId) {
        this.convincingFriendId = convincingFriendId;
    }

    @NotNull
    public boolean isResponsible() {
        return isResponsible;
    }

    public void setResponsible(boolean responsible) {
        isResponsible = responsible;
    }

    public Long getResponsibleId() {
        return responsibleId;
    }

    public void setResponsibleId(Long responsibleId) {
        this.responsibleId = responsibleId;
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

}
