package br.com.prontus.paciente.domain;

import br.com.prontus.paciente.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class PacienteTest {

    @Test
    void testCriarPacienteComDadosValidos() {

        Clock clock = Clock.fixed(
                Instant.parse("2026-09-12T12:00:00Z"),
                ZoneOffset.UTC
        );

        LocalDate dataNascimento = LocalDate.of(1990, 5, 20);

        Paciente paciente = new Paciente(
                "Maria Silva",
                dataNascimento,
                clock
        );

        assertNull(paciente.getId());
        assertEquals("Maria Silva", paciente.getNomeCompleto());
        assertEquals(dataNascimento, paciente.getDataNascimento());
    }

    @Test
    void testRejeitarDataNascimentoFutura() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-12T12:00:00Z"),
                ZoneOffset.UTC
        );

        LocalDate dataNascimentoFutura = LocalDate.of(2026, 9, 13);

        DomainException excecao = assertThrows(
                DomainException.class,
                () -> new Paciente(
                        "Maria Silva",
                        dataNascimentoFutura,
                        clock
                )
        );

        assertEquals(
                "A data de nascimento não pode ser futura.",
                excecao.getMessage()
        );
    }

    @Test
    void testPermitirNascimentoNaDataAtual() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-12T12:00:00Z"),
                ZoneOffset.UTC
        );

        LocalDate dataAtual = LocalDate.of(2026, 9, 12);

        Paciente paciente = new Paciente(
                "Maria Silva",
                dataAtual,
                clock
        );

        assertEquals(dataAtual, paciente.getDataNascimento());
    }

    @Test
    void testRejeitarDataNascimentoNula() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-12T12:00:00Z"),
                ZoneOffset.UTC
        );

        DomainException excecao = assertThrows(
                DomainException.class,
                () -> new Paciente("Maria Silva", null, clock)
        );

        assertEquals(
                "A data de nascimento é obrigatória.",
                excecao.getMessage()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testRejeitarNomeCompletoInvalido(String nomeCompleto) {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-12T12:00:00Z"),
                ZoneOffset.UTC
        );

        LocalDate dataNascimento = LocalDate.of(1990, 5, 20);

        DomainException excecao = assertThrows(
                DomainException.class,
                () -> new Paciente(nomeCompleto, dataNascimento, clock)
        );

        assertEquals(
                "O nome completo é obrigatório.",
                excecao.getMessage()
        );
    }

    @Test
    void testRemoverEspacosNasExtremidadesDoNomeCompleto() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-12T12:00:00Z"),
                ZoneOffset.UTC
        );

        LocalDate dataNascimento = LocalDate.of(1990, 5, 20);

        Paciente paciente = new Paciente(
                "  Maria Silva  ",
                dataNascimento,
                clock
        );

        assertEquals("Maria Silva", paciente.getNomeCompleto());
    }

    @Test
    void testReconstituirPacienteComIdentificadorValido() {
        Long id = 1L;
        String nomeCompleto = "Maria Silva";
        LocalDate dataNascimento = LocalDate.of(1990, 5, 20);

        Paciente paciente = Paciente.reconstituir(
                id,
                nomeCompleto,
                dataNascimento
        );

        assertEquals(id, paciente.getId());
        assertEquals(nomeCompleto, paciente.getNomeCompleto());
        assertEquals(dataNascimento, paciente.getDataNascimento());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void testRejeitarReconstituicaoComIdentificadorInvalido(Long id) {
        // Preparar
        LocalDate dataNascimento = LocalDate.of(1990, 5, 20);

        // Executar e verificar
        DomainException excecao = assertThrows(
                DomainException.class,
                () -> Paciente.reconstituir(
                        id,
                        "Maria Silva",
                        dataNascimento
                )
        );

        assertEquals(
                "O identificador do paciente deve ser positivo.",
                excecao.getMessage()
        );
    }

    @Test
    void testPermitirNomeCompletoCom255Caracteres() {
        String nome = "A".repeat(255);

        Paciente paciente = new Paciente(
                nome,
                LocalDate.of(1990, 5, 20)
        );

        assertEquals(nome, paciente.getNomeCompleto());
    }

    @Test
    void testRejeitarNomeCompletoCom256Caracteres() {
        String nome = "A".repeat(256);

        DomainException excecao = assertThrows(
                DomainException.class,
                () -> new Paciente(
                        nome,
                        LocalDate.of(1990, 5, 20)
                )
        );

        assertEquals(
                "O nome completo deve ter no máximo 255 caracteres.",
                excecao.getMessage()
        );
    }

    @Test
    void testValidarTamanhoDoNomeAposRemoverEspacosExternos() {
        String nome = "A".repeat(255);

        Paciente paciente = new Paciente(
                "  " + nome + "  ",
                LocalDate.of(1990, 5, 20)
        );

        assertEquals(nome, paciente.getNomeCompleto());
    }
}