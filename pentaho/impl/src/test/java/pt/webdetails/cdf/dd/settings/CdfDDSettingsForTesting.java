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

package pt.webdetails.cdf.dd.settings;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import pt.webdetails.cdf.dd.CdeSettings;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IRWAccess;

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
      input = new FileInputStream( SETTINGS_XML );
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
