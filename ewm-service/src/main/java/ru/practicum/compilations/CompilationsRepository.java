package ru.practicum.compilations;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompilationsRepository extends JpaRepository<Compilation, Long> {
    boolean existsByTitle(String title);
}
