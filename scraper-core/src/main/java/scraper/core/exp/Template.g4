grammar Template;

@header {
    package scraper.core.exp;
}

LEFTP : '{';
RIGHTP : '}';
LEFTA : '[';
LEFTB : ']';
LOOKUP : '@';
ANYCHAR : ~('\n' | '\r' | '{' | '}' | '[' | ']' | '^' | '@');
ESCAPECHAR : ('\\{' | '\\}' | '\\[' | '\\]' | '\\^' | '\\@');


root :
    | template EOF
    ;

template :
    stringcontent               // string content
    | fmlookup                  // single object fm lookup
    | LEFTP template RIGHTP arraylookup   // array lookup
    | LEFTP template RIGHTP maplookup   // map lookup
    | template template         // mixed template
    ;

fmlookup:
    LEFTP template RIGHTP;

arraylookup:
    LEFTA template LEFTB;

maplookup:
    LOOKUP template;

stringcontent :  (ANYCHAR | ESCAPECHAR)+;
