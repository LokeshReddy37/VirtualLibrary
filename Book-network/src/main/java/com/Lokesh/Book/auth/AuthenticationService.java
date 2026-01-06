package com.Lokesh.Book.auth;


import com.Lokesh.Book.email.EmailService;
import com.Lokesh.Book.email.EmailTemplateName;
import com.Lokesh.Book.role.RoleRepository;
import com.Lokesh.Book.security.JwtService;
import com.Lokesh.Book.user.Token;
import com.Lokesh.Book.user.TokenRepository;
import com.Lokesh.Book.user.User;
import com.Lokesh.Book.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor

public class AuthenticationService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.mailing.frontend.activation-url}")
    String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER").orElseThrow(()->new IllegalStateException("Role User Was Not Initialized"));

        var user= User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        //generating and saving token
        var token=generateAndSaveActivationToken(user);
        //sending email
        emailService.sendEmail(
                user.getEmail(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                user.fullName(),
                activationUrl,
                token,
                "Account activation"
        );

    }

    private String generateAndSaveActivationToken(User user) {
            String code = generateActivationCode(6);
            var token= Token.builder()
                    .token(code)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .expiredAt(LocalDateTime.now().plusMinutes(15))
                    .build();
            tokenRepository.save(token);
        return code;
    }

    private String generateActivationCode(int length) {
        String  character = "0123456789";
        StringBuilder code= new StringBuilder();
        SecureRandom random= new SecureRandom();
        for(int i=0;i<length;i++){
            int idx=random.nextInt(character.length());
            code.append(character.charAt(idx));
        }
        return code.toString();
    }

    public  AuthenticationResponse authenticate(@Valid AuthenticationRequest request) {
        var auth=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )

        );
        var claims=new HashMap<String,Object>();
        var user=(User)auth.getPrincipal();
        claims.put("fullName",user.fullName());
        var jwtToken=jwtService.generateToken(claims,user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public void activateAccount(String token) throws MessagingException {
        Token savedToken=tokenRepository.findByToken(token).orElseThrow(
                ()->new RuntimeException("Invalid Token"));
        if(LocalDateTime.now().isAfter(savedToken.getExpiredAt())){
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Token has been expired,new token is generated");
        }
        var user=userRepository.findById(savedToken.getUser().getId()).orElseThrow(
                ()->    new UsernameNotFoundException("User not found")
        );
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }
}
