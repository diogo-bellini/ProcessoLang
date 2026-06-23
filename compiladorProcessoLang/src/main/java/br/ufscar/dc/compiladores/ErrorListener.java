package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

// Classe que intercepta os erros padrão do ANTLR para customizar as mensagens e aplicar o fail-fast
public class ErrorListener extends BaseErrorListener {

    // Método chamado automaticamente pelo ANTLR sempre que encontra um erro léxico ou sintático
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {

        // Verifica se o erro ocorreu durante a Análise Léxica (caractere inválido)
        if (recognizer instanceof ProcessoLangLexer) {
            // Extrai apenas o caractere problemático da mensagem padrão do ANTLR
            String caractereInvalido = msg.substring(msg.lastIndexOf(':') + 1).trim();

            // Lança exceção para interromper a compilação imediatamente com uma mensagem clara em português
            throw new RuntimeException("Erro Léxico na linha " + line + ", coluna " + charPositionInLine + " -> O caractere " + caractereInvalido + " não é reconhecido pela linguagem.");

            // Caso contrário, o erro ocorreu durante a Análise Sintática (estrutura gramatical inválida)
        } else {
            String tokenTexto = "";

            // Tenta resgatar o texto exato do token que quebrou a regra gramatical
            if (offendingSymbol instanceof Token) {
                tokenTexto = "'" + ((Token) offendingSymbol).getText() + "'";
            }

            // Lança exceção detalhando onde a sintaxe falhou para interromper o processo
            throw new RuntimeException("Erro Sintático na linha " + line + ", coluna " + charPositionInLine + " -> Estrutura inválida próximo ao token " + tokenTexto + ". Detalhes: " + msg);
        }
    }
}