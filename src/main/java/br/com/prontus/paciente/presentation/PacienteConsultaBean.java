package br.com.prontus.paciente.presentation;

import br.com.prontus.paciente.application.ListarPacientes;
import br.com.prontus.paciente.application.PaginaPacientes;
import br.com.prontus.paciente.domain.FiltroPacientes;
import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.exception.DomainException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.time.LocalDate;
import java.util.List;

@Named("pacienteConsultaBean")
@RequestScoped
public class PacienteConsultaBean {

    private static final int TAMANHO_PAGINA = 10;

    @Inject
    private ListarPacientes listarPacientes;

    private String nome;
    private LocalDate dataNascimentoIni;
    private LocalDate dataNascimentoFim;
    private int primeiraPosicao;

    private List<Paciente> pacientes = List.of();
    private long totalRegistros;

    public void carregar() {
        pacientes = List.of();
        totalRegistros = 0;

        try {
            FiltroPacientes filtro = new FiltroPacientes(
                    nome,
                    dataNascimentoIni,
                    dataNascimentoFim
            );

            PaginaPacientes pagina = listarPacientes.executar(
                    filtro,
                    primeiraPosicao,
                    TAMANHO_PAGINA
            );

            pacientes = pagina.pacientes();
            totalRegistros = pagina.totalRegistros();
        } catch (DomainException excecao) {
            FacesContext contexto = FacesContext.getCurrentInstance();

            contexto.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            excecao.getMessage(),
                            null
                    )
            );

            contexto.validationFailed();
        }
    }

    public boolean isPossuiPaginaAnterior() {
        return primeiraPosicao > 0;
    }

    public boolean isPossuiProximaPagina() {
        return (long) primeiraPosicao + TAMANHO_PAGINA
                < totalRegistros;
    }

    public int getPosicaoAnterior() {
        return Math.max(0, primeiraPosicao - TAMANHO_PAGINA);
    }

    public long getPosicaoProxima() {
        return (long) primeiraPosicao + TAMANHO_PAGINA;
    }

    public long getPaginaAtual() {
        return totalRegistros == 0
                ? 0
                : (long) primeiraPosicao / TAMANHO_PAGINA + 1;
    }

    public long getTotalPaginas() {
        return totalRegistros / TAMANHO_PAGINA
                + (totalRegistros % TAMANHO_PAGINA == 0 ? 0 : 1);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataNascimentoIni() {
        return dataNascimentoIni;
    }

    public void setDataNascimentoIni(LocalDate dataNascimentoIni) {
        this.dataNascimentoIni = dataNascimentoIni;
    }

    public LocalDate getDataNascimentoFim() {
        return dataNascimentoFim;
    }

    public void setDataNascimentoFim(LocalDate dataNascimentoFim) {
        this.dataNascimentoFim = dataNascimentoFim;
    }

    public int getPrimeiraPosicao() {
        return primeiraPosicao;
    }

    public void setPrimeiraPosicao(int primeiraPosicao) {
        this.primeiraPosicao = primeiraPosicao;
    }

    public List<Paciente> getPacientes() {
        return pacientes;
    }

    public long getTotalRegistros() {
        return totalRegistros;
    }

    public String getDataMaximaNascimento() {
        return LocalDate.now().toString();
    }
}