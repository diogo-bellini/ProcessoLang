package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class App
{
    public static void main( String[] args ) {
        if (args.length < 2) {
            System.err.println("Erro: Forneça o arquivo de entrada e o de saída.");
            System.err.println("Exemplo: java -jar target/compiladorProcessoLang-1.0-SNAPSHOT-jar-with-dependencies.jar entrada.txt saida.dot");
            System.exit(1);
        }

        try {
            CharStream cs = CharStreams.fromFileName(args[0]);
            ErrorListener errorListener = new ErrorListener();

            ProcessoLangLexer lexer = new ProcessoLangLexer(cs);
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            ProcessoLangParser parser = new ProcessoLangParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            ProcessoLangParser.ProgramaContext arvoreSintatica = parser.programa();

            ProcessoLangSemantico semantico = new ProcessoLangSemantico();
            semantico.visit(arvoreSintatica);

            ProcessoLangGeradorCodigo gerador = new ProcessoLangGeradorCodigo();
            gerador.visit(arvoreSintatica);

            try (PrintWriter out = new PrintWriter(new FileWriter(args[1]))) {
                out.print(gerador.getCodigo());
            }

            System.out.println("Sucesso! Compilação e Geração de Código concluídas.");
        } catch (IOException e) {
            System.err.println("Erro ao abrir o arquivo: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}