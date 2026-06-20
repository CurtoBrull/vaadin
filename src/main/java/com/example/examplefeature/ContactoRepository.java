package com.example.examplefeature;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    Slice<Contacto> findAllBy(Pageable pageable);
    Slice<Contacto> findAllByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
