package br.com.prontus.paciente.presentation;

import br.com.prontus.paciente.application.ListarPacientes;
import br.com.prontus.paciente.application.PaginaPacientes;
import br.com.prontus.paciente.domain.FiltroPacientes;
import br.com.prontus.paciente.domain.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PacienteTabelaModelTest {

    private ListarPacientes listarPacientes;
    private FiltroPacientes filtro;
    private PacienteTabelaModel modelo;

    @BeforeEach
    void preparar() {
        listarPacientes = mock(ListarPacientes.class);
        filtro = new FiltroPacientes("Maria", null, null);
        modelo = new PacienteTabelaModel(listarPacientes, filtro);
    }

    @Test
    void testCarregarPaginaComFiltroETotal() {
        Paciente paciente = criarPaciente();

        when(listarPacientes.executar(filtro, 10, 5))
                .thenReturn(new PaginaPacientes(List.of(paciente), 11));

        var linhas = modelo.load(10, 5, Map.of(), Map.of());

        assertEquals(1, linhas.size());
        assertEquals(paciente.getId(), linhas.get(0).getId());
        assertEquals(
                paciente.getNomeCompleto(),
                linhas.get(0).getNomeCompleto()
        );
        assertEquals(
                paciente.getDataNascimento(),
                linhas.get(0).getDataNascimento()
        );
        assertEquals(11, modelo.getRowCount());

        verify(listarPacientes).executar(filtro, 10, 5);
        verifyNoMoreInteractions(listarPacientes);
    }

    @Test
    void testCarregarConsultaSemResultados() {
        when(listarPacientes.executar(filtro, 0, 10))
                .thenReturn(new PaginaPacientes(List.of(), 0));

        var linhas = modelo.load(0, 10, Map.of(), Map.of());

        assertTrue(linhas.isEmpty());
        assertEquals(0, modelo.getRowCount());

        verify(listarPacientes).executar(filtro, 0, 10);
        verifyNoMoreInteractions(listarPacientes);
    }

    @Test
    void testAjustarPaginaQueDeixouDeExistir() {
        Paciente paciente = criarPaciente();

        when(listarPacientes.executar(filtro, 20, 10))
                .thenReturn(new PaginaPacientes(List.of(), 1));

        when(listarPacientes.executar(filtro, 0, 10))
                .thenReturn(new PaginaPacientes(List.of(paciente), 1));

        var linhas = modelo.load(20, 10, Map.of(), Map.of());

        assertEquals(1, linhas.size());
        assertEquals(paciente.getId(), linhas.get(0).getId());
        assertEquals(1, modelo.getRowCount());

        verify(listarPacientes).executar(filtro, 20, 10);
        verify(listarPacientes).executar(filtro, 0, 10);
        verifyNoMoreInteractions(listarPacientes);
    }

    @Test
    void testIdentificarLinhaPeloId() {
        PacienteLinha linha = new PacienteLinha(criarPaciente());
        modelo.setWrappedData(List.of(linha));

        assertEquals("10", modelo.getRowKey(linha));
        assertSame(linha, modelo.getRowData("10"));
        assertNull(modelo.getRowData("999"));
        assertNull(modelo.getRowData(null));
    }

    @Test
    void testPropagarFalhaDoCarregamento() {
        RuntimeException falhaEsperada =
                new RuntimeException("Falha simulada na consulta.");

        when(listarPacientes.executar(filtro, 0, 10))
                .thenThrow(falhaEsperada);

        RuntimeException falhaRecebida = assertThrows(
                RuntimeException.class,
                () -> modelo.load(0, 10, Map.of(), Map.of())
        );

        assertSame(falhaEsperada, falhaRecebida);
    }

    @Test
    void testRejeitarCasoDeUsoNulo() {
        assertThrows(
                NullPointerException.class,
                () -> new PacienteTabelaModel(null, filtro)
        );
    }

    @Test
    void testRejeitarFiltroNulo() {
        assertThrows(
                NullPointerException.class,
                () -> new PacienteTabelaModel(listarPacientes, null)
        );
    }

    private Paciente criarPaciente() {
        return Paciente.reconstituir(
                10L,
                "Maria Silva",
                LocalDate.of(1990, 5, 20)
        );
    }
}