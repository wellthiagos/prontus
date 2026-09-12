package br.com.prontus.paciente.domain;

import br.com.prontus.paciente.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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
}