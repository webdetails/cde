/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.dependencies;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.packager.JSMin;
import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IRWAccess;

public class JsMinifiedDependency extends PackagedFileDependency {

  private static Log logger = LogFactory.getLog(JsMinifiedDependency.class);

  public JsMinifiedDependency( PathOrigin origin, String path, IRWAccess writer, Iterable<FileDependency> inputFiles, IUrlProvider urlProvider ) {
    super( origin, path, writer, inputFiles, urlProvider );
  }

  @Override
  protected InputStream minifyPackage(Iterable<FileDependency> inputFiles) {
    return new SequenceInputStream( new JsMinificationEnumeration( inputFiles.iterator()) );
  }
  
  public static class JsMinificationEnumeration implements Enumeration<InputStream> {
  
    private Iterator<FileDependency> deps;
  
    public JsMinificationEnumeration(Iterator<FileDependency> deps) {
      this.deps = deps;
    }
  
    @Override
    public boolean hasMoreElements() {
      return deps.hasNext();
    }
  
    @Override
    public InputStream nextElement() {
      FileDependency dep = deps.next();
      InputStream input = null;
      try {
        input = dep.getFileInputStream();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        JSMin jsMin = new JSMin( input, bytesOut );
        jsMin.jsmin();
        return new ByteArrayInputStream( bytesOut.toByteArray() );
      } catch ( ParseException e ) {
        logger.error( "Error parsing javascript dependency " + dep + " at offset " + e.getErrorOffset() + ", skipping..", e );
      } catch ( IOException e ) {
        logger.error( "Error getting input stream for dependency " + dep + ", skipping..", e );
      }
      finally {
        IOUtils.closeQuietly( input );
      }
      return Util.toInputStream( "" );
    }
  
  }


}
