package es.udc.camisetas.backend.rest.dtos;


import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información pública de un nivel")
public class LevelDto {

    @Schema(
        description="Identificador del nivel",
        example="1"
    )
    private Long id;

    @Schema(
        description="Descripción del nivel",
        example="Nivel 2 del grupo A"
    )
    private String levelDescription;

    @Schema(
        description="Precio que costará el premio que se entregará al ganador del nivel",
        example="1.5"
    )
    private BigDecimal price; //Precio que nos tenemos que gastar

    @Schema(
        description="Número de participantes para un grupo de camiseta a partir del cual se entra en ese nivel",
        example="20"
    )
    private int necessaryParticipants; //Número de participantes que lo activa
    
    @Schema(
        description="Identificador del ganador del nivel",
        example="1"
    )
    private Long winnerId;

    @Schema(
        description="Instagram del ganador del nivel",
        example="1"
    )
    private String winnerInstagram;

    @Schema(
        description="Identificador del sorteo al que pertenece el nivel",
        example="1"
    )
    private Long raffleId;

    public LevelDto(Long id, String levelDescription, BigDecimal price, int necessaryParticipants, Long winnerId, String winnerInstagram, Long raffleId) {
        this.id = id;
        this.levelDescription = levelDescription;
        this.price = price;
        this.necessaryParticipants = necessaryParticipants;
        this.winnerId = winnerId;
        this.winnerInstagram = winnerInstagram;
        this.raffleId = raffleId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLevelDescription() {
        return levelDescription;
    }

    public void setLevelDescription(String levelDescription) {
        this.levelDescription = levelDescription;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getNecessaryParticipants() {
        return necessaryParticipants;
    }

    public void setNecessaryParticipants(int necessaryParticipants) {
        this.necessaryParticipants = necessaryParticipants;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerInstagram() {
        return winnerInstagram;
    }

    public void setWinnerInstagram(String winnerInstagram) {
        this.winnerInstagram = winnerInstagram;
    }

    public Long getRaffleId() {
        return raffleId;
    }

    public void setRaffleId(Long raffleId) {
        this.raffleId = raffleId;
    }
}
