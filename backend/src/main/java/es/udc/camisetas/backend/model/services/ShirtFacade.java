package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.Responsible;
import es.udc.camisetas.backend.model.entities.Shirt;
import es.udc.camisetas.backend.model.entities.ShirtType;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.ShirtTypeHasAlreadyAResponsibleException;
import es.udc.camisetas.backend.rest.dtos.AddShirtParamsDto;
import es.udc.camisetas.backend.rest.dtos.UpdateShirtParamsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShirtFacade {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ShirtService shirtService;

    public Shirt addShirtWithCustomer(AddShirtParamsDto params) throws InstanceNotFoundException, DuplicateInstanceException, ShirtTypeHasAlreadyAResponsibleException {

        Shirt shirt;

        ShirtType shirtType = shirtService.getShirtType(params.getShirtTypeId());

        shirtService.validateCustomer(params.getInstagram(), params.getShirtTypeId());

        if (params.isResponsible()) {

            if (shirtType.getResponsible() != null){
                throw new ShirtTypeHasAlreadyAResponsibleException(shirtType.getId());
            }

            Customer customer = customerService.addCustomer(params.getInstagram(), params.getConvincingFriendId());

            Responsible responsible = customerService.addResponsible(params.getName(), params.getSurname(), params.getPhoneNumber(), customer.getId());

            shirtType = shirtService.updateShirtType(shirtType.getId(), shirtType.getName(), shirtType.getImage(), shirtType.getBaseSalesPrice(), shirtType.getDescription(), shirtType.getFreeShirtPeople(), responsible.getId());

            shirt = shirtService.addShirt(params.getPurchasePrice(), params.getSize(), shirtType.getId(), params.getInvestorId(), customer.getId());
        }

        else {

            Customer customer = customerService.addCustomer(params.getInstagram(), params.getConvincingFriendId());

            shirt = shirtService.addShirt(params.getPurchasePrice(), params.getSize(), shirtType.getId(), params.getInvestorId(), customer.getId());

        }

        return shirt;
    }

    public Shirt updateShirtWithCustomer(Long id, UpdateShirtParamsDto params) throws InstanceNotFoundException, DuplicateInstanceException, ShirtTypeHasAlreadyAResponsibleException {

        Shirt shirt = shirtService.getShirtById(id);
        ShirtType shirtType = shirtService.getShirtType(params.getShirtTypeId());
        Customer customer = customerService.updateCustomer(shirt.getCustomer().getId(), params.getInstagram(), params.getConvincingFriendId());

        //En caso de que sea responsable: Se admite añadir un responsable nuevo o modificar los datos del que ya había
        if(params.isResponsible()) {

            Responsible responsible;

            //En caso de que la camiseta tenga responsable
            try {
                responsible = customerService.getResponsible(params.getResponsibleId());

                //En caso de que se intente cambiar al antiguo responsable salta la excepción
                if (shirtType.getResponsible() != null && !shirtType.getResponsible().equals(responsible)) {
                    throw new ShirtTypeHasAlreadyAResponsibleException(shirtType.getId());
                }

                //La camiseta tiene responsable y es la del id de los parámetros: Se procede a cambiarlo
                responsible = customerService.updateResponsible(params.getResponsibleId(), params.getName(), params.getSurname(), params.getPhoneNumber());
            }

            //En caso de que la camiseta no tenga responsable: Se añade uno
            catch (InstanceNotFoundException e) {

                responsible = customerService.addResponsible(params.getName(), params.getSurname(), params.getPhoneNumber(), customer.getId());
            }

            shirtType = shirtService.updateShirtType(shirtType.getId(), shirtType.getName(), shirtType.getImage(), shirtType.getBaseSalesPrice(), shirtType.getDescription(), shirtType.getFreeShirtPeople(), responsible.getId());
            shirt = shirtService.updateShirt(id, params.getPurchasePrice(), params.getSalePrice(), params.getSize(), shirtType.getId(), params.getInvestorId(), customer.getId());
        }

        //En caso de que no sea responsable
        else {
            if(shirtType.getResponsible() != null && shirtType.getResponsible().getId().equals(params.getResponsibleId())) {
                customerService.deleteResponsible(params.getResponsibleId());
            }
            shirt = shirtService.updateShirt(id, params.getPurchasePrice(), params.getSalePrice(), params.getSize(), params.getShirtTypeId(), params.getInvestorId(), customer.getId());
        }

        return shirt;
    }
}
