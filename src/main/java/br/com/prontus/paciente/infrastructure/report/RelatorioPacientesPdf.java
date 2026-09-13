package br.com.prontus.paciente.infrastructure.report;

import br.com.prontus.paciente.domain.CalculoIdade;
import br.com.prontus.paciente.domain.FiltroPacientes;
import br.com.prontus.paciente.domain.Paciente;
import br.com.prontus.paciente.domain.PacienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class RelatorioPacientesPdf {

    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Locale PORTUGUES =
            Locale.forLanguageTag("pt-BR");

    @Inject
    private PacienteRepository repository;

    @Inject
    private CalculoIdade calculoIdade;

    private volatile JasperReport modelo;

    private JasperReport modelo() throws JRException, IOException {
        if (modelo == null) {
            synchronized (this) {
                if (modelo == null) {
                    try (InputStream fonte = getClass()
                            .getResourceAsStream("/relatorios/pacientes.jrxml")) {

                        if (fonte == null) {
                            throw new IOException(
                                    "Modelo do relatório não encontrado."
                            );
                        }

                        modelo = JasperCompileManager.compileReport(fonte);
                    }
                }
            }
        }

        return modelo;
    }

    public byte[] gerar(FiltroPacientes filtro, InputStream logo)
            throws JRException, IOException {

        Collection<Map<String, ?>> linhas = new ArrayList<>();

        // Inclui todos os resultados, independentemente da página aberta.
        for (int inicio = 0; ; inicio += 500) {
            var pacientes = repository.listar(filtro, inicio, 500);

            for (Paciente paciente : pacientes) {
                Map<String, Object> linha = new HashMap<>();

                linha.put("codigo", paciente.getId().toString());
                linha.put("nome", paciente.getNomeCompleto().toUpperCase(PORTUGUES));
                linha.put(
                        "nascimento",
                        DATA.format(paciente.getDataNascimento())
                );
                linha.put(
                        "idade",
                        Integer.toString(
                                calculoIdade.calcular(paciente.getId())
                        )
                );

                linhas.add(linha);
            }

            if (pacientes.size() < 500) {
                break;
            }
        }

        Map<String, Object> parametros = new HashMap<>();

        parametros.put("LOGO", logo);
        parametros.put(
                "NOME",
                Objects.toString(filtro.nome(), "").toUpperCase(PORTUGUES)
        );
        parametros.put(
                "INICIO",
                filtro.dataNascimentoIni() == null
                        ? ""
                        : DATA.format(filtro.dataNascimentoIni())
        );
        parametros.put(
                "FIM",
                filtro.dataNascimentoFim() == null
                        ? ""
                        : DATA.format(filtro.dataNascimentoFim())
        );
        parametros.put(
                "GERADO",
                ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        parametros.put("TOTAL", Integer.toString(linhas.size()));

        JasperPrint documento = JasperFillManager.fillReport(
                modelo(),
                parametros,
                new JRMapCollectionDataSource(linhas)
        );

        return JasperExportManager.exportReportToPdf(documento);
    }
}