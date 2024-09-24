/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cdf.dd.reader.factory;

import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IACAccess;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityParameterProvider;

public class SystemResourceLoader implements IResourceLoader {

  private IReadAccess reader;
  private IACAccess accessControl;
  private IRWAccess writer;

  public SystemResourceLoader() {
  }

  public SystemResourceLoader( String path ) {
    this.reader = Utils.getAppropriateReadAccess( path );
    this.writer = Utils.getAppropriateWriteAccess( path );

    this.accessControl = new IACAccess() {
      public boolean hasAccess( String file, FileAccess access ) {
        return isAdmin();
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

  protected boolean isAdmin() {
    SecurityParameterProvider securityParams = new SecurityParameterProvider( PentahoSessionHolder.getSession() );
    return securityParams.getParameter( "principalAdministrator" ).equals( "true" );
  }

}
