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

A classe PacienteTest verifica:

- Criação de paciente com dados válidos.
- Rejeição de nascimento futuro.
- Aceitação de nascimento na data atual.
- Rejeição de data de nascimento nula.
- Rejeição de nome nulo, vazio ou composto apenas por espaços,
  tabulação ou quebra de linha.
- Remoção de espaços nas extremidades do nome.

São 10 execuções, incluindo os cenários do teste parametrizado.

Os testes utilizam um Clock fixo para que o resultado não dependa
da data real de execução.

Testes dos casos de uso, da persistência e do serviço da procedure
serão adicionados nas etapas correspondentes.

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

## Estado atual

- Projeto Maven configurado e compilação validada.
- Imagem Docker construída com Maven e Java 17.
- Aplicação executando no WildFly pelo Docker Compose.
- Página inicial JSF com componente PrimeFaces validada no navegador.
- Modelo de domínio Paciente com validação dos dados obrigatórios e rejeição de nascimento futuro.
- DomainException para representar violações das regras de negócio.
- Testes unitários do domínio: 10 execuções aprovadas.

Casos de uso de cadastro, persistência, procedure, gráficos,
relatório e integração contínua serão implementados
nas próximas etapas.

