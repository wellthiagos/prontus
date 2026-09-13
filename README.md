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
O servidor adotado é o WildFly 37.0.0.Final, compatível com Jakarta EE 10.

Os requisitos funcionais e os critérios de entrega previstos
no desafio permanecem mantidos.

## Tecnologias configuradas

- Java 17.
- Maven 3.9.9 no Docker.
- Jakarta EE 10.
- PrimeFaces 14.0.12, na variante Jakarta.
- WildFly 37.0.0.Final com Java 17.
- Oracle Free (imagem gvenzl/oracle-free:23-slim).
- JasperReports 6.21.5.
- Empacotamento WAR.

## Arquitetura e diretrizes

O projeto utiliza Clean Code, princípios SOLID
e Domain-Driven Design (DDD).

A implementação está organizada nas seguintes camadas:

- **Domínio:** modelo de paciente e regras de negócio.
- **Aplicação:** coordenação dos casos de uso.
- **Infraestrutura:** persistência, procedures e geração de relatórios.
- **Apresentação:** telas JSF e componentes PrimeFaces.

As responsabilidades são separadas para facilitar manutenção
e testes, com abstrações introduzidas conforme a necessidade.

## Funcionalidades implementadas

- Cadastro de pacientes com nome completo e data de nascimento.
- Listagem paginada com PrimeFaces DataTable e ordenação por Código, Nome e Data de Nascimento.
- Edição dos dados dos pacientes.
- Validações com Jakarta Bean Validation.
- Procedure Oracle P_PATIENT_AGE, com identificador do paciente
  como entrada e idade em anos como saída.
- Botão “Calcular Idade”, com exibição do resultado em Dialog.
- Gráficos de pacientes por faixa etária.
- Relatório PDF de pacientes com JasperReports.

O relatório com JasperReports é uma funcionalidade adicional,
não exigida no enunciado.

## Testes

Os testes unitários utilizam JUnit 5 e são executados pelo
Maven Surefire. A suíte padrão inclui integração com H2 em memória e não exige Docker, WildFly ou banco externo.

Para executar:

    mvn test

Para executar os testes e gerar o WAR:

    mvn clean package

### Cobertura atual

A suíte contém 182 execuções de testes:

- 166 na suíte padrão (domínio, casos de uso, apresentação e integração H2).
- 16 na suíte de integração Oracle, executada separadamente pelo perfil oracle-it.

O comando mvn test executa os 166 testes padrão. Os comandos do ambiente
de integração Oracle, documentados abaixo, executam as duas suítes.

Os testes dos casos de uso verificam cadastro, atualização, busca,
paginação, rejeição de dados inválidos e propagação de falhas.

Mockito simula o contrato PacienteRepository nesses testes.
O funcionamento real da persistência é verificado separadamente
pelos testes de integração.

Os testes executados fora do WildFly não validam os interceptadores
CDI e o gerenciamento de transações JTA.

## Docker e integração contínua

Estão implementados:

- Dockerfile com build em múltiplas etapas.
- Ambiente de execução com WildFly compatível com Jakarta EE 10.
- Docker Compose para iniciar o ambiente local.
- Persistência dos dados entre reinicializações dos contêineres.

O workflow `.github/workflows/ci.yml` executa em pushes para `main`, pull requests
e manualmente pela aba **Actions** do GitHub.

- **Testes Java e Oracle:** executa os 166 testes padrão e os 16 testes Oracle
  em um banco novo, criado pelos scripts do projeto. Falhas nos testes reprovam o job.
- **Build da imagem Docker:** valida os arquivos Compose e constrói a imagem
  usando o Dockerfile do projeto, incluindo a compilação e os testes padrão.
- Os relatórios JUnit e os logs do ambiente Oracle ficam no artefato
  `resultados-testes`, disponível por 7 dias, inclusive quando um teste falha.
- O ambiente de testes é removido ao final. Não são necessárias credenciais
  pessoais nem secrets para esses jobs; os valores de exemplo são locais aos runners.

O workflow valida testes e construção, sem publicar imagem nem implantar a aplicação.
A primeira execução remota ocorrerá após o envio desse arquivo ao GitHub.

Docker, integração contínua e gráficos atendem à categoria
de extras prevista nos critérios de avaliação.

## Compilação

### Pré-requisitos

- JDK 17.
- Maven 3.9.9 no Docker.

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

A geração do WAR valida o empacotamento. Para executar a aplicação com
WildFly e Oracle configurados, siga a seção de execução local com Docker.

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

Na primeira execução, na raiz do projeto, use o PowerShell:

```powershell
Copy-Item .env.example .env
docker compose up --build -d
```

No Linux/macOS, substitua a primeira linha por `cp .env.example .env`.
O `.env.example` contém valores de exemplo para desenvolvimento local.
O `.env` é ignorado pelo Git. Se ele já existir, preserve sua configuração e
execute apenas `docker compose up --build -d`.

Em um volume Oracle novo, a inicialização ocorre automaticamente:

