# ProcessoLang - Compilador de Processos de Negócio
## Autor: Diogo Conforti Vaz Bellini
## Instituição: Universidade Federal de São Carlos (UFSCar) - Ciência da Computação

Um compilador desenvolvido para a construção, validação e visualização de fluxos de processos de negócio. A **ProcessoLang** é uma DSL (Domain-Specific Language) declarativa que permite desenhar lógicas de roteamento, decisões e etapas de forma intuitiva, traduzindo esse código em representações visuais geradas via Graphviz (.dot).

---

## 🛠️ Arquitetura e Fases de Compilação

Este projeto implementa integralmente as três fases clássicas de um compilador, garantindo segurança na escrita e saída visual consistente.

### 1. Análise Léxica e Sintática (ALS)
A gramática (`ProcessoLang.g4`) foi projetada utilizando o ANTLR4 para ser simultaneamente flexível na escrita humana e rígida na estrutura lógica.
* **Léxica:** A linguagem ignora espaços em branco dinamicamente e permite que textos descritivos sejam escritos tanto com aspas duplas "..." quanto simples '...'.
* **Sintática:** A declaração de um processo exige obrigatoriamente um passo-inicio, seguido por um bloco de passos, com roteamentos bem definidos (proximo ou blocos se/senao). O compilador possui um tratador de erros customizado que aplica fail-fast, interrompendo a execução imediatamente ao encontrar problemas estruturais e indicando a linha/coluna exata.

**Testes de validação ALS (ver pasta testes/):**
* testes-lexico/lexico_sucesso_espacos_vazios.txt: Demonstra a resiliência do Lexer ao lidar com excesso de espaços e quebras de linha irregulares.
* testes-lexico/lexico_erro_caractere_invalido.txt: Demonstra o fail-fast disparando um erro léxico customizado ao encontrar o caractere inválido @.
* testes-sintatico/sintatico_erro_falta_passo_inicio.txt: Parser rejeita um processo que não declara o ponto de partida.
* testes-sintatico/sintatico_erro_falta_chaves.txt: Dispara erro sintático exato por chave não fechada.

### 2. Análise Semântica (AS)
Implementa uma tabela de símbolos baseada em escopos para validar regras de negócio rígidas. Foram construídas **4 verificações de consistência**:
1. **Unicidade Global de Processos:** Impede a redefinição de processos com o mesmo identificador.
2. **Unicidade Local de Passos:** Garante que não existam dois passos com o mesmo nome no mesmo escopo.
3. **Garantia de Encerramento:** Verifica se o processo possui ao menos um passo-final declarado (previne loops infinitos sem saída).
4. **Verificação de Destinos (Caça a Fantasmas):** O compilador cruza os destinos apontados nos comandos de fluxo com a tabela de símbolos. Se um passo direcionar o fluxo para um nó inexistente, o compilador lança exceção.

**Testes de validação AS (ver pasta testes/):**
* testes-semantico/semantico_erro_processo_duplicado.txt: Acusa escopo global repetido.
* testes-semantico/semantico_erro_passo_duplicado.txt: Acusa redefinição de variáveis/passos no mesmo processo.
* testes-semantico/semantico_erro_falta_passo_final.txt: Compila na ALS, mas o semântico barra por violar a garantia de encerramento.
* testes-semantico/semantico_erro_destino_fantasma.txt: Lança exceção acusando destino não implementado.

### 3. Geração de Código (GCI)
Traduz a Árvore Sintática Abstrata (AST) validada para a linguagem DOT, atuando como um desenhista automático via Graphviz.
* Agrupa múltiplos processos em subgraphs separados.
* Aplica formatação condicional visual (inícios verdes, finais vermelhos).
* Higieniza e escapa aspas dinamicamente para não quebrar a sintaxe de destino do DOT.

**Testes de validação GCI (ver pasta testes/):**
* testes-semantico/semantico_sucesso_multiplos_senao_se.txt: Gera um grafo complexo com múltiplos destinos em cascata.
* testes-semantico/gerador_sucesso_caracteres_especiais.txt: Demonstra a higienização do código, renderizando descrições com aspas aninhadas sem quebrar a interpretação do Graphviz.

---

## 📖 Exemplo de Sintaxe

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

Se você deseja modificar a gramática (.g4) ou as regras Java, precisará recompilar o projeto.

**Pré-requisitos:** Java (JDK 11+) e Maven.

1. Clone o repositório e navegue até a pasta raiz:
   
       cd ProcessoLang/compiladorProcessoLang

2. Utilize o Maven para limpar builds anteriores e gerar o JAR com as dependências (incluindo ANTLR):
   
       mvn clean package

3. O executável estará em: target/compiladorProcessoLang-1.0-SNAPSHOT-jar-with-dependencies.jar

---

## 🚀 Como Utilizar (Executando o Compilador)

O compilador exige dois parâmetros: o arquivo de entrada (.txt) e o arquivo de saída (.dot).

**Comando base (estando na pasta compiladorProcessoLang):**

    java -jar target/compiladorProcessoLang-1.0-SNAPSHOT-jar-with-dependencies.jar <entrada.txt> <saida.dot>

**Exemplo prático executando um teste local:**

    java -jar target/compiladorProcessoLang-1.0-SNAPSHOT-jar-with-dependencies.jar ../testes/testes-semantico/semantico_sucesso_senao_se.txt grafo_saida.dot

### Gerando a Imagem Visual do Grafo
Para transformar o arquivo .dot em uma imagem .png, instale o Graphviz.

Rode o comando:

    dot -Tpng grafo_saida.dot -o diagrama.png

---