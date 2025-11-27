package es.udc.camisetas.backend.rest.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Parámetros necesarios para actualizar un tipo de camiseta")
public class UpdateShirtTypeParamsDto {

    @Schema(
        description="Nombre del grupo de la camiseta",
        example = "Grupo A"
    )
    private String name;

    @Schema(
        description="Url de la imagen del grupo de la camiseta",
        example="image/grupoA.png"
    )
    private String image;

    @Schema(
        description="Precio base que tendrá cada camiseta del grupo de camisetas",
        example="12.5"
    )
    private BigDecimal baseSalesPrice;

    @Schema(
        description="Descripción del grupo de camisetas",
        example="Grupo de camisetas de la chica de Santiago"
    )
    private String description;

    @Schema(
        description="Número de personas que tienen que venir de tu parte para llevarte gratis una camiseta de ese grupo",
        example="5"
    )
    private int freeShirtPeople;

    @Schema(
        description="Identificador del responsable del grupo de camisetas",
        example="1"
    )
    private Long responsibleId;

    public UpdateShirtTypeParamsDto(String name, String image, BigDecimal baseSalesPrice, String description, int freeShirtPeople, Long responsibleId) {
        this.name = name;
        this.image = image;
        this.baseSalesPrice = baseSalesPrice;
        this.description = description;
        this.freeShirtPeople = freeShirtPeople;
        this.responsibleId = responsibleId;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @NotNull
    @Min(value=0)
    @DecimalMax(value = "999999999.99")
    public BigDecimal getBaseSalesPrice() {
        return baseSalesPrice;
    }

    public void setBaseSalesPrice(BigDecimal baseSalesPrice) {
        this.baseSalesPrice = baseSalesPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull
    @Min(value=0)
    @Max(value=1000000000)
    public int getFreeShirtPeople() {
        return freeShirtPeople;
    }

    public void setFreeShirtPeople(int freeShirtPeople) {
        this.freeShirtPeople = freeShirtPeople;
    }

    public Long getResponsibleId() {
        return responsibleId;
    }

    public void setResponsibleId(Long responsibleId) {
        this.responsibleId = responsibleId;
    }
}
