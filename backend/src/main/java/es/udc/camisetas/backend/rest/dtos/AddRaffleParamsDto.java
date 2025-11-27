package es.udc.camisetas.backend.rest.dtos;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Parámetros necesarios para crear o actualizar un sorteo")
public class AddRaffleParamsDto {

    @Schema(
        description="Precio que hay que pagar para participar en el sorteo",
        example="1.5"
    )
    private BigDecimal participationPrice;

    @Schema(
        description="Descripción del sorteo",
        example="Sorteo para el grupo A"
    )
    private String description;

    @Schema(
        description="Id del grupo de camiseta al que está asociado el sorteo",
        example="1"
    )
    private Long shirtTypeId;

    public AddRaffleParamsDto(BigDecimal participationPrice, String description, Long shirtTypeId) {
        this.participationPrice = participationPrice;
        this.description = description;
        this.shirtTypeId = shirtTypeId;
    }

    @NotNull
    @Min(value=0)
    @DecimalMax(value = "999999999.99")
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

    @NotNull
    public Long getShirtTypeId() {
        return shirtTypeId;
    }

    public void setShirtTypeId(Long shirtTypeId) {
        this.shirtTypeId = shirtTypeId;
    }
}
