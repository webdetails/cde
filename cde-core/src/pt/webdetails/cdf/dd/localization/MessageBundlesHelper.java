package pt.webdetails.cdf.dd.localization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

public class MessageBundlesHelper {

  private static final Log logger = LogFactory.getLog( MessageBundlesHelper.class );

  public static final String SYSTEM_PLUGIN_GLOBAL_LANGUAGES_DIR = "lang"; //$NON-NLS-1$
  public static final String BASE_GLOBAL_MESSAGE_SET_FILENAME = "messages"; //$NON-NLS-1$
  public static final String BASE_CACHE_DIR = "tmp/.cache"; //$NON-NLS-1$

  private IBasicFile globalBaseMessageFile;

  // Name the dashboard target i18n messages file. If we have a dashboard specific language file it will be named
  // the same otherwise it will have the name of the global message file. The target message file contains globals and
  // local translations
  // (if the dashboard has a specific set of translations) or the name of the global one if no translations are
  // specified.
  // This way we eliminate fake error messages that are given by the unexpected unavailability of message files.
  private IBasicFile targetDashboardBaseMsgFile;
  private IBasicFile globalMsgCacheFile;
  private IBasicFile sourceDashboardBaseMsgFile;
  private String languagesCacheUrl;
  private String msgsRelativeDir;
  private String staticBaseContentUrl;

  public MessageBundlesHelper( String msgsRelativeDir, IBasicFile sourceDashboardBaseMsgFile ) {

    this.staticBaseContentUrl = CdeEngine.getEnv().getExtApi().getPluginStaticBaseUrl();

    this.msgsRelativeDir = msgsRelativeDir;
    this.sourceDashboardBaseMsgFile = sourceDashboardBaseMsgFile;
    this.languagesCacheUrl = Utils.joinPath( staticBaseContentUrl, BASE_CACHE_DIR, msgsRelativeDir );
  }

  protected IBasicFile getGlobalBaseMessageFile() {
    if ( globalBaseMessageFile == null ) {
      globalBaseMessageFile =
          CdeEnvironment.getPluginSystemReader( SYSTEM_PLUGIN_GLOBAL_LANGUAGES_DIR ).fetchFile(
              BASE_GLOBAL_MESSAGE_SET_FILENAME + ".properties" );
    }
    return globalBaseMessageFile;
  }

  protected IBasicFile getTargetDashboardBaseMessageFile() {
    if ( targetDashboardBaseMsgFile == null ) {

      IReadAccess systemReader =
          CdeEnvironment.getPluginSystemReader( Utils.joinPath( BASE_CACHE_DIR, msgsRelativeDir ) );

      String msg =
          sourceDashboardBaseMsgFile != null ? sourceDashboardBaseMsgFile.getName() : BASE_GLOBAL_MESSAGE_SET_FILENAME
              + ".properties";

      if ( systemReader.fileExists( msg ) ) {
        targetDashboardBaseMsgFile = systemReader.fetchFile( msg );
      }
    }
    return targetDashboardBaseMsgFile;

  }

  protected IBasicFile getGlobalMsgCacheFile() {
    if ( globalMsgCacheFile == null ) {

      IReadAccess systemReader =
          CdeEnvironment.getPluginSystemReader( Utils.joinPath( BASE_CACHE_DIR, msgsRelativeDir ) );

      String msg = BASE_GLOBAL_MESSAGE_SET_FILENAME + ".properties";

      if ( systemReader.fileExists( msg ) ) {
        globalMsgCacheFile = systemReader.fetchFile( msg );
      }
    }

    return globalMsgCacheFile;
  }

  public void saveI18NMessageFilesToCache() {

    if ( !CdeEnvironment.getPluginSystemReader( BASE_CACHE_DIR ).fileExists( msgsRelativeDir ) ) {
      CdeEnvironment.getPluginSystemWriter().createFolder( Utils.joinPath( BASE_CACHE_DIR, msgsRelativeDir ) );
    }

    try {

      copyStdGlobalMessageFileToCache();

      appendMessageFiles( sourceDashboardBaseMsgFile, getGlobalBaseMessageFile(), getTargetDashboardBaseMessageFile() );

    } catch ( IOException e ) {
      logger.error( e );
    }
  }

  public String getMessageFilesCacheUrl() {
    return FilenameUtils.normalize( FilenameUtils.separatorsToUnix( languagesCacheUrl ) );
  }

