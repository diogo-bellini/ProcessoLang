package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.*;
import java.io.IOException;

public class App
{
    public static void main( String[] args ) {
        if (args.length == 0) {
            System.err.println("Erro: Por favor, forneça o caminho do arquivo de teste por argumento.");
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
            System.out.println("Análise Léxica e Sintática concluída com SUCESSO!");

            ProcessoLangSemantico semantico = new ProcessoLangSemantico();
            semantico.visit(arvoreSintatica);
            System.out.println("Análise Semântica concluída com SUCESSO!");

        } catch (IOException e) {
            System.err.println("Erro ao abrir o arquivo: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}