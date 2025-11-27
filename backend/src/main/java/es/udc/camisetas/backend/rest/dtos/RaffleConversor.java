package es.udc.camisetas.backend.rest.dtos;

import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.Level;
import es.udc.camisetas.backend.model.entities.Raffle;
import es.udc.camisetas.backend.model.entities.ShirtType;

import java.util.ArrayList;
import java.util.List;

public class RaffleConversor {

    private RaffleConversor() {}

    public final static RaffleDto toRaffleDto(Raffle raffle) {
        ShirtType shirtType = raffle.getShirtType();

        List<LevelDto> battlePass = new ArrayList<>();

        for (Level aux : raffle.getBattlePass().stream().toList()) {
            battlePass.add(toLevelDto(aux));
        }
        return new RaffleDto(raffle.getId(), raffle.getParticipationPrice(), raffle.getDescription(), shirtType != null ? shirtType.getId() : null, battlePass);
    }

    public final static LevelDto toLevelDto(Level level) {

        Customer winner = level.getWinner();
        Raffle raffle = level.getRaffle();

        return new LevelDto(level.getId(), level.getLevelDescription(), level.getPrice(), level.getNecessaryParticipants(), winner != null ? winner.getId() : null, winner != null ? winner.getInstagram() : null, raffle != null ? raffle.getId() : null);
    }
}
