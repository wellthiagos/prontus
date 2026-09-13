package br.com.prontus.paciente.presentation;

import br.com.prontus.paciente.application.ListarPacientes;
import br.com.prontus.paciente.application.PaginaPacientes;
import br.com.prontus.paciente.domain.FiltroPacientes;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PacienteTabelaModel extends LazyDataModel<PacienteLinha> {

    private static final long serialVersionUID = 1L;

    private final ListarPacientes listarPacientes;
    private final FiltroPacientes filtro;

    public PacienteTabelaModel(
            ListarPacientes listarPacientes,
            FiltroPacientes filtro
    ) {
        this.listarPacientes = Objects.requireNonNull(
                listarPacientes,
                "O caso de uso de listagem é obrigatório."
        );

        this.filtro = Objects.requireNonNull(
                filtro,
                "O filtro é obrigatório."
        );
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        return 0;
    }

    @Override
    public List<PacienteLinha> load(
            int first,
            int pageSize,
            Map<String, SortMeta> sortBy,
            Map<String, FilterMeta> filterBy
    ) {
        PaginaPacientes pagina = listarPacientes.executar(
                filtro,
                first,
                pageSize
        );

        int total = Math.toIntExact(pagina.totalRegistros());
        int primeiraPosicao = recalculateFirst(first, pageSize, total);

        if (primeiraPosicao != first && total > 0) {
            pagina = listarPacientes.executar(
                    filtro,
                    primeiraPosicao,
                    pageSize
            );

            total = Math.toIntExact(pagina.totalRegistros());
        }

        setRowCount(total);

        return pagina.pacientes()
                .stream()
                .map(PacienteLinha::new)
                .toList();
    }

    @Override
    public String getRowKey(PacienteLinha linha) {
        return linha.getId().toString();
    }

    @Override
    public PacienteLinha getRowData(String rowKey) {
        List<PacienteLinha> linhas = getWrappedData();

        if (rowKey == null || linhas == null) {
            return null;
        }

        return linhas.stream()
                .filter(linha -> getRowKey(linha).equals(rowKey))
                .findFirst()
                .orElse(null);
    }
}