package dev.dnbln.asms.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static dev.dnbln.asms.lang.psi.AsmElementTypes.*;

%%

%{
  public _AsmLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _AsmLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state STRING_STATE

EOL=\R
WHITE_SPACE=[ \t\r\f]+
NEWLINE=\n

ID=[a-zA-Z_@][\.a-zA-Z0-9_@]*
HEX_LITERAL=0x[0-9a-fA-F]+
DEC_LITERAL=([1-9][0-9]*)
OCT_LITERAL=(0[0-7]*)

CHAR_LITERAL='([^'\\\n]|\\.)'


COMMENT=#[^\n]*

%%
<YYINITIAL> {
  {NEWLINE}          { return NEWLINE; }
  {WHITE_SPACE}      { return WHITE_SPACE; }
  ","                { return COMMA; }
  "+"                { return PLUS; }
  "-"                { return MINUS; }
  "*"                { return ASTERISK; }
  {ID}               { return ID; }
  ":"                { return COLON; }
  "$"                { return DOLLAR_SIGN; }
  {HEX_LITERAL}      { return HEX_LITERAL; }
  {DEC_LITERAL}      { return DEC_LITERAL; }
  {OCT_LITERAL}      { return OCT_LITERAL; }
  {CHAR_LITERAL}     { return CHAR_LITERAL; }
  "("                { return OPEN_PAREN; }
  ")"                { return CLOSE_PAREN; }
  "%"                { return PERCENT; }
  "."                { return DOT; }
  {COMMENT}          { return COMMENT; }
  \"                 { yybegin(STRING_STATE); }
}

<STRING_STATE> {
  \"                             { yybegin(YYINITIAL);
                                   return STRING_LITERAL; }
  [^\n\r\"\\]+                   {  }
  \\t                            {  }
  \\n                            {  }

  \\r                            {  }
  \\\"                           {  }
  \\                             {  }
}

[^] { return BAD_CHARACTER; }
