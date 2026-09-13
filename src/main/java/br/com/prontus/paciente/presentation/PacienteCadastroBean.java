package br.com.prontus.paciente.presentation;

import br.com.prontus.paciente.application.CadastrarPaciente;
import br.com.prontus.paciente.domain.exception.DomainException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;

import java.time.LocalDate;

@Named("pacienteCadastroBean")
@RequestScoped
public class PacienteCadastroBean {

    @Inject
    private CadastrarPaciente cadastrarPaciente;

    private final PacienteFormulario formulario =
            new PacienteFormulario();

    public void salvar() {
        FacesContext contexto = FacesContext.getCurrentInstance();

        try {
            cadastrarPaciente.executar(
                    formulario.getNomeCompleto(),
                    formulario.getDataNascimento()
            );
        } catch (DomainException excecao) {
            contexto.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            excecao.getMessage(),
                            null
                    )
            );

            contexto.validationFailed();
            return;
        }

        PrimeFaces.current()
                .ajax()
                .addCallbackParam("cadastroEfetuado", true);
    }

    public PacienteFormulario getFormulario() {
        return formulario;
    }

    public String getDataMaximaNascimento() {
        return LocalDate.now().toString();
    }
}