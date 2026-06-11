package br.ufscar.dc.compiladores;

import java.util.ArrayList;
import java.util.List;

public class ProcessoLangSemantico extends ProcessoLangBaseVisitor<Void>{
    private final Escopos escopos = new Escopos();
    private final List<String> processosDeclarados = new ArrayList<>();
    private final List<String> destinosDoProcessoAtual = new ArrayList<>();
    private boolean processoAtualPossuiFinal = false;

    @Override
    public Void visitPrograma(ProcessoLangParser.ProgramaContext ctx) {
        for(var processo : ctx.processo()){
            visitProcesso(processo);
        }
        return null;
    }

    @Override
    public Void visitProcesso(ProcessoLangParser.ProcessoContext ctx) {
        String nomeProcesso = ctx.IDENTIFICADOR().getText();
        if (processosDeclarados.contains(nomeProcesso)){
            throw new RuntimeException("Erro Semântico: O processo '" + nomeProcesso + "' já foi definido neste arquivo.");
        }

        processosDeclarados.add(nomeProcesso);

        escopos.criarEscopo(nomeProcesso);
        destinosDoProcessoAtual.clear();
        processoAtualPossuiFinal = false;

        visitPasso_inicio(ctx.passo_inicio());

        visitBloco_passos(ctx.bloco_passos());

        if (!processoAtualPossuiFinal) {
            throw new RuntimeException("Erro Semântico no processo '" + nomeProcesso + "': O processo precisa ter pelo menos um 'passo-final' definido.");
        }

        Escopo escopoAtual = escopos.obterEscopoAtual();
        for (String destino : destinosDoProcessoAtual) {
            if (!escopoAtual.existe(destino)) {
                throw new RuntimeException("Erro Semântico no processo '" + nomeProcesso + "': O passo '" + destino + "' foi indicado como destino mas nunca foi criado.");
            }
        }

        escopos.removerEscopoAtual();
        return null;
    }

    @Override
    public Void visitPasso_inicio(ProcessoLangParser.Passo_inicioContext ctx) {
        Escopo escopoAtual = escopos.obterEscopoAtual();
        String nomePasso = ctx.IDENTIFICADOR().getText();

        if (escopoAtual.existe(nomePasso)) {
            throw new RuntimeException("Erro Semântico: O passo '" + nomePasso + "' já existe no processo " + escopoAtual.nomeProcesso + ".");
        }

        escopoAtual.inserir(nomePasso, false);

        visitComando_fluxo(ctx.comando_fluxo());
        return null;
    }

    @Override
    public Void visitBloco_passos(ProcessoLangParser.Bloco_passosContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            var filho = ctx.getChild(i);
            if (filho instanceof ProcessoLangParser.Passo_normalContext) {
                visitPasso_normal((ProcessoLangParser.Passo_normalContext) filho);
            } else if (filho instanceof ProcessoLangParser.Passo_finalContext) {
                visitPasso_final((ProcessoLangParser.Passo_finalContext) filho);
            }
        }
        return null;
    }

    @Override
    public Void visitPasso_normal(ProcessoLangParser.Passo_normalContext ctx) {
        Escopo escopoAtual = escopos.obterEscopoAtual();
        String nomePasso = ctx.IDENTIFICADOR().getText();

        if (escopoAtual.existe(nomePasso)) {
            throw new RuntimeException("Erro Semântico: O passo '" + nomePasso + "' já existe no processo " + escopoAtual.nomeProcesso + ".");
        }

        escopoAtual.inserir(nomePasso, false);

        visitComando_fluxo(ctx.comando_fluxo());
        return null;
    }

    @Override
    public Void visitPasso_final(ProcessoLangParser.Passo_finalContext ctx) {
        Escopo escopoAtual = escopos.obterEscopoAtual();
        String nomePasso = ctx.IDENTIFICADOR().getText();

        if (escopoAtual.existe(nomePasso)) {
            throw new RuntimeException("Erro Semântico: O passo-final '" + nomePasso + "' reutiliza um nome já existente no processo " + escopoAtual.nomeProcesso + ".");
        }

        escopoAtual.inserir(nomePasso, true);
        processoAtualPossuiFinal = true;
        return null;
    }

    @Override
    public Void visitComando_fluxo(ProcessoLangParser.Comando_fluxoContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("proximo")) {
                String destino = ctx.getChild(i + 2).getText();
                destinosDoProcessoAtual.add(destino);
            }
        }

        if (ctx.clausula_senao_se() != null) {
            for (var senaoSe : ctx.clausula_senao_se()) {
                visitClausula_senao_se(senaoSe);
            }
        }
        return null;
    }

    @Override
    public Void visitClausula_senao_se(ProcessoLangParser.Clausula_senao_seContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("proximo")) {
                String destino = ctx.getChild(i + 2).getText();
                destinosDoProcessoAtual.add(destino);
            }
        }
        return null;
    }
}