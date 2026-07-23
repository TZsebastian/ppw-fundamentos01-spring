package ec.edu.ups.icc.fundamentos01.security.dtos;

import java.util.Set;

/**
 * DTO de respuesta para login, register y refresh.
 * 
 * token:
 * - representa el access token
 * - se usa en Authorization: Bearer <token>
 * 
 * refreshToken:
 * - se usa solo en /auth/refresh
 * - no debe usarse para consumir endpoints protegidos
 */
public class AuthResponseDto {

    private String token;
    private String refreshToken; // Nuevo campo añadido
    private String type = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private Set<String> roles;

    // Constructor vacío
    public AuthResponseDto() {}

    // Constructor actualizado con el nuevo campo
    public AuthResponseDto(String token, String refreshToken, Long userId, String name, String email, Set<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }

    // Getters y Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; } // Nuevo Getter
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; } // Nuevo Setter

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}