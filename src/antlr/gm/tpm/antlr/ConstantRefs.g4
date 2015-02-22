grammar ConstantRefs;

file
    : code+
    ;

code 
    : MACRO_NAME
    | reference
    | .
    ;

reference
    : NAME
    ;


NEW_LINE : '\n' ;
WS       : [ \t\u000C]+ ;

// macro name and numbers included here just to
// distinguish them from names in the lexer.
MACRO_NAME
    : NAME OPENP
    ;

HEX_NUM
    : '0x' [0-9A-Fa-f]+
    ;

BIN_NUM
    : '0b' [0-1]+
    ;

FLOAT_END
    : [0-9]+ [fF]
    ;

LONG_END
    : [0-9]+ [lL]
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
