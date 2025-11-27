package es.udc.camisetas.backend.rest.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Parámetros necesarios para crear un nivel")
public class AddLevelParamsDto {

    @Schema(
        description="Descripción en la que se explica el nivel",
        example="Nivel 3 para el grupo A"
    )
    private String levelDescription;

    @Schema(
        description="Precio que costará el premio que se entregará al ganador del nivel",
        example="12.5"
    )
    private BigDecimal price; //Precio que nos tenemos que gastar

    @Schema(
        description="Número de participantes para un grupo de camiseta a partir del cual se entra en ese nivel",
        example="20"
    )
    private int necessaryParticipants; //Número de participantes que lo activa

    @Schema(
        description="Id del sorteo al que pertenece",
        example="1"
    )
    private Long raffleId;

    public AddLevelParamsDto(String levelDescription, BigDecimal price, int necessaryParticipants, Long raffleId) {
        this.levelDescription = levelDescription;
        this.price = price;
        this.necessaryParticipants = necessaryParticipants;
        this.raffleId = raffleId;
    }

    public String getLevelDescription() {
        return levelDescription;
    }

    public void setLevelDescription(String levelDescription) {
        this.levelDescription = levelDescription;
    }

    @NotNull
    @Min(value=0)
    @DecimalMax(value = "999999999.99")
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @NotNull
    @Min(value=0)
    @Max(value=999999999)
    public int getNecessaryParticipants() {
        return necessaryParticipants;
    }

    public void setNecessaryParticipants(int necessaryParticipants) {
        this.necessaryParticipants = necessaryParticipants;
    }

    @NotNull
    public Long getRaffleId() {
        return raffleId;
    }

    public void setRaffleId(Long raffleId) {
        this.raffleId = raffleId;
    }
}
