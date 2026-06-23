package br.ufscar.dc.compiladores;

public class ProcessoLangGeradorCodigo extends ProcessoLangBaseVisitor<Void> {
    private final StringBuilder saida = new StringBuilder();
    private String passoAtual = "";

    public String getCodigo() {
        return saida.toString();
    }

    private String formatarLabel(String textoOriginal) {
        String textoPuro = textoOriginal.substring(1, textoOriginal.length() - 1);
        return "\"" + textoPuro.replace("\"", "\\\"") + "\"";
    }

    @Override
    public Void visitPrograma(ProcessoLangParser.ProgramaContext ctx) {
        saida.append("digraph Processos {\n");
        saida.append("  fontname=\"Helvetica,Arial,sans-serif\";\n");
        saida.append("  node [fontname=\"Helvetica,Arial,sans-serif\", style=filled, fillcolor=white, shape=box];\n");
        saida.append("  edge [fontname=\"Helvetica,Arial,sans-serif\", fontsize=10];\n\n");

        for(var processo : ctx.processo()){
            visitProcesso(processo);
        }

        saida.append("}\n");
        return null;
    }

    @Override
    public Void visitProcesso(ProcessoLangParser.ProcessoContext ctx) {
        String nomeProcesso = ctx.IDENTIFICADOR().getText();

        saida.append("  subgraph cluster_").append(nomeProcesso).append(" {\n");
        saida.append("    label=\"Processo: ").append(nomeProcesso).append("\";\n");
        saida.append("    style=dashed;\n");
        saida.append("    color=gray50;\n\n");

        visitPasso_inicio(ctx.passo_inicio());
        visitBloco_passos(ctx.bloco_passos());

        saida.append(" }\n\n");
        return null;
    }

    @Override
    public Void visitPasso_inicio(ProcessoLangParser.Passo_inicioContext ctx) {
        passoAtual = ctx.IDENTIFICADOR().getText();
        String descricao = ctx.TEXTO().getText();

        saida.append("    ").append(passoAtual).append(" [label=").append(formatarLabel(descricao)).append(", shape=oval, fillcolor=palegreen];\n");

        visitComando_fluxo(ctx.comando_fluxo());
        return null;
    }

    @Override
    public Void visitPasso_normal(ProcessoLangParser.Passo_normalContext ctx) {
        passoAtual = ctx.IDENTIFICADOR().getText();
        String descricao = ctx.TEXTO().getText();

        saida.append("    ").append(passoAtual).append(" [label=").append(formatarLabel(descricao)).append("];\n");

        visitComando_fluxo(ctx.comando_fluxo());
        return null;
    }

    @Override
    public Void visitPasso_final(ProcessoLangParser.Passo_finalContext ctx) {
        String nomePasso = ctx.IDENTIFICADOR().getText();
        String descricao = ctx.TEXTO().getText();

        saida.append("    ").append(nomePasso).append(" [label=").append(formatarLabel(descricao)).append(", shape=doubleoctagon, fillcolor=mistyrose];\n");

        return null;
    }

    @Override
    public Void visitComando_fluxo(ProcessoLangParser.Comando_fluxoContext ctx) {
        if(ctx.getChild(0).getText().equals("proximo")){
            String destino = ctx.getChild(2).getText();
            saida.append("    ").append(passoAtual).append(" -> ").append(destino).append(";\n");

        }else if(ctx.getChild(0).getText().equals("se")){
            String condicao = ctx.getChild(2).getText().replace("\"", "").replace("'", "");

            String destinoSe = ctx.getChild(7).getText();
            saida.append("    ").append(passoAtual).append(" -> ").append(destinoSe).append(" [label=\"").append(condicao).append("\"];\n");

            if(ctx.clausula_senao_se() != null){
                for(var senaoSe : ctx.clausula_senao_se()){
                    visitClausula_senao_se(senaoSe);
                }
            }

            String destinoSenao = ctx.getChild(ctx.getChildCount() - 2).getText();
            saida.append("    ").append(passoAtual).append(" -> ").append(destinoSenao).append(" [label=\"senao\"];\n");
        }

        return null;
    }

    @Override
    public Void visitClausula_senao_se(ProcessoLangParser.Clausula_senao_seContext ctx) {
        String condicao = ctx.getChild(3).getText().replace("\"", "").replace("'", "");
        String destino = ctx.getChild(8).getText();

        saida.append("    ").append(passoAtual).append(" -> ").append(destino).append(" [label=\"").append(condicao).append("\"];\n");

        return null;
    }
}
