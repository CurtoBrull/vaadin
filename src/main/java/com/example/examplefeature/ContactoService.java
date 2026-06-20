package com.example.examplefeature;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ContactoService {

    private final ContactoRepository contactoRepository;

    @Transactional(readOnly = true)
    public Slice<Contacto> list(Pageable pageable, @Nullable String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return contactoRepository.findAllBy(pageable);
        }
        return contactoRepository.findAllByNombreContainingIgnoreCase(filtro, pageable);
    }

    @Transactional
    public Contacto save(Contacto contacto) {
        return contactoRepository.saveAndFlush(contacto);
    }

    @Transactional
    public void delete(Long id) {
        contactoRepository.deleteById(id);
    }
}
