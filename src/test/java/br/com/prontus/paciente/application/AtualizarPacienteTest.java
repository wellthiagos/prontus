package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.DomainException;
import br.com.prontus.paciente.domain.exception.PacienteNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AtualizarPacienteTest {

    private PacienteRepository repository;
    private AtualizarPaciente atualizarPaciente;

    @BeforeEach
    void preparar() {
        repository = mock(PacienteRepository.class);
        atualizarPaciente = new AtualizarPaciente(repository);
    }

    @Test
    void testAtualizarPacienteComDadosValidos() {
        LocalDate nascimento = LocalDate.of(1990, 5, 21);

        Paciente salvo = Paciente.reconstituir(
                1L,
                "Maria Souza",
                nascimento
        );

        when(repository.salvar(any(Paciente.class)))
                .thenReturn(salvo);

        Paciente resultado = atualizarPaciente.executar(
                1L,
                "Maria Souza",
                nascimento
        );

        ArgumentCaptor<Paciente> captor =
                ArgumentCaptor.forClass(Paciente.class);

        verify(repository).salvar(captor.capture());

        Paciente enviado = captor.getValue();

        assertEquals(1L, enviado.getId());
        assertEquals("Maria Souza", enviado.getNomeCompleto());
        assertEquals(nascimento, enviado.getDataNascimento());
        assertSame(salvo, resultado);
    }

    @Test
    void testNormalizarNomeAntesDeAtualizar() {
        LocalDate nascimento = LocalDate.of(1990, 5, 20);

        when(repository.salvar(any(Paciente.class)))
                .thenReturn(Paciente.reconstituir(
                        1L,
                        "Maria Souza",
                        nascimento
                ));

        atualizarPaciente.executar(
                1L,
                "  Maria Souza  ",
                nascimento
        );

        ArgumentCaptor<Paciente> captor =
                ArgumentCaptor.forClass(Paciente.class);

        verify(repository).salvar(captor.capture());

        assertEquals("Maria Souza", captor.getValue().getNomeCompleto());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void testRejeitarIdentificadorInvalidoSemChamarRepositorio(Long id) {
        assertThrows(
                DomainException.class,
                () -> atualizarPaciente.executar(
                        id,
                        "Maria Souza",
                        LocalDate.of(1990, 5, 20)
                )
        );

        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testRejeitarNomeInvalidoSemChamarRepositorio(String nome) {
        assertThrows(
                DomainException.class,
                () -> atualizarPaciente.executar(
                        1L,
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
                () -> atualizarPaciente.executar(
                        1L,
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
                () -> atualizarPaciente.executar(
                        1L,
                        "Maria Souza",
                        null
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void testRejeitarNascimentoFuturoSemChamarRepositorio() {
        assertThrows(
                DomainException.class,
                () -> atualizarPaciente.executar(
                        1L,
                        "Maria Souza",
                        LocalDate.MAX
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void testPropagarPacienteNaoEncontrado() {
        PacienteNaoEncontradoException falhaEsperada =
                new PacienteNaoEncontradoException(999L);

        when(repository.salvar(any(Paciente.class)))
                .thenThrow(falhaEsperada);

        PacienteNaoEncontradoException falhaRecebida = assertThrows(
                PacienteNaoEncontradoException.class,
                () -> atualizarPaciente.executar(
                        999L,
                        "Maria Souza",
                        LocalDate.of(1990, 5, 20)
                )
        );

        assertSame(falhaEsperada, falhaRecebida);
        verify(repository).salvar(any(Paciente.class));
    }

    @Test
    void testPropagarFalhaDoRepositorio() {
        RuntimeException falhaEsperada =
                new RuntimeException("Falha simulada na persistência.");

        when(repository.salvar(any(Paciente.class)))
                .thenThrow(falhaEsperada);

        RuntimeException falhaRecebida = assertThrows(
                RuntimeException.class,
                () -> atualizarPaciente.executar(
                        1L,
                        "Maria Souza",
                        LocalDate.of(1990, 5, 20)
                )
        );

        assertSame(falhaEsperada, falhaRecebida);
    }

    @Test
    void testRejeitarRepositorioNulo() {
        assertThrows(
                NullPointerException.class,
                () -> new AtualizarPaciente(null)
        );
    }
}