CREATE SEQUENCE IF NOT EXISTS SEQ_PACIENTE
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS PACIENTE (
    ID BIGINT NOT NULL,
    NOME_COMPLETO VARCHAR(255) NOT NULL,
    DATA_NASCIMENTO TIMESTAMP(0) NOT NULL,
    DATA_CADASTRO TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    DATA_ATUALIZACAO TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT PK_PACIENTE PRIMARY KEY (ID)
);

CREATE ALIAS IF NOT EXISTS P_PATIENT_AGE AS $$
Integer calcularIdade(java.sql.Connection conexao, Long pacienteId)
        throws java.sql.SQLException {

    if (pacienteId == null || pacienteId <= 0) {
        throw new java.sql.SQLException(
            "O identificador do paciente deve ser positivo."
        );
}

    String sql =
        "SELECT DATA_NASCIMENTO, CURRENT_DATE AS HOJE " +
        "FROM PACIENTE WHERE ID = ?";

    try (java.sql.PreparedStatement comando =
            conexao.prepareStatement(sql)) {

        comando.setLong(1, pacienteId);

        try (java.sql.ResultSet resultado = comando.executeQuery()) {
            if (!resultado.next()) {
                throw new java.sql.SQLException(
                    "Paciente não encontrado."
                );
}

            java.time.LocalDate nascimento =
                resultado.getDate("DATA_NASCIMENTO").toLocalDate();

            java.time.LocalDate hoje =
                resultado.getDate("HOJE").toLocalDate();

            if (nascimento.isAfter(hoje)) {
                throw new java.sql.SQLException(
                    "A data de nascimento não pode estar no futuro."
                );
}

            return java.time.Period.between(nascimento, hoje).getYears();
}
    }
}
$$;