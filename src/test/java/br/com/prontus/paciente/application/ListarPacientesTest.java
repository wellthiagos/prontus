package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ListarPacientesTest {

    private PacienteRepository repository;
    private ListarPacientes listarPacientes;

    @BeforeEach
    void preparar() {
        repository = mock(PacienteRepository.class);
        listarPacientes = new ListarPacientes(repository);
    }

    @Test
    void testListarPaginaComTotalDeRegistros() {
        Paciente paciente = Paciente.reconstituir(
                11L,
                "Maria Silva",
                LocalDate.of(1990, 5, 20)
        );

        when(repository.listar(10, 5))
                .thenReturn(List.of(paciente));
        when(repository.contar()).thenReturn(11L);

        PaginaPacientes resultado = listarPacientes.executar(10, 5);

        assertEquals(List.of(paciente), resultado.pacientes());
        assertEquals(11L, resultado.totalRegistros());

        verify(repository).listar(10, 5);
        verify(repository).contar();
    }

    @Test
    void testListarBancoSemPacientes() {
        when(repository.listar(0, 10)).thenReturn(List.of());
        when(repository.contar()).thenReturn(0L);

        PaginaPacientes resultado = listarPacientes.executar(0, 10);

        assertTrue(resultado.pacientes().isEmpty());
        assertEquals(0L, resultado.totalRegistros());
    }

    @Test
    void testRetornarPaginaVaziaPreservandoTotal() {
        when(repository.listar(20, 10)).thenReturn(List.of());
        when(repository.contar()).thenReturn(5L);

        PaginaPacientes resultado = listarPacientes.executar(20, 10);

        assertTrue(resultado.pacientes().isEmpty());
        assertEquals(5L, resultado.totalRegistros());
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "0, 0",
            "0, -1"
    })
    void testRejeitarPaginacaoInvalidaSemChamarRepositorio(
            int primeiraPosicao, int quantidade) {

        assertThrows(
                DomainException.class,
                () -> listarPacientes.executar(
                        primeiraPosicao,
                        quantidade
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void testPropagarFalhaDaListagem() {
        RuntimeException falhaEsperada =
                new RuntimeException("Falha simulada na listagem.");

        when(repository.listar(0, 10)).thenThrow(falhaEsperada);

        RuntimeException falhaRecebida = assertThrows(
                RuntimeException.class,
                () -> listarPacientes.executar(0, 10)
        );

        assertSame(falhaEsperada, falhaRecebida);
    }

    @Test
    void testPropagarFalhaDaContagem() {
        RuntimeException falhaEsperada =
                new RuntimeException("Falha simulada na contagem.");

        when(repository.listar(0, 10)).thenReturn(List.of());
        when(repository.contar()).thenThrow(falhaEsperada);

        RuntimeException falhaRecebida = assertThrows(
                RuntimeException.class,
                () -> listarPacientes.executar(0, 10)
        );

        assertSame(falhaEsperada, falhaRecebida);
    }

    @Test
    void testRejeitarRepositorioNulo() {
        assertThrows(
                NullPointerException.class,
                () -> new ListarPacientes(null)
        );
    }
}