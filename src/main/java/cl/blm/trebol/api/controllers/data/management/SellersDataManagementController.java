package cl.blm.trebol.api.controllers.data.management;

import java.util.Collection;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.blm.trebol.api.GenericCrudController;
import cl.blm.trebol.api.pojo.SellerPojo;
import cl.blm.trebol.config.CustomProperties;
import cl.blm.trebol.jpa.entities.Salesperson;
import cl.blm.trebol.services.crud.GenericCrudService;

/**
 * API point of entry for Salesperson entities
 *
 * @author Benjamin La Madrid <bg.lamadrid at gmail.com>
 */
@RestController
@RequestMapping("/data")
public class SellersDataManagementController
    extends GenericCrudController<SellerPojo, Salesperson, Integer> {

  @Autowired
  public SellersDataManagementController(CustomProperties globals,
      GenericCrudService<SellerPojo, Salesperson, Integer> crudService) {
    super(globals, crudService);
  }

  @Override
  @PostMapping("/sellers")
  @PreAuthorize("hasAuthority('sellers:create')")
  public Integer create(@RequestBody @Valid SellerPojo input) {
    return super.create(input);
  }

  @Override
  @GetMapping("/sellers/{id}")
  @PreAuthorize("hasAuthority('sellers:read')")
  public SellerPojo readOne(@PathVariable Integer id) {
    return super.readOne(id);
  }

  @GetMapping("/sellers")
  @PreAuthorize("hasAuthority('sellers:read')")
  public Collection<SellerPojo> readMany(@RequestParam Map<String, String> allRequestParams) {
    return super.readMany(null, null, allRequestParams);
  }

//  @GetMapping("/sellers/{requestPageSize}")
//  @PreAuthorize("hasAuthority('sellers:read')")
//  public Collection<SellerPojo> readMany(@PathVariable Integer requestPageSize,
//      @RequestParam Map<String, String> allRequestParams) {
//    return super.readMany(requestPageSize, null, allRequestParams);
//  }
//
//  @Override
//  @GetMapping("/sellers/{requestPageSize}/{requestPageIndex}")
//  @PreAuthorize("hasAuthority('sellers:read')")
//  public Collection<SellerPojo> readMany(@PathVariable Integer requestPageSize, @PathVariable Integer requestPageIndex,
//      @RequestParam Map<String, String> allRequestParams) {
//    return super.readMany(requestPageSize, requestPageIndex, allRequestParams);
//  }
//
//  @PutMapping("/sellers")
//  @PreAuthorize("hasAuthority('sellers:update')")
//  public Integer update(@RequestBody @Valid SellerPojo input) {
//    return super.update(input, input.getId());
//  }

  @Override
  @PutMapping("/sellers/{id}")
  @PreAuthorize("hasAuthority('sellers:update')")
  public Integer update(@RequestBody @Valid SellerPojo input, @PathVariable Integer id) {
    return super.update(input, id);
  }

  @Override
  @DeleteMapping("/sellers/{id}")
  @PreAuthorize("hasAuthority('sellers:delete')")
  public boolean delete(@PathVariable Integer id) {
    return super.delete(id);
  }

  @Override
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
    return super.handleValidationExceptions(ex);
  }
}
