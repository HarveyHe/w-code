package com.harvey.common.core.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.harvey.common.core.model.UserBaseModel;


public interface UserBaseService extends UserDetailsService {
  /*IUser findByUserName(String userName);*/

  public abstract void authenticate(
      UserBaseModel user,
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken)
      throws AuthenticationException;
}
