# ProcessoLang - Compilador de Processos de Negócio
## Autor: Diogo Conforti Vaz Bellini - 823829
## Instituição: Universidade Federal de São Carlos (UFSCar) - Ciência da Computação

Um compilador desenvolvido para a construção, validação e visualização de fluxos de processos de negócio. A **ProcessoLang** é uma DSL (Domain-Specific Language) declarativa que permite desenhar lógicas de roteamento, decisões e etapas de forma intuitiva, traduzindo esse código em representações visuais geradas via Graphviz (.dot).

---

## 📖 Sobre a Linguagem

A linguagem foi projetada para ser robusta e à prova de falhas. Seu analisador semântico garante regras estritas de modelagem, impedindo a geração de fluxos inválidos. 

**Principais garantias e validações da linguagem:**
* **Garantia de Início:** Todo processo exige obrigatoriamente um passo-inicio.
* **Garantia de Fim:** Todo processo precisa alcançar ao menos um passo-final (impede loops infinitos sem saída).
* **Escopo Estrito:** Nomes de processos e passos não podem ser duplicados.
* **Integridade de Roteamento:** O compilador barra rotas apontadas para "passos fantasmas" (destinos não declarados).
* **Flexibilidade Léxica:** Suporte a textos formatados com aspas simples ou duplas.

### Exemplo de Sintaxe

    processo TriagemClinica {
        passo-inicio Entrada {
            descricao: "Avaliar o estado do paciente"
            se (urgente) {
                proximo: Emergencia
            } senao se ("caso moderado") {
                proximo: Amarelo
            } senao {
                proximo: FilaComum
            }
        }

        passo Emergencia {
            descricao: 'Encaminhar para a UTI'
            proximo: FimEmergencia
        }

        passo-final FimEmergencia { descricao: "Internado" }
        passo-final Amarelo { descricao: "Aguardando médico" }
        passo-final FilaComum { descricao: "Aguardando triagem básica" }
    }

---

## ⚙️ Como Compilar o Compilador (Para Desenvolvedores)

Se você deseja modificar a gramática (.g4), adicionar novas regras semânticas ou alterar o gerador de código, precisará recompilar o projeto.

### Pré-requisitos
* **Java** (JDK 11 ou superior)
* **Maven** instalado e configurado no PATH

### Passo a passo para build
1. Clone o repositório e navegue até a pasta raiz do código-fonte:
   
       cd ProcessoLang/compiladorProcessoLang

2. Utilize o Maven para limpar builds anteriores e empacotar o projeto gerando o JAR com todas as dependências (incluindo o runtime do ANTLR):
   
       mvn clean package

3. Se a compilação for bem-sucedida, o executável estará disponível no caminho:
   target/compiladorProcessoLang-1.0-SNAPSHOT-jar-with-dependencies.jar

---

## 🚀 Como Utilizar (Executando o Compilador)

Com o projeto compilado, você pode processar os seus arquivos .txt escritos em ProcessoLang e convertê-los em grafos direcionados.

### Sintaxe de Execução
O compilador exige dois parâmetros: o arquivo de entrada e o arquivo de saída. Estando na pasta compiladorProcessoLang:

    java -jar target/compiladorProcessoLang-1.0-SNAPSHOT-jar-with-dependencies.jar <caminho_entrada.txt> <caminho_saida.dot>

**Exemplo prático usando a pasta de testes do repositório:**

    java -jar target/compiladorProcessoLang-1.0-SNAPSHOT-jar-with-dependencies.jar ../testes/testes-semantico/semantico_sucesso_senao_se.txt grafo_saida.dot

### Gerando a Imagem Visual do Grafo
Para transformar o arquivo .dot gerado em uma imagem .png, você precisa ter o Graphviz instalado na sua máquina (ex: brew install graphviz no macOS).

Rode o comando:

    dot -Tpng grafo_saida.dot -o diagrama.png

---

## 📂 Estrutura do Repositório

* compiladorProcessoLang/: Contém todo o ecossistema do compilador (projeto Maven).
  * src/main/antlr4/br/ufscar/dc/compiladores/ProcessoLang.g4: Gramática da linguagem (Análise Léxica e Sintática).
  * src/main/java/br/ufscar/dc/compiladores/: Classes Java (Tratamento de Erros, Análise Semântica, Tabela de Símbolos e Geração de Código).
* testes/: Bateria completa de testes automatizados e manuais.
  * testes-lexico/: Validações de strings flexíveis, aspas, comentários e caracteres inválidos.
  * testes-sintatico/: Validações da estrutura de blocos e obrigatoriedade de palavras-chave.
  * testes-semantico/: Validações das regras de negócio (passos repetidos, rotas falsas, ausência de finalização).

---