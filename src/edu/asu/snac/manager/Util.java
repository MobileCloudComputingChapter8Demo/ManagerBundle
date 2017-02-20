package edu.asu.snac.manager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by jjAsu on 2/18/2017.
 */

public class Util {
  public static final String surrogateIp = "192.168.1.10"; // change it
  public static final int jarFilePort = 6701;

  /**
   * Compares two version strings.
   * <p>
   * Use this instead of String.compareTo() for a non-lexicographical comparison that works for
   * version strings. e.g. "1.10".compareTo("1.6").
   *
   * @param str1 a string of ordinal numbers separated by decimal points.
   * @param str2 a string of ordinal numbers separated by decimal points.
   * @return The result is a negative integer if str1 is _numerically_ less than str2. The result is
   *         a positive integer if str1 is _numerically_ greater than str2. The result is zero if
   *         the strings are _numerically_ equal.
   * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
   */
  public static int versionCompare(String str1, String str2) {
    String[] vals1 = str1.split("\\.");
    String[] vals2 = str2.split("\\.");
    int i = 0;
    // set index to first non-equal ordinal or length of shortest version string
    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
      i++;
    }
    // compare first non-equal ordinal number
    if (i < vals1.length && i < vals2.length) {
      int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
      return Integer.signum(diff);
    }
    // the strings are equal or one string is a substring of the other
    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
    return Integer.signum(vals1.length - vals2.length);
  }

  public static String getJarPath(Long id) throws IOException {
    // runtime-cache path printed on phone screen
    String base_path = "/sdcard/Android/data/edu.asu.snac.offloading/files/bundle-cache-dir";
    // get the bundle id directory
    String version_path = base_path + "/bundle" + id;
    String lastest = "0.0";
    File dir = new File(version_path);
    // find the latest version
    for (File child : dir.listFiles()) {
      String dir_name = child.getName();
      String start = "version";
      if (dir_name.startsWith(start)) {
        String sub_str = dir_name.substring(start.length());
        if (versionCompare(lastest, sub_str) < 0) {
          lastest = sub_str;
        }
      }
    }
    // the path is like W/bundleX/versionY.Z/bundle.jar
    return version_path + "/version" + lastest + "/bundle.jar";
  }

  public static void sendFile(final String path) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        BufferedInputStream bis = null;
        Socket sock = null;
        OutputStream os = null;
        try {
          // connecting ...
          sock = new Socket(surrogateIp, jarFilePort);
          // send file
          File myFile = new File(path);
          byte[] mybytearray = new byte[(int) myFile.length()];
          bis = new BufferedInputStream(new FileInputStream(myFile));
          bis.read(mybytearray, 0, mybytearray.length);
          os = sock.getOutputStream();
          os.write(mybytearray, 0, mybytearray.length);
          os.flush();
          System.out.println("sent file " + path);
        } catch (Exception e) {

        } finally {
          if (bis != null)
            try {
              bis.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          if (os != null)
            try {
              os.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          if (sock != null)
            try {
              sock.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
        }
      }
    }).start();
  }

}
