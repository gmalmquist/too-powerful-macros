package com.gmalmquist.tpm.processor;

import com.gmalmquist.tpm.antlr.ProcDefinitionsLexer;
import com.gmalmquist.tpm.antlr.ProcDefinitionsParser;
import com.gmalmquist.tpm.processor.listeners.ProcessDefinitions;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

public class DefinitionProcessor extends AbstractParserProcessor<ProcDefinitionsLexer,ProcDefinitionsParser,ProcessDefinitions> {

  public ProcDefinitionsLexer createLexer(CharStream input) {
    return new ProcDefinitionsLexer(input);
  }

  public ProcDefinitionsParser createParser(TokenStream tokens) {
    return new ProcDefinitionsParser(tokens);
  }

  public ProcessDefinitions createListener(ProcDefinitionsParser parser) {
    return new ProcessDefinitions(parser, getContext());
  }

  public ParserRuleContext getRoot(ProcDefinitionsParser parser) {
    return parser.file();
  }

  public String getName() {
    return "definition";
  }

}