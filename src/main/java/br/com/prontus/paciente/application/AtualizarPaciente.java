package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@ApplicationScoped
public class AtualizarPaciente {

    private final PacienteRepository repository;

    protected AtualizarPaciente() {
        this.repository = null;
    }

    @Inject
    public AtualizarPaciente(PacienteRepository repository) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório é obrigatório."
        );
    }

    @Transactional
    public Paciente executar(
            Long id,
            String nomeCompleto,
            LocalDate dataNascimento) {

        Paciente paciente = Paciente.reconstituir(
                id,
                nomeCompleto,
                dataNascimento
        );

        return repository.salvar(paciente);
    }
}