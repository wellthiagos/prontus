package br.com.prontus.paciente.presentation;

import br.com.prontus.paciente.domain.FiltroPacientes;
import br.com.prontus.paciente.domain.exception.DomainException;
import br.com.prontus.paciente.infrastructure.report.RelatorioPacientesPdf;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/relatorios/pacientes.pdf")
public class RelatorioPacientesServlet extends HttpServlet {
    @Inject private RelatorioPacientesPdf relatorio;
    private static final Logger LOG = Logger.getLogger(RelatorioPacientesServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        final FiltroPacientes filtro;
        try {
            filtro = new FiltroPacientes(request.getParameter("nome"),
                    data(request.getParameter("dataNascimentoIni")),
                    data(request.getParameter("dataNascimentoFim")));
        } catch (DomainException | DateTimeParseException e) {
            response.sendError(400, "Parâmetros de consulta inválidos.");
            return;
        }
        byte[] pdf;
        try (var logo = getServletContext().getResourceAsStream("/resources/images/logo-prontus.png")) {
            pdf = relatorio.gerar(filtro, logo);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Não foi possível gerar o relatório de pacientes.", e);
            response.sendError(500, "Não foi possível gerar o PDF. Tente novamente.");
            return;
        }
        response.setContentType("application/pdf");
        String nomeArquivo = "relatorio-registros-"
                + ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss-SSS"))
                + ".pdf";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }

    private static LocalDate data(String valor) {
        return valor == null || valor.isBlank() ? null : LocalDate.parse(valor);
    }
}
