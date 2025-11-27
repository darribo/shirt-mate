package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.daos.LevelDao;
import es.udc.camisetas.backend.model.daos.RaffleDao;
import es.udc.camisetas.backend.model.daos.ShirtDao;
import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.Raffle;
import es.udc.camisetas.backend.model.entities.Shirt;
import es.udc.camisetas.backend.model.entities.ShirtType;
import es.udc.camisetas.backend.model.entities.Level;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.NoParticipantsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class RaffleServiceImpl implements RaffleService {

    @Autowired
    private Finder finder;

    @Autowired
    private RaffleDao raffleDao;

    @Autowired
    private ShirtDao shirtDao;

    @Autowired
    private LevelDao levelDao;

    @Override
    public Raffle addRaffle(BigDecimal participationPrice, String description, Long shirtTypeId) throws InstanceNotFoundException, DuplicateInstanceException {

        ShirtType shirtType = finder.findByShirtTypeId(shirtTypeId);

        if (raffleDao.existsRaffleByShirtTypeId(shirtTypeId))
            throw new DuplicateInstanceException("project.entities.raffle", "un sorteo para " + shirtType.getName());

        Raffle raffle = new Raffle(participationPrice, description, shirtType);

        return raffleDao.save(raffle);

    }

    @Override
    public Raffle getRaffle(Long raffleId) throws InstanceNotFoundException {
        return finder.findRaffleById(raffleId);
    }

    @Override
    public Raffle updateRaffle(Long raffleId, BigDecimal participationPrice, String description, Long shirtTypeId) throws InstanceNotFoundException, DuplicateInstanceException {

        Raffle raffle = finder.findRaffleById(raffleId);

        ShirtType shirtType = finder.findByShirtTypeId(shirtTypeId);

        if (!Objects.equals(raffle.getShirtType().getId(), shirtType.getId()) && raffleDao.existsRaffleByShirtTypeId(shirtTypeId)) {
            throw new DuplicateInstanceException("project.entities.raffle", "un sorteo para" + shirtType.getName());
        }

        raffle.setParticipationPrice(participationPrice);
        raffle.setDescription(description);
        raffle.setShirtType(shirtType);

        return raffle;
    }

    @Override
    public void deleteRaffle(Long raffleId) throws InstanceNotFoundException {

        Raffle raffle = finder.findRaffleById(raffleId);

        raffleDao.delete(raffle);

    }

    private List<Shirt> getParticipants(Raffle raffle) {

        ShirtType shirtType = raffle.getShirtType();

        List<Shirt> participants = shirtDao.findAllByShirtTypeId(shirtType.getId());

        participants.removeIf(participant -> !participant.isPurchased());

        return participants;
    }

    @Override
    @Transactional(readOnly = true)
    public int getParticipantsNumber(Long raffleId) throws InstanceNotFoundException {
        Raffle raffle = finder.findRaffleById(raffleId);

        return getParticipants(raffle).size();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRafflePrice(Long raffleId) throws InstanceNotFoundException {
        Raffle raffle = finder.findRaffleById(raffleId);

        BigDecimal price = BigDecimal.ZERO;

        for (Level level : raffle.getBattlePass()) {
            if(levelRaised(level.getId()))
                price = price.add(level.getPrice());
        }
        return price;
    }

    @Override
    @Transactional(readOnly = true)
    public Raffle getRaffleByShirtTypeId(Long shirtTypeId) throws InstanceNotFoundException{
        Optional<Raffle> optional = raffleDao.getRaffleByShirtTypeId(shirtTypeId);

        if (optional.isPresent()) {
            return optional.get();
        }
        else {
            throw new InstanceNotFoundException("project.entities.shirtType", shirtTypeId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsRaffleByShirtTypeId(Long shirtTypeId) {
        return raffleDao.existsRaffleByShirtTypeId(shirtTypeId);
    }

    @Override
    public Level addLevel(String description, BigDecimal price, int neccessaryParticipants, Long raffleId) throws InstanceNotFoundException {

        Raffle raffle = finder.findRaffleById(raffleId);

        Level level = new Level(description, price, neccessaryParticipants);
        raffle.addLevel(level);

        return levelDao.save(level);
    }

    @Override
    @Transactional(readOnly = true)
    public Level getLevel(Long levelId) throws InstanceNotFoundException {
        return finder.findLevelById(levelId);
    }

    @Override
    public Level updateLevel(Long levelId, String description, BigDecimal price, int neccessaryParticipants) throws InstanceNotFoundException {

        Level level = finder.findLevelById(levelId);

        level.setLevelDescription(description);
        level.setPrice(price);
        level.setNecessaryParticipants(neccessaryParticipants);

        if (level.getWinner() != null && getParticipantsNumber(level.getRaffle().getId()) < neccessaryParticipants)
            level.setWinner(null);

        return level;
    }

    @Override
    public void removeLevel(Long levelId) throws InstanceNotFoundException {
        Level level = finder.findLevelById(levelId);
        level.getRaffle().removeLevel(level);
        levelDao.delete(level);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean levelRaised(Long levelId) throws InstanceNotFoundException {
        Level level = finder.findLevelById(levelId);
        return getParticipantsNumber(level.getRaffle().getId()) >= level.getNecessaryParticipants();
    }

    //Se pasa el conjunto por si se quiere sortear sólo algunos niveles
    @Override
    public Level play(Long levelId) throws InstanceNotFoundException, NoParticipantsException {

        Level level = finder.findLevelById(levelId);

        if(!levelRaised(levelId)){
            throw new NoParticipantsException(levelId);
        }

        Raffle raffle = level.getRaffle();

        List<Shirt> participants = getParticipants(raffle);

        if(participants.isEmpty()) {
            throw new NoParticipantsException(raffle.getId());
        }

        level.setWinner(null);
        Customer winner;

        do {
            Random random = new Random();
            int randomIndex = random.nextInt(participants.size());
            Shirt shirtWinner = participants.get(randomIndex);
            winner = shirtWinner.getCustomer();

        } while (levelDao.existsLevelByRaffleIdAndWinnerId(raffle.getId(), winner.getId()));

        level.setWinner(winner);

        return level;

    }
}
