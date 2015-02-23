grammar ExternalCalls;

file
    : codeLine (NEW_LINE codeLine)*
    ;

codeLine
    : notExternal
    | externalCall
    ;

notExternal
    : (~EXTERNAL | NEW_LINE)+
    ;

externalCall
    : WS? EXTERNAL WS shellCall NEW_LINE WS? externalBlock WS? END WS?
    ;

externalBlock
    : (notEndLine? NEW_LINE)*
    ;

notEndLine
    : (~(END | NEW_LINE))+ ;

shellCall
    : (~NEW_LINE)+
    ;


NEW_LINE : '\n' ;
WS       : [ \t\u000C]+ ;

NAME
    : ALPHA ALPHA_NUMERIC*
    ;

fragment ALPHA : [a-zA-Z_$] ;
fragment ALPHA_NUMERIC : ALPHA | [0-9] ; 

EXTERNAL : '#external' ;

END      : '#end' ;

OTHER : . ;
