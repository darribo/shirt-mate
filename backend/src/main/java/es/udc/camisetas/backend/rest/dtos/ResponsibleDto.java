package es.udc.camisetas.backend.rest.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información pública de un cliente responsable de un grupo de camisetas")
public class ResponsibleDto {

    @Schema(
        description="Identificador del responsable",
        example="1"
    )
    private Long id;

    @Schema(
        description="Nombre del responsable",
        example="Lucas"
    )
    private String name;

    @Schema(
        description="Apellido del responsable",
        example="Pérez"
    )
    private String surname;

    @Schema(
        description="Número de teléfono del responsable",
        example="612234567"
    )
    private String phoneNumber;

    @Schema(
        description="Identificador del cliente",
        example="1"
    )
    private Long customerId;

    public ResponsibleDto(Long id, String name, String surname, String phoneNumber, Long customerId) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.customerId = customerId;
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
