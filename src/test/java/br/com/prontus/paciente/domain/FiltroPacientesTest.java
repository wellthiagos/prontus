package br.com.prontus.paciente.domain;

import br.com.prontus.paciente.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiltroPacientesTest {

    @Test
    void testRejeitarDataInicialPosteriorAFinal() {
        LocalDate inicio = LocalDate.of(1990, 5, 21);
        LocalDate fim = LocalDate.of(1990, 5, 20);

        DomainException excecao = assertThrows(
                DomainException.class,
                () -> new FiltroPacientes(null, inicio, fim)
        );

        assertEquals(
                "A data de nascimento inicial não pode ser posterior à final.",
                excecao.getMessage()
        );
    }

    @Test
    void testAceitarDatasInicialEFinalIguais() {
        LocalDate nascimento = LocalDate.of(1990, 5, 20);

        FiltroPacientes filtro = new FiltroPacientes(
                null,
                nascimento,
                nascimento
        );

        assertEquals(nascimento, filtro.dataNascimentoIni());
        assertEquals(nascimento, filtro.dataNascimentoFim());
    }

    @Test
    void testRejeitarDataInicialFutura() {
        LocalDate futura = LocalDate.now().plusDays(2);

        DomainException excecao = assertThrows(
                DomainException.class,
                () -> new FiltroPacientes(null, futura, null)
        );

        assertEquals(
                "A data de nascimento inicial não pode ser futura.",
                excecao.getMessage()
        );
    }

    @Test
    void testRejeitarDataFinalFutura() {
        LocalDate futura = LocalDate.now().plusDays(2);

        DomainException excecao = assertThrows(
                DomainException.class,
                () -> new FiltroPacientes(null, null, futura)
        );

        assertEquals(
                "A data de nascimento final não pode ser futura.",
                excecao.getMessage()
        );
    }

    @Test
    void testAceitarDataAtualNosDoisLimites() {
        LocalDate hoje = LocalDate.now();

        FiltroPacientes filtro = new FiltroPacientes(null, hoje, hoje);

        assertEquals(hoje, filtro.dataNascimentoIni());
        assertEquals(hoje, filtro.dataNascimentoFim());
    }

    @Test
    void testRejeitarAnoZeroNaDataInicial() {
        LocalDate data = LocalDate.of(0, 1, 1);

        DomainException excecao = assertThrows(
                DomainException.class,
                () -> new FiltroPacientes(null, data, null)
        );

        assertEquals(
                "A data de nascimento inicial deve possuir ano maior ou igual a 1.",
                excecao.getMessage()
        );
    }

    @Test
    void testRejeitarAnoZeroNaDataFinal() {
        LocalDate data = LocalDate.of(0, 1, 1);

        DomainException excecao = assertThrows(
                DomainException.class,
                () -> new FiltroPacientes(null, null, data)
        );

        assertEquals(
                "A data de nascimento final deve possuir ano maior ou igual a 1.",
                excecao.getMessage()
        );
    }

    @Test
    void testAceitarPrimeiroAnoValido() {
        LocalDate data = LocalDate.of(1, 1, 1);

        FiltroPacientes filtro = new FiltroPacientes(null, data, data);

        assertEquals(data, filtro.dataNascimentoIni());
        assertEquals(data, filtro.dataNascimentoFim());
    }
}