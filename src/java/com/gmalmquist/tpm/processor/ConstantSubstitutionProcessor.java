package com.gmalmquist.tpm.processor;

import com.gmalmquist.tpm.antlr.ConstantRefsLexer;
import com.gmalmquist.tpm.antlr.ConstantRefsParser;
import com.gmalmquist.tpm.processor.listeners.ConstantSubstitution;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

public class ConstantSubstitutionProcessor extends AbstractParserProcessor<ConstantRefsLexer,ConstantRefsParser,ConstantSubstitution> {

  public ConstantRefsLexer createLexer(CharStream input) {
    return new ConstantRefsLexer(input);
  }

  public ConstantRefsParser createParser(TokenStream tokens) {
    return new ConstantRefsParser(tokens);
  }

  public ConstantSubstitution createListener(ConstantRefsParser parser) {
    return new ConstantSubstitution(parser, getContext());
  }

  public ParserRuleContext getRoot(ConstantRefsParser parser) {
    return parser.file();
  }

  public String getName() {
    return "constant";
  }
}