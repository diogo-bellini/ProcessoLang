package br.ufscar.dc.compiladores;

// Representa uma entrada imutável na tabela de símbolos (Escopo) para um passo do processo
public record EntradaEscopo(
        // Identificador único do passo (ex: 'ReceberPacote')
        String nome,

        // Flag que indica se este nó é um 'passo-final' (usado para validar a garantia de encerramento)
        boolean ehFinal
){}