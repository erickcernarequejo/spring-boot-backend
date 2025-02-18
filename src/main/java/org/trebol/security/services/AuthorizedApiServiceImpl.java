package org.trebol.security.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import org.trebol.security.IAuthorizedApiService;

/**
 *
 * @author Benjamin La Madrid <bg.lamadrid@gmail.com>
 */
@Service
public class AuthorizedApiServiceImpl
    implements IAuthorizedApiService {

  @Override
  public Collection<String> getAuthorizedApiRoutes(UserDetails userDetails) {
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    Set<String> resourceRoutes = new HashSet<>();
    for (GrantedAuthority authority : authorities) {
      String resourceAuthority = authority.getAuthority();
      String resourceName = resourceAuthority.replaceAll(":.+$", "");
      resourceRoutes.add(resourceName);
    }

    return resourceRoutes;
  }

  @Override
  public Collection<String> getAuthorizedApiRouteAccess(UserDetails userDetails, String apiRoute) {
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    Set<String> authorizedActions = new HashSet<>();
    for (GrantedAuthority authority : authorities) {
      String resourceAuthority = authority.getAuthority();
      if (resourceAuthority.contains(apiRoute)) {
        String actionName = resourceAuthority.replaceAll("^.+:", "");
        authorizedActions.add(actionName);
      }
    }
    return authorizedActions;
  }

}
