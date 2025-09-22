package demo.app.demogradle.infrastructure.persistence.repository;

import demo.app.demogradle.infrastructure.persistence.entity.VehiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoJpaRepository extends JpaRepository<VehiculoEntity, String> {
    
    @Query("SELECT v FROM VehiculoEntity v WHERE v.activo = true")
    List<VehiculoEntity> findByActivoTrue();
}
