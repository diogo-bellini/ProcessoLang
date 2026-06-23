package br.ufscar.dc.compiladores;

import java.util.ArrayList;
import java.util.List;

// Visitor responsável pela Análise Semântica: valida regras lógicas, escopos e referências
public class ProcessoLangSemantico extends ProcessoLangBaseVisitor<Void>{

    // Gerenciador da tabela de símbolos (pilha de escopos)
    private final Escopos escopos = new Escopos();

    // Lista global para impedir que dois processos tenham o mesmo nome no arquivo
    private final List<String> processosDeclarados = new ArrayList<>();

    // Acumulador temporário para guardar todos os destinos citados nos comandos "proximo" do processo atual
    private final List<String> destinosDoProcessoAtual = new ArrayList<>();

    // Flag de controle para garantir a regra de encerramento obrigatório
    private boolean processoAtualPossuiFinal = false;

    @Override
    public Void visitPrograma(ProcessoLangParser.ProgramaContext ctx) {
        // Ponto de entrada: visita sequencialmente todos os processos declarados no arquivo
        for(var processo : ctx.processo()){
            visitProcesso(processo);
        }
        return null;
    }

    @Override
    public Void visitProcesso(ProcessoLangParser.ProcessoContext ctx) {
        String nomeProcesso = ctx.IDENTIFICADOR().getText();

        // VALIDAÇÃO 1: Impede a redefinição de um processo global
        if (processosDeclarados.contains(nomeProcesso)){
            throw new RuntimeException("Erro Semântico: O processo '" + nomeProcesso + "' já foi definido neste arquivo.");
        }
        processosDeclarados.add(nomeProcesso);

        // Prepara o ambiente isolado para analisar o interior deste processo
        escopos.criarEscopo(nomeProcesso);
        destinosDoProcessoAtual.clear();
        processoAtualPossuiFinal = false;

        // Dispara a visita aos nós filhos (onde as listas e flags serão populadas)
        visitPasso_inicio(ctx.passo_inicio());
        visitBloco_passos(ctx.bloco_passos());

        // VALIDAÇÃO 2: Garante que o processo não fique em loop infinito sem saída
        if (!processoAtualPossuiFinal) {
            throw new RuntimeException("Erro Semântico no processo '" + nomeProcesso + "': O processo precisa ter pelo menos um 'passo-final' definido.");
        }

        // VALIDAÇÃO 3: Caça aos fantasmas. Verifica se todos os destinos apontados realmente existem
        Escopo escopoAtual = escopos.obterEscopoAtual();
        for (String destino : destinosDoProcessoAtual) {
            if (!escopoAtual.existe(destino)) {
                throw new RuntimeException("Erro Semântico no processo '" + nomeProcesso + "': O passo '" + destino + "' foi indicado como destino mas nunca foi criado.");
            }
        }

        // Limpa a tabela de símbolos deste processo ao finalizar a análise
        escopos.removerEscopoAtual();
        return null;
    }

    @Override
    public Void visitPasso_inicio(ProcessoLangParser.Passo_inicioContext ctx) {
        Escopo escopoAtual = escopos.obterEscopoAtual();
        String nomePasso = ctx.IDENTIFICADOR().getText();

        // VALIDAÇÃO 4: Impede passos com nomes duplicados dentro do mesmo processo
        if (escopoAtual.existe(nomePasso)) {
            throw new RuntimeException("Erro Semântico: O passo '" + nomePasso + "' já existe no processo " + escopoAtual.nomeProcesso + ".");
        }

        escopoAtual.inserir(nomePasso, false);
        visitComando_fluxo(ctx.comando_fluxo());
        return null;
    }

    @Override
    public Void visitBloco_passos(ProcessoLangParser.Bloco_passosContext ctx) {
        // Roteador que direciona a visitação dependendo do tipo do passo filho
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

        // VALIDAÇÃO 4: Verificação de duplicação para passos intermediários
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

        // VALIDAÇÃO 4: Verificação de duplicação para passos de encerramento
        if (escopoAtual.existe(nomePasso)) {
            throw new RuntimeException("Erro Semântico: O passo-final '" + nomePasso + "' reutiliza um nome já existente no processo " + escopoAtual.nomeProcesso + ".");
        }

        escopoAtual.inserir(nomePasso, true);
        processoAtualPossuiFinal = true; // Sinaliza que o processo atendeu à regra de fechamento
        return null;
    }

    @Override
    public Void visitComando_fluxo(ProcessoLangParser.Comando_fluxoContext ctx) {
        // Varre a superfície do comando em busca das palavras chave 'proximo' para capturar o destino (+2 posições)
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("proximo")) {
                String destino = ctx.getChild(i + 2).getText();
                destinosDoProcessoAtual.add(destino);
            }
        }

        // Desce explicitamente para as sub-árvores das ramificações senao se
        if (ctx.clausula_senao_se() != null) {
            for (var senaoSe : ctx.clausula_senao_se()) {
                visitClausula_senao_se(senaoSe);
            }
        }
        return null;
    }

    @Override
    public Void visitClausula_senao_se(ProcessoLangParser.Clausula_senao_seContext ctx) {
        // Realiza a mesma captura segura de destino, agora dentro da "caixa fechada" do senao se
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("proximo")) {
                String destino = ctx.getChild(i + 2).getText();
                destinosDoProcessoAtual.add(destino);
            }
        }
        return null;
    }
}