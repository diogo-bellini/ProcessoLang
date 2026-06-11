package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

public class ErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if (recognizer instanceof ProcessoLangLexer) {
            String caractereInvalido = msg.substring(msg.lastIndexOf(':') + 1).trim();
            throw new RuntimeException("Erro Léxico na linha " + line + ", coluna " + charPositionInLine + " -> O caractere " + caractereInvalido + " não é reconhecido pela linguagem.");
        } else {
            String tokenTexto = "";
            if (offendingSymbol instanceof Token) {
                tokenTexto = "'" + ((Token) offendingSymbol).getText() + "'";
            }
            throw new RuntimeException("Erro Sintático na linha " + line + ", coluna " + charPositionInLine + " -> Estrutura inválida próximo ao token " + tokenTexto + ". Detalhes: " + msg);
        }
    }
}
