package gm.tpm;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import gm.tpm.antlr.*;

public class DefinitionsProcessor extends Processor<ProcDefinitionsLexer,ProcDefinitionsParser,ProcessDefinitions> {

  private ProcessingContext context;

  public DefinitionsProcessor(ProcessingContext context) {
    this.context = context;
  }

  public ProcDefinitionsLexer createLexer(CharStream input) {
    return new ProcDefinitionsLexer(input);
  }

  public ProcDefinitionsParser createParser(TokenStream tokens) {
    return new ProcDefinitionsParser(tokens);
  }

  public ProcessDefinitions createListener(ProcDefinitionsParser parser) {
    return new ProcessDefinitions(parser, context);
  }

  public ParserRuleContext getRoot(ProcDefinitionsParser parser) {
    return parser.file();
  }

}