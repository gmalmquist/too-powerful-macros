package com.gmalmquist.tpm.processor;

import com.gmalmquist.tpm.antlr.ProcIncludesLexer;
import com.gmalmquist.tpm.antlr.ProcIncludesParser;
import com.gmalmquist.tpm.processor.listeners.FindIncludes;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

public class IncludeProcessor extends AbstractParserProcessor<ProcIncludesLexer,ProcIncludesParser,FindIncludes> {


  public ProcIncludesLexer createLexer(CharStream input) {
    return new ProcIncludesLexer(input);
  }

  public ProcIncludesParser createParser(TokenStream tokens) {
    return new ProcIncludesParser(tokens);
  }

  public FindIncludes createListener(ProcIncludesParser parser) {
    return new FindIncludes(parser);
  }

  public ParserRuleContext getRoot(ProcIncludesParser parser) {
    return parser.file();
  }

  public String getName() {
    return "include";
  }

}