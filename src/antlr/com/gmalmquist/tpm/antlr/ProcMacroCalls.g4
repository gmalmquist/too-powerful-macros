grammar ProcMacroCalls;

file
    : code+
    ;

code
    : (~MACRO_NAME)+
    | macroCall
    | .
    ;

macroCall
    : MACRO_NAME WS? parameters WS? CLOSEP ;

// If this starts going slow, may be able to speed it up 
// by banning new-lines.
parameter
    : OPENP parameter* (~(OPENP | CLOSEP))* CLOSEP parameter?
    | macroCall parameter?
    | (~(COMMA | OPENP | CLOSEP)) parameter?
    ;

parameters
    : parameter (COMMA parameter)*
    ;


NEW_LINE : '\n' ;
WS       : [ \t\u000C]+ ;
MACRO_NAME
    : NAME OPENP
    ;

NAME
    : ALPHA ALPHA_NUMERIC*
    ;

fragment ALPHA : [a-zA-Z_$] ;
fragment ALPHA_NUMERIC : ALPHA | [0-9] ; 

OPENP   : '(' ;
CLOSEP  : ')' ;
COMMA   : ',' ;

OTHER : . ;
