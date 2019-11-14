/*!
 * Copyright 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.testUtils;

import pt.webdetails.cpf.web.DelegatingServletOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Locale;

/**
 * This class is used for unit test only and is used to test the buffer, headers and status. The headers are strings
 * only. This class was needed to retrieve header values for testing.
 */
public class StringHeaderHttpServletResponseForTests implements HttpServletResponse {
  private String contentType;
  private final ByteArrayOutputStream content;
  private final DelegatingServletOutputStream servletOutputStream;
  private final Hashtable<String, String> headers = new Hashtable<>();
  private int status = HttpServletResponse.SC_OK;
  private String errorMessage;

  public StringHeaderHttpServletResponseForTests( ByteArrayOutputStream outputStream ) {
    this.content = outputStream;
    this.servletOutputStream = new DelegatingServletOutputStream( this.content );
  }

  @Override
  public void addCookie( Cookie cookie ) {  }

  @Override
  public boolean containsHeader( String name ) {
    return this.headers.keySet().contains( name );
  }

  @Override
  public String encodeURL( String url ) {
    return null;
  }

  @Override
  public String encodeRedirectURL( String url ) {
    return null;
  }

  @Override
  public String encodeUrl( String url ) {
    return null;
  }

  @Override
  public String encodeRedirectUrl( String url ) {
    return null;
  }

  @Override
  public void sendError( int sc, String msg ) {  }

  @Override
  public void sendError( int sc ) {  }

  @Override
  public void sendRedirect( String location ) {  }

  @Override
  public void setDateHeader( String name, long date ) {  }

  @Override
  public void addDateHeader( String name, long date ) {  }

  @Override
  public void setHeader( String name, String value ) {
    this.headers.put( name, value );
  }

  @Override
  public void addHeader( String name, String value ) {
    if ( name == null ) {
      this.headers.put( name, value );
    }
  }

  @Override
  public void setIntHeader( String name, int value ) {  }

  @Override
  public void addIntHeader( String name, int value ) {  }

  @Override
  public void setStatus( int status ) {
    this.status = status;
  }

  @Override
  public void setStatus( int status, String errorMessage ) {
    this.status = status;
    this.errorMessage = errorMessage;
  }

  @Override
  public int getStatus() {
    return this.status;
  }

  @Override
  public String getHeader( String name ) {
    return this.headers.get( name );
  }

  @Override
  public Collection<String> getHeaders( String name ) {
    return null;
  }

  @Override
  public Collection<String> getHeaderNames() {
    return this.headers.keySet();
  }

  @Override
  public String getCharacterEncoding() {
    return null;
  }

  @Override
  public String getContentType() {
    return this.contentType;
  }

  @Override
  public ServletOutputStream getOutputStream() {
    return this.servletOutputStream;
  }

  @Override
  public PrintWriter getWriter() {
    return null;
  }

  @Override
  public void setCharacterEncoding( String charset ) {  }

  @Override
  public void setContentLength( int len ) {  }

  @Override
  public void setContentType( String type ) {
    this.contentType = type;
  }

  @Override
  public void setBufferSize( int size ) {  }

  @Override
  public int getBufferSize() {
    return 0;
  }

  @Override
  public void flushBuffer() {  }

  @Override
  public void resetBuffer() {  }

  @Override
  public boolean isCommitted() {
    return false;
  }

  @Override
  public void reset() {  }

  @Override
  public void setLocale( Locale loc ) {  }

  @Override
  public Locale getLocale() {
    return null;
  }
}
