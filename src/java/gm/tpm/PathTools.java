package gm.tpm;

import java.io.File;
import java.nio.file.Path;

public class PathTools {
  
  public static String realPath(String path) {
    return new File(path).toPath().normalize().toAbsolutePath().normalize().toString();
  }

  public static String moved(String srcDir, String dstDir, String file) {
    Path srcPath = new File(srcDir).toPath().normalize().toAbsolutePath();
    Path dstPath = new File(dstDir).toPath().normalize().toAbsolutePath();
    Path filePath = new File(file).toPath().normalize().toAbsolutePath();
    return dstPath.resolve(srcPath.relativize(filePath)).normalize().toAbsolutePath().toString();
  }

  public static boolean inDirectory(String directory, String file) {
    String dirPath = new File(directory).toPath().normalize().toString();
    String filePath = new File(file).toPath().normalize().toAbsolutePath().toString();
    //System.out.println();
    //System.out.println("dir:  " + dirPath);
    //System.out.println("file: " + filePath);
    if (filePath.startsWith(dirPath + File.separatorChar))
      return true;
    return filePath.contains(File.separatorChar + dirPath + File.separatorChar);
  }

}