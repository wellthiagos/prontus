WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

CREATE OR REPLACE PROCEDURE P_PATIENT_AGE (
    p_patient_id IN NUMBER,
    p_age_years OUT NUMBER,
    p_reference_date IN DATE DEFAULT NULL
)
AS
    v_nascimento DATE;
    v_hoje DATE := TRUNC(
        COALESCE(p_reference_date, CAST(SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo' AS DATE))
    );
BEGIN
    IF p_patient_id IS NULL
       OR p_patient_id <= 0
       OR p_patient_id <> TRUNC(p_patient_id) THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'O identificador deve ser um número inteiro positivo.'
        );
END IF;

SELECT TRUNC(CAST(DATA_NASCIMENTO AS DATE))
INTO v_nascimento
FROM PACIENTE
WHERE ID = p_patient_id;

IF v_nascimento > v_hoje THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'A data de nascimento não pode ser futura.'
        );
END IF;

    p_age_years :=
        EXTRACT(YEAR FROM v_hoje)
        - EXTRACT(YEAR FROM v_nascimento);

    IF EXTRACT(MONTH FROM v_hoje)
           < EXTRACT(MONTH FROM v_nascimento)
       OR (
           EXTRACT(MONTH FROM v_hoje)
               = EXTRACT(MONTH FROM v_nascimento)
           AND EXTRACT(DAY FROM v_hoje)
               < EXTRACT(DAY FROM v_nascimento)
       ) THEN
        p_age_years := p_age_years - 1;
END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(
            -20003,
            'Registro não encontrado para o identificador: '
                || p_patient_id
        );
END P_PATIENT_AGE;
/

SHOW ERRORS PROCEDURE P_PATIENT_AGE;