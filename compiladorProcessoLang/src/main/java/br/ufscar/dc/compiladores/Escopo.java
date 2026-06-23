package br.ufscar.dc.compiladores;

import java.util.HashMap;

// Representa a tabela de símbolos de um processo específico (escopo local)
public class Escopo {

    // Nome do processo ao qual esta tabela de símbolos pertence
    public final String nomeProcesso;

    // Tabela Hash que mapeia o nome do passo para as suas propriedades na linguagem
    private HashMap<String, EntradaEscopo> escopo = new HashMap<>();

    // Construtor que inicializa o escopo atrelando-o a um processo
    public Escopo(String nomeProcesso) {
        this.nomeProcesso = nomeProcesso;
    }

    // Adiciona um novo passo na tabela de símbolos local
    public void inserir(String nomePasso, boolean ehFinal){
        EntradaEscopo entrada = new EntradaEscopo(nomePasso, ehFinal);
        escopo.put(nomePasso, entrada);
    }

    // Verifica se um passo com o nome informado já foi declarado dentro deste processo
    public boolean existe(String nomePasso){
        return escopo.containsKey(nomePasso);
    }
}