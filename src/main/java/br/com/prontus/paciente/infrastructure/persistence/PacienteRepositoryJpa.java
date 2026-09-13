package br.com.prontus.paciente.infrastructure.persistence;

import br.com.prontus.paciente.domain.FiltroPacientes;
import br.com.prontus.paciente.domain.OrdenacaoPaciente;
import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.PacienteNaoEncontradoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
                throw new PacienteNaoEncontradoException(paciente.getId());
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
        return listar(FiltroPacientes.semFiltros(), primeiraPosicao, quantidade);
    }

    @Override
    public List<Paciente> listar(
            FiltroPacientes filtro,
            int primeiraPosicao,
            int quantidade
    ) {

        return listar(filtro, primeiraPosicao, quantidade, OrdenacaoPaciente.CODIGO_ASC);
    }

    @Override
    public List<Paciente> listar(FiltroPacientes filtro, int primeiraPosicao,
                                 int quantidade, OrdenacaoPaciente ordem) {
        Objects.requireNonNull(filtro, "O filtro é obrigatório.");

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

        String jpql = "SELECT p FROM PacienteEntity p"
                + montarCondicoes(filtro)
                + " ORDER BY " + switch (Objects.requireNonNull(ordem, "A ordenação é obrigatória.")) {
                    case CODIGO_ASC -> "p.id ASC";
                    case CODIGO_DESC -> "p.id DESC";
                    case NOME_ASC -> "UPPER(p.nomeCompleto) ASC, p.id ASC";
                    case NOME_DESC -> "UPPER(p.nomeCompleto) DESC, p.id ASC";
                    case NASCIMENTO_ASC -> "p.dataNascimento ASC, p.id ASC";
                    case NASCIMENTO_DESC -> "p.dataNascimento DESC, p.id ASC";
                };

        TypedQuery<PacienteEntity> consulta = entityManager.createQuery(
                jpql,
                PacienteEntity.class
        );
        aplicarParametros(consulta, filtro);

        return consulta
                .setFirstResult(primeiraPosicao)
                .setMaxResults(quantidade)
                .getResultList()
                .stream()
                .map(PacienteEntity::paraDominio)
                .toList();
    }

    @Override
    public long contar() {
        return contar(FiltroPacientes.semFiltros());
    }

    @Override
    public long contar(FiltroPacientes filtro) {
        Objects.requireNonNull(filtro, "O filtro é obrigatório.");

        String jpql = "SELECT COUNT(p) FROM PacienteEntity p"
                + montarCondicoes(filtro);

        TypedQuery<Long> consulta = entityManager.createQuery(jpql, Long.class);
        aplicarParametros(consulta, filtro);

        return consulta.getSingleResult();
    }

    private String montarCondicoes(FiltroPacientes filtro) {
        List<String> condicoes = new ArrayList<>();

        if (filtro.possuiNome()) {
            condicoes.add("LOWER(p.nomeCompleto) LIKE :nome ESCAPE '!'");
        }

        if (filtro.possuiDataNascimentoIni()) {
            condicoes.add("p.dataNascimento >= :dataNascimentoIni");
        }

        if (filtro.possuiDataNascimentoFim()) {
            condicoes.add("p.dataNascimento <= :dataNascimentoFim");
        }

        return condicoes.isEmpty()
                ? ""
                : " WHERE " + String.join(" AND ", condicoes);
    }

    private void aplicarParametros(
            TypedQuery<?> consulta,
            FiltroPacientes filtro
    ) {
        if (filtro.possuiNome()) {
            consulta.setParameter(
                    "nome",
                    "%" + escaparNome(filtro.nome()) + "%"
            );
        }

        if (filtro.possuiDataNascimentoIni()) {
            consulta.setParameter(
                    "dataNascimentoIni",
                    filtro.dataNascimentoIni()
            );
        }

        if (filtro.possuiDataNascimentoFim()) {
            consulta.setParameter(
                    "dataNascimentoFim",
                    filtro.dataNascimentoFim()
            );
        }
    }

    private String escaparNome(String nome) {
        return nome.toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}