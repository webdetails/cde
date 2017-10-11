/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

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
