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


root :
    | template EOF
    ;

template :
    stringcontent               // string content
    | fmlookup                  // single object fm lookup
    | LEFTP template RIGHTP arraymaplookup   // map or array lookup
    | LEFTP template RIGHTP append           // append operation
    | template template         // mixed template
    ;

fmlookup:
    LEFTP template RIGHTP;

arraymaplookup:
    LEFTA template LEFTB;

append:
    APPEND LEFTP template RIGHTP;

stringcontent :  (ANYCHAR | ESCAPECHAR)+;
