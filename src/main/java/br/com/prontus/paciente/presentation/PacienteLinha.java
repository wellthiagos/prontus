package br.com.prontus.paciente.presentation;

import br.com.prontus.paciente.domain.Paciente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class PacienteLinha implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;

    @NotBlank(message = "O nome completo é obrigatório.")
    @Size(
            max = 255,
            message = "O nome completo deve possuir no máximo 255 caracteres."
    )
    private String nomeCompleto;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @PastOrPresent(message = "A data de nascimento não pode ser futura.")
    private LocalDate dataNascimento;

    public PacienteLinha(Paciente paciente) {
        Objects.requireNonNull(paciente, "O paciente é obrigatório.");

        this.id = Objects.requireNonNull(
                paciente.getId(),
                "O identificador do paciente é obrigatório."
        );
        this.nomeCompleto = paciente.getNomeCompleto();
        this.dataNascimento = paciente.getDataNascimento();
    }

    public Long getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto == null
                ? null
                : nomeCompleto.strip();
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}