package br.com.prontus.paciente.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import br.com.prontus.paciente.domain.Paciente;
import java.util.Objects;

@Entity
@Table(name = "PACIENTE")
public class PacienteEntity {
    @Id
    @SequenceGenerator(
            name = "pacienteSequence",
            sequenceName = "SEQ_PACIENTE",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "pacienteSequence"
    )
    private Long id;

    @Column(name = "NOME_COMPLETO", nullable = false, length = 255)
    private String nomeCompleto;

    @Column(
            name = "DATA_NASCIMENTO",
            nullable = false,
            columnDefinition = "TIMESTAMP(0)"
    )
    private LocalDate dataNascimento;

    @Column(name = "DATA_CADASTRO", nullable = false, updatable = false)
    private Instant dataCadastro;

    @Column(name = "DATA_ATUALIZACAO", nullable = false)
    private Instant dataAtualizacao;

    protected PacienteEntity() {
    }

    public PacienteEntity(Paciente paciente) {
        Objects.requireNonNull(paciente, "O registro é obrigatório.");

        this.nomeCompleto = paciente.getNomeCompleto();
        this.dataNascimento = paciente.getDataNascimento();
    }

    public Paciente paraDominio() {
        return Paciente.reconstituir(
                id,
                nomeCompleto,
                dataNascimento
        );
    }

    public void atualizarDados(Paciente paciente) {
        Objects.requireNonNull(paciente, "O paciente é obrigatório.");

        if (id == null || !id.equals(paciente.getId())) {
            throw new IllegalArgumentException(
                    "O registro informado deve possuir o mesmo identificador da entidade."
            );
        }

        this.nomeCompleto = paciente.getNomeCompleto();
        this.dataNascimento = paciente.getDataNascimento();
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

    public Instant getDataCadastro() {
        return dataCadastro;
    }

    public Instant getDataAtualizacao() {
        return dataAtualizacao;
    }

    @PrePersist
    void registrarCadastro() {
        Instant agora = Instant.now();

        this.dataCadastro = agora;
        this.dataAtualizacao = agora;
    }

    @PreUpdate
    void registrarAtualizacao() {
        this.dataAtualizacao = Instant.now();
    }
}