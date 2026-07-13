package ec.edu.ups.icc.fundamentos01.security.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

/**
 * UserDetailsServiceImpl: Carga usuarios activos desde la base de datos para Spring Security
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * loadUserByUsername: Busca al usuario por su email y valida que no esté eliminado lógicamente.
     * * @param email: Email del usuario (mapeado como username en el contrato de Spring Security)
     * @return UserDetails: El usuario transformado al formato de seguridad del framework
     * @throws UsernameNotFoundException: Si el email no existe o el usuario está de baja
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // 1. Buscar usuario activo por email en la base de datos
        UserEntity user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado o inactivo con email: " + email));

        // 2. Convertir y retornar UserEntity → UserDetailsImpl usando el Factory Method
        return UserDetailsImpl.build(user);
    }
}