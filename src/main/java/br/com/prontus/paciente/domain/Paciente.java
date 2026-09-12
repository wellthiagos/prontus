package br.com.prontus.paciente.domain;

import br.com.prontus.paciente.domain.exception.DomainException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

public class Paciente {
    private final String nomeCompleto;
    private final LocalDate dataNascimento;

    public Paciente(String nomeCompleto, LocalDate dataNascimento) {
        this(nomeCompleto, dataNascimento, Clock.systemDefaultZone());
    }

    Paciente(String nomeCompleto, LocalDate dataNascimento, Clock clock) {
        Objects.requireNonNull(clock, "O relógio é obrigatório.");

        this.nomeCompleto = validarNomeCompleto(nomeCompleto);
        this.dataNascimento = validarDataNascimento(dataNascimento, clock);
    }

    private static String validarNomeCompleto(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new DomainException(
                    "O nome completo é obrigatório."
            );
        }

        return nomeCompleto.strip();
    }

    private static LocalDate validarDataNascimento(
            LocalDate dataNascimento, Clock clock) {

        if (dataNascimento == null) {
            throw new DomainException(
                    "A data de nascimento é obrigatória."
            );
        }

        if (dataNascimento.isAfter(LocalDate.now(clock))) {
            throw new DomainException(
                    "A data de nascimento não pode ser futura."
            );
        }

        return dataNascimento;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
}