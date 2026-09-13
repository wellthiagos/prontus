package br.com.prontus.paciente.presentation;

import br.com.prontus.paciente.domain.FiltroPacientes;
import br.com.prontus.paciente.domain.PacienteRepository;
import br.com.prontus.paciente.domain.exception.DomainException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Named("pacienteGraficosBean")
@RequestScoped
public class PacienteGraficosBean {
    @Inject private PacienteRepository repository;
    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    private final List<Faixa> faixas = List.of(
            new Faixa("0–17 anos"), new Faixa("18–29 anos"),
            new Faixa("30–44 anos"), new Faixa("45–59 anos"),
            new Faixa("60 anos ou mais"));
    private long total;
    private double media;
    private double mediana;
    private boolean carregado;

    public void carregar() {
        try {
            FiltroPacientes filtro = FiltroPacientes.semFiltros();
            TreeMap<Integer, Long> idades = new TreeMap<>();
            long soma = 0;
            for (int inicio = 0; ; inicio += 500) {
                var pacientes = repository.listar(filtro, inicio, 500);
                for (var paciente : pacientes) {
                    int idade = Period.between(paciente.getDataNascimento(), hoje).getYears();
                    int grupo = idade < 18 ? 0 : idade < 30 ? 1 : idade < 45 ? 2 : idade < 60 ? 3 : 4;
                    faixas.get(grupo).quantidade++;
                    idades.merge(idade, 1L, Long::sum);
                    soma += idade;
                    total++;
                }
                if (pacientes.size() < 500) break;
            }
            if (total > 0) {
                media = (double) soma / total;
                long acumulado = 0;
                Integer centroInicial = null;
                for (var entrada : idades.entrySet()) {
                    acumulado += entrada.getValue();
                    if (centroInicial == null && acumulado > (total - 1) / 2) {
                        centroInicial = entrada.getKey();
                    }
                    if (acumulado > total / 2) {
                        mediana = (centroInicial + entrada.getKey()) / 2.0;
                        break;
                    }
                }
                long maior = faixas.stream().mapToLong(Faixa::getQuantidade).max().orElse(1);
                for (Faixa faixa : faixas) {
                    faixa.largura = 100.0 * faixa.quantidade / maior;
                    faixa.percentual = 100.0 * faixa.quantidade / total;
                }
            }
            carregado = true;
        } catch (DomainException e) {
            FacesContext contexto = FacesContext.getCurrentInstance();
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            contexto.validationFailed();
        }
    }

    public static class Faixa {
        private final String nome;
        private long quantidade;
        private double largura;
        private double percentual;
        public Faixa(String nome) { this.nome = nome; }
        public String getNome() { return nome; }
        public long getQuantidade() { return quantidade; }
        public String getLargura() { return String.format(Locale.ROOT, "%.2f", largura); }
        public String getPercentual() { return String.format(Locale.forLanguageTag("pt-BR"), "%.1f%%", percentual); }
    }

    public List<Faixa> getFaixas() { return faixas; }
    public long getTotal() { return total; }
    public boolean isCarregado() { return carregado; }
    public String getMedia() { return formatar(media); }
    public String getMediana() { return formatar(mediana); }
    private String formatar(double valor) {
        return total == 0 ? "—" : String.format(Locale.forLanguageTag("pt-BR"), "%.1f anos", valor);
    }
    public String getDataReferencia() { return hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
}
