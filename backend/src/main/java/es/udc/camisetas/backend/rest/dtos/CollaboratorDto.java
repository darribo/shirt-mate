package es.udc.camisetas.backend.rest.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Información pública de un colaborador/inversor")
public class CollaboratorDto {

    @Schema(
        description = "Identificador del colaborador",
        example = "12"
    )
    private Long id;

    @Schema(
        description = "Nombre del colaborador",
        example = "María López"
    )
    private String name;

    @Schema(
        description = "Porcentaje de beneficio asignado al colaborador",
        example = "15.5"
    )
    private BigDecimal profitPercentage;

    public CollaboratorDto(Long id, String name, BigDecimal profitPercentage) {
        this.id = id;
        this.name = name;
        this.profitPercentage = profitPercentage;
    }

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

    public BigDecimal getProfitPercentage() {
        return profitPercentage;
    }

    public void setProfitPercentage(BigDecimal profitPercentage) {
        this.profitPercentage = profitPercentage;
    }
}
