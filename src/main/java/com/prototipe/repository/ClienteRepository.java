package com.prototipe.repository;



import com.prototipe.model.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    public Optional<Cliente> findByRfc(String rfc) {
        return find("rfc", rfc).firstResultOptional();
    }

    public List<Cliente> findActivos() {
        return find("activo", "S").list();
    }

    public List<Cliente> findByNombre(String nombre) {
        return find("UPPER(nombre) LIKE UPPER(?1)", "%" + nombre + "%").list();
    }
}
