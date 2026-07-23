package ec.edu.ups.icc.fundamentos01.security.services;

import ec.edu.ups.icc.fundamentos01.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RefreshTokenRequestDto;
import ec.edu.ups.icc.fundamentos01.security.entities.RoleEntity;
import ec.edu.ups.icc.fundamentos01.security.entities.RefreshTokenEntity;
import ec.edu.ups.icc.fundamentos01.security.enums.RoleName;
import ec.edu.ups.icc.fundamentos01.security.repositories.RoleRepository;
import ec.edu.ups.icc.fundamentos01.security.utils.JwtUtil;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Login:
     * 
     * 1. Valida credenciales.
     * 2. Genera access token (JWT).
     * 3. Revoca refresh tokens anteriores.
     * 4. Genera refresh token nuevo.
     * 5. Devuelve ambos tokens al cliente.
     */
    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        
        // 1. Validar email y password con Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        // 2. Establecer usuario autenticado en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generar Access Token (JWT) con los datos del usuario
        String accessToken = jwtUtil.generateAccessToken(authentication);

        // 4. Extraer información del usuario autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserEntity user = findActiveUserById(userDetails.getId());

        /* 
         * En esta práctica se deja una sola sesión activa por usuario.
         * Por eso se revocan todos los refresh tokens anteriores.
         */
        refreshTokenService.revokeAllByUser(user);

        // 5. Crear el nuevo Refresh Token
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(
                user,
                userDetails
        );

        // 6. Construir y retornar la respuesta
        return buildAuthResponse(
                accessToken,
                refreshToken.getToken(),
                user
        );
    }

    /**
     * Registro:
     * 
     * 1. Crea el usuario.
     * 2. Asigna ROLE_USER.
     * 3. Genera access token.
     * 4. Genera refresh token.
     */
    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequest) {
        
        // 1. Validar que el email no esté tomado
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }

        // 2. Crear nueva entidad mapeando setters (Usa passwordHash de tu UserEntity)
        UserEntity user = new UserEntity();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        // 3. Asignar el rol por defecto (ROLE_USER)
        RoleEntity userRole = roleRepository.findByName(RoleName.ROLE_USER)
            .orElseThrow(() -> new BadRequestException("Rol por defecto no encontrado"));

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // 4. Persistir en PostgreSQL
        UserEntity savedUser = userRepository.save(user);

        // 5. Autologin: Generar tokens directamente con UserDetails sin pasar por el authenticationManager
        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        String accessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);

        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(
                savedUser,
                userDetails
        );

        // 6. Retornar JWT (access y refresh) + datos del usuario registrado
        return buildAuthResponse(
                accessToken,
                refreshToken.getToken(),
                savedUser
        );
    }

    /**
     * Refresh:
     * 
     * 1. Valida el refresh token recibido.
     * 2. Revoca el refresh token usado.
     * 3. Genera nuevo access token.
     * 4. Genera nuevo refresh token.
     * 
     * Esto se llama rotación de refresh token.
     */
    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {

        RefreshTokenEntity currentRefreshToken =
                refreshTokenService.validateAndGetActiveToken(request.getRefreshToken());

        UserEntity user = currentRefreshToken.getUser();

        // Revocar token utilizado
        refreshTokenService.revoke(currentRefreshToken);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // Generar nuevos tokens
        String newAccessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);

        RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(
                user,
                userDetails
        );

        return buildAuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                user
        );
    }

    /**
     * Logout:
     * 
     * Revoca el refresh token enviado.
     * Después de esto, ese refresh token ya no podrá usarse para renovar sesión.
     */
    @Transactional
    public void logout(RefreshTokenRequestDto request) {

        RefreshTokenEntity refreshToken =
                refreshTokenService.validateAndGetActiveToken(request.getRefreshToken());

        refreshTokenService.revoke(refreshToken);
    }

    /**
     * Busca un usuario activo por id.
     */
    private UserEntity findActiveUserById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BadRequestException("Usuario no válido"));
    }

    /**
     * Construye la respuesta de autenticación.
     */
    private AuthResponseDto buildAuthResponse(
            String accessToken,
            String refreshToken,
            UserEntity user
    ) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles
        );
    }
}