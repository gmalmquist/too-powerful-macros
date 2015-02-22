package gm.tpm;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import gm.tpm.antlr.*;

public class ExternalProcessor extends Processor<ExternalCallsLexer,ExternalCallsParser,ProcessExternalCalls> {

  private ProcessingContext context;

  public ExternalProcessor(ProcessingContext context) {
    this.context = context;
  }

  public ExternalCallsLexer createLexer(CharStream input) {
    return new ExternalCallsLexer(input);
  }

  public ExternalCallsParser createParser(TokenStream tokens) {
    return new ExternalCallsParser(tokens);
  }

  public ProcessExternalCalls createListener(ExternalCallsParser parser) {
    return new ProcessExternalCalls(parser, context);
  }

  public ParserRuleContext getRoot(ExternalCallsParser parser) {
    return parser.file();
  }

}