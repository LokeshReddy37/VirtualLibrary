package com.Lokesh.Book.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @Email(message = "Email must be formatted ")
    @NotBlank(message = "Email is Mandatory")
    @NotEmpty(message = "Email is Mandatory")
    private String email;
    @Size(min = 8 , message = "Password must be atLeast 8 Characters Long ")
    private String password;
}
