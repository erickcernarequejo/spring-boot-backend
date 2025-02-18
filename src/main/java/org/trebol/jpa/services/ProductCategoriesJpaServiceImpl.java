package org.trebol.jpa.services;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import org.trebol.pojo.ProductCategoryPojo;
import org.trebol.exceptions.BadInputException;
import org.trebol.jpa.entities.ProductCategory;
import org.trebol.jpa.entities.QProductCategory;
import org.trebol.jpa.GenericJpaService;
import org.trebol.jpa.repositories.IProductsCategoriesJpaRepository;

/**
 *
 * @author Benjamin La Madrid <bg.lamadrid at gmail.com>
 */
@Transactional
@Service
public class ProductCategoriesJpaServiceImpl
  extends GenericJpaService<ProductCategoryPojo, ProductCategory> {

  private final IProductsCategoriesJpaRepository categoriesRepository;
  private final ConversionService conversion;

  @Autowired
  public ProductCategoriesJpaServiceImpl(IProductsCategoriesJpaRepository repository,
                                         ConversionService conversion) {
    super(repository, LoggerFactory.getLogger(ProductCategoriesJpaServiceImpl.class));
    this.categoriesRepository = repository;
    this.conversion = conversion;
  }

  @Override
  public ProductCategoryPojo convertToPojo(ProductCategory source) {
    return conversion.convert(source, ProductCategoryPojo.class);
  }

  @Override
  public ProductCategory convertToNewEntity(ProductCategoryPojo source) {
    ProductCategory target = new ProductCategory();
    if (source.getCode() != null) {
      target.setId(source.getCode());
    }
    target.setName(source.getName());
    this.applyParent(source, target);
    return target;
  }

  @Override
  public ProductCategory applyChangesToExistingEntity(ProductCategoryPojo source, ProductCategory existing)
          throws BadInputException {
    ProductCategory target = new ProductCategory(existing);

    String name = source.getName();
    if (name != null && !name.isBlank() && !target.getName().equals(name)) {
      target.setName(name);
    }

    this.applyParent(source, target);

    return target;
  }

  @Override
  public Predicate parsePredicate(Map<String, String> queryParamsMap) {
    QProductCategory qProductCategory = QProductCategory.productCategory;
    BooleanBuilder predicate = new BooleanBuilder();
    for (String paramName : queryParamsMap.keySet()) {
      String stringValue = queryParamsMap.get(paramName);
      try {
        switch (paramName) {
          case "id":
            return predicate.and(qProductCategory.id.eq(Long.valueOf(stringValue))); // match por id es único
          case "nameLike":
            predicate.and(qProductCategory.name.likeIgnoreCase("%" + stringValue + "%"));
            break;
          case "parentId":
            if (stringValue == null) {
              predicate.and(qProductCategory.parent.isNull());
            } else {
              predicate.and(qProductCategory.parent.id.eq(Long.valueOf(stringValue)));
            }
            break;
          default:
            break;
        }
      } catch (NumberFormatException exc) {
        logger.info("Param '{}' couldn't be parsed as number (value: '{}')", paramName, stringValue);
      }
    }

    return predicate;
  }

  @Override
  public Optional<ProductCategory> getExisting(ProductCategoryPojo input) throws BadInputException {
    String name = input.getName();
    if (name == null || name.isBlank()) {
      throw new BadInputException("Invalid category name");
    } else {
      return this.categoriesRepository.findByName(name);
    }
  }

  private void applyParent(ProductCategoryPojo source, ProductCategory target) {
    ProductCategoryPojo parent = source.getParent();
    if (parent != null) {
      Long parentCode = parent.getCode();
      ProductCategory previousParent = target.getParent();
      if (parentCode == null) {
        this.applyNewParent(target, parent);
      } else if (previousParent == null || !previousParent.getId().equals(parentCode)) {
        Optional<ProductCategory> parentMatch = categoriesRepository.findById(parentCode);
        if (parentMatch.isPresent()) {
          target.setParent(parentMatch.get());
        } else {
          this.applyNewParent(target, parent);
        }
      }
    }
  }

  private void applyNewParent(ProductCategory target, ProductCategoryPojo parent) {
    ProductCategory newParentEntity = this.convertToNewEntity(parent);
    newParentEntity = categoriesRepository.save(newParentEntity);
    target.setParent(newParentEntity);
  }
}
