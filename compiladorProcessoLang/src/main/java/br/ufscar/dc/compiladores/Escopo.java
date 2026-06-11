package br.ufscar.dc.compiladores;

import java.util.HashMap;

public class Escopo {
    public final String nomeProcesso;
    private HashMap<String, EntradaEscopo> escopo = new HashMap<>();

    public Escopo(String nomeProcesso) {
        this.nomeProcesso = nomeProcesso;
    }

    public void inserir(String nomePasso, boolean ehFinal){
        EntradaEscopo entrada = new EntradaEscopo(nomePasso, ehFinal);
        escopo.put(nomePasso, entrada);
    }

    public boolean existe(String nomePasso){
        return escopo.containsKey(nomePasso);
    }
}
