package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.DomainException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;

@Dependent
public class ListarPacientes {

    private final PacienteRepository repository;

    @Inject
    public ListarPacientes(PacienteRepository repository) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório é obrigatório."
        );
    }

    public PaginaPacientes executar(int primeiraPosicao, int quantidade) {
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

        List<Paciente> pacientes =
                repository.listar(primeiraPosicao, quantidade);

        long totalRegistros = repository.contar();

        return new PaginaPacientes(pacientes, totalRegistros);
    }
}