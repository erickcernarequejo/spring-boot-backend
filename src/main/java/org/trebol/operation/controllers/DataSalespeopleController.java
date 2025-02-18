package org.trebol.operation.controllers;

import java.util.Map;

import javax.validation.Valid;

import io.jsonwebtoken.lang.Maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.trebol.pojo.DataPagePojo;
import org.trebol.operation.GenericDataController;
import org.trebol.pojo.SalespersonPojo;
import org.trebol.config.OperationProperties;
import org.trebol.jpa.entities.Salesperson;
import org.trebol.exceptions.EntityAlreadyExistsException;
import org.trebol.jpa.GenericJpaService;

import com.querydsl.core.types.Predicate;

import org.trebol.operation.IDataCrudController;
import org.trebol.exceptions.BadInputException;

import javassist.NotFoundException;

/**
 * API point of entry for Salesperson entities
 *
 * @author Benjamin La Madrid <bg.lamadrid at gmail.com>
 */
@RestController
@RequestMapping("/data/salespeople")
public class DataSalespeopleController
  extends GenericDataController<SalespersonPojo, Salesperson>
  implements IDataCrudController<SalespersonPojo, String> {

  @Autowired
  public DataSalespeopleController(OperationProperties globals,
                                   GenericJpaService<SalespersonPojo, Salesperson> crudService) {
    super(globals, crudService);
  }

  @GetMapping({"", "/"})
  @PreAuthorize("hasAuthority('salespeople:read')")
  public DataPagePojo<SalespersonPojo> readMany(@RequestParam Map<String, String> allRequestParams) {
    return super.readMany(null, null, allRequestParams);
  }

  @Override
  @PostMapping({"", "/"})
  @PreAuthorize("hasAuthority('salespeople:create')")
  public void create(@Valid @RequestBody SalespersonPojo input) throws BadInputException, EntityAlreadyExistsException {
    crudService.create(input);
  }

  @Override
  @GetMapping({"/{idNumber}", "/{idNumber}/"})
  @PreAuthorize("hasAuthority('salespeople:read')")
  public SalespersonPojo readOne(@PathVariable String idNumber) throws NotFoundException {
    Map<String, String> idNumberMatchMap = Maps.of("idnumber", idNumber).build();
    Predicate filters = crudService.parsePredicate(idNumberMatchMap);
    return crudService.readOne(filters);
  }

  @Override
  @PutMapping({"/{idNumber}", "/{idNumber}/"})
  @PreAuthorize("hasAuthority('salespeople:update')")
  public void update(@RequestBody SalespersonPojo input, @PathVariable String idNumber)
    throws BadInputException, NotFoundException {
     // TODO improve this implementation; the same salesperson will be fetched twice
    Long salespersonId = this.readOne(idNumber).getId();
    crudService.update(input, salespersonId);
  }

  @Override
  @DeleteMapping({"/{idNumber}", "/{idNumber}/"})
  @PreAuthorize("hasAuthority('salespeople:delete')")
  public void delete(@PathVariable String idNumber) throws NotFoundException {
    Long salespersonId = this.readOne(idNumber).getId();
    crudService.delete(salespersonId);
  }
}
