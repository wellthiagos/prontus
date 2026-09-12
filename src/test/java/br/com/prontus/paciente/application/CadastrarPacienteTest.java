package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CadastrarPacienteTest {

    private PacienteRepository repository;
    private CadastrarPaciente cadastrarPaciente;

    @BeforeEach
    void preparar() {
        repository = mock(PacienteRepository.class);
        cadastrarPaciente = new CadastrarPaciente(repository);
    }

    @Test
    void testCadastrarPacienteComDadosValidos() {
        LocalDate nascimento = LocalDate.of(1990, 5, 20);

        Paciente salvo = Paciente.reconstituir(
                1L,
                "Maria Silva",
                nascimento
        );

        when(repository.salvar(any(Paciente.class)))
                .thenReturn(salvo);

        Paciente resultado = cadastrarPaciente.executar(
                "Maria Silva",
                nascimento
        );

        ArgumentCaptor<Paciente> captor =
                ArgumentCaptor.forClass(Paciente.class);

        verify(repository).salvar(captor.capture());

        Paciente enviado = captor.getValue();

        assertNull(enviado.getId());
        assertEquals("Maria Silva", enviado.getNomeCompleto());
        assertEquals(nascimento, enviado.getDataNascimento());

        assertSame(salvo, resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testNormalizarNomeAntesDeSalvar() {
        LocalDate nascimento = LocalDate.of(1990, 5, 20);

        when(repository.salvar(any(Paciente.class)))
                .thenReturn(Paciente.reconstituir(
                        1L,
                        "Maria Silva",
                        nascimento
                ));

        cadastrarPaciente.executar("  Maria Silva  ", nascimento);

        ArgumentCaptor<Paciente> captor =
                ArgumentCaptor.forClass(Paciente.class);

        verify(repository).salvar(captor.capture());

        assertEquals("Maria Silva", captor.getValue().getNomeCompleto());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testRejeitarNomeInvalidoSemChamarRepositorio(String nome) {
        assertThrows(
                DomainException.class,
                () -> cadastrarPaciente.executar(
                        nome,
                        LocalDate.of(1990, 5, 20)
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void testRejeitarNomeAcimaDoLimiteSemChamarRepositorio() {
        assertThrows(
                DomainException.class,
                () -> cadastrarPaciente.executar(
                        "A".repeat(256),
                        LocalDate.of(1990, 5, 20)
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void testRejeitarNascimentoNuloSemChamarRepositorio() {
        assertThrows(
                DomainException.class,
                () -> cadastrarPaciente.executar("Maria Silva", null)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void testRejeitarNascimentoFuturoSemChamarRepositorio() {
        assertThrows(
                DomainException.class,
                () -> cadastrarPaciente.executar(
                        "Maria Silva",
                        LocalDate.MAX
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void testPropagarFalhaDoRepositorio() {
        RuntimeException falhaEsperada =
                new RuntimeException("Falha simulada na persistência.");

        when(repository.salvar(any(Paciente.class)))
                .thenThrow(falhaEsperada);

        RuntimeException falhaRecebida = assertThrows(
                RuntimeException.class,
                () -> cadastrarPaciente.executar(
                        "Maria Silva",
                        LocalDate.of(1990, 5, 20)
                )
        );

        assertSame(falhaEsperada, falhaRecebida);
        verify(repository).salvar(any(Paciente.class));
    }

    @Test
    void testRejeitarRepositorioNulo() {
        assertThrows(
                NullPointerException.class,
                () -> new CadastrarPaciente(null)
        );
    }
}