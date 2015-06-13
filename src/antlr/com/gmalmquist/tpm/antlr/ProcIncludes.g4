grammar ProcIncludes;

file
    : line (NEW_LINE line)*
    ;

line
    : include
    | other
    ;

include
    : WS? INCLUDE WS QUOTE path QUOTE WS?
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
INCLUDE  : '#include' ;

OTHER : . ;
