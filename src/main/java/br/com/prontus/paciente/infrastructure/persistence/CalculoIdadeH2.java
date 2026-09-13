package br.com.prontus.paciente.infrastructure.persistence;

import br.com.prontus.paciente.domain.CalculoIdade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class CalculoIdadeH2 implements CalculoIdade {

    @PersistenceContext(unitName = "prontusPU")
    private EntityManager entityManager;

    @Override
    public int calcular(Long pacienteId) {
        if (pacienteId == null || pacienteId <= 0) {
            throw new IllegalArgumentException(
                    "O identificador do paciente deve ser positivo."
            );
        }

        Number resultado = (Number) entityManager
                .createNativeQuery("SELECT P_PATIENT_AGE(?1)")
                .setParameter(1, pacienteId)
                .getSingleResult();

        if (resultado == null) {
            throw new IllegalStateException(
                    "O banco não retornou a idade do paciente."
            );
        }

        return resultado.intValue();
    }
}