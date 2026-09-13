package br.com.prontus.paciente.infrastructure.persistence;

import org.h2.tools.RunScript;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculoIdadePacienteH2Test {

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Aniversário hoje,       0,  40",
            "Aniversário amanhã,     1,  39",
            "Aniversário ontem,     -1,  40"
    })
    void deveCalcularAnosCompletos(
            String cenario,
            int deslocamentoDias,
            int idadeEsperada
    ) throws Exception {

        String url = "jdbc:h2:mem:idade-" + UUID.randomUUID()
                + ";MODE=Oracle";

        try (var conexao = DriverManager.getConnection(url, "sa", "sa")) {
            try (var script = Files.newBufferedReader(
                    Path.of("docker", "h2", "init.sql"),
                    StandardCharsets.UTF_8
            )) {
                RunScript.execute(conexao, script);
            }

            String inserir = """
                    INSERT INTO PACIENTE (
                        ID,
                        NOME_COMPLETO,
                        DATA_NASCIMENTO,
                        DATA_CADASTRO,
                        DATA_ATUALIZACAO
                    ) VALUES (
                        1,
                        'Paciente de teste',
                        DATEADD('DAY', ?, DATEADD('YEAR', -40, CURRENT_DATE)),
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """;

            try (var comando = conexao.prepareStatement(inserir)) {
                comando.setInt(1, deslocamentoDias);
                comando.executeUpdate();
            }

            try (var comando = conexao.prepareStatement(
                    "SELECT P_PATIENT_AGE(?)"
            )) {
                comando.setLong(1, 1L);

                try (var resultado = comando.executeQuery()) {
                    assertTrue(resultado.next());
                    assertEquals(idadeEsperada, resultado.getInt(1));
                }
            }
        }
    }
}