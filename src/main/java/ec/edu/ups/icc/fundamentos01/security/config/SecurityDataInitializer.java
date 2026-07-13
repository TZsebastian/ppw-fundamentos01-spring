package ec.edu.ups.icc.fundamentos01.security.config;

import ec.edu.ups.icc.fundamentos01.security.entities.RoleEntity;
import ec.edu.ups.icc.fundamentos01.security.enums.RoleName;
import ec.edu.ups.icc.fundamentos01.security.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SecurityDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public SecurityDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Inicializar rol estándar de usuario
        createRoleIfNotExists(
                RoleName.ROLE_USER,
                "Usuario estándar del sistema"
        );

        // Inicializar rol administrativo
        createRoleIfNotExists(
                RoleName.ROLE_ADMIN,
                "Administrador del sistema"
        );
    }

    private void createRoleIfNotExists(RoleName roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            RoleEntity role = new RoleEntity(roleName, description);
            roleRepository.save(role);
        }
    }
}