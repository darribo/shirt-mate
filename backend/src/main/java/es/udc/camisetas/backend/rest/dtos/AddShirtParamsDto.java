package es.udc.camisetas.backend.rest.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description="Parámetros necesarios para registrar que un vliente ha comprado una camiseta")
public class AddShirtParamsDto {

    @Schema(
        description="Instagram del cliente que encarga la camiseta",
        example="@lucass.perez"
    )
    private String instagram;

    @Schema(
        description="Precio por el que el inversor compró la camiseta",
        example="12.5"
    )
    private BigDecimal purchasePrice;

    @Schema(
        description="Identificador del grupo de camiseta al que pertenece la camiseta",
        example="1"
    )
    private Long shirtTypeId;

    @Schema(
        description="Identificador del inversor que asume la compra de la camiseta",
        example="1"
    )
    private Long investorId;
    
    @Schema(
        description = "Talla seleccionada por el cliente",
        example = "L",
        allowableValues = {"XS", "S", "M", "L", "XL"}
    )
    private String size;

    @Schema(
        description="En caso de que el cliente venga de parte de un amigo, su identificador",
        example="1"
    )
    private Long convincingFriendId;

    @Schema(
        description="Booleano que indica si el cliente es el responsable de su grupo"
    )
    private boolean isResponsible;

    @Schema(
        description="Nombre del cliente",
        example="Lucas"
    )
    private String name;

    @Schema(
        description="Apellido del cliente",
        example="Pérez"
    )
    private String surname;

    @Schema(
        description="Teléfono del cliente",
        example="612234567"
    )
    private String phoneNumber;

    public AddShirtParamsDto(String instagram, BigDecimal purchasePrice, Long investorId, String size, Long shirtTypeId, Long convincingFriendId,
                             boolean isResponsible, String name, String surname, String phoneNumber) {
        this.instagram = instagram;
        this.purchasePrice = purchasePrice;
        this.investorId = investorId;
        this.size = size;
        this.convincingFriendId = convincingFriendId;
        this.shirtTypeId = shirtTypeId;
        this.isResponsible = isResponsible;
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

    @Pattern(regexp = "\\+?[0-9]{9,15}", message = "Número de teléfono no válido")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
