package es.udc.camisetas.backend.rest.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.udc.camisetas.backend.model.entities.Collaborator;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.InvalidPercentageException;
import es.udc.camisetas.backend.model.services.Block;
import es.udc.camisetas.backend.model.services.CollaboratorService;
import es.udc.camisetas.backend.rest.common.ErrorsDto;
import es.udc.camisetas.backend.rest.dtos.AddCollaboratorParamsDto;
import es.udc.camisetas.backend.rest.dtos.BlockDto;
import es.udc.camisetas.backend.rest.dtos.CollaboratorConversor;
import es.udc.camisetas.backend.rest.dtos.CollaboratorDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/collaborators")
@Tag(
    name = "Colaboradores",
    description = "Operaciones relacionadas con la gestión de colaboradores, su porcentaje de beneficio, inversión y beneficios."
)
public class CollaboratorController {

    private final int PAGE_SIZE = 10;

    @ExceptionHandler(InvalidPercentageException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    @Operation(
        summary = "Manejo de porcentaje inválido",
        description = "Manejador interno para cuando el porcentaje de beneficio de un colaborador no es válido.",
        hidden = true
    )
    public ErrorsDto handleInvalidPercentageException(InvalidPercentageException exception) {
        return new ErrorsDto(exception.getMessage());
    }

    @Autowired
    CollaboratorService collaboratorService;


    @Operation(
        summary = "Crear un nuevo colaborador",
        description = "Crea un colaborador con nombre y porcentaje de beneficio. El porcentaje debe ser válido según las reglas de negocio."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Colaborador creado correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CollaboratorDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Porcentaje de beneficio inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada no válidos",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @PostMapping("/collaborator")
    @ResponseStatus(HttpStatus.CREATED)
    public CollaboratorDto addCollaborator(@Validated @RequestBody AddCollaboratorParamsDto params) throws InvalidPercentageException {
        Collaborator collaborator = collaboratorService.addCollaborator(params.getName(), params.getProfitPercentage());

        return CollaboratorConversor.toCollaboratorDto(collaborator);
    }


    @Operation(
        summary = "Actualizar un colaborador existente",
        description = "Actualiza el nombre y el porcentaje de beneficio de un colaborador identificado por su ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Colaborador actualizado correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CollaboratorDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Colaborador no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Porcentaje de beneficio inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada no válidos",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @PutMapping("/collaborator/{id}")
    public CollaboratorDto updateCollaborator(
        @Parameter(description = "ID del colaborador", example = "1")
        @PathVariable Long id,
        @Validated @RequestBody AddCollaboratorParamsDto params) throws InvalidPercentageException, InstanceNotFoundException {
        
            Collaborator collaborator = collaboratorService.updateCollaborator(id, params.getName(), params.getProfitPercentage());

        return CollaboratorConversor.toCollaboratorDto(collaborator);
    }


    @Operation(
        summary = "Obtener un colaborador por ID",
        description = "Devuelve la información detallada de un colaborador a partir de su ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Colaborador encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CollaboratorDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Colaborador no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/collaborator/{id}")
    public CollaboratorDto getCollaborator(
        @Parameter(description = "ID del colaborador", example = "1")
        @PathVariable Long id) throws InstanceNotFoundException {
        
            return CollaboratorConversor.toCollaboratorDto(collaboratorService.getCollaborator(id));
    }


    @Operation(
        summary = "Obtener un colaborador por ID",
        description = "Devuelve la información detallada de un colaborador a partir de su ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Colaborador encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CollaboratorDto.class))
        )
    })
    @GetMapping("/collaborators")
    public BlockDto<CollaboratorDto> getCollaborators(
        @Parameter(description = "Filtro por nombre (contiene)", example = "Juan")
        @RequestParam(defaultValue = "") String name,
        @Parameter(description = "Número de página (empezando en 0)", example = "0")
        @RequestParam(defaultValue = "0") int page) {

        Block<Collaborator> collaboratorBlock = collaboratorService.searchCollaborators(name, page, PAGE_SIZE);

        return new BlockDto<>(CollaboratorConversor.tocollaboratorDtos(collaboratorBlock.getItems()), collaboratorBlock.getExistMoreItems());
    }


    @Operation(
        summary = "Obtener todos los colaboradores",
        description = "Devuelve la lista completa de colaboradores sin paginación."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Listado de colaboradores",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CollaboratorDto.class))
        )
    })
    @GetMapping("/allCollaborators")
    public List<CollaboratorDto> getAllCollaborators() {
        return CollaboratorConversor.tocollaboratorDtos(collaboratorService.getAllCollaborators());
    }


    @Operation(
        summary = "Eliminar un colaborador",
        description = "Elimina un colaborador a partir de su ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Colaborador eliminado correctamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Colaborador no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @DeleteMapping("/collaborator/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollaborator(
        @Parameter(description = "ID del colaborador", example = "1")
        @PathVariable Long id) throws InstanceNotFoundException {

        collaboratorService.deleteCollaborator(id);
    }


    @Operation(
        summary = "Obtener número de camisetas compradas por un colaborador",
        description = "Devuelve el número de camisetas que han sido compradas asociadas a un colaborador concreto."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Número de camisetas comprado devuelto correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Integer.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Colaborador no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/collaborators/numberOfBoughtShirts/{id}")
    public int getNumberOfBoughtShirts(
        @Parameter(description = "ID del colaborador", example = "1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return collaboratorService.getNumberOfBoughtShirts(id);
    }


    @Operation(
        summary = "Obtener la inversión total de un colaborador",
        description = "Devuelve la cantidad total invertida por un colaborador."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Inversión devuelta correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = BigDecimal.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Colaborador no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/collaborators/investment/{id}")
    public BigDecimal getInvestment(
        @Parameter(description = "ID del colaborador", example = "1")
        @PathVariable Long id) throws InstanceNotFoundException {

        return collaboratorService.getInvestment(id);
    }


    @Operation(
        summary = "Obtener el beneficio de un colaborador",
        description = "Devuelve el beneficio que ha obtenido un colaborador según las ventas asociadas."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Beneficio devuelto correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = BigDecimal.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Colaborador no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/collaborators/profit/{id}")
    public BigDecimal getProfit(
        @Parameter(description = "ID del colaborador", example = "1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return collaboratorService.getProfit(id);
    }


    @Operation(
        summary = "Validar un porcentaje de beneficio",
        description = "Comprueba si un porcentaje de beneficio es válido para un colaborador. Puede utilizarse tanto para creación como para actualización."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Resultado de la validación devuelto correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Colaborador no encontrado (si se indica ID)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/collaborators/validPercentage")
    public boolean getValidPercentage(
        @Parameter(description = "ID del colaborador (opcional, para actualización)", example = "1")
        @RequestParam(required = false) Long id,
        @Parameter(description = "Porcentaje a validar", example = "15.5")
        @RequestParam(defaultValue = "0") BigDecimal percentage)
        throws InstanceNotFoundException {

        return collaboratorService.validatePercentage(id, percentage);
    }


    @Operation(
        summary = "Obtener la inversión retornada a un colaborador",
        description = "Devuelve cuánto de la inversión inicial del colaborador ha sido ya retornada."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Cantidad retornada devuelta correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = BigDecimal.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Colaborador no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/collaborators/returnedInvestment/{id}")
    public BigDecimal getReturnedInvestment(
        @Parameter(description = "ID del colaborador", example = "1")
        @PathVariable Long id) throws InstanceNotFoundException {

        return collaboratorService.returnedInvestment(id);
    }

}
