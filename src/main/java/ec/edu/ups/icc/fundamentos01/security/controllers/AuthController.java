package ec.edu.ups.icc.fundamentos01.security.controllers;

import ec.edu.ups.icc.fundamentos01.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RefreshTokenRequestDto;
import ec.edu.ups.icc.fundamentos01.security.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/*
 * Controlador REST para autenticación.
 *
 * Estos endpoints son públicos (no requieren access token):
 * - login
 * - register
 * - refresh (se valida con refresh token, no con access token)
 * - logout (se valida con refresh token, no con access token)
 *
 * Por eso NO se usa @SecurityRequirement a nivel de clase.
 */
@Tag(
        name = "Autenticación",
        description = "Endpoints públicos para registro, login, renovación y cierre de sesión"
)
@RestController
@RequestMapping("/auth") // Prefijo base global: /auth
public class AuthController {

    private final AuthService authService;

    // Inyección por constructor del servicio de autenticación
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /auth/login
     * Endpoint para iniciar sesión. Retorna un 200 OK con el JWT y datos del usuario.
     */
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida credenciales y devuelve un access token y un refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login correcto"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        AuthResponseDto response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/register
     * Endpoint para crear nuevas cuentas. Retorna un 201 CREATED con el JWT generado para autologin.
     */
    @Operation(
            summary = "Registrar usuario",
            description = "Crea un nuevo usuario, asigna ROLE_USER y devuelve un access token y un refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        AuthResponseDto response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/refresh
     * Recibe un refresh token válido y devuelve nuevos tokens (access y refresh).
     */
    @Operation(
            summary = "Renovar tokens",
            description = """
                    Recibe un refresh token válido y devuelve un nuevo access token
                    y un nuevo refresh token (rotación de refresh token).

                    El refresh token anterior queda revocado tras esta operación.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens renovados correctamente"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido, expirado o revocado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        AuthResponseDto response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/logout
     * Revoca el refresh token recibido para invalidar la sesión actual.
     */
    @Operation(
            summary = "Cerrar sesión",
            description = "Revoca el refresh token recibido. Después de esto, ya no podrá usarse para renovar sesión."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sesión cerrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido, expirado o revocado")
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        authService.logout(request);
    }
}