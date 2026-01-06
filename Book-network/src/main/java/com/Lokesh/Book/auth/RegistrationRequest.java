package com.Lokesh.Book.auth;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    @NotEmpty(message = "Firstname is Mandatory")
    @NotBlank(message = "Firstname is Mandatory")
    private String firstname;
    @NotEmpty(message = "Lastname is Mandatory")
    @NotBlank(message = "Lastname is Mandatory")
    private String lastname;
    @Email(message = "Email must be formatted ")
    @NotBlank(message = "Email is Mandatory")
    @NotEmpty(message = "Email is Mandatory")
    private String email;
    @Size(min = 8 , message = "Password must be atLeast 8 Characters Long ")
    private String password;
}
