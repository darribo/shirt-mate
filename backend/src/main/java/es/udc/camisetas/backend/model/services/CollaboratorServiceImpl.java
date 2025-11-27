package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.daos.CollaboratorDao;
import es.udc.camisetas.backend.model.daos.ShirtDao;
import es.udc.camisetas.backend.model.entities.Collaborator;
import es.udc.camisetas.backend.model.entities.Shirt;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.InvalidPercentageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class CollaboratorServiceImpl implements CollaboratorService {

    @Autowired
    CollaboratorDao collaboratorDao;

    @Autowired
    ShirtDao shirtDao;

    @Autowired
    Finder finder;
    @Autowired
    private ShirtService shirtService;

    @Autowired
    private CustomerService customerService;

    @Override
    @Transactional(readOnly = true)
    public boolean validatePercentage(Long id, BigDecimal profitPercentage) {
        BigDecimal totalPorcentage = profitPercentage;

        List<Collaborator> collaborators = collaboratorDao.findAll(Sort.by(Sort.Direction.ASC, "name"));

        for(Collaborator collaborator : collaborators){
            totalPorcentage = totalPorcentage.add(collaborator.getProfitPercentage());
        }

        try{
            Collaborator collaborator = finder.findCollaboratorById(id);
            totalPorcentage = totalPorcentage.subtract(collaborator.getProfitPercentage());

        } catch (InstanceNotFoundException ignored) {

        }
        return totalPorcentage.compareTo(BigDecimal.valueOf(100)) <= 0;
    }

    //Todo: Comprobar en el servicio que profitPercentage esté entre 0 y 100
    @Override
    public Collaborator addCollaborator(String name, BigDecimal profitPercentage) throws InvalidPercentageException {

        if (!validatePercentage(null, profitPercentage))
            throw new InvalidPercentageException();

        Collaborator collaborator = new Collaborator(name, profitPercentage);

        return collaboratorDao.save(collaborator);

    }

    @Override
    @Transactional(readOnly = true)
    public Block<Collaborator> searchCollaborators(String name, int page, int size) {

        Slice<Collaborator> collaborators = collaboratorDao.findByNameOrderByProfitPercentage(name, PageRequest.of(page, size));
        return new Block<>(collaborators.getContent(), collaborators.hasNext());
    }


    @Override
    @Transactional(readOnly = true)
    public Collaborator getCollaborator(Long collaboratorId) throws InstanceNotFoundException {
        return finder.findCollaboratorById(collaboratorId);
    }

    @Override
    public Collaborator updateCollaborator(Long collaboratorId, String name, BigDecimal profitPercentage) throws InstanceNotFoundException, InvalidPercentageException {

        if (!validatePercentage(collaboratorId, profitPercentage))
            throw new InvalidPercentageException();

        Collaborator collaborator = finder.findCollaboratorById(collaboratorId);


        collaborator.setName(name);
        collaborator.setProfitPercentage(profitPercentage);

        return collaborator;
    }

    @Override
    public void deleteCollaborator(Long collaboratorId) throws InstanceNotFoundException {

        Collaborator collaborator = finder.findCollaboratorById(collaboratorId);

        Slice<Shirt> shirts = shirtDao.getShirtsByInvestorId(collaborator.getId());

        for (Shirt shirt : shirts.getContent()) {
            customerService.deleteCustomer(shirt.getCustomer().getId());
        }

        collaboratorDao.delete(collaborator);

    }

    @Override
    @Transactional(readOnly = true)
    public int getNumberOfBoughtShirts(Long collaboratorId) throws InstanceNotFoundException {

        Collaborator collaborator = finder.findCollaboratorById(collaboratorId);
        Slice<Shirt> boughtShirts = shirtDao.getShirtsByInvestorId(collaborator.getId());

        return boughtShirts.getContent().size();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getInvestment(Long collaboratorId) throws InstanceNotFoundException {
        BigDecimal investment = BigDecimal.ZERO;
        Collaborator collaborator = finder.findCollaboratorById(collaboratorId);
        Slice<Shirt> boughtShirts = shirtDao.getShirtsByInvestorId(collaborator.getId());

        for(Shirt shirt : boughtShirts.getContent()){
            investment = investment.add(shirt.getPurchasePrice());
        }

        return investment.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Collaborator> getAllCollaborators() {
        return collaboratorDao.findAll((Sort.by(Sort.Direction.ASC, "name")));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal returnedInvestment(Long collaboratorId) throws InstanceNotFoundException {
        Collaborator collaborator = finder.findCollaboratorById(collaboratorId);

        List<Collaborator> collaborators = getAllCollaborators();

        BigDecimal totalProfit = shirtService.getTotalProfit();

        if (totalProfit.compareTo(BigDecimal.ZERO) >= 0)
            return getInvestment(collaborator.getId());

        else {
            BigDecimal totalSpent = BigDecimal.ZERO;

            for(Collaborator collaboratorSpent : collaborators){
                totalSpent = totalSpent.add(getInvestment(collaboratorSpent.getId()));
            }

            BigDecimal percentageInvestment = getInvestment(collaborator.getId()).divide(totalSpent, 2, RoundingMode.HALF_UP);

            BigDecimal totalSold = totalProfit.add(totalSpent);

            return totalSold.multiply(percentageInvestment).setScale(2, RoundingMode.HALF_UP);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getProfit(Long collaboratorId) throws InstanceNotFoundException {

        Collaborator collaborator = finder.findCollaboratorById(collaboratorId);

        BigDecimal totalProfit = shirtService.getTotalProfit();

        if (totalProfit.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        else {
            return (totalProfit.multiply((collaborator.getProfitPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))).setScale(2, RoundingMode.HALF_UP);
        }
    }

}
