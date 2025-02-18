package org.trebol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author Benjamin La Madrid <bg.lamadrid at gmail.com>
 */
@SpringBootApplication
public class BackendApp {
  /**
   * Application starting point.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(BackendApp.class, args);
  }

}
