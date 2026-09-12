package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.DomainException;
import br.com.prontus.paciente.domain.exception.PacienteNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BuscarPacienteTest {

    private PacienteRepository repository;
    private BuscarPaciente buscarPaciente;

    @BeforeEach
    void preparar() {
        repository = mock(PacienteRepository.class);
        buscarPaciente = new BuscarPaciente(repository);
    }

    @Test
    void testBuscarPacienteExistente() {
        Paciente paciente = Paciente.reconstituir(
                1L,
                "Maria Silva",
                LocalDate.of(1990, 5, 20)
        );

        when(repository.buscarPorId(1L))
                .thenReturn(Optional.of(paciente));

        Paciente resultado = buscarPaciente.executar(1L);

        assertSame(paciente, resultado);
        verify(repository).buscarPorId(1L);
    }

    @Test
    void testRejeitarPacienteInexistente() {
        when(repository.buscarPorId(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                PacienteNaoEncontradoException.class,
                () -> buscarPaciente.executar(999L)
        );

        verify(repository).buscarPorId(999L);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void testRejeitarIdentificadorInvalidoSemChamarRepositorio(Long id) {
        assertThrows(
                DomainException.class,
                () -> buscarPaciente.executar(id)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void testPropagarFalhaDoRepositorio() {
        RuntimeException falhaEsperada =
                new RuntimeException("Falha simulada na consulta.");

        when(repository.buscarPorId(1L))
                .thenThrow(falhaEsperada);

        RuntimeException falhaRecebida = assertThrows(
                RuntimeException.class,
                () -> buscarPaciente.executar(1L)
        );

        assertSame(falhaEsperada, falhaRecebida);
    }

    @Test
    void testRejeitarRepositorioNulo() {
        assertThrows(
                NullPointerException.class,
                () -> new BuscarPaciente(null)
        );
    }
}