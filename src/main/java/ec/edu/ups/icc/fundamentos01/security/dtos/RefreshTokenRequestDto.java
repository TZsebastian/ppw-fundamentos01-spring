package ec.edu.ups.icc.fundamentos01.security.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO usado para recibir un refresh token desde el cliente.
 * 
 * Se usa en:
 * POST /api/auth/refresh
 * POST /api/auth/logout
 */
@Schema(description = "Datos requeridos para renovar sesión o cerrar sesión")
public class RefreshTokenRequestDto {

    @Schema(description = "Refresh token obtenido en login o register", example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;

    // Constructor vacío
    public RefreshTokenRequestDto() {
    }

    // Constructor con parámetros
    public RefreshTokenRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getter para refreshToken
    public String getRefreshToken() {
        return refreshToken;
    }

    // Setter para refreshToken
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}