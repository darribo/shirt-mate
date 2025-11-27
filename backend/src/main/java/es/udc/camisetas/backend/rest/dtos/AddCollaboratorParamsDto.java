package es.udc.camisetas.backend.rest.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Parámetros necesarios para crear o actualizar un colaborador")
public class AddCollaboratorParamsDto {

    @Schema(
        description = "Nombre del colaborador",
        example = "Juan Pérez"
    )
    private String name;

    @Schema(
        description = "Porcentaje de beneficio asignado al colaborador. Debe ser >= 0 y la suma total no puede superar el 100%.",
        example = "12.5"
    )
    private BigDecimal profitPercentage;


    public AddCollaboratorParamsDto(String name, BigDecimal profitPercentage) {
        this.name = name;
        this.profitPercentage = profitPercentage;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    @Min(0)
    @DecimalMax(value = "100.00")
    public BigDecimal getProfitPercentage() {
        return profitPercentage;
    }

    public void setProfitPercentage(BigDecimal profitPercentage) {
        this.profitPercentage = profitPercentage;
    }
}
