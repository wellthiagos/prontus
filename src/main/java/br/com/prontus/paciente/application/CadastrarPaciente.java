package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Dependent
public class CadastrarPaciente {

    private final PacienteRepository repository;

    @Inject
    public CadastrarPaciente(PacienteRepository repository) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório é obrigatório."
        );
    }

    @Transactional
    public Paciente executar(String nomeCompleto, LocalDate dataNascimento) {
        Paciente paciente = new Paciente(nomeCompleto, dataNascimento);

        return repository.salvar(paciente);
    }
}