package org.trebol.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.trebol.pojo.DataPagePojo;
import org.trebol.exceptions.BadInputException;

import com.querydsl.core.types.Predicate;

import org.trebol.exceptions.EntityAlreadyExistsException;

import javassist.NotFoundException;

/**
 * Base abstraction for JPA-based CRUD services that communicate with Pojos, keeping entity classes out of scope.
 *
 * @author Benjamin La Madrid <bg.lamadrid at gmail.com>
 *
 * @param <P> The pojo class
 * @param <E> The entity class
 */
public abstract class GenericJpaService<P, E>
  implements IJpaCrudService<P, Long, Predicate>, IJpaConverterService<P, E> {

  protected final IJpaRepository<E> repository;
  protected final Logger logger;

  public GenericJpaService(IJpaRepository<E> repository, Logger logger) {
    this.repository = repository;
    this.logger = logger;
  }

  /**
   * Attempts to match the given example input's identification to an existing entity in the persistence context.
   * @param example The pojo class instance that should hold a valid identifying property
   * @return An optional holding
   * @throws BadInputException When the pojo doesn't have its identifying property.
   */
  public abstract Optional<E> getExisting(P example) throws BadInputException;

  /**
   * Swiftly check that a matching entity exists. Uses getExisting().
   * @param input The pojo class instance
   * @return true if the item exists in the database, false otherwise
   * @throws BadInputException When the pojo doesn't have its identifying property.
   */
  public boolean itemExists(P input) throws BadInputException {
    return this.getExisting(input).isPresent();
  }

  /**
   * Query all entities for the type class. Override this method if you need
   * custom queries. Remember to declare the correct repository interface first.
   *
   * @param page Paging parameters (size/index).
   * @param filters Object used for filtering.
   * @return A page of entities.
   */
  public Page<E> getAllEntities(Pageable page, Predicate filters) {
    if (filters == null) {
      return repository.findAll(page);
    } else {
      return repository.findAll(filters, page);
    }
  }

  /**
   * Convert a pojo to an entity, save it, convert it back to a pojo and return
   * it.
   * @param inputPojo The Pojo instance to be converted and inserted.
   */
  @Transactional
  @Override
  public P create(P inputPojo) throws BadInputException, EntityAlreadyExistsException {
    if (this.itemExists(inputPojo)) {
      throw new EntityAlreadyExistsException("The item to be created already exists");
    } else {
      E input = this.convertToNewEntity(inputPojo);
      E output = repository.saveAndFlush(input);
      return this.convertToPojo(output);
    }
  }

  /**
   * Read entities, convert them to pojos and return the collection.
   */
  @Override
  public DataPagePojo<P> readMany(int pageSize, int pageIndex, Predicate filters) {
    // TODO figure out sort order parameter
    Pageable paged = PageRequest.of(pageIndex, pageSize);
    Page<E> iterable = this.getAllEntities(paged, filters);
    long totalCount = repository.count(filters);

    List<P> pojoList = new ArrayList<>();
    for (E item : iterable) {
      P outputItem = this.convertToPojo(item);
      pojoList.add(outputItem);
    }

    return new DataPagePojo<>(pojoList, pageIndex, totalCount, pageSize);
  }

  /**
   * Look up the entity, save it if exists and differs from existing, convert back
   * to pojo and return.If it does not differ, return as-is.
   * @param input The Pojo instance.
   * @param id The database-backed id of the entity.
   */
  @Transactional
  @Override
  public P update(P input, Long id) throws NotFoundException, BadInputException {
    Optional<E> itemById = repository.findById(id);
    if (itemById.isEmpty()) {
      throw new NotFoundException("The requested item does not exist");
    } else {
      E existingEntity = itemById.get();
      E updatedEntity = this.applyChangesToExistingEntity(input, existingEntity);
      if (existingEntity.equals(updatedEntity)) {
        return input;
      } else {
        E output = repository.saveAndFlush(updatedEntity);
        return this.convertToPojo(output);
      }
    }
  }

  @Override
  public void delete(Long id) throws NotFoundException {
    if (!repository.existsById(id)) {
      throw new NotFoundException("The requested item does not exist");
    } else {
      repository.deleteById(id);
      repository.flush();
    }
  }

  @Override
  public P readOne(Long id) throws NotFoundException {
    Optional<E> entityById = repository.findById(id);
    if (entityById.isEmpty()) {
      throw new NotFoundException("The requested item does not exist");
    } else {
      E found = entityById.get();
      return this.convertToPojo(found);
    }
  }

  @Override
  public P readOne(Predicate filters) throws NotFoundException {
    Optional<E> entity = repository.findOne(filters);
    if (entity.isEmpty()) {
      throw new NotFoundException("The requested item does not exist");
    } else {
      E found = entity.get();
      return this.convertToPojo(found);
    }
  }
}
