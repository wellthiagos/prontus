package br.com.prontus.paciente.presentation;

import br.com.prontus.paciente.domain.Paciente;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacienteLinhaTest {

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
    void testCriarLinhaComDadosDoPaciente() {
        Paciente paciente = criarPaciente();

        PacienteLinha linha = new PacienteLinha(paciente);

        assertEquals(paciente.getId(), linha.getId());
        assertEquals(paciente.getNomeCompleto(), linha.getNomeCompleto());
        assertEquals(
                paciente.getDataNascimento(),
                linha.getDataNascimento()
        );
        assertTrue(validator.validate(linha).isEmpty());
    }

    @Test
    void testEditarLinhaSemAlterarPacienteOriginal() {
        Paciente paciente = criarPaciente();
        PacienteLinha linha = new PacienteLinha(paciente);
        LocalDate novoNascimento = LocalDate.of(1991, 6, 21);

        linha.setNomeCompleto("Maria Souza");
        linha.setDataNascimento(novoNascimento);

        assertEquals(10L, linha.getId());
        assertEquals("Maria Souza", linha.getNomeCompleto());
        assertEquals(novoNascimento, linha.getDataNascimento());

        assertEquals("Maria Silva", paciente.getNomeCompleto());
        assertEquals(
                LocalDate.of(1990, 5, 20),
                paciente.getDataNascimento()
        );
        assertTrue(validator.validate(linha).isEmpty());
    }

    @Test
    void testRejeitarPacienteNulo() {
        assertThrows(
                NullPointerException.class,
                () -> new PacienteLinha(null)
        );
    }

    @Test
    void testRejeitarPacienteSemIdentificador() {
        Paciente paciente = new Paciente(
                "Maria Silva",
                LocalDate.of(1990, 5, 20)
        );

        assertThrows(
                NullPointerException.class,
                () -> new PacienteLinha(paciente)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testRejeitarNomeSemConteudo(String nome) {
        PacienteLinha linha = criarLinha();
        linha.setNomeCompleto(nome);

        verificarUnicaViolacao(
                linha,
                "nomeCompleto",
                "O nome completo é obrigatório."
        );
    }

    @Test
    void testAceitarNomeCom255Caracteres() {
        PacienteLinha linha = criarLinha();
        linha.setNomeCompleto("A".repeat(255));

        assertTrue(validator.validate(linha).isEmpty());
    }

    @Test
    void testRejeitarNomeCom256Caracteres() {
        PacienteLinha linha = criarLinha();
        linha.setNomeCompleto("A".repeat(256));

        verificarUnicaViolacao(
                linha,
                "nomeCompleto",
                "O nome completo deve possuir no máximo 255 caracteres."
        );
    }

    @Test
    void testRemoverEspacosNasExtremidadesDoNome() {
        PacienteLinha linha = criarLinha();
        linha.setNomeCompleto(" \tMaria Souza\n ");

        assertEquals("Maria Souza", linha.getNomeCompleto());
        assertTrue(validator.validate(linha).isEmpty());
    }

    @Test
    void testRejeitarDataNascimentoNula() {
        PacienteLinha linha = criarLinha();
        linha.setDataNascimento(null);

        verificarUnicaViolacao(
                linha,
                "dataNascimento",
                "A data de nascimento é obrigatória."
        );
    }

    @Test
    void testAceitarNascimentoNaDataAtual() {
        PacienteLinha linha = criarLinha();
        linha.setDataNascimento(HOJE);

        assertTrue(validator.validate(linha).isEmpty());
    }

    @Test
    void testRejeitarNascimentoFuturo() {
        PacienteLinha linha = criarLinha();
        linha.setDataNascimento(HOJE.plusDays(1));

        verificarUnicaViolacao(
                linha,
                "dataNascimento",
                "A data de nascimento não pode ser futura."
        );
    }

    private Paciente criarPaciente() {
        return Paciente.reconstituir(
                10L,
                "Maria Silva",
                LocalDate.of(1990, 5, 20)
        );
    }

    private PacienteLinha criarLinha() {
        return new PacienteLinha(criarPaciente());
    }

    private void verificarUnicaViolacao(
            PacienteLinha linha,
            String campo,
            String mensagem
    ) {
        var violacoes = validator.validate(linha);

        assertEquals(1, violacoes.size());

        var violacao = violacoes.iterator().next();

        assertEquals(campo, violacao.getPropertyPath().toString());
        assertEquals(mensagem, violacao.getMessage());
    }
}