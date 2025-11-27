package es.udc.camisetas.backend.rest.dtos;

import es.udc.camisetas.backend.model.entities.Collaborator;

import java.util.ArrayList;
import java.util.List;

public class CollaboratorConversor {

    private CollaboratorConversor() {}

    public final static CollaboratorDto toCollaboratorDto(Collaborator collaborator) {
        return new CollaboratorDto(collaborator.getId(), collaborator.getName(), collaborator.getProfitPercentage());
    }

    public final static List<CollaboratorDto> tocollaboratorDtos(List<Collaborator> collaborators) {
        List<CollaboratorDto> collaboratorDtos = new ArrayList<>();

        for (Collaborator collaborator : collaborators) {
            collaboratorDtos.add(toCollaboratorDto(collaborator));
        }

        return collaboratorDtos;
    }
}
