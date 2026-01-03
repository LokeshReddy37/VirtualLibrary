package com.Lokesh.Book.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
//generate,extract,validate,decode the token
public class JwtService {
        @Value("${application.security.jwt.secret-key}")
       private  String secretKey;
        @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }
    public <T> T extractClaims(String token, Function<Claims,T> claimResolver){
        final Claims claims=extractAllClaims(token);
        return claimResolver.apply(claims);
    }
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(),userDetails);
    }

    public String generateToken(HashMap<String,Object> claims,UserDetails userDetails){
        return buildToken(claims,userDetails,jwtExpiration);
    }

    public String buildToken(HashMap<String,Object> extraClaims,UserDetails userDetails,long jwtExpiration){
        var authorities=userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+jwtExpiration))
                .claim("authorities",authorities)
                .signWith(getSignInKey())
                .compact();
    }

    public  SecretKey getSignInKey() {
        byte[] keyBites= Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBites);
    }


    public boolean isTokenValid(String token,UserDetails userDetails){
        String username=extractUsername(token);
        if(!username.equals(userDetails.getUsername())&&isTokenExpired(token)){
            return false;
        }
        return true;
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token,Claims::getExpiration).before(new Date(System.currentTimeMillis()));
    }

}
