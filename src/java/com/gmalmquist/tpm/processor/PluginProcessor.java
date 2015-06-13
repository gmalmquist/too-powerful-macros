package com.gmalmquist.tpm.processor;

import com.gmalmquist.tpm.model.ProcessingContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

public class PluginProcessor extends AbstractBlockProcessor {

  private static PluginProcessor instance;
  public static PluginProcessor getInstance() {
    if (instance == null)
      instance = new PluginProcessor();
    return instance;
  }


  private Map<String, Object> plugins;
  private List<Runnable> finishes = new LinkedList<>();

  private PluginProcessor() {
    super("plugin", true);
    plugins = new HashMap<>();
  }

  public String runPlugin(String srcFile, String dstFile, String args, String input) {
    args = args.trim();
    int space = args.indexOf(' ');
    String name = args;
    if (space >= 0) {
      name = args.substring(0, space);
      args = args.substring(space+1).trim();
    } else {
      name = args;
      args = "";
    }

    try {
      ClassLoader classLoader = this.getClass().getClassLoader();
      final Class<?> pluginClass = classLoader.loadClass("gm.tpm.plugins." + name);

      Object plugin = null;
      if (plugins.containsKey(name)) {
        plugin = plugins.get(name);
      } else {
        System.err.println("Instantiating new " + pluginClass.getSimpleName() + ".");
        plugin = pluginClass.getConstructor().newInstance();
        plugins.put(name, plugin);

        final Object pl = plugin;
        final String finalName = name;
        finishes.add(new Runnable() {
          public void run() {
            System.out.println("[debug] finishing " + finalName);
            try {
              pluginClass.getMethod("finish").invoke(pl);
            } catch (Exception ex) {}
          }
        });
      }

      Method process = pluginClass.getMethod("process", String.class, String.class, String.class, String.class);

      String result = String.valueOf(process.invoke(plugin, srcFile, dstFile, args, input));

      return result;
    } catch (Exception ex) {
      System.err.println(ex);
      return String.valueOf(ex);
    }
  }

  @Override
  public String processBlock(ProcessingContext context, String blockArgs, String blockData) {
    return runPlugin(context.filepath, context.outpath, blockArgs, blockData);
  }

  @Override
  public void finish() {
    for (Runnable run : finishes) {
      run.run();
    }
    finishes.clear();
  }

}