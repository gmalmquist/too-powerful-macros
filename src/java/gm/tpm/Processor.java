package gm.tpm;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public abstract class Processor<X extends Lexer, P extends Parser, T extends ParseTreeListener> {

  public abstract X createLexer(CharStream input);

  public abstract P createParser(TokenStream tokens);

  public abstract T createListener(P parser);

  public abstract ParserRuleContext getRoot(P parser);

  private String name() {
    return getClass().getSimpleName();
  }

  public String processFile(String path) {
    System.out.println("debug: " + name() + " loading " + path);

    if (!new File(path).exists()) {
      System.err.println("File " + path + " does not exists.");
      return "";
    }

    CharStream input = null;
    try {
      input = new ANTLRFileStream(path);
    } catch (IOException ex) {
      System.err.println(ex);
      return "";
    }

    return process(input);
  }

  public String processText(String text) {
    CharStream input = null;
    try {
      input = new ANTLRInputStream(new StringReader(text));
    } catch (IOException ex) {
      System.err.println(ex);
      return "";
    }
    return process(input);
  }

  public String process(CharStream input) {
    System.out.println("  debug: " + name() + " lexing...");
    X lexer = createLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    System.out.println("  debug: " + name() + " parsing...");
    P parser = createParser(tokens);
    ParserRuleContext tree = getRoot(parser);
    ParseTreeWalker walker = new ParseTreeWalker();
    System.out.println("  debug: " + name() + " walking...");
    T finder = createListener(parser);
    walker.walk(finder, tree);
    return finder.toString();
  }

}