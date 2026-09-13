package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.DomainException;
import br.com.prontus.paciente.domain.FiltroPacientes;
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

import static org.mockito.Mockito.verifyNoMoreInteractions;

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

    @Test
    void testListarComFiltrosPreservandoPaginacaoETotal() {
        LocalDate nascimento = LocalDate.of(1990, 5, 20);
        FiltroPacientes filtro = new FiltroPacientes("Maria", nascimento, nascimento);

        Paciente paciente = Paciente.reconstituir(
                11L,
                "Maria Silva",
                nascimento
        );

        when(repository.listar(filtro, 10, 5))
                .thenReturn(List.of(paciente));
        when(repository.contar(filtro)).thenReturn(11L);

        PaginaPacientes resultado =
                listarPacientes.executar(filtro, 10, 5);

        assertEquals(List.of(paciente), resultado.pacientes());
        assertEquals(11L, resultado.totalRegistros());

        verify(repository).listar(filtro, 10, 5);
        verify(repository).contar(filtro);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testRetornarConsultaFiltradaSemResultados() {
        FiltroPacientes filtro =
                new FiltroPacientes("Inexistente", null, null);

        when(repository.listar(filtro, 0, 10)).thenReturn(List.of());
        when(repository.contar(filtro)).thenReturn(0L);

        PaginaPacientes resultado =
                listarPacientes.executar(filtro, 0, 10);

        assertTrue(resultado.pacientes().isEmpty());
        assertEquals(0L, resultado.totalRegistros());

        verify(repository).listar(filtro, 0, 10);
        verify(repository).contar(filtro);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testRetornarPaginaFiltradaVaziaPreservandoTotal() {
        FiltroPacientes filtro = new FiltroPacientes("Maria", null, null);

        when(repository.listar(filtro, 20, 10)).thenReturn(List.of());
        when(repository.contar(filtro)).thenReturn(5L);

        PaginaPacientes resultado =
                listarPacientes.executar(filtro, 20, 10);

        assertTrue(resultado.pacientes().isEmpty());
        assertEquals(5L, resultado.totalRegistros());

        verify(repository).listar(filtro, 20, 10);
        verify(repository).contar(filtro);
    }

    @Test
    void testAceitarObjetoSemFiltros() {
        FiltroPacientes filtro = FiltroPacientes.semFiltros();

        Paciente paciente = Paciente.reconstituir(
                1L,
                "Ana Souza",
                LocalDate.of(1985, 8, 15)
        );

        when(repository.listar(filtro, 0, 10))
                .thenReturn(List.of(paciente));
        when(repository.contar(filtro)).thenReturn(1L);

        PaginaPacientes resultado =
                listarPacientes.executar(filtro, 0, 10);

        assertEquals(List.of(paciente), resultado.pacientes());
        assertEquals(1L, resultado.totalRegistros());

        verify(repository).listar(filtro, 0, 10);
        verify(repository).contar(filtro);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testRejeitarFiltroNuloSemChamarRepositorio() {
        NullPointerException excecao = assertThrows(
                NullPointerException.class,
                () -> listarPacientes.executar(null, 0, 10)
        );

        assertEquals("O filtro é obrigatório.", excecao.getMessage());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "0, 0",
            "0, -1"
    })
    void testRejeitarPaginacaoFiltradaInvalidaSemChamarRepositorio(
            int primeiraPosicao,
            int quantidade
    ) {
        FiltroPacientes filtro = new FiltroPacientes("Maria", null,null);

        assertThrows(
                DomainException.class,
                () -> listarPacientes.executar(
                        filtro,
                        primeiraPosicao,
                        quantidade
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void testPropagarFalhaDaListagemFiltrada() {
        FiltroPacientes filtro = new FiltroPacientes("Maria", null, null);
        RuntimeException falhaEsperada =
                new RuntimeException("Falha simulada na listagem.");

        when(repository.listar(filtro, 0, 10))
                .thenThrow(falhaEsperada);

        RuntimeException falhaRecebida = assertThrows(
                RuntimeException.class,
                () -> listarPacientes.executar(filtro, 0, 10)
        );

        assertSame(falhaEsperada, falhaRecebida);

        verify(repository).listar(filtro, 0, 10);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testPropagarFalhaDaContagemFiltrada() {
        FiltroPacientes filtro = new FiltroPacientes("Maria", null, null);
        RuntimeException falhaEsperada =
                new RuntimeException("Falha simulada na contagem.");

        when(repository.listar(filtro, 0, 10)).thenReturn(List.of());
        when(repository.contar(filtro)).thenThrow(falhaEsperada);

        RuntimeException falhaRecebida = assertThrows(
                RuntimeException.class,
                () -> listarPacientes.executar(filtro, 0, 10)
        );

        assertSame(falhaEsperada, falhaRecebida);

        verify(repository).listar(filtro, 0, 10);
        verify(repository).contar(filtro);
        verifyNoMoreInteractions(repository);
    }
}