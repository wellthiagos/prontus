package br.com.prontus.paciente.infrastructure.persistence;

import br.com.prontus.paciente.domain.Paciente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PacienteRepositoryJpaTest {

    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private PacienteRepositoryJpa repository;

    @BeforeEach
    void preparar() {
        entityManagerFactory =
                Persistence.createEntityManagerFactory("prontusTestPU");

        entityManager = entityManagerFactory.createEntityManager();
        repository = new PacienteRepositoryJpa(entityManager);
    }

    @AfterEach
    void finalizar() {
        try {
            if (entityManager != null && entityManager.isOpen()) {
                try {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                } finally {
                    entityManager.close();
                }
            }
        } finally {
            if (entityManagerFactory != null
                    && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        }
    }

    @Test
    void testSalvarNovoPaciente() {
        Paciente paciente = new Paciente(
                "Maria Silva",
                LocalDate.of(1990, 5, 20)
        );

        entityManager.getTransaction().begin();

        Paciente salvo = repository.salvar(paciente);

        entityManager.getTransaction().commit();
        entityManager.clear();

        assertNotNull(salvo.getId());
        assertTrue(salvo.getId() > 0);

        PacienteEntity entidade = entityManager.find(
                PacienteEntity.class,
                salvo.getId()
        );

        assertNotNull(entidade);
        assertEquals("Maria Silva", entidade.getNomeCompleto());
        assertEquals(
                LocalDate.of(1990, 5, 20),
                entidade.getDataNascimento()
        );

        assertNotNull(entidade.getDataCadastro());
        assertEquals(
                entidade.getDataCadastro(),
                entidade.getDataAtualizacao()
        );
    }

    @Test
    void testBuscarPacientePorId() {
        Paciente paciente = new Paciente(
                "Ana Souza",
                LocalDate.of(1985, 8, 15)
        );

        entityManager.getTransaction().begin();
        Paciente salvo = repository.salvar(paciente);
        entityManager.getTransaction().commit();

        entityManager.clear();

        Paciente encontrado = repository.buscarPorId(salvo.getId())
                .orElseThrow(() -> new AssertionError(
                        "O paciente salvo deveria ser encontrado."
                ));

        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Ana Souza", encontrado.getNomeCompleto());
        assertEquals(
                LocalDate.of(1985, 8, 15),
                encontrado.getDataNascimento()
        );
    }

    @Test
    void testBuscarPacienteInexistente() {
        var resultado = repository.buscarPorId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void testListarPacientesComPaginacao() {
        LocalDate nascimento = LocalDate.of(1990, 5, 20);

        entityManager.getTransaction().begin();

        Paciente primeiro = repository.salvar(
                new Paciente("Ana Souza", nascimento)
        );
        Paciente segundo = repository.salvar(
                new Paciente("Bruno Lima", nascimento)
        );
        Paciente terceiro = repository.salvar(
                new Paciente("Carla Santos", nascimento)
        );

        entityManager.getTransaction().commit();
        entityManager.clear();

        var primeiraPagina = repository.listar(0, 2);
        var segundaPagina = repository.listar(2, 2);
        var paginaVazia = repository.listar(3, 2);
        long total = repository.contar();

        assertEquals(3L, total);

        assertEquals(2, primeiraPagina.size());
        assertEquals(primeiro.getId(), primeiraPagina.get(0).getId());
        assertEquals(segundo.getId(), primeiraPagina.get(1).getId());

        assertEquals(1, segundaPagina.size());
        assertEquals(terceiro.getId(), segundaPagina.get(0).getId());

        assertTrue(paginaVazia.isEmpty());
    }

    @Test
    void testAtualizarPacientePreservandoDataCadastro() {
        entityManager.getTransaction().begin();

        Paciente salvo = repository.salvar(
                new Paciente(
                        "Maria Silva",
                        LocalDate.of(1990, 5, 20)
                )
        );

        entityManager.getTransaction().commit();
        entityManager.clear();

        PacienteEntity entidadeOriginal = entityManager.find(
                PacienteEntity.class,
                salvo.getId()
        );

        var cadastroOriginal = entidadeOriginal.getDataCadastro();
        var atualizacaoOriginal = cadastroOriginal.minusSeconds(60);

        entityManager.getTransaction().begin();

        entityManager.createNativeQuery(
                        "UPDATE PACIENTE SET DATA_ATUALIZACAO = ? WHERE ID = ?"
                )
                .setParameter(
                        1,
                        atualizacaoOriginal.atOffset(java.time.ZoneOffset.UTC)
                )
                .setParameter(2, salvo.getId())
                .executeUpdate();

        entityManager.getTransaction().commit();
        entityManager.clear();

        Paciente alterado = Paciente.reconstituir(
                salvo.getId(),
                "Maria Souza",
                LocalDate.of(1990, 5, 21)
        );

        entityManager.getTransaction().begin();
        Paciente atualizado = repository.salvar(alterado);
        entityManager.getTransaction().commit();

        entityManager.clear();

        PacienteEntity entidadeAtualizada = entityManager.find(
                PacienteEntity.class,
                salvo.getId()
        );

        assertNotNull(entidadeAtualizada);
        assertEquals(salvo.getId(), atualizado.getId());
        assertEquals("Maria Souza", entidadeAtualizada.getNomeCompleto());
        assertEquals(
                LocalDate.of(1990, 5, 21),
                entidadeAtualizada.getDataNascimento()
        );
        assertEquals(
                cadastroOriginal,
                entidadeAtualizada.getDataCadastro()
        );
        assertNotNull(entidadeAtualizada.getDataAtualizacao());
        assertTrue(
                entidadeAtualizada.getDataAtualizacao()
                        .isAfter(atualizacaoOriginal)
        );
        assertEquals(1L, repository.contar());
    }

    @Test
    void testRejeitarAtualizacaoDePacienteInexistente() {
        Paciente paciente = Paciente.reconstituir(
                999L,
                "Maria Silva",
                LocalDate.of(1990, 5, 20)
        );

        entityManager.getTransaction().begin();

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> repository.salvar(paciente)
        );

        assertEquals(
                "Paciente não encontrado para atualização.",
                excecao.getMessage()
        );

        entityManager.getTransaction().rollback();
        entityManager.clear();

        assertEquals(0L, repository.contar());
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "0, 0",
            "0, -1"
    })
    void testRejeitarParametrosDePaginacaoInvalidos(
            int primeiraPosicao, int quantidade) {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.listar(primeiraPosicao, quantidade)
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void testRejeitarBuscaComIdentificadorInvalido(Long id) {
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> repository.buscarPorId(id)
        );

        assertEquals(
                "O identificador deve ser positivo.",
                excecao.getMessage()
        );
    }
}