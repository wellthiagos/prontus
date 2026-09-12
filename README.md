# Prontus

Sistema de cadastro e consulta de pacientes, desenvolvido como
desafio técnico Java.

## Objetivo

Implementar cadastro, listagem paginada e edição de pacientes,
com validação dos dados e cálculo de idade por procedure no banco.

O desafio propõe a migração de um fluxo legado em Struts 2
para JSF e PrimeFaces, com persistência utilizando JPA.

## Versões utilizadas e justificativa da alteração

O enunciado original solicita Java 11, Jakarta EE 9.1 e PrimeFaces 12.

Após consulta à recrutadora, foi autorizada a utilização de
Java 17, Jakarta EE 10 e PrimeFaces 14. Por esse motivo, o projeto
adota essas versões em substituição às indicadas no enunciado.

Essa autorização se refere às versões mencionadas acima.
O servidor de aplicação será escolhido de acordo com a
compatibilidade com Jakarta EE 10.

Os requisitos funcionais e os critérios de entrega previstos
no desafio permanecem mantidos.

## Tecnologias configuradas

- Java 17.
- Maven.
- Jakarta EE 10.
- PrimeFaces 14.0.12, na variante Jakarta.
- Empacotamento WAR.

## Arquitetura e diretrizes

O projeto será desenvolvido com Clean Code, princípios SOLID
e Domain-Driven Design (DDD).

A implementação será organizada nas seguintes camadas:

- **Domínio:** modelo de paciente e regras de negócio.
- **Aplicação:** coordenação dos casos de uso.
- **Infraestrutura:** persistência, procedures e geração de relatórios.
- **Apresentação:** telas JSF e componentes PrimeFaces.

As responsabilidades serão separadas para facilitar manutenção
e testes, com abstrações introduzidas conforme a necessidade.

## Funcionalidades planejadas

- Cadastro de pacientes com nome completo e data de nascimento.
- Listagem paginada com PrimeFaces DataTable.
- Edição dos dados dos pacientes.
- Validações com Jakarta Bean Validation.
- Procedure Oracle P_PATIENT_AGE, com identificador do paciente
  como entrada e idade em anos como saída.
- Botão “Calcular Idade”, com exibição do resultado em Growl ou Dialog.
- Gráficos de pacientes por faixa etária.
- Relatório PDF de pacientes com JasperReports.

O relatório com JasperReports é uma funcionalidade adicional,
não exigida no enunciado.

## Testes

Os testes unitários utilizam JUnit 5 e são executados pelo
Maven Surefire, sem necessidade de Docker, WildFly ou banco.

Para executar:

    mvn test

Para executar os testes e gerar o WAR:

    mvn clean package

### Cobertura atual

São 73 execuções de testes:

- 17 de domínio.
- 44 dos casos de uso, com Mockito.
- 12 de integração da persistência, com Hibernate e H2.

Os testes dos casos de uso verificam cadastro, atualização, busca,
paginação, rejeição de dados inválidos e propagação de falhas.

Mockito simula o contrato PacienteRepository nesses testes.
O funcionamento real da persistência é verificado separadamente
pelos testes de integração.

Os testes executados fora do WildFly não validam os interceptadores
CDI e o gerenciamento de transações JTA.

## Docker e integração contínua

Estão planejados:

- Dockerfile com build em múltiplas etapas.
- Ambiente de execução com WildFly compatível com Jakarta EE 10.
- Docker Compose para iniciar o ambiente local.
- Persistência dos dados entre reinicializações dos contêineres.
- GitHub Actions para compilar, executar testes e validar
  a construção da imagem Docker.

Docker, integração contínua e gráficos atendem à categoria
de extras prevista nos critérios de avaliação.

## Compilação

### Pré-requisitos

- JDK 17.
- Maven.

### Verificação do ambiente

Execute:

    java -version
    mvn -version

Confira se o Maven está utilizando o JDK 17.

### Geração do WAR

Na raiz do projeto, execute:

    mvn clean package

O artefato será gerado em:

    target/prontus.war

A geração do WAR valida o empacotamento. A execução da aplicação
dependerá da configuração do servidor, prevista na próxima etapa.

