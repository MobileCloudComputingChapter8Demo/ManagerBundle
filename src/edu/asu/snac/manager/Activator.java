package edu.asu.snac.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.asu.snac.simpleservice.SimpleServiceInterface;

public class Activator implements BundleActivator {
  public static final String surrogateIp = "192.168.1.10"; // change it
  public static final int invocationPort = 6702;

  Bundle b;

  private void generateProxy(BundleContext context) {
    System.out.println("generate service proxy");
    Object p = Proxy.newProxyInstance(SimpleServiceInterface.class.getClassLoader(),
        new Class[] {SimpleServiceInterface.class}, new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // connect to surrogate server
            Socket socket1 = new Socket(surrogateIp, invocationPort);
            // get input stream
            BufferedReader br = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
            // get output stream
            PrintWriter pw = new PrintWriter(socket1.getOutputStream(), true);
            // get encoded request
            String str = new Marshalling().setService(SimpleServiceInterface.class.getName())
                .setMethod(method.getName()).addParams(args).build();
            // send to surrogate server
            pw.println(str);
            // read response
            if ((str = br.readLine()) != null) {
              System.out.println("received surrogate reply " + str);
              br.close();
              pw.close();
              socket1.close();
              // return result
              return Integer.valueOf(str);
            } else {
              System.out.println("surrogate fail");
              return null;
            }
          }
        });
    // register the proxy to osgi framework
    context.registerService(SimpleServiceInterface.class.getName(), p, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    // stop local service bundle
    b = context.getServiceReference(SimpleServiceInterface.class).getBundle();
    b.stop();
    // send service bundle to remote
    String path = Util.getJarPath(b.getBundleId());
    Util.sendFile(path);
    // generate service proxy
    generateProxy(context);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    System.out.println("proxy gone");
  }

}
