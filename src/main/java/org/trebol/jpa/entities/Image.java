package org.trebol.jpa.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 *
 * @author Benjamin La Madrid <bg.lamadrid@gmail.com>
 */
@Entity
@Table(name = "images")
public class Image
  implements Serializable {

  private static final long serialVersionUID = 5L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_id", nullable = false)
  private Long id;
  @Size(min = 1, max = 50)
  @Column(name = "image_code", nullable = false)
  private String code;
  @Size(min = 1, max = 100)
  @Column(name = "image_filename", nullable = false)
  private String filename;
  @Size(min = 1, max = 500)
  @Column(name = "image_url", nullable = false)
  private String url;

  public Image() { }

  public Image(Image source) {
    this.id = source.id;
    this.code = source.code;
    this.filename = source.filename;
    this.url = source.url;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + Objects.hashCode(this.id);
    hash = 89 * hash + Objects.hashCode(this.code);
    hash = 89 * hash + Objects.hashCode(this.filename);
    hash = 89 * hash + Objects.hashCode(this.url);
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
    final Image other = (Image)obj;
    if (!Objects.equals(this.code, other.code)) {
      return false;
    }
    if (!Objects.equals(this.filename, other.filename)) {
      return false;
    }
    if (!Objects.equals(this.url, other.url)) {
      return false;
    }
    return Objects.equals(this.id, other.id);
  }

  @Override
  public String toString() {
    return "Image{id=" + id +
        ", code=" + code +
        ", filename=" + filename +
        ", url=" + url + '}';
  }

}
