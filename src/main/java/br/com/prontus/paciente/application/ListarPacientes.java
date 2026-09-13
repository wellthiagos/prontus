package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.FiltroPacientes;
import br.com.prontus.paciente.domain.OrdenacaoPaciente;
import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.DomainException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ListarPacientes {

    private final PacienteRepository repository;

    protected ListarPacientes() {
        this.repository = null;
    }

    @Inject
    public ListarPacientes(PacienteRepository repository) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório é obrigatório."
        );
    }

    public PaginaPacientes executar(
            int primeiraPosicao,
            int quantidade
    ) {
        validarPaginacao(primeiraPosicao, quantidade);

        List<Paciente> pacientes =
                repository.listar(primeiraPosicao, quantidade);

        long totalRegistros = repository.contar();

        return new PaginaPacientes(pacientes, totalRegistros);
    }

    public PaginaPacientes executar(
            FiltroPacientes filtro,
            int primeiraPosicao,
            int quantidade
    ) {
        Objects.requireNonNull(filtro, "O filtro é obrigatório.");
        validarPaginacao(primeiraPosicao, quantidade);

        List<Paciente> pacientes = repository.listar(
                filtro,
                primeiraPosicao,
                quantidade
        );

        long totalRegistros = repository.contar(filtro);

        return new PaginaPacientes(pacientes, totalRegistros);
    }

    public PaginaPacientes executar(FiltroPacientes filtro, int primeiraPosicao,
                                     int quantidade, OrdenacaoPaciente ordem) {
        Objects.requireNonNull(filtro, "O filtro é obrigatório.");
        Objects.requireNonNull(ordem, "A ordenação é obrigatória.");
        validarPaginacao(primeiraPosicao, quantidade);
        return new PaginaPacientes(repository.listar(filtro, primeiraPosicao, quantidade, ordem),
                repository.contar(filtro));
    }

    private void validarPaginacao(
            int primeiraPosicao,
            int quantidade
    ) {
        if (primeiraPosicao < 0) {
            throw new DomainException(
                    "A primeira posição não pode ser negativa."
            );
        }

        if (quantidade <= 0) {
            throw new DomainException(
                    "A quantidade deve ser positiva."
            );
        }
    }
}