1. O contêiner inicializa o banco e cria o usuário `prontus` em `FREEPDB1`.
2. Executa `docker/oracle/00-inicializar.sql`.
3. Esse script chama `01-schema.sql`, que cria `SEQ_PACIENTE` e `PACIENTE`,
   e `02-procedure-idade.sql`, que cria `P_PATIENT_AGE` e verifica sua validade.
4. A aplicação aguarda o Oracle ficar saudável antes de iniciar.

O arquivo `teste-idade.sql` é um teste manual e não executa automaticamente.
A criação do banco do zero foi validada pela suíte de integração Oracle.
A primeira inicialização pode levar alguns minutos; o início do contêiner
não significa que o WildFly já concluiu a publicação da aplicação.

Com um volume existente, os dados são preservados e os scripts de inicialização
não são executados novamente. Alterações posteriores no esquema ou na procedure
precisam ser aplicadas ao banco existente; reconstruir a imagem não as aplica.

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

A aplicação utiliza Oracle no contêiner gvenzl/oracle-free:23-slim e armazena
os dados no volume Docker oracle-dados. O datasource conecta ao serviço FREEPDB1.

O H2 permanece nos testes da suíte padrão. O volume prontus-dados é legado
da configuração anterior; seus registros não são transferidos automaticamente
para o Oracle. Os scripts Oracle são descritos na seção de execução local.

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
- Interface JSF/PrimeFaces com cadastro, listagem paginada e edição.
- Modelo de domínio Paciente com regras de validação.
- Persistência JPA com Oracle e volume Docker.
- Preenchimento automático das datas de cadastro e atualização.
- Casos de uso para cadastrar, atualizar, buscar e listar pacientes.
- Tratamento específico de paciente não encontrado.
- Cálculo de idade pela procedure Oracle, com exibição em Dialog.
- Gráficos por faixa etária e relatório PDF com JasperReports.
- Testes de domínio, casos de uso, apresentação e integração H2/Oracle.
- Licença MIT.


### Testes de integração Oracle

Requer Docker Compose. O ambiente abaixo usa um banco separado, sem portas
publicadas e sem acesso ao volume da aplicacao. As senhas desse Compose sao
exclusivas para o banco descartavel de testes; nao utiliza o arquivo .env.

```powershell
docker compose -p prontus-oracle-it -f compose.oracle-it.yaml up --abort-on-container-exit --exit-code-from testes
docker compose -p prontus-oracle-it -f compose.oracle-it.yaml down -v
```

Execute o segundo comando mesmo se os testes falharem; ele remove somente o
ambiente prontus-oracle-it e permite validar a inicializacao do zero na proxima
execucao. Nao execute simultaneamente com outro build Maven neste diretorio.
Relatorios: target/failsafe-reports (Oracle) e target/surefire-reports (testes existentes).
O perfil oracle-it executa os testes existentes e os testes Oracle no Maven verify.
O build normal continua sem exigir um Oracle de testes.

A procedure aceita p_reference_date como terceiro parametro opcional para
validacao deterministica de aniversarios. Quando omitido, utiliza a data atual
em America/Sao_Paulo. A aplicacao continua chamando os dois parametros originais.
Em bancos ja existentes, reaplique docker/oracle/02-procedure-idade.sql para
disponibilizar esse parametro; os scripts de inicializacao rodam apenas em bancos novos.
## Roteiro de avaliação

1. Copie `.env.example` para `.env` e execute `docker compose up --build -d`.
2. Aguarde a publicação de `prontus.war` nos logs e abra http://localhost:8080/prontus/.
3. Cadastre registros, consulte por nome e intervalo de nascimento e edite pela listagem.
4. Com mais de 10 registros, navegue entre as páginas; o total é a quantidade de
   registros encontrados, não o maior código da sequência.
5. Clique na calculadora para consultar a idade retornada pela procedure Oracle.
6. Baixe o PDF: ele inclui todos os registros da consulta, independentemente da
   página aberta. O botão aparece quando há registros.
7. Abra os gráficos: os indicadores consideram todos os pacientes, sem filtros.
8. Execute o ambiente de testes Oracle descrito acima para validar as duas suítes.

O banco novo começa sem pacientes. Os testes usam dados próprios em um ambiente
isolado. A aplicação não possui autenticação nesta entrega.

## Diagnóstico de inicialização

```powershell
docker compose ps
docker compose logs --tail 100 oracle
docker compose logs --tail 100 prontus
```

Aguarde o Oracle ficar `healthy` e o WildFly registrar `Deployed "prontus.war"`.
Se faltar uma variável obrigatória, confira o `.env` na raiz do projeto.
Alterar a senha no `.env` não altera a senha de um usuário em um volume Oracle
já inicializado: mantenha os valores correspondentes ao banco existente.
`docker compose down` preserva os dados; `docker compose down -v` remove os
volumes e seus dados, portanto não deve ser usado para uma reinicialização comum.