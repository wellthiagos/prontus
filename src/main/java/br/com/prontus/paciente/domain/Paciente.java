package br.com.prontus.paciente.domain;

import br.com.prontus.paciente.domain.exception.DomainException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

public class Paciente {

    private static final int TAMANHO_MAXIMO_NOME = 255;

    private final Long id;
    private final String nomeCompleto;
    private final LocalDate dataNascimento;

    public Paciente(String nomeCompleto, LocalDate dataNascimento) {
        this(null, nomeCompleto, dataNascimento, Clock.systemDefaultZone());
    }

    Paciente(String nomeCompleto, LocalDate dataNascimento, Clock clock) {
        this(null, nomeCompleto, dataNascimento, clock);
    }

    private Paciente(
            Long id,
            String nomeCompleto,
            LocalDate dataNascimento,
            Clock clock) {

        Objects.requireNonNull(clock, "O relógio é obrigatório.");

        this.id = id;
        this.nomeCompleto = validarNomeCompleto(nomeCompleto);
        this.dataNascimento = validarDataNascimento(dataNascimento, clock);
    }

    public static Paciente reconstituir(
            Long id,
            String nomeCompleto,
            LocalDate dataNascimento) {

        if (id == null || id <= 0) {
            throw new DomainException(
                    "O identificador deve ser positivo."
            );
        }

        return new Paciente(
                id,
                nomeCompleto,
                dataNascimento,
                Clock.systemDefaultZone()
        );
    }

    private static String validarNomeCompleto(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new DomainException(
                    "O nome é obrigatório."
            );
        }

        String nomeNormalizado = nomeCompleto.strip();

        if (nomeNormalizado.length() > TAMANHO_MAXIMO_NOME) {
            throw new DomainException(
                    "O nome deve ter no máximo 255 caracteres."
            );
        }

        return nomeNormalizado;
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

    public Long getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
}