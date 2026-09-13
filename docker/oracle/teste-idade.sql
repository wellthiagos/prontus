SET SERVEROUTPUT ON;

DECLARE
v_id NUMBER;
    v_idade NUMBER;
BEGIN
SAVEPOINT teste_idade;

SELECT SEQ_PACIENTE.NEXTVAL INTO v_id FROM DUAL;

INSERT INTO PACIENTE (
    ID, NOME_COMPLETO, DATA_NASCIMENTO,
    DATA_CADASTRO, DATA_ATUALIZACAO
) VALUES (
             v_id,
             'TESTE DE IDADE',
             TIMESTAMP '2000-01-01 00:00:00',
             SYSTIMESTAMP,
             SYSTIMESTAMP
         );

P_PATIENT_AGE(v_id, v_idade);

    DBMS_OUTPUT.PUT_LINE('Idade calculada: ' || v_idade);

ROLLBACK TO teste_idade;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO teste_idade;
        RAISE;
END;
/