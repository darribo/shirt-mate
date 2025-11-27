package es.udc.camisetas.backend.rest.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Parámetros necesarios para crear un grupo de camisetas")
public class AddShirtTypeParamsDto {

    @Schema(
        description="Nombre del grupo de camisetas",
        example="Grupo A"
    )
    private String name;

    @Schema(
        description="Url de la imagen del grupo de camisetas",
        example="images/GrupoA.png"
    )
    private String image;

    @Schema(
        description="Precio base al que se venderá cada camiseta del grupo",
        example="12.5"
    )
    private BigDecimal baseSalesPrice;

    @Schema(
        description="Descripción del grupo de camisetas",
        example="Grupo del amigo de Sergio"
    )
    private String description;

    @Schema(
        description="Número de personas que tienen que venir de tu parte para llevarte gratis una camiseta de ese grupo",
        example="5"
    )
    private int freeShirtPeople;


    public AddShirtTypeParamsDto(String name, String image, BigDecimal baseSalesPrice, String description, int freeShirtPeople) {
        this.name = name;
        this.image = image;
        this.baseSalesPrice = baseSalesPrice;
        this.description = description;
        this.freeShirtPeople = freeShirtPeople;
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
}
