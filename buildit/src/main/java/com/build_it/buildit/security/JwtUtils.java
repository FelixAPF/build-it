package com.build_it.buildit.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.expiration-ms}")
  private long jwtExpirationMs;

  public String generateToken(String email, String role) {
    return JWT.create()
      .withSubject(email)
      .withClaim("role", role)
      .withIssuedAt(new Date())
      .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
      .sign(Algorithm.HMAC256(jwtSecret));
  }

  public String getEmailFromToken(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    return decodedJWT.getSubject();
  }

  public String getRoleFromToken(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    return decodedJWT.getClaim("role").asString();
  }

  public boolean validateToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      JWTVerifier verifier = JWT.require(algorithm).build();
      verifier.verify(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
