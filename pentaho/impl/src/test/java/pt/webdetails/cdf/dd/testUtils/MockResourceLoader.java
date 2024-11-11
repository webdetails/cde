/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.testUtils;

import pt.webdetails.cdf.dd.reader.factory.IResourceLoader;
import pt.webdetails.cpf.repository.api.IACAccess;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MockResourceLoader implements IResourceLoader {

  private boolean hasAccess = true;
  private boolean savedFile = true;

  public void setHasAccess( boolean hasAccess ) {
    this.hasAccess = hasAccess;
  }

  public void setSavedFile( boolean savedFile ) {
    this.savedFile = savedFile;
  }

  public IACAccess getAccessControl() {
    return new IACAccess() {
      @Override public boolean hasAccess( String s, FileAccess fileAccess ) {
        return hasAccess;
      }
    };
  }

  public IReadAccess getReader() {
    return new IReadAccess() {
      @Override public InputStream getFileInputStream( String s ) throws IOException {
        return null;
      }

      @Override public boolean fileExists( String s ) {
        return false;
      }

      @Override public long getLastModified( String s ) {
        return 0;
      }

      @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i, boolean b,
                                                   boolean b2 ) {
        return null;
      }

      @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i, boolean b ) {
        return null;
      }

      @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i ) {
        return null;
      }

      @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter ) {
        return null;
      }

      @Override public IBasicFile fetchFile( String s ) {
        return null;
      }
    };
  }

  public IRWAccess getWriter() {
    return new IRWAccess() {
      @Override public boolean saveFile( String s, InputStream inputStream ) {
        return savedFile;
      }

      @Override public boolean copyFile( String s, String s2 ) {
        return false;
      }

      @Override public boolean deleteFile( String s ) {
        return false;
      }

      @Override public boolean createFolder( String s ) {
        return false;
      }

      @Override public boolean createFolder( String path, boolean isHidden ) {
        return false;
      }

      @Override public InputStream getFileInputStream( String s ) throws IOException {
        return null;
      }

      @Override public boolean fileExists( String s ) {
        return false;
      }

      @Override public long getLastModified( String s ) {
        return 0;
      }

      @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i, boolean b,
                                                   boolean b2 ) {
        return null;
      }

      @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i, boolean b ) {
        return null;
      }

      @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i ) {
        return null;
      }

      @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter ) {
        return null;
      }

      @Override public IBasicFile fetchFile( String s ) {
        return null;
      }
    };
  }

}
