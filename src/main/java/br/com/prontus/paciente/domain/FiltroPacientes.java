package br.com.prontus.paciente.domain;

import br.com.prontus.paciente.domain.exception.DomainException;

import java.io.Serializable;
import java.time.LocalDate;

public record FiltroPacientes(
        String nome,
        LocalDate dataNascimentoIni,
        LocalDate dataNascimentoFim
) implements Serializable {

    public FiltroPacientes {
        nome = nome == null ? null : nome.strip();

        if (nome != null && nome.isEmpty()) {
            nome = null;
        }

        LocalDate hoje = LocalDate.now();

        validarData(dataNascimentoIni, hoje, "inicial");
        validarData(dataNascimentoFim, hoje, "final");

        if (dataNascimentoIni != null
                && dataNascimentoFim != null
                && dataNascimentoIni.isAfter(dataNascimentoFim)) {
            throw new DomainException(
                    "A data de nascimento inicial não pode ser posterior à final."
            );
        }
    }

    private static void validarData(
            LocalDate data,
            LocalDate hoje,
            String campo
    ) {
        if (data == null) {
            return;
        }

        if (data.getYear() < 1) {
            throw new DomainException(
                    "A data de nascimento " + campo
                            + " deve possuir ano maior ou igual a 1."
            );
        }

        if (data.isAfter(hoje)) {
            throw new DomainException(
                    "A data de nascimento " + campo
                            + " não pode ser futura."
            );
        }
    }

    public static FiltroPacientes semFiltros() {
        return new FiltroPacientes(null, null, null);
    }

    public boolean possuiNome() {
        return nome != null;
    }

    public boolean possuiDataNascimentoIni() {
        return dataNascimentoIni != null;
    }

    public boolean possuiDataNascimentoFim() {
        return dataNascimentoFim != null;
    }
}