package br.com.prontus.paciente.infrastructure.persistence;

import br.com.prontus.paciente.domain.Paciente;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OracleIT {
    static EntityManagerFactory factory;
    EntityManager em;
    static String url;
    static String password;

    @BeforeAll static void abrirBanco() {
        url = Objects.requireNonNull(System.getenv("ORACLE_IT_URL"), "Execute pelo compose.oracle-it.yaml");
        password = Objects.requireNonNull(System.getenv("ORACLE_IT_PASSWORD"));
        // Valida o esquema criado pelos scripts reais; Hibernate nao cria tabelas.
        factory = Persistence.createEntityManagerFactory("prontusTestPU", Map.of(
                "jakarta.persistence.jdbc.driver", "oracle.jdbc.OracleDriver",
                "jakarta.persistence.jdbc.url", url,
                "jakarta.persistence.jdbc.user", "prontus",
                "jakarta.persistence.jdbc.password", password,
                "hibernate.hbm2ddl.auto", "validate"));
    }
    @AfterAll static void fecharBanco() { if (factory != null) factory.close(); }
    @BeforeEach void iniciar() {
        em = factory.createEntityManager();
        em.getTransaction().begin();
    }
    @AfterEach void desfazer() {
        if (em != null) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }
    @Test void inicializacaoAutomatica() {
        assertEquals(0L, new PacienteRepositoryJpa(em).contar());
        assertEquals("VALID", em.createNativeQuery("SELECT STATUS FROM USER_OBJECTS WHERE OBJECT_NAME='P_PATIENT_AGE' AND OBJECT_TYPE='PROCEDURE'").getSingleResult());
        assertEquals(1, ((Number) em.createNativeQuery("SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME='SEQ_PACIENTE'").getSingleResult()).intValue());
    }
    @Test void adapterJavaChamaProcedureOracle() throws Exception {
        LocalDate hoje = (LocalDate) em.createNativeQuery("SELECT TRUNC(CAST(SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo' AS DATE)) FROM DUAL", LocalDate.class).getSingleResult();
        LocalDate nascimento = LocalDate.of(2000, 1, 1);
        Paciente paciente = new PacienteRepositoryJpa(em).salvar(new Paciente("Integracao", nascimento));
        CalculoIdadeOracle calculo = new CalculoIdadeOracle();
        var campo = CalculoIdadeOracle.class.getDeclaredField("entityManager");
        campo.setAccessible(true);
        campo.set(calculo, em);
        assertEquals(Period.between(nascimento, hoje).getYears(), calculo.calcular(paciente.getId()));
        assertThrows(IllegalArgumentException.class, () -> calculo.calcular(null));
        assertThrows(IllegalArgumentException.class, () -> calculo.calcular(0L));
        assertThrows(PersistenceException.class, () -> calculo.calcular(Long.MAX_VALUE));
    }
    @Test void onzeRegistrosEmDuasPaginas() {
        var repository = new PacienteRepositoryJpa(em);
        for (int i = 0; i < 11; i++) repository.salvar(new Paciente("Registro " + i, LocalDate.of(2000, 1, 1)));
        assertEquals(11, repository.contar());
        var primeira = repository.listar(0, 10);
        var segunda = repository.listar(10, 10);
        assertEquals(10, primeira.size());
        assertEquals(1, segunda.size());
        assertTrue(primeira.stream().noneMatch(p -> p.getId().equals(segunda.get(0).getId())));
        assertEquals(11, repository.listar(0, 500).size());
    }
    @ParameterizedTest
    @CsvSource({"2000-09-13,2026-09-12,25", "2000-09-13,2026-09-13,26", "2000-09-13,2026-09-14,26",
        "2000-02-29,2025-02-28,24", "2000-02-29,2025-03-01,25", "2000-02-29,2024-02-29,24",
        "2026-09-13,2026-09-13,0"})
    void calcularEmDatasFixas(String nascimento, String referencia, int esperado) throws Exception {
        try (Connection c = DriverManager.getConnection(url, "prontus", password)) {
            c.setAutoCommit(false);
            try {
                long id = inserir(c, nascimento);
                assertEquals(esperado, calcular(c, BigDecimal.valueOf(id), referencia));
            } finally { c.rollback(); }
        }
    }
    @ParameterizedTest
    @CsvSource({"NULL,20001", "0,20001", "-1,20001", "1.5,20001", "999999999,20003"})
    void rejeitarIdentificador(String id, int codigo) throws Exception {
        try (Connection c = DriverManager.getConnection(url, "prontus", password)) {
            SQLException erro = assertThrows(SQLException.class, () -> calcular(c, id.equals("NULL") ? null : new BigDecimal(id), "2026-09-13"));
            assertEquals(codigo, erro.getErrorCode());
        }
    }
    @Test void rejeitarNascimentoFuturo() throws Exception {
        try (Connection c = DriverManager.getConnection(url, "prontus", password)) {
            c.setAutoCommit(false);
            try {
                long id = inserir(c, "2026-09-14");
                SQLException erro = assertThrows(SQLException.class, () -> calcular(c, BigDecimal.valueOf(id), "2026-09-13"));
                assertEquals(20002, erro.getErrorCode());
            } finally { c.rollback(); }
        }
    }
    static long inserir(Connection c, String nascimento) throws SQLException {
        long id;
        try (var st = c.createStatement(); var rs = st.executeQuery("SELECT SEQ_PACIENTE.NEXTVAL FROM DUAL")) { rs.next(); id = rs.getLong(1); }
        try (var st = c.prepareStatement("INSERT INTO PACIENTE VALUES (?, 'Teste Oracle', ?, SYSTIMESTAMP, SYSTIMESTAMP)")) {
            st.setLong(1, id); st.setTimestamp(2, Timestamp.valueOf(LocalDate.parse(nascimento).atStartOfDay())); st.executeUpdate();
        }
        return id;
    }
    static int calcular(Connection c, BigDecimal id, String referencia) throws SQLException {
        try (var st = c.prepareCall("{call P_PATIENT_AGE(?, ?, ?)}")) {
            st.setBigDecimal(1, id); st.registerOutParameter(2, Types.NUMERIC);
            st.setDate(3, java.sql.Date.valueOf(referencia)); st.execute();
            return st.getBigDecimal(2).intValueExact();
        }
    }
}