grammar ProcessoLang;

// --- REGRAS LÉXICAS ---
IDENTIFICADOR : [a-zA-ZáéíóúÁÉÍÓÚçÇ_][a-zA-Z0-9áéíóúÁÉÍÓÚçÇ_]*;
TEXTO : '"' (~["\r\n])* '"'
        | '\'' (~['\r\n])* '\'';
DOIS_PONTOS  : ':';
WS           : ( ' ' | '\n' | '\r' | '\t' ) { skip();};
COMENTARIO   : '//' ~[\r\n]* -> skip;



// --- REGRAS SINTÁTICAS ---
programa : processo+ EOF;

// Um processo agora é composto por um cabeçalho, OBRIGATORIAMENTE um passo-inicio, e depois os demais passos
processo : 'processo' IDENTIFICADOR '{' passo_inicio bloco_passos '}';

bloco_passos : (passo_normal | passo_final)+;

passo_inicio : 'passo-inicio' IDENTIFICADOR '{' 'descricao' DOIS_PONTOS TEXTO comando_fluxo '}';

passo_normal : 'passo' IDENTIFICADOR '{' 'descricao' DOIS_PONTOS TEXTO comando_fluxo '}';

passo_final : 'passo-final' IDENTIFICADOR '{' 'descricao' DOIS_PONTOS TEXTO '}';

comando_fluxo : 'proximo' DOIS_PONTOS IDENTIFICADOR
                | 'se' '(' (IDENTIFICADOR | TEXTO) ')' '{' 'proximo' DOIS_PONTOS IDENTIFICADOR '}' clausula_senao_se* 'senao' '{' 'proximo' DOIS_PONTOS IDENTIFICADOR '}';

clausula_senao_se : 'senao' 'se' '(' (IDENTIFICADOR | TEXTO) ')' '{' 'proximo' DOIS_PONTOS IDENTIFICADOR '}';