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


package pt.webdetails.cdf.dd.extapi;

import java.io.InputStream;

import pt.webdetails.cpf.repository.api.IRWAccess;

public interface IFileHandler {

  public boolean saveDashboardAs( String path, String title, String description, String cdfdeJsText, boolean isPreview )
    throws Exception;

  /**
   * Each environment is responsible for the implementation of the Basic CDE files creation
   *
   * @param access  repositoryAccessor
   * @param file    name of the basic CDE file ( widget.cdfde, widget.wcdf, widget.cda, widget.xml )
   * @param content content of the basic CDE file
   * @return operation success
   */
  public boolean createBasicFileIfNotExists( final IRWAccess access, final String file, final InputStream content );

  /**
   * Each environment is responsible for the implementation of the Basic CDE folders creation
   *
   * @param access             repositoryAccessor
   * @param relativeFolderPath name of the basic CDE folder ( styles, templates, components, wigdets )
   * @return operation success
   */
  public boolean createBasicDirIfNotExists( final IRWAccess access, final String relativeFolderPath, boolean isHidden );

}
