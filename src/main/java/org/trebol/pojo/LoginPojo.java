package org.trebol.pojo;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

public class LoginPojo {
  @NotBlank
  private String name;
  @NotBlank
  private String password;

  public LoginPojo() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + Objects.hashCode(this.name);
    hash = 97 * hash + Objects.hashCode(this.password);
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
    final LoginPojo other = (LoginPojo)obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    return Objects.equals(this.password, other.password);
  }

  @Override
  public String toString() {
    return "LoginPojo{name=" + name +
        ", password=" + password + '}';
  }
}
