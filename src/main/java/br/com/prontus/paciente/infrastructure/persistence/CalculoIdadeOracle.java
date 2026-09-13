package br.com.prontus.paciente.infrastructure.persistence;

import br.com.prontus.paciente.domain.CalculoIdade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

import java.math.BigDecimal;

@ApplicationScoped
public class CalculoIdadeOracle implements CalculoIdade {

    @PersistenceContext(unitName = "prontusPU")
    private EntityManager entityManager;

    @Override
    public int calcular(Long pacienteId) {
        if (pacienteId == null || pacienteId <= 0) {
            throw new IllegalArgumentException(
                    "O identificador do paciente deve ser positivo."
            );
        }

        StoredProcedureQuery procedure = entityManager
                .createStoredProcedureQuery("P_PATIENT_AGE");

        procedure.registerStoredProcedureParameter(
                1, Long.class, ParameterMode.IN
        );
        procedure.registerStoredProcedureParameter(
                2, BigDecimal.class, ParameterMode.OUT
        );

        procedure.setParameter(1, pacienteId);
        procedure.execute();

        BigDecimal idade =
                (BigDecimal) procedure.getOutputParameterValue(2);

        if (idade == null) {
            throw new IllegalStateException(
                    "O banco não retornou a idade do paciente."
            );
        }

        return idade.intValueExact();
    }
}