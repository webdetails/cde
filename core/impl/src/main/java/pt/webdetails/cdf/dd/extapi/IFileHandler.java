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
