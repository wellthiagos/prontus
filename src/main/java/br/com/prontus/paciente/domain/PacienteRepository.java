package br.com.prontus.paciente.domain;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository {
    Paciente salvar(Paciente paciente);

    Optional<Paciente> buscarPorId(Long id);

    List<Paciente> listar(int primeiraPosicao, int quantidade);

    List<Paciente> listar(FiltroPacientes filtro, int primeiraPosicao, int quantidade);

    long contar();

    long contar(FiltroPacientes filtro);
}