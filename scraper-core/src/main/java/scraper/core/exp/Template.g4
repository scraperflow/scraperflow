grammar Template;

@header {
    package scraper.core.exp;
}

LEFTP : '{';
RIGHTP : '}';
LEFTA : '[';
LEFTB : ']';
APPEND : '^';
ANYCHAR : ~('\n' | '\r' | '{' | '}' | '[' | ']' | '^');
ESCAPECHAR : ('\\{' | '\\}' | '\\[' | '\\]' | '\\^');


root : template EOF;

template : mixedtemplate
    | fmlookup
    | mllookup
    | append
    ;

mixedtemplate :
    | (stringcontent+ fmlookup*)*
    | (fmlookup stringcontent+ fmlookup*)*
    | (fmlookup fmlookup+ stringcontent*)*;

fmlookup :  LEFTP template RIGHTP;
mllookup :  fmlookup LEFTA template LEFTB;
append :  fmlookup APPEND LEFTP template RIGHTP;
stringcontent :  ANYCHAR | ESCAPECHAR;
