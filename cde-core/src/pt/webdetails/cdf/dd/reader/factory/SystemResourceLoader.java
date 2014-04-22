/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cdf.dd.reader.factory;

import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IACAccess;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class SystemResourceLoader implements IResourceLoader {

  private IReadAccess reader;
  private IACAccess accessControl;
  private IRWAccess writer;

  public SystemResourceLoader( String path ) {
    this.reader = Utils.getAppropriateReadAccess( path );
    this.writer = Utils.getSystemOrUserRWAccess( path );
    this.accessControl = new IACAccess() {
      public boolean hasAccess( String file, FileAccess access ) {
        switch( access ) {
          case EXECUTE:
          case READ:
            return true;
          default:
            return false;
        }
      }
    };
  }

  public IReadAccess getReader() {
    return this.reader;
  }

  public IACAccess getAccessControl() {
    return this.accessControl;
  }

  public IRWAccess getWriter() {
    return this.writer;
  }

}
