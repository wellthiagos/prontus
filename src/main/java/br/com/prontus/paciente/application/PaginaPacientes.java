package br.com.prontus.paciente.application;

import br.com.prontus.paciente.domain.Paciente;

import java.util.List;

public record PaginaPacientes(
        List<Paciente> pacientes,
        long totalRegistros) {

    public PaginaPacientes {
        pacientes = List.copyOf(pacientes);

        if (totalRegistros < 0) {
            throw new IllegalArgumentException(
                    "O total de registros não pode ser negativo."
            );
        }
    }
}