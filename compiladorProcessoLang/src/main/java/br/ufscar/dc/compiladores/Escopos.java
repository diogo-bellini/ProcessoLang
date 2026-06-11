package br.ufscar.dc.compiladores;

import java.util.LinkedList;

public class Escopos {
    LinkedList<Escopo> escopos = new LinkedList<>();

    public void criarEscopo(String nomeProcesso){
        escopos.push(new Escopo(nomeProcesso));
    }

    public Escopo obterEscopoAtual(){
        return escopos.peek();
    }

    public void removerEscopoAtual(){
        escopos.pop();
    }
}
