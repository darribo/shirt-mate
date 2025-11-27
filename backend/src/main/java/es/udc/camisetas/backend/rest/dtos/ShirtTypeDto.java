package es.udc.camisetas.backend.rest.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información pública de un grupo de camisetas")
public class ShirtTypeDto {

    @Schema(
        description="Identificador del grupo de camisetas",
        example="1"
    )
    private Long id;

    @Schema(
        description="Nombre del grupo de camisetas",
        example="Grupo A"
    )
    private String name;

    @Schema(
        description="Url de la imagen del grupo de camisetas",
        example="images/grupoA.png"
    )
    private String image;

    @Schema(
        description="Precio base a partir del cual se venderá cada camiseta del grupo de camisetas",
        example="12.5"
    )
    private BigDecimal baseSalesPrice;

    @Schema(
        description="Descripción del grupo de camisetas",
        example="Grupo de camisetas de la chica de Ourense"
    )
    private String description;

    @Schema(
        description="Número de personas que tienen que venir de tu parte para llevarte gratis una camiseta de ese grupo",
        example="5"
    )
    private int freeShirtPeople;

    @Schema(
        description="Identificador del cliente que es responsable del grupo de camisetas",
        example="1"
    )
    private Long responsibleId;

    public ShirtTypeDto(Long id, String name, String image, BigDecimal baseSalesPrice, String description, int freeShirtPeople, Long responsibleId) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.baseSalesPrice = baseSalesPrice;
        this.description = description;
        this.freeShirtPeople = freeShirtPeople;
        this.responsibleId = responsibleId;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

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
