package org.jcvalram.test.springboot.app.repositories;

import org.jcvalram.test.springboot.app.models.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BancoRepository extends JpaRepository<Banco, Long> {

    //List<Banco> findAll();
    //Optional<Banco> findById(Long id);
    //void update(Banco banco);
}
