package es.udc.camisetas.backend.rest.controllers;

import es.udc.camisetas.backend.model.entities.Shirt;
import es.udc.camisetas.backend.model.entities.ShirtType;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.ShirtAlreadyBoughtException;
import es.udc.camisetas.backend.model.exceptions.ShirtTypeHasAlreadyAResponsibleException;
import es.udc.camisetas.backend.model.services.Block;
import es.udc.camisetas.backend.model.services.CustomerService;
import es.udc.camisetas.backend.model.services.ShirtFacade;
import es.udc.camisetas.backend.model.services.ShirtService;
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
import java.util.List;

@Tag(
    name = "Camisetas",
    description = "Operaciones relacionadas con camisetas, tipos de camiseta, responsables y precios."
)
@RestController
@RequestMapping("/shirts")
public class CustomerController {

    private final int SHIRT_PAGE_SIZE = 20;
    private final int SHIRT_TYPE_PAGE_SIZE = 4;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ShirtService shirtService;

    @Autowired
    private ShirtFacade shirtFacade;

    @ExceptionHandler(ShirtTypeHasAlreadyAResponsibleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    @Operation(summary = "Tipo de camiseta ya tiene responsable", hidden = true)
    public ErrorsDto handleShirtTypeHasAlreadyAResponsibleException(ShirtTypeHasAlreadyAResponsibleException exception) {
        return new ErrorsDto(exception.getMessage());
    }

    @ExceptionHandler(ShirtAlreadyBoughtException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    @Operation(summary = "Camiseta ya comprada", hidden = true)
    public ErrorsDto handleShirtAlreadyBoughtExceptionException(ShirtAlreadyBoughtException exception) {
        return new ErrorsDto(exception.getMessage());
    }

    
    @Operation(
        summary = "Crear un tipo de camiseta",
        description = "Crea un nuevo tipo de camiseta sin responsable asignado inicialmente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tipo de camiseta creado",
            content = @Content(schema = @Schema(implementation = ShirtTypeDto.class))),
        @ApiResponse(responseCode = "404", description = "Entidad no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @PostMapping("/shirtType")
    @ResponseStatus(HttpStatus.CREATED)
    public ShirtTypeDto addShirtType(@Validated @RequestBody AddShirtTypeParamsDto params) throws InstanceNotFoundException {

        ShirtType shirtType = shirtService.addShirtType(params.getName(), params.getImage(), params.getBaseSalesPrice(), params.getDescription(), params.getFreeShirtPeople(), null);

        return ShirtConversor.toShirtTypeDto(shirtType);
    }


    @Operation(
        summary = "Actualizar tipo de camiseta",
        description = "Actualiza los datos de un tipo de camiseta, incluyendo responsable, precio, imagen y descripción."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tipo actualizado",
            content = @Content(schema = @Schema(implementation = ShirtTypeDto.class))),
        @ApiResponse(responseCode = "404", description = "No existe el tipo solicitado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @PutMapping("/shirtType/{id}")
    public ShirtTypeDto updateShirtType(
        @Parameter(description = "ID del tipo de camiseta", example = "3")
        @PathVariable Long id,
        @Validated @RequestBody UpdateShirtTypeParamsDto params) throws InstanceNotFoundException {

        ShirtType shirtType = shirtService.getShirtType(id);

        if(shirtType.getResponsible() != null && params.getResponsibleId() == null) {
            customerService.deleteResponsible(shirtType.getResponsible().getId());
        }

        shirtType = shirtService.updateShirtType(id, params.getName(), params.getImage(), params.getBaseSalesPrice(), params.getDescription(), params.getFreeShirtPeople(), params.getResponsibleId());

        return ShirtConversor.toShirtTypeDto(shirtType);
    }


    @Operation(summary = "Obtener tipo de camiseta por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tipo encontrado",
            content = @Content(schema = @Schema(implementation = ShirtTypeDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "No encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirtType/{id}")
    public ShirtTypeDto getShirtType(
        @Parameter(description = "ID del tipo de camiseta", example="1")
        @PathVariable Long id)
        throws InstanceNotFoundException {

        return ShirtConversor.toShirtTypeDto(shirtService.getShirtType(id));
    }


    @Operation(summary = "Eliminar tipo de camiseta")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "No existe el tipo solicitado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @DeleteMapping("/shirtType/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShirtType(
        @Parameter(description = "ID del tipo de camiseta", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        shirtService.deleteShirtType(id);
    }


    @Operation(
        summary = "Buscar tipos de camiseta",
        description = "Resultado paginado filtrado por nombre. Página de tamaño 4."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado exitoso",
            content = @Content(schema = @Schema(implementation = BlockDto.class))
        ),
    })
    public BlockDto<ShirtTypeDto> getShirtTypes(
        @Parameter(description = "Nombre para filtrar grupos de camiseta", example = "Grupo A")
        @RequestParam(defaultValue = "") String name,
        @Parameter(description = "Número de página para paginación", example = "0")
        @RequestParam(defaultValue = "0") int page) {

        Block<ShirtType> shirtTypeBlock = shirtService.searchShirtTypes(name, page, SHIRT_TYPE_PAGE_SIZE);

        return new BlockDto<>(ShirtConversor.toShirtTypeDtos(shirtTypeBlock.getItems()), shirtTypeBlock.getExistMoreItems());
    }


    @Operation(summary = "Obtener todos los tipos de camiseta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado exitoso",
            content = @Content(schema = @Schema(implementation = List.class))
        )
    })
    @GetMapping("/allShirtTypes")
    public List<ShirtTypeDto> getAllShirtTypes() {
        return ShirtConversor.toShirtTypeDtos(shirtService.getAllShirtTypes());
    }


    @Operation(
        summary = "Crear camiseta",
        description = "Crea una camiseta e incluye la lógica que vincula al cliente correspondiente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Camiseta creada",
            content = @Content(schema = @Schema(implementation = ShirtDto.class))),
        @ApiResponse(responseCode = "404", description = "Entidad relacionada no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))),
        @ApiResponse(responseCode = "403", description = "El tipo de camiseta ya tiene un responsable",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @PostMapping("/shirt")
    @ResponseStatus(HttpStatus.CREATED)
    public ShirtDto addShirt(@Validated @RequestBody AddShirtParamsDto params) throws InstanceNotFoundException, DuplicateInstanceException, ShirtTypeHasAlreadyAResponsibleException {

        Shirt shirt = shirtFacade.addShirtWithCustomer(params);
        return ShirtConversor.toShirtDto(shirt);
    }

    

    @Operation(summary="Actualizar una camiseta existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Camiseta actualizada",
            content = @Content(schema = @Schema(implementation = ShirtDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "No encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))),
        @ApiResponse(responseCode = "403", description = "El tipo de camiseta ya tiene un responsable",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @PutMapping("/shirt/{id}")
    public ShirtDto updateShirt(
        
        @Parameter(description = "ID de la camiseta", example="1")
        @PathVariable Long id,
        @Validated @RequestBody UpdateShirtParamsDto params) throws InstanceNotFoundException, DuplicateInstanceException, ShirtTypeHasAlreadyAResponsibleException {

        Shirt shirt = shirtFacade.updateShirtWithCustomer(id, params);
        return ShirtConversor.toShirtDto(shirt);
    }


    @Operation(summary = "Obtener camiseta por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrada",
            content = @Content(schema = @Schema(implementation = ShirtDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "No encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirt/{id}")
    public ShirtDto getShirt(
        @Parameter(description = "ID de la camiseta", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return ShirtConversor.toShirtDto(shirtService.getShirtById(id));
    }


    @Operation(summary = "Eliminar camiseta")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminada"),
        @ApiResponse(responseCode = "404", description = "No encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @DeleteMapping("/shirt/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShirt(
        @Parameter(description="ID de la camiseta", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        shirtService.deleteShirt(id);
    }


    @Operation(summary = "Buscar camisetas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = BlockDto.class))
        )
    })
    @GetMapping("/shirts")
    public BlockDto<ShirtDto> searchShirts(

            @Parameter(description="Instagram del cliente (lo contiene)", example="@lucas.perezz")
            @RequestParam(required = false) String keywords,
            @Parameter(description="Id del tipo de camiseta", example="1")
            @RequestParam(required = false) Long shirtTypeId,
            @Parameter(description="Id de la persona que invirtió en su compra", example="1")
            @RequestParam(required = false) Long investorId,
            @Parameter(description="Talla de la camiseta", example="L")
            @RequestParam(required = false) String shirtSize,
            @Parameter(description="Booleano referido a si el cliente es el responsable de su grupo")
            @RequestParam(defaultValue = "false") boolean isResponsible,
            @Parameter(description="Número de página para la paginación", example="0")
            @RequestParam(defaultValue = "0") int page) {

        Block<Shirt> shirtBlock = shirtService.searchShirts(keywords, shirtTypeId, investorId, shirtSize, isResponsible, page, SHIRT_PAGE_SIZE);

        return new BlockDto<>(ShirtConversor.toShirtDtos(shirtBlock.getItems()), shirtBlock.getExistMoreItems());

    }


    @Operation(summary = "Ingresos generados por un tipo de camiseta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = BigDecimal.class))
        ),
        @ApiResponse(responseCode = "404", description = "Tipo no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirtType/shirtTypeRevenue/{id}")
    public BigDecimal getShirtTypeRevenue(
        @Parameter(description="Id del tipo de camiseta", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return shirtService.getShirtTypeRevenue(id);
    }


    @Operation(summary="Número de clientes de un tipo de camiseta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Integer.class))
        ),
        @ApiResponse(responseCode = "404", description = "Tipo no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("shirtType/customersNumber/{id}")
    public int customersNumber(
        @Parameter(description="Id del tipo de camiseta", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return shirtService.customersNumber(id);
    }


    @Operation(summary = "Amigos convencidos por un cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = ShirtDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/convincedFriends/{id}")
    public List<ShirtDto> convincedFriends(
        @Parameter(description="Id del cliente", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return ShirtConversor.toShirtDtos(shirtService.getConvincedFriendsShirt(id));
    }

    
    @Operation(
        summary = "Número total de amigos convencidos por un cliente",
        description = "Devuelve cuántos amigos ha conseguido convencer el cliente para unirse a su grupo y comprar una camiseta."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Número devuelto correctamente",
            content = @Content(schema = @Schema(implementation = Integer.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/convincedFriendsNumber/{id}")
    public int getConvincedFriendsNumber(
        @Parameter(description="Id del cliente", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return shirtService.getConvincedFriendsNumber(id);
    }

    
    @Operation(
    summary = "Número de amigos convencidos que han pagado",
    description = "Devuelve cuántos amigos convencidos por el cliente ya han pagado su camiseta."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Número devuelto correctamente",
            content = @Content(schema = @Schema(implementation = Integer.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/convincedFriendsPaid/{id}")
    public int getConvincedFriendsPaid(
        @Parameter(description="Id del cliente", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return shirtService.getConvincedFriendsPaid(id);
    }

    
    @Operation(
    summary = "Indica si la camiseta será gratis para el cliente",
    description = "Determina si el cliente cumple los requisitos para obtener su camiseta de manera gratuita (por ejemplo: amigos que haya convencido y hayan pagado)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado devuelto correctamente",
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "Camiseta no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/isFree/{id}")
    public boolean isFreeShirt(
        @Parameter(description="Id de la camiseta", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return shirtService.isFree(id);
    }

    
    @Operation(
    summary = "Precio actual de una camiseta",
    description = "Devuelve el precio final que debe pagar el cliente en función de su situación (responsable, número de amigos convencidos y otros factores)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Precio obtenido correctamente",
            content = @Content(schema = @Schema(implementation = BigDecimal.class))),
        @ApiResponse(responseCode = "404", description = "Camiseta no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/currentPrice/{id}")
    public BigDecimal getCurrentPrice(
        @Parameter(description="Id de la camiseta", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return shirtService.getCurrentShirtPrice(id);
    }

    
    @Operation(
        summary = "Comprar una camiseta",
        description = "Realiza la acción de compra de una camiseta. Si ya está comprada o si los datos no son válidos, se devolverán los errores correspondientes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Camiseta comprada correctamente",
            content = @Content(schema = @Schema(implementation = ShirtDto.class))),
        @ApiResponse(responseCode = "403", description = "La camiseta ya ha sido comprada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class))),
        @ApiResponse(responseCode = "404", description = "Camiseta no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @PostMapping("/shirts/buy/{id}")
    public ShirtDto buy(
        @Parameter(description="Id de la camiseta", example="1")
        @PathVariable Long id,
        @Parameter(description="Precio de compra de la camiseta", example="12.5")
        @RequestParam BigDecimal salePrice) throws InstanceNotFoundException, ShirtAlreadyBoughtException, DuplicateInstanceException {
        return ShirtConversor.toShirtDto(shirtService.buy(id, salePrice));
    }


    @Operation(
        summary = "Beneficio total generado por todas las ventas",
        description = "Devuelve la suma total de beneficios acumulados considerando todas las camisetas vendidas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Beneficio total devuelto correctamente",
            content = @Content(schema = @Schema(implementation = BigDecimal.class)))
    })
    @GetMapping("/shirts/totalProfit")
    public BigDecimal getTotalProfit() throws InstanceNotFoundException {
        return shirtService.getTotalProfit();
    }

    
    @Operation(
        summary = "¿Está comprada la camiseta?",
        description = "Devuelve true si la camiseta ya fue comprada, false si aún está pendiente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado devuelto correctamente",
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "Camiseta no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/isBought/{id}")
    public boolean isBought(
        @Parameter(description="Id de la camiseta", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return shirtService.isBought(id);
    }

    
    @Operation(
        summary = "Obtener responsable asociado a un cliente",
        description = "Devuelve el responsable del grupo al que pertenece el cliente, si existe."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Responsable encontrado",
            content = @Content(schema = @Schema(implementation = ResponsibleDto.class))),
        @ApiResponse(responseCode = "404", description = "Cliente o responsable no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/responsibleByCustomerId/{customerId}")
    public ResponsibleDto getResponsibleByCustomerId(
        @Parameter(description="Id de cliente", example="1")
        @PathVariable Long customerId) throws InstanceNotFoundException {
        return ShirtConversor.toResponsibleDto(customerService.getResponsibleByCustomerId(customerId));
    }


    @Operation(
        summary = "Obtener responsable por su ID",
        description = "Devuelve el responsable especificado por ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Responsable encontrado",
            content = @Content(schema = @Schema(implementation = ResponsibleDto.class))),
        @ApiResponse(responseCode = "404", description = "Camiseta no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/responsible/{id}")
    public ResponsibleDto getResponsible(
        @Parameter(description="Id del responsable", example="1")
        @PathVariable Long id) throws InstanceNotFoundException {
        return ShirtConversor.toResponsibleDto(customerService.getResponsible(id));
    }


    @Operation(
        summary = "¿Es responsable este cliente?",
        description = "Devuelve true si el cliente es responsable del grupo de camisetas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado devuelto correctamente",
            content = @Content(schema = @Schema(implementation = Boolean.class)))
    })
    @GetMapping("/shirts/isResponsible/{customerId}")
    public boolean isResponsible(
        @Parameter(description="Id del cliente", example="1")
        @PathVariable Long customerId) {
        return customerService.isResponsible(customerId);
    }


    @Operation(
        summary = "Obtener camiseta de un cliente",
        description = "Devuelve la camiseta asociada al cliente indicado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Camiseta encontrada",
            content = @Content(schema = @Schema(implementation = ShirtDto.class))),
        @ApiResponse(responseCode = "404", description = "Camiseta no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/shirtByCustomer/{customerId}")
    public ShirtDto getShirtByCustomerId(
        @Parameter(description="Id del cliente", example="1")
        @PathVariable Long customerId) throws InstanceNotFoundException {
        return ShirtConversor.toShirtDto(shirtService.getShirtByCustomerId(customerId));
    }

    @Operation(
        summary = "Obtener camiseta por Instagram del cliente",
        description = "Permite buscar una camiseta asociada al cliente indicado por su nombre de usuario de Instagram."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Camiseta encontrada",
            content = @Content(schema = @Schema(implementation = ShirtDto.class))),
        @ApiResponse(responseCode = "404", description = "Camiseta no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorsDto.class)))
    })
    @GetMapping("/shirts/getByInstagram")
    public ShirtDto getCustomerbyInstagram(
        @Parameter(description="Instagram del cliente", example="@lucass.perez")
        @RequestParam String instagram) throws InstanceNotFoundException {
        return ShirtConversor.toShirtDto(shirtService.getShirtByInstagram(instagram));
    }
}
