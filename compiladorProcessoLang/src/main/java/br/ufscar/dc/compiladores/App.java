package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// Classe principal que orquestra todas as fases da compilação
public class App
{
    public static void main( String[] args ) {
        // Valida se os arquivos de entrada (.txt) e saída (.dot) foram fornecidos via terminal
        if (args.length < 2) {
            System.err.println("Erro: Forneça o arquivo de entrada e o de saída.");
            System.err.println("Exemplo: java -jar target/compiladorProcessoLang-1.0-SNAPSHOT-jar-with-dependencies.jar entrada.txt saida.dot");
            System.exit(1);
        }

        try {
            // Carrega o arquivo de entrada em um stream de caracteres
            CharStream cs = CharStreams.fromFileName(args[0]);

            // Instancia o tratador de erros customizado (fail-fast)
            ErrorListener errorListener = new ErrorListener();

            // FASE 1: Análise Léxica (Converte texto em tokens)
            ProcessoLangLexer lexer = new ProcessoLangLexer(cs);
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            // Cria um canal de tokens para alimentar o Parser
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // FASE 2: Análise Sintática (Garante a estrutura gramatical)
            ProcessoLangParser parser = new ProcessoLangParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Dispara a análise e guarda a Árvore Sintática Abstrata (AST) gerada
            ProcessoLangParser.ProgramaContext arvoreSintatica = parser.programa();

            // FASE 3: Análise Semântica (Valida regras de negócio e referências)
            ProcessoLangSemantico semantico = new ProcessoLangSemantico();
            semantico.visit(arvoreSintatica);

            // FASE 4: Geração de Código (Traduz a AST para Graphviz DOT)
            // Só executa se o analisador semântico não lançar nenhuma exceção
            ProcessoLangGeradorCodigo gerador = new ProcessoLangGeradorCodigo();
            gerador.visit(arvoreSintatica);

            // Escreve a string final gerada dentro do arquivo de saída especificado
            try (PrintWriter out = new PrintWriter(new FileWriter(args[1]))) {
                out.print(gerador.getCodigo());
            }

            System.out.println("Sucesso! Compilação e Geração de Código concluídas.");
        } catch (IOException e) {
            // Captura erros de I/O (ex: arquivo de entrada não encontrado)
            System.err.println("Erro ao abrir o arquivo: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            // Captura as exceções customizadas lançadas pelo Lexer, Parser ou Semântico
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}