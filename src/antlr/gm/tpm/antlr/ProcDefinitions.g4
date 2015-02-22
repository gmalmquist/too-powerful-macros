grammar ProcDefinitions;

file
    : line (NEW_LINE line)*
    ;

line
    : WS? definition
    | WS? macro
    | WS? ifStatement
    | other
    ;

definition
    : DEFINE WS NAME WS longValue
    ;

macro
    : DEFINE WS MACRO_NAME args CLOSEP WS longValue
    ;

argument
    : NAME
    ;

args
    : WS? argument WS? (COMMA WS? argument WS?)*
    ;

ifStatement
    : ifDef
    | ifNdef
    | ifExt
    | ifNext
    | ifDir
    | ifNdir
    ;

ifDef   : IFDEF     WS NAME WS? ifBody ifEnd   ;
ifNdef  : IFNDEF    WS NAME WS? ifBody ifEnd   ;
ifExt   : IFEXT     WS value    ifBody ifEnd   ;
ifNext  : IFNEXT    WS value    ifBody ifEnd   ;
ifDir   : IFDIR     WS value    ifBody ifEnd   ;
ifNdir  : IFNDIR    WS value    ifBody ifEnd   ;

ifBody
    : (NEW_LINE line)+?
    ;

ifEnd
    : NEW_LINE WS? ENDIF WS?
    ;

value
    : other
    ;

lineSkip
    : '\\' WS? NEW_LINE 
    ;

longValue
    : lineSkip longValue?
    | ~NEW_LINE longValue?
    ;

other
    : (~NEW_LINE)*
    ;

path
    : (~QUOTE)+
    ;

QUOTE    : '"' ;

NEW_LINE : '\n' ;
WS       : [ \t\u000C]+ ;

DEFINE   : '#define' ;
IFDEF    : '#ifdef' ;
IFNDEF   : '#ifndef' ;
IFEXT    : '#ifext' ;
IFNEXT   : '#ifnext' ;
IFDIR    : '#ifdir' ;
IFNDIR   : '#ifndir' ;
ENDIF    : '#endif' ;

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
COMMA    : ',' ;

OTHER : . ;
