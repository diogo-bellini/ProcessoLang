package br.ufscar.dc.compiladores;

import java.util.LinkedList;

// Gerenciador da pilha de escopos (tabelas de símbolos) durante a compilação
public class Escopos {

    // Estrutura de dados que atua como uma pilha (LIFO) para armazenar os escopos ativos
    LinkedList<Escopo> escopos = new LinkedList<>();

    // Cria um novo escopo atrelado a um processo e o empilha como o escopo atual
    public void criarEscopo(String nomeProcesso){
        escopos.push(new Escopo(nomeProcesso));
    }

    // Retorna o escopo que está no topo da pilha (o processo que está sendo analisado no momento)
    public Escopo obterEscopoAtual(){
        return escopos.peek();
    }

    // Remove o escopo do topo da pilha (chamado após finalizar a análise de um processo inteiro)
    public void removerEscopoAtual(){
        escopos.pop();
    }
}