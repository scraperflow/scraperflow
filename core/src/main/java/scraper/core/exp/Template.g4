grammar Template;

@header {
    package scraper.core.exp;
}

LEFTP : '{';
RIGHTP : '}';
LEFTA : '[';
LEFTB : ']';
LOOKUP : '@';
ANYCHAR    : ~('{'  | '}'   | '['   | ']'   | '^'   | '@');
ESCAPECHAR : ('\\{' | '\\}' | '\\[' | '\\]' | '\\^' | '\\@');


root :
    | template EOF
    ;

template :
    stringcontent               // string content
    | fmlookup                  // single object fm lookup
    | fmlookupconsume           // single object fm lookup
    | LEFTP template RIGHTP arraylookup   // array lookup
    | LEFTP template maplookup RIGHTP // map lookup
    | template template         // mixed template
    ;

fmlookupconsume:
    LEFTP LOOKUP template LOOKUP RIGHTP;

fmlookup:
    LEFTP template RIGHTP;

arraylookup:
    LEFTA template LEFTB;

maplookup:
    LOOKUP template;

stringcontent :  (ANYCHAR | ESCAPECHAR)+;
