package gm.tpm;

public class CmdArg {

  private String name;
  private String value;

  public CmdArg(String name, String value) {
    while (name.startsWith("-"))
      name = name.substring(1);
    this.name = name;
    this.value = value;
  }

  public String getName() { return name; }

  public String getValue() { return value; }

  public boolean is(String name) { 
    return this.name.equals(name); 
  }

}