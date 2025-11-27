package es.udc.camisetas.backend.rest.controllers;

import es.udc.camisetas.backend.model.entities.Level;
import es.udc.camisetas.backend.model.entities.Raffle;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.NoParticipantsException;
import es.udc.camisetas.backend.model.services.RaffleService;
import es.udc.camisetas.backend.rest.common.ErrorsDto;
import es.udc.camisetas.backend.rest.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/raffles")

@Tag(
    name = "Sorteos",
    description = "Operaciones relacionadas con los sorteos, niveles, participantes y premios."
)
public class RaffleController {

    @Autowired
    private RaffleService raffleService;

    @ExceptionHandler(NoParticipantsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    @Operation(summary = "Error por falta de participantes", hidden = true)
    public ErrorsDto handleSNoParticipantsException(NoParticipantsException exception) {
        return new ErrorsDto(exception.getMessage());
    }


    @Operation(
        summary = "Crear un sorteo",
        description = "Crea un sorteo asociado a un tipo de camiseta, indicando precio de participación y descripción."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sorteo creado correctamente",
                content = @Content(schema = @Schema(implementation = RaffleDto.class))),
        @ApiResponse(responseCode = "404", description = "Tipo de camiseta no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada no válidos",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @PostMapping("/raffle")
    @ResponseStatus(HttpStatus.CREATED)
    public RaffleDto addRaffle(@Validated @RequestBody AddRaffleParamsDto params) throws InstanceNotFoundException, DuplicateInstanceException {

        Raffle raffle = raffleService.addRaffle(params.getParticipationPrice(), params.getDescription(), params.getShirtTypeId());

        return RaffleConversor.toRaffleDto(raffle);
    }


    @Operation(
        summary = "Actualizar un sorteo",
        description = "Actualiza la información de un sorteo existente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sorteo actualizado correctamente",
            content = @Content(schema = @Schema(implementation = RaffleDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Sorteo no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @PutMapping("/raffle/{id}")
    public RaffleDto updateRaffle(
        @Parameter(description = "ID del sorteo", example = "5")
        @PathVariable Long id,
        @Validated @RequestBody AddRaffleParamsDto params) throws InstanceNotFoundException, DuplicateInstanceException {
        Raffle raffle = raffleService.updateRaffle(id, params.getParticipationPrice(), params.getDescription(), params.getShirtTypeId());

        return RaffleConversor.toRaffleDto(raffle);
    }
    

    @Operation(
        summary = "Obtener sorteo por ID",
        description = "Devuelve la información detallada de un sorteo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sorteo encontrado",
            content = @Content(schema = @Schema(implementation = RaffleDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Sorteo no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/raffle/{id}")
    public RaffleDto getRaffle(
        @Parameter(description = "ID del sorteo", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return RaffleConversor.toRaffleDto(raffleService.getRaffle(id));
    }


    @Operation(
        summary = "Eliminar un sorteo",
        description = "Elimina un sorteo existente a partir de su ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Sorteo no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @DeleteMapping("/raffle/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRaffle(
        @Parameter(description = "ID del sorteo", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        raffleService.deleteRaffle(id);
    }


    @Operation(
        summary = "Número de participantes en un sorteo",
        description = "Devuelve la cantidad de personas que participan en un sorteo concreto."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Número obtenido correctamente",
            content = @Content(schema = @Schema(implementation = Integer.class))),
        @ApiResponse(responseCode = "404", description = "Sorteo no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/raffle/participantsNumber/{id}")
    public int getRaffleParticipantsNumber(
        @Parameter(description = "ID del sorteo", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return raffleService.getParticipantsNumber(id);
    }


    @Operation(
        summary = "Precio total recaudado en un sorteo",
        description = "Devuelve la recaudación total del sorteo hasta el momento."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recaudación obtenida correctamente",
            content = @Content(schema = @Schema(implementation = BigDecimal.class))),
        @ApiResponse(responseCode = "404", description = "Sorteo no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/raffle/price/{id}")
    public BigDecimal getRafflePrice(
        @Parameter(description = "ID del sorteo", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return raffleService.getRafflePrice(id);
    }


    @Operation(
        summary = "Obtener sorteo por tipo de camiseta",
        description = "Devuelve el sorteo asociado a un tipo de camiseta."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sorteo encontrado",
            content = @Content(schema = @Schema(implementation = RaffleDto.class))),
        @ApiResponse(responseCode = "404", description = "No existe sorteo para ese tipo",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/raffle/getByShirtTypeId")
    public RaffleDto getRaffleByShirtTypeId(
        @Parameter(description = "ID del tipo de camiseta", example="1")
        @RequestParam Long shirtTypeId) throws InstanceNotFoundException {
        return RaffleConversor.toRaffleDto(raffleService.getRaffleByShirtTypeId(shirtTypeId));
    }


    @Operation(
        summary = "Comprobar si existe un sorteo asociado a un tipo de camiseta",
        description = "Devuelve true/false según exista o no."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Valor devuelto correctamente",
            content = @Content(schema = @Schema(implementation = Boolean.class)))
    })
    @GetMapping("/raffle/existsByShirtTypeId")
    public boolean existsRaffleByShirtTypeId(
        @Parameter(description = "ID del tipo de camiseta", example="1")
        @RequestParam Long shirtTypeId) {
        return raffleService.existsRaffleByShirtTypeId(shirtTypeId);
    }


    @Operation(
        summary = "Crear nivel",
        description = "Añade un nivel a un sorteo indicando precio, descripción y participantes necesarios."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Nivel creado",
            content = @Content(schema = @Schema(implementation = LevelDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Sorteo no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada no válidos",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @PostMapping("/level")
    @ResponseStatus(HttpStatus.CREATED)
    public LevelDto addLevel(@Validated @RequestBody AddLevelParamsDto params) throws InstanceNotFoundException {

        Level level = raffleService.addLevel(params.getLevelDescription(), params.getPrice(), params.getNecessaryParticipants(), params.getRaffleId());

        return RaffleConversor.toLevelDto(level);
    }   


    @Operation(
        summary = "Actualizar un nivel",
        description = "Actualiza los datos de un nivel existente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nivel actualizado",
            content = @Content(schema = @Schema(implementation = LevelDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Nivel no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada no válidos",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @PutMapping("/level/{id}")
    public LevelDto updateLevel(
        @Parameter(description = "ID del nivel", example="1")
        @PathVariable Long id,
        @Validated @RequestBody UpdateLevelParamsDto params) throws InstanceNotFoundException {

        Level level = raffleService.updateLevel(id, params.getLevelDescription(), params.getPrice(), params.getNecessaryParticipants());

        return RaffleConversor.toLevelDto(level);

    }


    @Operation(
        summary = "Obtener nivel por ID",
        description = "Devuelve la información detallada de un nivel."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nivel encontrado",
            content = @Content(schema = @Schema(implementation = LevelDto.class))),
        @ApiResponse(responseCode = "404", description = "Nivel no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/level/{id}")
    public LevelDto getLevel(
        @Parameter(description = "ID del nivel", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return RaffleConversor.toLevelDto(raffleService.getLevel(id));
    }


    @Operation(
        summary = "Eliminar un nivel",
        description = "Elimina un nivel de un sorteo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Nivel eliminado"),
        @ApiResponse(responseCode = "404", description = "Nivel no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @DeleteMapping("/level/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLevel(
        @Parameter(description = "ID del nivel", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        raffleService.removeLevel(id);
    }


    @Operation(
        summary = "Comprobar si un nivel ha sido superado",
        description = "Devuelve true/false indicando si el nivel ha alcanzado los participantes necesarios."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado devuelto correctamente",
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "Nivel no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @GetMapping("/level/raised/{id}")
    public boolean levelRaised(
        @Parameter(description = "ID del nivel", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return raffleService.levelRaised(id);
    }


    @Operation(
        summary = "Jugar nivel",
        description = "Realiza la operación de sorteo para un nivel concreto."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nivel jugado correctamente",
            content = @Content(schema = @Schema(implementation = LevelDto.class))
        ),
        @ApiResponse(responseCode = "403", description = "No hay participantes suficientes",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Nivel no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))
        )
    })
    @PostMapping("/level/play/{id}")
    public LevelDto playLevel(
        @Parameter(description = "ID del nivel", example="1")
        @PathVariable Long id) throws InstanceNotFoundException, NoParticipantsException {
        return RaffleConversor.toLevelDto(raffleService.play(id));
    }
}
