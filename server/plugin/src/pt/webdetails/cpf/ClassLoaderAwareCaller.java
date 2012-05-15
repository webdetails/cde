/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cpf;

import java.util.concurrent.Callable;

/**
 * Boilerplate to run a method in a different ClassLoader.
 */
public class ClassLoaderAwareCaller {
  private ClassLoader classLoader;
  
  public ClassLoaderAwareCaller(ClassLoader classLoader){
   this.classLoader = classLoader; 
  }
  
  public <T> T callInClassLoader(Callable<T> callable) throws Exception{
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try
    {
      if(this.classLoader != null)
      {
        Thread.currentThread().setContextClassLoader(this.classLoader);
      }
      
      return callable.call();
      
    }
    finally{
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }
      
  public void runInClassLoader(Runnable runnable)
  {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try
    {
      if(this.classLoader != null)
      {
        Thread.currentThread().setContextClassLoader(this.classLoader);
      }
      
      runnable.run();
      
    }
    finally{
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }
  
  
}
