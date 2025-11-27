package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.daos.CustomerDao;
import es.udc.camisetas.backend.model.daos.ResponsibleDao;
import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.Responsible;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private Finder finder;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private ResponsibleDao responsibleDao;

    @Override
    public Customer addCustomer(String instagram, Long convincingFriendId) throws InstanceNotFoundException {

        Customer customer = new Customer(instagram);

        if (convincingFriendId != null) {
            Customer convincingFriend = finder.findCustomerById(convincingFriendId);
            convincingFriend.addConvincedFriend(customer);
        }

        return customerDao.save(customer);

    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomer(Long customerId) throws InstanceNotFoundException {
        return finder.findCustomerById(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByInstagram(String instagram) throws InstanceNotFoundException {

        Optional<Customer> customer = customerDao.findCustomerByInstagram(instagram);

        if(customer.isPresent())
            return customer.get();
        else
            throw new InstanceNotFoundException("project.entities.customer", instagram);
    }


    @Override
    public Customer updateCustomer(Long customerId, String instagram, Long convincingFriendId) throws InstanceNotFoundException {

        Customer customer = finder.findCustomerById(customerId);

        customer.setInstagram(instagram);

        // Si no era nulo y se quiere poner a nulo se borra. Si ya era nulo y se quiere poner a nulo no se hace nada
        if (convincingFriendId == null) {
            if (customer.getConvincingFriend() != null)
                customer.deleteConvincingFriend(customer.getConvincingFriend());
        }

        // Si se quiere cambiar a uno que no sea nulo, se borra el antiguo y se añade el nuevo
        else {
            Customer newConvincingFriend = finder.findCustomerById(convincingFriendId);

            if(customer.getConvincingFriend() != null)
                customer.deleteConvincingFriend(customer.getConvincingFriend());

            newConvincingFriend.addConvincedFriend(customer);
        }
        return customer;
    }

    @Override
    public void deleteCustomer(Long customerId) throws InstanceNotFoundException {

        Customer customer = finder.findCustomerById(customerId);

        customerDao.delete(customer);

    }

    @Override
    public void deleteConvincedFriend(Long customerId, Long convincedFriendId) throws InstanceNotFoundException {

        if (customerId == null || convincedFriendId == null)
            throw new InstanceNotFoundException("project.entities.customer", customerId);

        Customer customer = finder.findCustomerById(customerId);
        Customer convincedFriend = finder.findCustomerById(convincedFriendId);

        if(convincedFriend.getConvincingFriend() != null && convincedFriend.getConvincingFriend().equals(customer))
            customer.deleteConvincedFriend(convincedFriend);

        else
            throw new InstanceNotFoundException("project.entities.customer", customerId);

    }

    @Override
    public Responsible addResponsible(String name, String surname, String phoneNumber, Long customerId)
            throws InstanceNotFoundException {

        Customer customer = finder.findCustomerById(customerId);

        Responsible responsible = new Responsible(name, surname, phoneNumber, customer);

        return responsibleDao.save(responsible);
    }

    @Override
    @Transactional(readOnly = true)
    public Responsible getResponsible(Long responsibleId) throws InstanceNotFoundException {
        return finder.findResponsibleById(responsibleId);
    }

    @Override
    @Transactional(readOnly = true)

    public boolean isResponsible(Long customerId) {
        Optional<Responsible> responsible = responsibleDao.getResponsibleByCustomerId(customerId);

        return responsible.isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Responsible getResponsibleByCustomerId(Long customerId) throws InstanceNotFoundException {
        Optional<Responsible> responsible = responsibleDao.getResponsibleByCustomerId(customerId);

        if(responsible.isPresent())
            return responsible.get();
        else
            throw new InstanceNotFoundException("project.entities.customer", customerId);
    }

    @Override
    public Responsible updateResponsible(Long responsibleId, String name, String surname, String phoneNumber) throws InstanceNotFoundException {

        Responsible responsible = getResponsible(responsibleId);
        responsible.setName(name);
        responsible.setSurname(surname);
        responsible.setPhoneNumber(phoneNumber);

        return responsible;
    }

    @Override
    public void deleteResponsible(Long responsibleId) throws InstanceNotFoundException {
        Responsible responsible = getResponsible(responsibleId);
        responsibleDao.delete(responsible);
    }

}
