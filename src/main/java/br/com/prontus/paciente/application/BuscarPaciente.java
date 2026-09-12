package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.DomainException;
import br.com.prontus.paciente.domain.exception.PacienteNaoEncontradoException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.Objects;

@Dependent
public class BuscarPaciente {

    private final PacienteRepository repository;

    @Inject
    public BuscarPaciente(PacienteRepository repository) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório é obrigatório."
        );
    }

    public Paciente executar(Long id) {
        if (id == null || id <= 0) {
            throw new DomainException(
                    "O identificador do paciente deve ser positivo."
            );
        }

        return repository.buscarPorId(id)
                .orElseThrow(() -> new PacienteNaoEncontradoException(id));
    }
}