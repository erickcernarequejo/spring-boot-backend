package org.trebol.jpa;

import org.springframework.lang.Nullable;
import org.trebol.exceptions.BadInputException;
import org.trebol.exceptions.EntityAlreadyExistsException;
import org.trebol.pojo.DataPagePojo;

import javassist.NotFoundException;


/**
 * Interface for implementing the basic CRUD service operations.
 *
 * @author Benjamin La Madrid <bg.lamadrid at gmail.com>
 * @param <T> The items type class.
 * @param <I> The identifier type class.
 * @param <F> The filter type class.
 */
public interface IJpaCrudService<T, I, F>
  extends IQueryDslPredicateParserService {

  /**
   * Inserts and persists an item.
   *
   * @param dto The item to be created.
   *
   * @return The created item, with updated properties (most importantly its ID),
   *         or null if the item could not be created.
   * @throws org.trebol.exceptions.BadInputException When the data in the input object is not valid or is insufficient.
   * @throws org.trebol.exceptions.EntityAlreadyExistsException When the data collides with an existing registry.
   */
  T create(T dto) throws BadInputException, EntityAlreadyExistsException;

  /**
   * Queries a paged collection of items.
   *
   * @param pageSize  Number of items per page.
   * @param pageIndex Page index (0-based).
   * @param filters   Filtering conditions
   *
   * @return The requested collection of items. May be zero-sized.
   */
  DataPagePojo<T> readMany(int pageSize, int pageIndex, @Nullable F filters);

  /**
   * Retrieves the first item that matches a certain filter.
   *
   * @param filters   Filtering conditions
   *
   * @return The requested item
   * @throws javassist.NotFoundException When no item matches the filter.
   */
  T readOne(F filters) throws NotFoundException;

  /**
   * Retrieves an item by its ID.
   *
   * @param id The unique identifier of the item.
   *
   * @return The requested item.
   * @throws javassist.NotFoundException When there are no item matches for the identifier.
   */
  T readOne(Long id) throws NotFoundException;

  /**
   * Updates an existing item.
   *
   * @param dto The item to be updated. Its identifying field may or may not be
   *            present, and can be different from the second method param.
   * @param id  The unique identifier of the item.
   *
   * @return The saved item, with updated properties, or null if the item was not
   *         found.
   * @throws javassist.NotFoundException When no item matches the identifier.
   * @throws org.trebol.exceptions.BadInputException When the data in the input object is not valid.
   */
  T update(T dto, I id) throws NotFoundException, BadInputException;

  /**
   * Finds an item by its ID and deletes it.
   *
   * @param id Its unique identifier.
   *
   * @throws javassist.NotFoundException When no item matches the identifier.
   */
  void delete(I id) throws NotFoundException;
}
