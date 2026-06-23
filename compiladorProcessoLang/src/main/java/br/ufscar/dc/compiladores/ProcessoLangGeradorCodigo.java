package br.ufscar.dc.compiladores;

// Visitor responsável por percorrer a árvore sintática já validada e gerar o código Graphviz (DOT)
public class ProcessoLangGeradorCodigo extends ProcessoLangBaseVisitor<Void> {

    // Acumula o código final gerado em texto estruturado
    private final StringBuilder saida = new StringBuilder();

    // Armazena o nome do passo sendo visitado no momento para ligar as setas de destino
    private String passoAtual = "";

    // Retorna a string completa contendo o código Graphviz finalizado
    public String getCodigo() {
        return saida.toString();
    }

    // Remove as aspas originais do usuário e escapa caracteres internos para evitar quebras de sintaxe no Graphviz
    private String formatarLabel(String textoOriginal) {
        String textoPuro = textoOriginal.substring(1, textoOriginal.length() - 1);
        return "\"" + textoPuro.replace("\"", "\\\"") + "\"";
    }

    @Override
    public Void visitPrograma(ProcessoLangParser.ProgramaContext ctx) {
        // Inicializa o grafo direcionado principal e define fontes e layouts globais
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

        // Cria um subgrafo (cluster visual) para agrupar graficamente todos os passos deste processo
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

        // Gera o nó inicial formatado com cantos ovais e cor verde clara
        saida.append("    ").append(passoAtual).append(" [label=").append(formatarLabel(descricao)).append(", shape=oval, fillcolor=palegreen];\n");

        visitComando_fluxo(ctx.comando_fluxo());
        return null;
    }

    @Override
    public Void visitPasso_normal(ProcessoLangParser.Passo_normalContext ctx) {
        passoAtual = ctx.IDENTIFICADOR().getText();
        String descricao = ctx.TEXTO().getText();

        // Gera um nó retangular simples com fundo branco para os passos intermediários
        saida.append("    ").append(passoAtual).append(" [label=").append(formatarLabel(descricao)).append("];\n");

        visitComando_fluxo(ctx.comando_fluxo());
        return null;
    }

    @Override
    public Void visitPasso_final(ProcessoLangParser.Passo_finalContext ctx) {
        String nomePasso = ctx.IDENTIFICADOR().getText();
        String descricao = ctx.TEXTO().getText();

        // Gera o nó de conclusão formatado com borda dupla octogonal e cor avermelhada
        saida.append("    ").append(nomePasso).append(" [label=").append(formatarLabel(descricao)).append(", shape=doubleoctagon, fillcolor=mistyrose];\n");

        return null;
    }

    @Override
    public Void visitComando_fluxo(ProcessoLangParser.Comando_fluxoContext ctx) {
        // Verifica se é um salto direto e desenha uma seta limpa
        if(ctx.getChild(0).getText().equals("proximo")){
            String destino = ctx.getChild(2).getText();
            saida.append("    ").append(passoAtual).append(" -> ").append(destino).append(";\n");

            // Verifica se é um bloco condicional e desenha a seta principal do 'se'
        }else if(ctx.getChild(0).getText().equals("se")){
            String condicao = ctx.getChild(2).getText().replace("\"", "").replace("'", "");

            String destinoSe = ctx.getChild(7).getText();
            saida.append("    ").append(passoAtual).append(" -> ").append(destinoSe).append(" [label=\"").append(condicao).append("\"];\n");

            // Visita todas as ramificações 'senao se' para desenhar suas respectivas setas
            if(ctx.clausula_senao_se() != null){
                for(var senaoSe : ctx.clausula_senao_se()){
                    visitClausula_senao_se(senaoSe);
                }
            }

            // Desenha a seta de saída padrão do 'senao' apontando para o penúltimo filho
            String destinoSenao = ctx.getChild(ctx.getChildCount() - 2).getText();
            saida.append("    ").append(passoAtual).append(" -> ").append(destinoSenao).append(" [label=\"senao\"];\n");
        }

        return null;
    }

    @Override
    public Void visitClausula_senao_se(ProcessoLangParser.Clausula_senao_seContext ctx) {
        // Captura a condição e o destino da cláusula e desenha a aresta rotulada correspondente
        String condicao = ctx.getChild(3).getText().replace("\"", "").replace("'", "");
        String destino = ctx.getChild(8).getText();

        saida.append("    ").append(passoAtual).append(" -> ").append(destino).append(" [label=\"").append(condicao).append("\"];\n");

        return null;
    }
}