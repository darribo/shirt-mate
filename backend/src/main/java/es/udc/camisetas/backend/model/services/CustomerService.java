package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.Responsible;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;

public interface CustomerService {

    Customer addCustomer(String instagram, Long convincingFriendId) throws InstanceNotFoundException;

    Customer getCustomer(Long customerId) throws InstanceNotFoundException;

    Customer getCustomerByInstagram(String instagram) throws InstanceNotFoundException;

    Customer updateCustomer(Long customerId, String instagram, Long convincingFriendId) throws InstanceNotFoundException;

    void deleteCustomer(Long customerId) throws InstanceNotFoundException;

    void deleteConvincedFriend(Long customerId, Long convincedFriendId) throws InstanceNotFoundException;

    Responsible addResponsible(String name, String surname, String phoneNumber, Long customerId) throws InstanceNotFoundException;

    Responsible getResponsible(Long responsibleId) throws InstanceNotFoundException;

    boolean isResponsible(Long customerId);

    Responsible getResponsibleByCustomerId(Long customerId) throws InstanceNotFoundException;

    Responsible updateResponsible(Long responsibleId, String name, String surname, String phoneNumber) throws InstanceNotFoundException;

    void deleteResponsible(Long responsibleId) throws InstanceNotFoundException;

}
