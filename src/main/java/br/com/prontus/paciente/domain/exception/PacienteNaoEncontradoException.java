package br.com.prontus.paciente.domain.exception;

public class PacienteNaoEncontradoException extends DomainException {

    private static final long serialVersionUID = 1L;

    public PacienteNaoEncontradoException(Long id) {
        super("Paciente não encontrado para o identificador: " + id);
    }
}