  protected void appendMessageFiles( IBasicFile sourceDashboardBaseMsgFile, IBasicFile globalBaseMessageFile,
      IBasicFile targetDashboardBaseMsgFile ) throws IOException {

    Locale locale = CdeEngine.getEnv().getLocale();

    IUserContentAccess userContentAccess =
        CdeEngine.getEnv().getContentAccessFactory().getUserContentAccess( msgsRelativeDir );
    IRWAccess systemWriter =
        CdeEngine.getEnv().getContentAccessFactory().getPluginSystemWriter(
            Utils.joinPath( BASE_CACHE_DIR, msgsRelativeDir ) );

    // If localized global message file doesn't exists then use the standard base global message file
    // and generate a fake global message file. So this way we're sure that we always have the file
    String localizedMsgGlobalName = BASE_GLOBAL_MESSAGE_SET_FILENAME + "_" + locale.getLanguage() + ".properties";

    if ( userContentAccess.fileExists( localizedMsgGlobalName ) ) {

      systemWriter.saveFile( localizedMsgGlobalName, userContentAccess.getFileInputStream( localizedMsgGlobalName ) );

    } else if ( globalBaseMessageFile != null ) {

      systemWriter.saveFile( localizedMsgGlobalName, globalBaseMessageFile.getContents() );
    }

    // Append specific message file only if it exists otherwise just use the global message files
    if ( sourceDashboardBaseMsgFile != null ) {

      systemWriter.saveFile( sourceDashboardBaseMsgFile.getName(), sourceDashboardBaseMsgFile.getContents() );

      String localizedMsgTargetName =
          FilenameUtils.getBaseName( sourceDashboardBaseMsgFile.getName() ) + "_" + locale.getLanguage()
              + ".properties";

      if ( userContentAccess.fileExists( localizedMsgTargetName ) ) {

        systemWriter.saveFile( localizedMsgTargetName, userContentAccess.getFileInputStream( localizedMsgTargetName ) );
      }
    }
  }

  protected void copyStdGlobalMessageFileToCache() throws IOException {

    IBasicFile globalMsgCacheFile = getGlobalMsgCacheFile();

    if ( globalMsgCacheFile != null && globalMsgCacheFile.getContents() != null ) {
      return;

    } else {

      String globalMsgFileName = BASE_GLOBAL_MESSAGE_SET_FILENAME + ".properties";

      IBasicFile globalMsgFile =
          CdeEnvironment.getPluginSystemReader( SYSTEM_PLUGIN_GLOBAL_LANGUAGES_DIR ).fetchFile( globalMsgFileName );

      CdeEnvironment.getPluginSystemWriter().saveFile(
          Utils.joinPath( BASE_CACHE_DIR, msgsRelativeDir, globalMsgFileName ), globalMsgFile.getContents() );
    }
  }

  public String replaceParameters( String text, ArrayList<String> i18nTagsList ) throws Exception {

    if ( i18nTagsList == null ) {
      i18nTagsList = new ArrayList<String>();
    }

    saveI18NMessageFilesToCache();

    // If dashboard specific files aren't specified set message filename in cache to the global messages file filename
    String dashboardsMessagesBaseFilename =
        sourceDashboardBaseMsgFile != null ? FilenameUtils.getBaseName( sourceDashboardBaseMsgFile.getName() )
            : BASE_GLOBAL_MESSAGE_SET_FILENAME;

    text = text.replaceAll( "\\{load\\}", "onload=\"load()\"" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    text = text.replaceAll( "\\{body-tag-unload\\}", "" ); //$NON-NLS-1$
    text = text.replaceAll( "#\\{GLOBAL_MESSAGE_SET_NAME\\}", dashboardsMessagesBaseFilename ); //$NON-NLS-1$
    text = text.replaceAll( "#\\{GLOBAL_MESSAGE_SET_PATH\\}", getMessageFilesCacheUrl() ); //$NON-NLS-1$
    text = text.replaceAll( "#\\{GLOBAL_MESSAGE_SET\\}", buildMessageSetCode( i18nTagsList ) ); //$NON-NLS-1$
    text = text.replaceAll( "#\\{LANGUAGE_CODE\\}", CdeEngine.getEnv().getLocale().getLanguage() ); //$NON-NLS-1$
    return text;
  }

  private String buildMessageSetCode( ArrayList<String> tagsList ) {
    StringBuffer messageCodeSet = new StringBuffer();
    for ( String tag : tagsList ) {
      messageCodeSet
          .append( "\\$('#" ).append( updateSelectorName( tag ) ).append( "').html(jQuery.i18n.prop('" ).append( tag ).append( "'));\n" ); //$NON-NLS-1$
    }
    return messageCodeSet.toString();
  }

  private String updateSelectorName( String name ) {
    // If we've the character . in the message key substitute it conventionally to _
    // when dynamically generating the selector name. The "." character is not permitted in the
    // selector id name
    return name.replace( ".", "_" );
  }
}
