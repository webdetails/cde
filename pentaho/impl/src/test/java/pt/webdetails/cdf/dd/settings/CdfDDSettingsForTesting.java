/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.settings;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import pt.webdetails.cdf.dd.CdeSettings;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IRWAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * The only thing this class does is to replace the source settings.xml file with the one placed at src/test/resources
 */
public class CdfDDSettingsForTesting extends CdeSettings.CdfDDSettings {

  private static final String SETTINGS_XML = "src/test/resources/settings.xml";

  private Document settings;

  public CdfDDSettingsForTesting( IRWAccess writeAccess ) {
    super( writeAccess );
    loadDocument();
  }

  @Override
  protected List<Element> getSettingsXmlSection( String section ) {
    return Utils.selectElements( settings, "/settings/" + section );
  }

  private boolean loadDocument() {
    InputStream input = null;

    try {
      input = new FileInputStream( new File( SETTINGS_XML ) );
      SAXReader reader = new SAXReader();
      settings = reader.read( input );
      return true;
    } catch ( Exception ex ) {
      logger.error( "Error while reading settings.xml", ex );
    } finally {
      IOUtils.closeQuietly( input );
    }
    return false;
  }
}
