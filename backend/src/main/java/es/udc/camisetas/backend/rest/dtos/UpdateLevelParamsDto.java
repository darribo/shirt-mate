package es.udc.camisetas.backend.rest.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Parámetros necesarios para actualizar un nivel")
public class UpdateLevelParamsDto {

    @Schema(
        description="Descripción del nivel",
        example="Nivel 2 del sorteo del grupo A"
    )
    private String levelDescription;

    @Schema(
        description="Precio que cuesta el premio que se entregará al ganador del nivel",
        example="10.5"
    )
    private BigDecimal price; //Precio que nos tenemos que gastar

    @Schema(
        description="Número mínimo de participantes que son necesarios para que se active el nivel",
        example="20"
    )
    private int necessaryParticipants; //Número de participantes que lo activa


    public UpdateLevelParamsDto(String levelDescription, BigDecimal price, int necessaryParticipants) {
        this.levelDescription = levelDescription;
        this.price = price;
        this.necessaryParticipants = necessaryParticipants;
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
}
