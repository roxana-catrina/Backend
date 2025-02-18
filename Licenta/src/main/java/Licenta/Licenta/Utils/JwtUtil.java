package Licenta.Licenta.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private final String secretKey = "2D4A614E645267556B58703273357638792F423F4428472B4B6250655368566D"; // Schimbă cu o cheie mai sigură!

    public String extractUserName(String token) {
        return extractClaims(token,Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {

        return generateToken(new HashMap<>(),userDetails);
    }

    public boolean isValidToken(String token,UserDetails userDetails) {
        final String userName=extractUserName(token);

        return(userName.equals(userDetails.getUsername()) && !isTokenExpirated(token));
    }

    private <T> T extractClaims(String token, Function<Claims,T> claimsResolvers) {
        final Claims claims=extractAllClaims(token);
        return claimsResolvers.apply(claims);

    }
    public String generateToken(Map<String,Object> extractClaims,UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extractClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token valabil 1 zi
                .signWith(getSigningKey(),SignatureAlgorithm.HS256)
                .compact();

    }

    public String generateRefreshToken(Map<String,Object> extractClaims,UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extractClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token valabil 1 zi
                .signWith(getSigningKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpirated(String token) {
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);

    }
   private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
   }


    private Key getSigningKey() {
        byte[] keyBytes= Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
