/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.*;
import org.pentaho.platform.engine.core.solution.FileInfo;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.InputStream;

/**
 * Parses a Dom4J document and creates an IFileInfo object containing the xcdf info.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class DashboardDesignerFileInfoGenerator extends SolutionFileMetaAdapter {

  private ILogger logger;

  public DashboardDesignerFileInfoGenerator() {
  }

  public IFileInfo getFileInfo( ISolutionFile solutionFile, InputStream in ) {
    String solution = solutionFile.getSolution();
    String path = solutionFile.getFullPath();
    String fileName = solutionFile.getFileName();
    SAXReader reader = new SAXReader();

    try {

      Document doc = reader.read( in );
      return getFileInfo( solution, path, fileName, doc );

    } catch ( DocumentException e ) {
      if ( logger != null )
        logger.error( "Error parsing document", e );
      return null;
    }
  }

  public IFileInfo getFileInfo( String solution, String path, String filename, Document doc ) {

    String result = "dashboard"; //$NON-NLS-1$
    doc.asXML();
    String author = XmlDom4JHelper.getNodeText( "/cdf/author", doc, "" ); //$NON-NLS-1$ //$NON-NLS-2$
    String description = XmlDom4JHelper.getNodeText( "/cdf/description", doc, "" ); //$NON-NLS-1$ //$NON-NLS-2$
    String icon = XmlDom4JHelper.getNodeText( "/cdf/icon", doc, "" ); //$NON-NLS-1$ //$NON-NLS-2$
    String title = XmlDom4JHelper.getNodeText( "/cdf/title", doc, "" ); //$NON-NLS-1$ //$NON-NLS-2$

    IFileInfo info = new FileInfo();
    info.setAuthor( author );
    info.setDescription( description );
    info.setDisplayType( result );
    info.setIcon( icon );
    info.setTitle( title );
    return info;
  }

  public IFileInfo getFileInfo( String solution, String path, String filename, InputStream in ) {
    // TODO Auto-generated method stub
    return null;
  }

  public IFileInfo getFileInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  public IFileInfo getFileInfo( String solution, String path, String filename, byte[] bytes ) {

    return getFileInfo( solution, path, filename, new String( bytes ) );
  }

  public IFileInfo getFileInfo( String solution, String path, String filename, String str ) {
    try {
      return getFileInfo( solution, path, filename, DocumentHelper.parseText( str ) );
    } catch ( Exception e ) {
      //logger.error( Messages.getErrorString("CdfFileInfoGenerator.ERROR_0001_PARSING_XCDF") ); //$NON-NLS-1$
      return null;
    }
  }

  public void setLogger( ILogger logger ) {
    this.logger = logger;
  }

}
