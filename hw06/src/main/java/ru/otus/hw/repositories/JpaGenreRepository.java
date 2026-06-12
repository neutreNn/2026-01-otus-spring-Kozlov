package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaGenreRepository implements GenreRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Genre> findAll() {
        return entityManager.createQuery("""
                        select g
                        from Genre g
                        order by g.id
                        """, Genre.class)
                .getResultList();
    }

    @Override
    public Optional<Genre> findById(long id) {
        return Optional.ofNullable(entityManager.find(Genre.class, id));
    }
}
