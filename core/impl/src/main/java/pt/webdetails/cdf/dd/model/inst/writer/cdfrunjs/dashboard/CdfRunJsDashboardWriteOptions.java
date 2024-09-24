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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.CdeConstants;

public class CdfRunJsDashboardWriteOptions implements Serializable {
  private static final long serialVersionUID = 1L;

  private final boolean _absolute, _debug, _amdModule;
  private final String _absRoot, _scheme, _aliasPrefix;
  // '.' and ':' are actually accepted in HTML4 ids, however its better to treat them
  // because jQuery selectors would need to explicitly escape these characters
  private static final String SPECIAL_CHARACTERS = "—[~!@#$%^&*(){}|,]=+|;'\"?<>`.: ";
  private static final Pattern SPECIAL_CHARACTERS_PATTERN =
    Pattern.compile( "[\\Q" + SPECIAL_CHARACTERS + "\\E]" );

  public CdfRunJsDashboardWriteOptions( boolean absolute, boolean debug, String absRoot, String scheme ) {
    this( "", false, absolute, debug, absRoot, scheme );
  }

  public CdfRunJsDashboardWriteOptions( boolean amdModule, boolean absolute, boolean debug,
                                        String absRoot, String scheme ) {
    this( "", amdModule, absolute, debug, absRoot, scheme );
  }

  public CdfRunJsDashboardWriteOptions( String aliasPrefix, boolean amdModule, boolean absolute,
                                        boolean debug, String absRoot, String scheme ) {
    this._aliasPrefix = escapeAlias( aliasPrefix );
    this._amdModule = amdModule;
    this._absolute = absolute;
    this._debug = debug;
    this._absRoot = absRoot;
    this._scheme = scheme;
  }

  public CdfRunJsDashboardWriteOptions addAliasPrefix( String aliasPrefix ) {
    if ( StringUtils.isEmpty( aliasPrefix ) ) {
      throw new IllegalArgumentException( "aliasPrefix" );
    }

    aliasPrefix = ( StringUtils.isEmpty( this._aliasPrefix ) ? "" : this._aliasPrefix ) + escapeAlias( aliasPrefix );

    return new CdfRunJsDashboardWriteOptions( aliasPrefix, _amdModule, _absolute, _debug, _absRoot, _scheme );
  }

  public String getAliasPrefix() {
    return this._aliasPrefix;
  }

  public String getAbsRoot() {
    return this._absRoot;
  }

  public String getSchemedRoot() {
    return ( StringUtils.isEmpty( this._scheme ) ? "" : ( this._scheme + "://" ) ) + this._absRoot;
  }

  public String getScheme() {
    return this._scheme;
  }

  public boolean isAbsolute() {
    return this._absolute;
  }

  public boolean isDebug() {
    return this._debug;
  }

  public boolean isAmdModule() {
    return this._amdModule;
  }

  /**
   * Method used to escape the alias. Will not escape the tag used for alias substitution.
   *
   * @param alias The alias to escape
   * @return The escaped alias
   */
  protected String escapeAlias( String alias ) {
    if ( alias.contains( CdeConstants.DASHBOARD_ALIAS_TAG ) ) {
      String left = alias.substring( 0, alias.indexOf( CdeConstants.DASHBOARD_ALIAS_TAG ) );
      String right = alias.substring( alias.indexOf( CdeConstants.DASHBOARD_ALIAS_TAG )
        + CdeConstants.DASHBOARD_ALIAS_TAG.length(), alias.length() );
      return toJavaIdentifier( left ) + CdeConstants.DASHBOARD_ALIAS_TAG + toJavaIdentifier( right );
    }
    return toJavaIdentifier( alias );
  }

  /**
   * Converting all occurrences of SPECIAL_CHARACTERS to a java identifier equivalent
   *
   * @param str The string to be converted
   * @return The converted string
   */
  protected String toJavaIdentifier( String str ) {
    // Theoretically, a dashboard could be named "I-[~!@#$%^&*(){}|.,]-=_+|;'"?<>~`"
    // to prevent going against id naming rules in HTML4, we'll convert the string to a java identifier
    if ( StringUtils.isNotEmpty( str ) && SPECIAL_CHARACTERS_PATTERN.matcher( str ).find() ) {
      StringBuilder sb = new StringBuilder( "id_" );

      for ( int i = 0; i < str.length(); i++ ) {
        if ( SPECIAL_CHARACTERS.indexOf( str.charAt( i ) ) >= 0 ) {
          sb.append( (int) str.charAt( i ) );
        } else {
          sb.append( str.charAt( i ) );
        }
      }

      return sb.toString();
    }

    return str;
  }

}
