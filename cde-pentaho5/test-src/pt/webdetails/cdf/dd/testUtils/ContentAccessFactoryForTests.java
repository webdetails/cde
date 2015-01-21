/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cdf.dd.testUtils;

import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

public class ContentAccessFactoryForTests implements IContentAccessFactory {
  private IUserContentAccess mockedContentAccess;
  private IReadAccess mockedReadAccess;

  public ContentAccessFactoryForTests( IUserContentAccess mockedContentAccess, IReadAccess mockedReadAccess ) {
    this.mockedContentAccess = mockedContentAccess;
    this.mockedReadAccess = mockedReadAccess;
  }

  @Override
  public IUserContentAccess getUserContentAccess( String s ) {
    return mockedContentAccess;
  }

  @Override
  public IReadAccess getPluginRepositoryReader( String s ) {
    return mockedReadAccess;
  }

  @Override
  public IRWAccess getPluginRepositoryWriter( String s ) {
    return null;
  }

  @Override
  public IReadAccess getPluginSystemReader( String s ) {
    return mockedReadAccess;
  }

  @Override
  public IRWAccess getPluginSystemWriter( String s ) {
    return null;
  }

  @Override
  public IReadAccess getOtherPluginSystemReader( String s, String s2 ) {
    return mockedReadAccess;
  }

  @Override
  public IRWAccess getOtherPluginSystemWriter( String s, String s2 ) {
    return null;
  }
}
