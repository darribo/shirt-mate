package es.udc.camisetas.backend.rest.dtos;

import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.Responsible;
import es.udc.camisetas.backend.model.entities.Shirt;
import es.udc.camisetas.backend.model.entities.ShirtType;

import java.util.ArrayList;
import java.util.List;

public class ShirtConversor {

    private ShirtConversor() {}

    public final static ShirtTypeDto toShirtTypeDto(ShirtType shirtType) {

        Responsible responsible = shirtType.getResponsible();

        return new ShirtTypeDto(shirtType.getId(), shirtType.getName(), shirtType.getImage(), shirtType.getBaseSalesPrice(), shirtType.getDescription(), shirtType.getFreeShirtPeople(), responsible != null ? responsible.getId() : null);

    }

    public final static ShirtDto toShirtDto(Shirt shirt) {

        Customer customer = shirt.getCustomer();
        ShirtType shirtType =  shirt.getShirtType();
        Customer convincedFriend = customer != null ? customer.getConvincingFriend() : null;


        return new ShirtDto(shirt.getId(), shirt.getPurchasePrice(), shirt.getSalePrice(), shirt.getSize().toString(),
                customer != null ? customer.getId() : null, convincedFriend != null ? convincedFriend.getId() : null,
                convincedFriend != null ? convincedFriend.getInstagram() : null, customer != null ? customer.getInstagram() : null,
                shirt.getInvestor().getId(), shirtType.getId(), shirtType.getName(), shirt.getSalePrice() != null);

    }

    public final static List<ShirtTypeDto> toShirtTypeDtos(List<ShirtType> shirtTypes) {
        List<ShirtTypeDto> shirtTypeDtos = new ArrayList<>();

        for(ShirtType shirtType : shirtTypes) {
            shirtTypeDtos.add(toShirtTypeDto(shirtType));
        }

        return shirtTypeDtos;
    }

    public final static List <ShirtDto> toShirtDtos(List<Shirt> shirts) {
        List<ShirtDto> shirtDtos = new ArrayList<>();

        for(Shirt shirt : shirts) {
            shirtDtos.add(toShirtDto(shirt));
        }

        return shirtDtos;
    }

    public final static ResponsibleDto toResponsibleDto(Responsible responsible) {

        return new ResponsibleDto(responsible.getId(), responsible.getName(), responsible.getSurname(), responsible.getPhoneNumber(), responsible.getCustomer().getId());

    }
}
