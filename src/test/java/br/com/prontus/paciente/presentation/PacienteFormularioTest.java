package br.com.prontus.paciente.presentation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacienteFormularioTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 12);

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void prepararValidador() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-12T12:00:00Z"),
                ZoneOffset.UTC
        );

        validatorFactory = Validation.byDefaultProvider()
                .configure()
                .clockProvider(() -> clock)
                .buildValidatorFactory();

        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void fecharValidador() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void testAceitarFormularioValido() {
        PacienteFormulario formulario = criarFormularioValido();

        assertTrue(validator.validate(formulario).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testRejeitarNomeSemConteudo(String nome) {
        PacienteFormulario formulario = criarFormularioValido();
        formulario.setNomeCompleto(nome);

        verificarUnicaViolacao(
                formulario,
                "nomeCompleto",
                "O nome completo é obrigatório."
        );
    }

    @Test
    void testAceitarNomeCom255Caracteres() {
        PacienteFormulario formulario = criarFormularioValido();
        formulario.setNomeCompleto("A".repeat(255));

        assertTrue(validator.validate(formulario).isEmpty());
    }

    @Test
    void testRejeitarNomeCom256Caracteres() {
        PacienteFormulario formulario = criarFormularioValido();
        formulario.setNomeCompleto("A".repeat(256));

        verificarUnicaViolacao(
                formulario,
                "nomeCompleto",
                "O nome completo deve possuir no máximo 255 caracteres."
        );
    }

    @Test
    void testRemoverEspacosNasExtremidadesDoNome() {
        PacienteFormulario formulario = criarFormularioValido();
        formulario.setNomeCompleto(" \tMaria Silva\n ");

        assertEquals("Maria Silva", formulario.getNomeCompleto());
        assertTrue(validator.validate(formulario).isEmpty());
    }

    @Test
    void testValidarTamanhoDepoisDeRemoverEspacosExternos() {
        PacienteFormulario formulario = criarFormularioValido();
        String nome = "A".repeat(255);

        formulario.setNomeCompleto("  " + nome + "  ");

        assertEquals(nome, formulario.getNomeCompleto());
        assertTrue(validator.validate(formulario).isEmpty());
    }

    @Test
    void testRejeitarDataNascimentoNula() {
        PacienteFormulario formulario = criarFormularioValido();
        formulario.setDataNascimento(null);

        verificarUnicaViolacao(
                formulario,
                "dataNascimento",
                "A data de nascimento é obrigatória."
        );
    }

    @Test
    void testAceitarNascimentoNaDataAtual() {
        PacienteFormulario formulario = criarFormularioValido();
        formulario.setDataNascimento(HOJE);

        assertTrue(validator.validate(formulario).isEmpty());
    }

    @Test
    void testRejeitarNascimentoNoDiaSeguinte() {
        PacienteFormulario formulario = criarFormularioValido();
        formulario.setDataNascimento(HOJE.plusDays(1));

        verificarUnicaViolacao(
                formulario,
                "dataNascimento",
                "A data de nascimento não pode ser futura."
        );
    }

    private PacienteFormulario criarFormularioValido() {
        PacienteFormulario formulario = new PacienteFormulario();
        formulario.setNomeCompleto("Maria Silva");
        formulario.setDataNascimento(LocalDate.of(1990, 5, 20));
        return formulario;
    }

    private void verificarUnicaViolacao(
            PacienteFormulario formulario,
            String campo,
            String mensagem
    ) {
        var violacoes = validator.validate(formulario);

        assertEquals(1, violacoes.size());

        var violacao = violacoes.iterator().next();

        assertEquals(campo, violacao.getPropertyPath().toString());
        assertEquals(mensagem, violacao.getMessage());
    }
}