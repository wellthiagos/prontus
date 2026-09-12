package br.com.prontus.paciente.infrastructure.persistence;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class PacienteRepositoryJpa implements PacienteRepository {
    @PersistenceContext(unitName = "prontusPU")
    private EntityManager entityManager;

    public PacienteRepositoryJpa() {
    }

    PacienteRepositoryJpa(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(
                entityManager,
                "O EntityManager é obrigatório."
        );
    }

    @Override
    @Transactional
    public Paciente salvar(Paciente paciente) {
        Objects.requireNonNull(paciente, "O paciente é obrigatório.");

        PacienteEntity entidade;

        if (paciente.getId() == null) {
            entidade = new PacienteEntity(paciente);
            entityManager.persist(entidade);
        } else {
            entidade = entityManager.find(
                    PacienteEntity.class,
                    paciente.getId()
            );

            if (entidade == null) {
                throw new IllegalArgumentException(
                        "Paciente não encontrado para atualização."
                );
            }

            entidade.atualizarDados(paciente);
        }

        entityManager.flush();

        return entidade.paraDominio();
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "O identificador deve ser positivo."
            );
        }

        PacienteEntity entidade = entityManager.find(
                PacienteEntity.class,
                id
        );

        return Optional.ofNullable(entidade)
                .map(PacienteEntity::paraDominio);
    }

    @Override
    public List<Paciente> listar(int primeiraPosicao, int quantidade) {
        if (primeiraPosicao < 0) {
            throw new IllegalArgumentException(
                    "A primeira posição não pode ser negativa."
            );
        }

        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser positiva."
            );
        }

        return entityManager.createQuery(
                        "SELECT p FROM PacienteEntity p ORDER BY p.id",
                        PacienteEntity.class
                )
                .setFirstResult(primeiraPosicao)
                .setMaxResults(quantidade)
                .getResultList()
                .stream()
                .map(PacienteEntity::paraDominio)
                .toList();
    }

    @Override
    public long contar() {
        return entityManager.createQuery(
                        "SELECT COUNT(p) FROM PacienteEntity p",
                        Long.class
                )
                .getSingleResult();
    }
}