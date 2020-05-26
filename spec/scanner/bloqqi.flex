package org.bloqqi.compiler.ast;

import org.bloqqi.compiler.ast.BloqqiParser.Terminals;

%%

%public
%final
%class BloqqiScanner
%extends beaver.Scanner

%type beaver.Symbol
%function nextToken
%yylexthrow beaver.Scanner.Exception
%scanerror BloqqiScanner.ScannerError

%line
%column
%{
  private StringBuilder stringLitSb = new StringBuilder();

  private beaver.Symbol sym(short id) {
    return new beaver.Symbol(id, yyline + 1, yycolumn + 1, yylength(), yytext());
  }

  private beaver.Symbol sym(short id, String text) {
    return new beaver.Symbol(id, yyline + 1, yycolumn + 1, yylength(), text);
  }

  public static class ScannerError extends Error {
    public ScannerError(String message) {
        super(message);
    }
  }
%}

WhiteSpace = [ ] | \t | \f | \n | \r | \r\n
TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" [^\n|\r|\r\n]*
Comment = {TraditionalComment} | {EndOfLineComment}

ID = [a-zA-Z$][a-zA-Z0-9$_]*
FLOAT  = [0-9]+ \. [0-9]+ {Exponent}?
Exponent = [eE] [+-]? [0-9]+
NUM = [0-9]+

%state STRING MULTILINE_STRING

%%
<YYINITIAL> {
    {WhiteSpace}    { /* ignore */ }
    {Comment}       { /* ignore */ }

    // Keywords
    "diagramtype"   { return sym(Terminals.DIAGRAMTYPE); }
    "external"      { return sym(Terminals.EXTERNAL); }
    "function"      { return sym(Terminals.FUNCTION); }
    "struct"        { return sym(Terminals.STRUCT); }
    "var"           { return sym(Terminals.VARIABLE); }
    "input"         { return sym(Terminals.INPUT); }
    "output"        { return sym(Terminals.OUTPUT); }
    "connect"       { return sym(Terminals.CONNECT); }
    "annotation"    { return sym(Terminals.ANNOTATION); }
    "extends"       { return sym(Terminals.EXTENDS); }
    "abstract"      { return sym(Terminals.ABSTRACT); }
    "intercept"     { return sym(Terminals.INTERCEPT); }
    "redeclare"     { return sym(Terminals.REDECLARE); }
    "source"        { return sym(Terminals.SOURCE); }
    "wiring"        { return sym(Terminals.WIRING); }
    "recommendation" { return sym(Terminals.RECOMMENDATION); }
    "configuration" { return sym(Terminals.CONFIGURATION); }
    "default"       { return sym(Terminals.DEFAULT); }
    "before"        { return sym(Terminals.BEFORE); }
    "replaceable"   { return sym(Terminals.REPLACEABLE); }
    "inline"        { return sym(Terminals.INLINE); }
    "super"         { return sym(Terminals.SUPER); }
    "with"          { return sym(Terminals.WITH); }
    "while"         { return sym(Terminals.WHILE); }
    "if"            { return sym(Terminals.IF); }
    "else"          { return sym(Terminals.ELSE); }
    "return"        { return sym(Terminals.RETURN); }
    "false"         { return sym(Terminals.FALSE); }
    "true"          { return sym(Terminals.TRUE); }
    "package"       { return sym(Terminals.PACKAGE); }
    "import"        { return sym(Terminals.IMPORT); }
    "statemachine"  { return sym(Terminals.STATEMACHINE); }
    "public"        { return sym(Terminals.PUBLIC); }
    "features"      { return sym(Terminals.FEATURES); }
    "feature"       { return sym(Terminals.FEATURE); }
    "excludes"      { return sym(Terminals.EXCLUDES); }

    // Operators
    "&&"            { return sym(Terminals.AND); }
    "||"            { return sym(Terminals.OR); }
    "="             { return sym(Terminals.ASSIGN); }
    "+"             { return sym(Terminals.PLUS); }
    "-"             { return sym(Terminals.MINUS); }
    "*"             { return sym(Terminals.ASTERIX); }
    "%"             { return sym(Terminals.MOD); }
    "/"             { return sym(Terminals.SLASH); }
    ">"             { return sym(Terminals.GT); }
    "<"             { return sym(Terminals.LT); }
    ">="            { return sym(Terminals.GE); }
    "<="            { return sym(Terminals.LE); }
    "=="            { return sym(Terminals.EQ); }
    "!"             { return sym(Terminals.NOT); }
    "!="            { return sym(Terminals.NEQ); }

    // Separators
    "("             { return sym(Terminals.LPAR); }
    ")"             { return sym(Terminals.RPAR); }
    "{"             { return sym(Terminals.LBRA); }
    "}"             { return sym(Terminals.RBRA); }
    "["             { return sym(Terminals.LSQUAREBRA); }
    "]"             { return sym(Terminals.RSQUAREBRA); }
    ";"             { return sym(Terminals.SCOL); }
    ":"             { return sym(Terminals.COL); }
    "::"            { return sym(Terminals.DOUBLECOL); }
    "."             { return sym(Terminals.DOT); }
    ","             { return sym(Terminals.COMMA); }
    "=>"            { return sym(Terminals.ARROW); }
    "@"             { return sym(Terminals.AT); }

    // ID
    {ID}            { return sym(Terminals.ID); }

    // Literals
    {FLOAT}         { return sym(Terminals.FLOAT); }
    {NUM}           { return sym(Terminals.NUM); }
    \"\"\"          { stringLitSb.setLength(0); yybegin(MULTILINE_STRING); }
    \"              { stringLitSb.setLength(0); yybegin(STRING); }
    <<EOF>>         { return sym(Terminals.EOF); }
}

/**
 * String literal (newlines are disallowed)
 */
<STRING> {
    \"              { yybegin(YYINITIAL); return sym(Terminals.STRING_LITERAL, stringLitSb.toString()); }
    [^\n\r\"\\]+    { stringLitSb.append(yytext()); }
    \\t             { stringLitSb.append('\t'); }
    \\n             { stringLitSb.append('\n'); }
    \\r             { stringLitSb.append('\r'); }
    \\\"            { stringLitSb.append('\"'); }
    \\              { stringLitSb.append('\\'); }
}


/**
 * Multi-line string literal (make sure to correctly escape escaped characters)
 */
<MULTILINE_STRING> {
    \"\"\"          { yybegin(YYINITIAL); return sym(Terminals.MULTILINE_STRING_LITERAL, stringLitSb.toString()); }
    \"              { stringLitSb.append('"'); }
    [^\"\\]+        { stringLitSb.append(yytext()); }
    \\              { stringLitSb.append('\\'); }
}


[^]             { throw new ScannerError((yyline+1) +"," + (yycolumn+1) + ": Illegal character <"+yytext()+">"); }
