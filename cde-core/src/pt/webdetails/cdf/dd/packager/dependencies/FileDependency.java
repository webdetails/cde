/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.dependencies;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

/**
 * Basic file-based dependency with md5 checksums.
 */
public class FileDependency extends Dependency {
  
  private static Log logger = LogFactory.getLog(FileDependency.class);
  
  protected String filePath;
  protected PathOrigin origin;
  private String hash;

  public FileDependency(String version, PathOrigin origin, String path) {
    super(version);
    this.filePath = path;
    this.hash = null;
    this.origin = origin;
  }


  public String getCheckSum() {
    if (hash == null) {
      InputStream in = null;
      try {
        in = getFileInputStream();
        hash = Util.getMd5Digest( in );
      }
      catch (IOException e) {
        logger.error( "Could not compute md5 checksum.", e);
      }
      finally {
        IOUtils.closeQuietly( in );
      }
    }
    return hash;
  }

  public InputStream getFileInputStream() throws IOException {
    return origin.getReader( getContentFactory() ).getFileInputStream( filePath );
  }

  /**
   * @return path for including this file
   */
  public String getDependencyInclude()
  {
    // the ?v=<hash> is used to bypass browser cache when needed
    String md5 = getCheckSum();
    String file = filePath + ((md5 == null) ? "" : "?v=" + md5);
    // translate local path to a url path
    return origin.getUrl(file);
  }

  @Override
  public String getContents() throws IOException {
    return Util.toString( origin.getReader( getContentFactory() ).getFileInputStream( filePath ) );
  }

  public String getUrlFilePath() {
    return origin.getUrl(filePath);
  }

  protected IContentAccessFactory getContentFactory() {
    return CdeEnvironment.getContentAccessFactory(); //XXX needs to be generic enough to move to cpf
  }

}