## Etapas de desenvolvimento

1. Estrutura inicial Maven, dependências, README e Git.
2. Ambiente Docker e aplicação inicial no WildFly.
3. Modelo de domínio, regras e testes unitários.
4. Persistência JPA e testes.
5. Casos de uso do cadastro, validações e testes.
6. Interface com cadastro, listagem paginada e edição.
7. Procedure de cálculo de idade, integração e testes.
8. Gráficos de pacientes por faixa etária.
9. Relatório PDF com JasperReports.
10. Integração contínua e documentação final.

Cada etapa será validada antes de um commit, mantendo um
histórico da evolução da implementação.

## Execução local com Docker

### Pré-requisitos

- Docker com suporte a contêineres Linux.
- Docker Compose.

Não é necessário instalar Java ou Maven na máquina para executar
por Docker: a compilação ocorre dentro da imagem de build.

### Iniciar a aplicação

Na raiz do projeto, execute:

    docker compose up --build -d

Acesse:

    http://localhost:8080/prontus/

O ambiente utiliza WildFly 37.0.0.Final com Java 17,
compatível com Jakarta EE 10.

### Consultar os logs

    docker compose logs -f prontus

### Encerrar o ambiente

    docker compose down

### Aplicar alterações no código

Após modificar o projeto, execute novamente:

    docker compose up --build -d

O código é incorporado à imagem durante a construção.

## Licença

Este projeto está licenciado sob a licença MIT.
Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.

As dependências utilizadas mantêm suas respectivas licenças.

## Persistência

A aplicação utiliza JPA com Hibernate, com a unidade de persistência
prontusPU e o datasource ProntusDS gerenciado pelo WildFly.

No ambiente local, o H2 funciona em modo Oracle e armazena os dados
no volume Docker prontus-dados.

O script docker/h2/init.sql cria a tabela PACIENTE e a sequência
SEQ_PACIENTE quando ainda não existem. Ele não realiza migrações
de estruturas existentes.

### Convenções

- Tabelas no singular e em maiúsculas.
- Colunas em maiúsculas, com palavras separadas por underscore.
- Identificadores gerados por sequência.

### Regras de armazenamento

- Nome completo obrigatório, com até 255 caracteres após
  a remoção de espaços nas extremidades.
- Data de nascimento obrigatória e não futura.
- Data de cadastro preenchida na inclusão e preservada nas edições.
- Data de atualização preenchida na inclusão e renovada
  quando o JPA atualiza o registro.

O limite de 255 caracteres é uma decisão da implementação.

No H2 em modo Oracle, DATE é interpretado como TIMESTAMP(0).
Por isso, DATA_NASCIMENTO utiliza esse tipo explicitamente
no mapeamento e no SQL, mantendo LocalDate no Java.

## Casos de uso

A camada application coordena as operações do sistema:

- CadastrarPaciente: valida os dados pelo domínio e solicita a inclusão.
- AtualizarPaciente: valida a identidade e os dados e solicita a edição.
- BuscarPaciente: retorna o paciente ou lança PacienteNaoEncontradoException.
- ListarPacientes: retorna uma página de pacientes e a contagem total.

Os casos de uso dependem da interface PacienteRepository,
sem conhecer a implementação JPA.

PaginaPacientes representa o resultado paginado e contém uma
lista não modificável e o total de registros.

A listagem e a contagem são consultas separadas e podem refletir
momentos diferentes quando há alterações concorrentes.

## Estado atual

### Implementado

- Projeto Maven com Java 17, Jakarta EE 10 e PrimeFaces 14.
- Aplicação executando no WildFly pelo Docker Compose.
- Página inicial JSF com componente PrimeFaces.
- Modelo de domínio Paciente com regras de validação.
- Persistência JPA com H2 em modo Oracle e volume Docker.
- Preenchimento automático das datas de cadastro e atualização.
- Casos de uso para cadastrar, atualizar, buscar e listar pacientes.
- Tratamento específico de paciente não encontrado.
- Testes de domínio, casos de uso e integração da persistência.
- Licença MIT.

