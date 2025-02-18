package org.trebol.jpa.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Benjamin La Madrid <bg.lamadrid@gmail.com>
 */
@Entity
@Table(name = "products_images")
public class ProductImage
  implements Serializable {

  private static final long serialVersionUID = 12L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "product_image_id", nullable = false)
  private Long id;
  @JoinColumn(name = "image_id", referencedColumnName = "image_id", updatable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Image image;
  @JoinColumn(name = "product_id", referencedColumnName = "product_id", updatable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Product product;

  public ProductImage() { }

  public ProductImage(ProductImage source) {
    this.id = source.id;
    this.image = source.image;
    this.product = source.product;
  }

  public Long getId() {
      return id;
  }

  public void setId(Long id) {
      this.id = id;
  }

  public Image getImage() {
      return image;
  }

  public void setImage(Image image) {
      this.image = image;
  }

  public Product getProduct() {
      return product;
  }

  public void setProduct(Product product) {
      this.product = product;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 53 * hash + Objects.hashCode(this.id);
    hash = 53 * hash + Objects.hashCode(this.image);
    hash = 53 * hash + Objects.hashCode(this.product);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ProductImage other = (ProductImage)obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.image, other.image)) {
      return false;
    }
    return Objects.equals(this.product, other.product);
  }

  @Override
  public String toString() {
    return "ProductImage{id=" + id +
        ", image=" + image +
        ", product=" + product + '}';
  }

}
