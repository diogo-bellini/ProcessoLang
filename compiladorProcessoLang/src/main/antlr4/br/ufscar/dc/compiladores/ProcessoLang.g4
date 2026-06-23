grammar ProcessoLang;

// --- REGRAS LÉXICAS (Definem os blocos básicos de texto da linguagem) ---

// Nomes de processos, passos e variáveis de condição (suporta acentos e números após a primeira letra)
IDENTIFICADOR : [a-zA-ZáéíóúÁÉÍÓÚçÇ_][a-zA-Z0-9áéíóúÁÉÍÓÚçÇ_]*;

// Textos descritivos flexíveis, aceitando aspas duplas ("...") ou aspas simples ('...')
TEXTO : '"' (~["\r\n])* '"'
        | '\'' (~['\r\n])* '\'';

// Token isolado para permitir que o usuário coloque espaços antes ou depois dos dois pontos
DOIS_PONTOS : ':';

// Ignora silenciosamente espaços, tabs e quebras de linha durante a compilação
WS : ( ' ' | '\n' | '\r' | '\t' ) { skip();};

// Ignora comentários de linha única (iniciados com //)
COMENTARIO : '//' ~[\r\n]* -> skip;



// --- REGRAS SINTÁTICAS (Definem a estrutura e a ordem lógica do código) ---

// Regra raiz: um arquivo pode conter um ou mais processos sequenciais
programa : processo+ EOF;

// Estrutura principal de um processo, exigindo obrigatoriamente um passo inicial
processo : 'processo' IDENTIFICADOR '{' passo_inicio bloco_passos '}';

// Corpo do processo, contendo pelo menos um passo (normal ou final)
bloco_passos : (passo_normal | passo_final)+;

// Ponto de entrada único e obrigatório do processo, exige descrição e comando de fluxo
passo_inicio : 'passo-inicio' IDENTIFICADOR '{' 'descricao' DOIS_PONTOS TEXTO comando_fluxo '}';

// Blocos intermediários de execução do processo
passo_normal : 'passo' IDENTIFICADOR '{' 'descricao' DOIS_PONTOS TEXTO comando_fluxo '}';

// Ponto de término do processo (não possui comando de fluxo 'proximo')
passo_final : 'passo-final' IDENTIFICADOR '{' 'descricao' DOIS_PONTOS TEXTO '}';

// Define o roteamento: pode ser um salto direto ou um bloco condicional (se/senao)
comando_fluxo : 'proximo' DOIS_PONTOS IDENTIFICADOR
                | 'se' '(' (IDENTIFICADOR | TEXTO) ')' '{' 'proximo' DOIS_PONTOS IDENTIFICADOR '}' clausula_senao_se* 'senao' '{' 'proximo' DOIS_PONTOS IDENTIFICADOR '}';

// Sub-regra para permitir múltiplas alternativas condicionais encadeadas
clausula_senao_se : 'senao' 'se' '(' (IDENTIFICADOR | TEXTO) ')' '{' 'proximo' DOIS_PONTOS IDENTIFICADOR '}';