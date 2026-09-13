package br.com.prontus.paciente.presentation;

import br.com.prontus.paciente.application.AtualizarPaciente;
import br.com.prontus.paciente.application.ListarPacientes;
import br.com.prontus.paciente.domain.FiltroPacientes;
import br.com.prontus.paciente.domain.CalculoIdade;
import br.com.prontus.paciente.domain.exception.DomainException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;

import java.io.Serializable;
import java.time.LocalDate;

@Named("pacienteConsultaBean")
@ViewScoped
public class PacienteConsultaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ListarPacientes listarPacientes;

    @Inject
    private AtualizarPaciente atualizarPaciente;

    @Inject
    private CalculoIdade calculoIdadePaciente;

    private String nome;
    private LocalDate dataNascimentoIni;
    private LocalDate dataNascimentoFim;
    private Integer idadeCalculada;
    private int primeiraPosicao;

    private FiltroPacientes filtroAplicado;
    private PacienteTabelaModel modelo;

    public void carregar() {
        modelo = null;

        try {
            filtroAplicado = new FiltroPacientes(
                    nome,
                    dataNascimentoIni,
                    dataNascimentoFim
            );

            modelo = new PacienteTabelaModel(
                    listarPacientes,
                    filtroAplicado
            );
        } catch (DomainException excecao) {
            informarErro(excecao.getMessage());
        }
    }

    public void salvarEdicao(RowEditEvent<PacienteLinha> evento) {
        PacienteLinha linha = evento.getObject();

        try {
            atualizarPaciente.executar(
                    linha.getId(),
                    linha.getNomeCompleto(),
                    linha.getDataNascimento()
            );
        } catch (DomainException excecao) {
            informarErro(excecao.getMessage());
            return;
        }

        PrimeFaces.current().ajax().addCallbackParam("pacienteAtualizado", true);
    }

    public void calcularIdade(Long pacienteId) {
        idadeCalculada = calculoIdadePaciente.calcular(pacienteId);

        PrimeFaces.current().ajax()
                .addCallbackParam("idadeCalculada", true);
    }

    public String getResultadoIdade() {
        if (idadeCalculada == null) {
            return "";
        }

        return "Resultado: " + idadeCalculada
                + (idadeCalculada == 1 ? " ano" : " anos");
    }

    private String mensagemErroEdicao;

    public String getMensagemErroEdicao() {
        var mensagens = FacesContext.getCurrentInstance().getMessageList();
        if (!mensagens.isEmpty()) {
            mensagemErroEdicao = mensagens.stream()
                    .map(FacesMessage::getSummary)
                    .distinct()
                    .collect(java.util.stream.Collectors.joining(" "));
        }
        return mensagemErroEdicao;
    }
    public void cancelarEdicao() {
        modelo = new PacienteTabelaModel(
                listarPacientes,
                filtroAplicado
        );
    }

    private void informarErro(String mensagem) {
        FacesContext contexto = FacesContext.getCurrentInstance();

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        mensagem,
                        null
                )
        );

        contexto.validationFailed();
    }

    public PacienteTabelaModel getModelo() {
        return modelo;
    }

    public long getTotalRegistros() {
        return modelo == null ? 0 : modelo.getRowCount();
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

    public String getDataMaximaNascimento() {
        return LocalDate.now().toString();
    }
}