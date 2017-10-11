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

package pt.webdetails.cdf.dd;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFileMetaProvider;
import org.pentaho.platform.engine.core.solution.FileInfo;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import pt.webdetails.cpf.utils.XmlParserFactoryProducer;

import java.io.InputStream;

/**
 * Parses a Dom4J document and creates an IFileInfo object containing the
 * xcdf info.
 *
 * @author Will Gorman (wgorman@pentaho.com)
 */

public class DashboardDesignerFileInfoGenerator implements ISolutionFileMetaProvider {

  private ILogger logger;

  public DashboardDesignerFileInfoGenerator() {
  }

  //FIXME: use ISolutionFileMetaProvider
//  public ContentType getContentType() {
//    return ContentType.DOM4JDOC;
//  }

  public IFileInfo getFileInfo( ISolutionFile solutionFile, InputStream in ) {
    String solution = solutionFile.getSolution();
    String path = solutionFile.getFullPath();
    String fileName = solutionFile.getFileName();
    SAXReader reader = XmlParserFactoryProducer.getSAXReader( null );

    try {

      Document doc = reader.read( in );
      return getFileInfo( solution, path, fileName, doc );

    } catch ( DocumentException e ) {
      if ( logger != null ) {
        logger.error( "Error parsing document", e );
      }
      return null;
    }
  }

  public IFileInfo getFileInfo( String solution, String path, String filename,
                               Document doc ) {

    String result = "dashboard";  //$NON-NLS-1$
    doc.asXML();
    String author = XmlDom4JHelper.getNodeText( "/cdf/author", doc, "" );  //$NON-NLS-1$ //$NON-NLS-2$
    String description = XmlDom4JHelper.getNodeText( "/cdf/description", doc, "" );  //$NON-NLS-1$ //$NON-NLS-2$
    String icon = XmlDom4JHelper.getNodeText( "/cdf/icon", doc, "" );  //$NON-NLS-1$ //$NON-NLS-2$
    String title = XmlDom4JHelper.getNodeText( "/cdf/title", doc, "" );  //$NON-NLS-1$ //$NON-NLS-2$

    IFileInfo info = new FileInfo();
    info.setAuthor( author );
    info.setDescription( description );
    info.setDisplayType( result );
    info.setIcon( icon );
    info.setTitle( title );
    return info;
  }

//  public IFileInfo getFileInfo(String solution, String path, String filename,
//                               InputStream in) {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  public IFileInfo getFileInfo() {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  public IFileInfo getFileInfo(String solution, String path, String filename,
//                               byte[] bytes) {
//
//    return getFileInfo(solution, path, filename, new String(bytes));
//  }
//
//  public IFileInfo getFileInfo(String solution, String path, String filename,
//                               String str) {
//    try {
//      return getFileInfo(solution, path, filename, DocumentHelper.parseText(str));
//    } catch (Exception e) {
//      //logger.error( Messages.getErrorString("CdfFileInfoGenerator.ERROR_0001_PARSING_XCDF") ); //$NON-NLS-1$
//      return null;
//    }
//  }

  public void setLogger( ILogger logger ) {
    this.logger = logger;
  }

}
