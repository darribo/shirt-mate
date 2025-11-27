package es.udc.camisetas.backend.rest.dtos;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información pública de un sorteo")
public class RaffleDto {

    @Schema(
        description="Identificador del sorteo",
        example="1"
    )
    private Long id;

    @Schema(
        description="Precio que hay que pagar para poder participar en el sorteo",
        example="1.5"
    )
    private BigDecimal participationPrice;

    @Schema(
        description="Descripción del sorteo",
        example="Sorteo del grupo A"
    )
    private String description;

    @Schema(
        description="Identificador del grupo al que pertenece",
        example="1"
    )
    private Long shirtTypeId;

    @Schema(
        description="Lista con los niveles del sorteo"
    )
    private List<LevelDto> battlePass;

    public RaffleDto(Long id, BigDecimal participationPrice, String description, Long shirtTypeId, List<LevelDto> battlePass) {
        this.id = id;
        this.participationPrice = participationPrice;
        this.description = description;
        this.shirtTypeId = shirtTypeId;
        this.battlePass = battlePass;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getShirtTypeId() {
        return shirtTypeId;
    }

    public void setShirtTypeId(Long shirtTypeId) {
        this.shirtTypeId = shirtTypeId;
    }

    public List<LevelDto> getBattlePass() {
        return battlePass;
    }

    public void setBattlePass(List<LevelDto> battlePass) {
        this.battlePass = battlePass;
    }
}
