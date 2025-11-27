package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.daos.CustomerDao;
import es.udc.camisetas.backend.model.daos.RaffleDao;
import es.udc.camisetas.backend.model.daos.ShirtDao;
import es.udc.camisetas.backend.model.daos.ShirtTypeDao;
import es.udc.camisetas.backend.model.entities.*;
import es.udc.camisetas.backend.model.enums.Size;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.ShirtAlreadyBoughtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ShirtServiceImpl implements ShirtService {

    @Autowired
    Finder finder;

    @Autowired
    ShirtTypeDao shirtTypeDao;

    @Autowired
    private ShirtDao shirtDao;

    @Autowired
    private RaffleDao raffleDao;

    @Autowired
    private RaffleService raffleService;

    @Autowired
    private CustomerDao customerDao;

    @Override
    public ShirtType addShirtType(String name, String image, BigDecimal baseSalesPrice, String description, int freeShirtPeople, Long responsibleId) throws InstanceNotFoundException {

        Responsible responsible = null;

        if(responsibleId != null)
            responsible = finder.findResponsibleById(responsibleId);

        ShirtType shirtType = new ShirtType(name, image, baseSalesPrice, description, freeShirtPeople, responsible);

        return shirtTypeDao.save(shirtType);
    }

    @Override
    @Transactional(readOnly = true)
    public Block<ShirtType> searchShirtTypes(String name, int page, int size) {

        Slice<ShirtType> shirtTypes = shirtTypeDao.getByNameOrderByCustomersNumber(name, PageRequest.of(page, size));

        return new Block<>(shirtTypes.getContent(), shirtTypes.hasNext());

    }

    @Override
    @Transactional(readOnly = true)
    public List<ShirtType> getAllShirtTypes() {
        return shirtTypeDao.findAll((Sort.by(Sort.Direction.ASC, "name")));
    }

    @Override
    @Transactional(readOnly = true)
    public ShirtType getShirtType(Long shirtTypeId) throws InstanceNotFoundException {
        return finder.findByShirtTypeId(shirtTypeId);
    }

    @Override
    public ShirtType updateShirtType(Long shirtTypeId, String name, String image, BigDecimal baseSalesPrice, String description, int freeShirtPeople, Long responsibleId) throws InstanceNotFoundException {

        ShirtType shirtType = finder.findByShirtTypeId(shirtTypeId);

        Responsible responsible = null;

        if(responsibleId != null)
            responsible = finder.findResponsibleById(responsibleId);

        shirtType.setName(name);
        shirtType.setImage(image);
        shirtType.setBaseSalesPrice(baseSalesPrice);
        shirtType.setDescription(description);
        shirtType.setFreeShirtPeople(freeShirtPeople);
        shirtType.setResponsible(responsible);

        return shirtType;
    }

    @Override
    public void deleteShirtType(Long shirtTypeId) throws InstanceNotFoundException {

        ShirtType shirtType = finder.findByShirtTypeId(shirtTypeId);

        List<Shirt> shirts = shirtDao.findAllByShirtTypeId(shirtType.getId());

        for(Shirt shirt : shirts) {
            customerDao.delete(shirt.getCustomer());
        }

        shirtTypeDao.delete(shirtType);

    }

    //Tiene que ser la suma de los beneficios de las camisetas - el dinero que te has gastado en el sorteo.
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getShirtTypeRevenue(Long ShirtTypeId) throws InstanceNotFoundException {

        ShirtType shirtType = finder.findByShirtTypeId(ShirtTypeId);

        List<Shirt> shirts = shirtDao.findAllByShirtTypeId(shirtType.getId());

        BigDecimal revenue = new BigDecimal(0);

        for (Shirt shirt : shirts) {
            revenue = revenue.add(shirt.getProfit());
        }

        Optional<Raffle> auxRaffle = raffleDao.getRaffleByShirtTypeId(ShirtTypeId);

        if(auxRaffle.isPresent()) {
            Raffle raffle = auxRaffle.get();
            revenue = revenue.subtract(raffleService.getRafflePrice(raffle.getId()));
        }

        return revenue;
    }

    @Override
    @Transactional(readOnly = true)
    public int customersNumber(Long ShirtTypeId) throws InstanceNotFoundException {

        ShirtType shirtType = finder.findByShirtTypeId(ShirtTypeId);

       return shirtDao.countShirtsByShirtTypeId(shirtType.getId());
    }

    @Override
    public Shirt addShirt(BigDecimal purchasePrice, String size, Long ShirtTypeId, Long investorId, Long customerId) throws InstanceNotFoundException, DuplicateInstanceException {

        ShirtType shirtType =  finder.findByShirtTypeId(ShirtTypeId);

        Collaborator investor = finder.findCollaboratorById(investorId);

        //Todo: Se supone que antes de eso ya se ha incluido el customer en la BD. Ponerlo en el Controller
        Customer customer = finder.findCustomerById(customerId);

        validateCustomer(customer.getInstagram(), shirtType.getId());

        Size enumSize = Size.valueOf(size);

        Shirt shirt = new Shirt(purchasePrice, enumSize, shirtType, investor, customer);

        return shirtDao.save(shirt);


    }

    @Override
    @Transactional(readOnly = true)
    public Shirt getShirtById(Long shirtId) throws InstanceNotFoundException {
        return finder.findShirtById(shirtId);
    }

    @Override
    public Shirt updateShirt(Long shirtId, BigDecimal purchasePrice, BigDecimal salePrice, String size, Long shirtTypeId, Long investorId, Long customerId) throws InstanceNotFoundException, DuplicateInstanceException {

        Shirt shirt = finder.findShirtById(shirtId);

        ShirtType shirtType = finder.findByShirtTypeId(shirtTypeId);

        Collaborator investor = finder.findCollaboratorById(investorId);

        Customer customer = finder.findCustomerById(customerId);

        if(!Objects.equals(shirt.getId(), shirtId) && shirtDao.existsShirtByShirtTypeIdAndCustomerInstagram(shirtType.getId(), customer.getInstagram())){
            throw new DuplicateInstanceException("project.entities.shirt", customer.getInstagram());
        }

        Size enumSize = Size.valueOf(size);

        shirt.setPurchasePrice(purchasePrice);
        shirt.setSalePrice(salePrice);
        shirt.setSize(enumSize);
        shirt.setShirtType(shirtType);
        shirt.setInvestor(investor);
        shirt.setCustomer(customer);

        return shirt;
    }

    @Override
    public void deleteShirt(Long shirtId) throws InstanceNotFoundException {

        Shirt shirt = finder.findShirtById(shirtId);
        customerDao.delete(shirt.getCustomer());
    }

    @Override
    @Transactional(readOnly = true)
    public Block<Shirt> searchShirts(String keywords, Long shirtTypeId, Long investorId, String shirtSize, boolean isResponsible, int page, int size) {

        Slice<Shirt> slice = shirtDao.find(keywords, shirtTypeId, investorId, shirtSize, isResponsible, page, size);

        return new Block<>(slice.getContent(), slice.hasNext());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shirt> getConvincedFriendsShirt(Long shirtId) throws InstanceNotFoundException {

        Shirt shirt = finder.findShirtById(shirtId);

        List<Customer> convincedFriends = shirt.getCustomer().getConvincedFriends().stream().toList();

        List<Shirt> convincedFriendsShirts = new ArrayList<>();

        for (Customer convincedFriend : convincedFriends) {
            Optional<Shirt> optional = shirtDao.findShirtByShirtTypeIdAndCustomerId(shirt.getShirtType().getId(), convincedFriend.getId());
            if (optional.isPresent()) {
                Shirt convincedFriendShirt = optional.get();
                convincedFriendsShirts.add(convincedFriendShirt);
            }
        }
        return convincedFriendsShirts;
    }

    @Override
    @Transactional(readOnly = true)
    public int getConvincedFriendsNumber(Long shirtId) throws InstanceNotFoundException {
        return getConvincedFriendsShirt(shirtId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public int getConvincedFriendsPaid(Long shirtId) throws InstanceNotFoundException {
        int paidShirts = 0;
        List<Shirt> convincedFriendsShirts = getConvincedFriendsShirt(shirtId);

        for (Shirt convincedFriendShirt : convincedFriendsShirts) {
            if (convincedFriendShirt.isPurchased())
                paidShirts++;
        }

        return paidShirts;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isFree(Long shirtId) throws InstanceNotFoundException {
        Shirt shirt = finder.findShirtById(shirtId);
        int freeShirtPeople = shirt.getShirtType().getFreeShirtPeople();
        return freeShirtPeople != 0 && getConvincedFriendsPaid(shirtId) >= freeShirtPeople;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentShirtPrice(Long shirtId) throws InstanceNotFoundException {

        if(isFree(shirtId))
            return BigDecimal.ZERO;

        Shirt shirt = finder.findShirtById(shirtId);

        BigDecimal price = shirt.getShirtType().getBaseSalesPrice();

        Optional<Raffle> optional = raffleDao.getRaffleByShirtTypeId(shirt.getShirtType().getId());

        if(optional.isPresent()){
            Raffle raffle = optional.get();
            price = price.add(raffle.getParticipationPrice());
        }

        return price;
    }
    @Override
    @Transactional(readOnly = true)
    public boolean isBought(Long shirtId) throws InstanceNotFoundException {
        Shirt shirt = finder.findShirtById(shirtId);

        return shirt.getSalePrice() != null;
    }

    //Todo: Se supone que en la pantalla de comprar ya se ha asignado el precio de compra (por defecto aparecerá el de getCurrentShirtPrice)
    @Override
    public Shirt buy(Long shirtId, BigDecimal salePrice) throws InstanceNotFoundException, ShirtAlreadyBoughtException, DuplicateInstanceException {
        Shirt shirt = finder.findShirtById(shirtId);

        if(shirt.getSalePrice() != null){
            throw new ShirtAlreadyBoughtException(shirtId);
        }

        shirt = updateShirt(shirt.getId(), shirt.getPurchasePrice(), salePrice, shirt.getSize().toString(), shirt.getShirtType().getId(), shirt.getInvestor().getId(), shirt.getCustomer().getId());

        return shirt;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalProfit() throws InstanceNotFoundException {
        BigDecimal totalProfit = BigDecimal.ZERO;
        List<ShirtType> shirtTypes = shirtTypeDao.findAll(Sort.by(Sort.Direction.ASC, "name"));

        for (ShirtType shirtType : shirtTypes) {
            totalProfit = totalProfit.add(getShirtTypeRevenue(shirtType.getId()));
        }

        return totalProfit;
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCustomer(String instagram, Long shirtTypeId) throws InstanceNotFoundException, DuplicateInstanceException {

        ShirtType shirtType = finder.findByShirtTypeId(shirtTypeId);

        if(shirtDao.existsShirtByShirtTypeIdAndCustomerInstagram(shirtType.getId(), instagram)){
            throw new DuplicateInstanceException("project.entities.shirt", instagram);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Shirt getShirtByCustomerId(Long customerId) throws InstanceNotFoundException {
        Optional<Shirt> optional =  shirtDao.getShirtByCustomerId(customerId);

        if (optional.isPresent()) {
            return optional.get();
        }
        else {
            throw new InstanceNotFoundException("project.entities.shirt", customerId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Shirt getShirtByInstagram(String instagram) throws InstanceNotFoundException {
        Optional<Shirt> optional = shirtDao.getShirtByCustomerInstagram(instagram);

        if(optional.isPresent()){
            return optional.get();
        }
        else {
            throw new InstanceNotFoundException("project.entities.shirt", instagram);
        }
    }
}
