package br.com.prontus.paciente.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PacienteFormulario {

    @NotBlank(message = "O nome completo é obrigatório.")
    @Size(
            max = 255,
            message = "O nome completo deve possuir no máximo 255 caracteres."
    )
    private String nomeCompleto;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @PastOrPresent(message = "A data de nascimento não pode ser futura.")
    private LocalDate dataNascimento;

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