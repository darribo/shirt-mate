package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.daos.*;
import es.udc.camisetas.backend.model.entities.*;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FinderImpl implements Finder {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CollaboratorDao collaboratorDao;

    @Autowired
    private ShirtTypeDao shirtTypeDao;

    @Autowired
    private RaffleDao raffleDao;

    @Autowired
    private ShirtDao shirtDao;

    @Autowired LevelDao levelDao;
    @Autowired
    private ResponsibleDao responsibleDao;


    @Override
    public Customer findCustomerById(Long customerId) throws InstanceNotFoundException {

        if (customerId == null)
            throw new InstanceNotFoundException("project.entities.customer",  customerId);

        Optional<Customer> customer = customerDao.findById(customerId);

        if(customer.isEmpty()) {
            throw new InstanceNotFoundException("project.entities.customer",  customerId);
        }

        return customer.get();
    }

    @Override
    public Collaborator findCollaboratorById(Long collaboratorId) throws InstanceNotFoundException {

        if(collaboratorId == null)
            throw new InstanceNotFoundException("project.entities.collaborator",  collaboratorId);

        Optional<Collaborator> collaborator = collaboratorDao.findById(collaboratorId);

        if(collaborator.isEmpty()) {
            throw new InstanceNotFoundException("project.entities.collaborator",  collaboratorId);
        }

        return collaborator.get();
    }

    @Override
    public Responsible findResponsibleById(Long responsibleId) throws InstanceNotFoundException {

        if (responsibleId == null)
            throw new InstanceNotFoundException("project.entities.responsible", responsibleId);

        Optional<Responsible> responsible = responsibleDao.findById(responsibleId);

        if(responsible.isEmpty()) {
            throw new InstanceNotFoundException("project.entities.responsible",  responsibleId);
        }

        return responsible.get();
    }

    @Override
    public ShirtType findByShirtTypeId(Long shirtTypeId) throws InstanceNotFoundException {

        if(shirtTypeId == null)
            throw new InstanceNotFoundException("project.entities.shirtType", shirtTypeId);

        Optional<ShirtType> shirtType = shirtTypeDao.findById(shirtTypeId);

        if(shirtType.isEmpty()) {
            throw new InstanceNotFoundException("project.entities.shirtType", shirtTypeId);
        }

        return shirtType.get();
    }

    @Override
    public Raffle findRaffleById(Long raffleId) throws InstanceNotFoundException {

        if(raffleId == null)
            throw new InstanceNotFoundException("project.entities.raffle", raffleId);

        Optional<Raffle> raffle = raffleDao.findById(raffleId);

        if(raffle.isEmpty()) {
            throw new InstanceNotFoundException("project.entities.raffle", raffleId);
        }

        return raffle.get();
    }

    @Override
    public Shirt findShirtById(Long shirtId) throws InstanceNotFoundException {

        if(shirtId == null)
            throw new InstanceNotFoundException("project.entities.shirt", shirtId);

        Optional<Shirt> shirt = shirtDao.findById(shirtId);

        if(shirt.isEmpty()) {
            throw new InstanceNotFoundException("project.entities.shirt", shirtId);
        }

        return shirt.get();
    }

    @Override
    public Level findLevelById(Long levelId) throws InstanceNotFoundException {

        if(levelId == null)
            throw new InstanceNotFoundException("project.entities.level", levelId);

        Optional<Level> level = levelDao.findById(levelId);

        if(level.isEmpty()) {
            throw new InstanceNotFoundException("project.entities.level", levelId);
        }

        return level.get();
    }
}
