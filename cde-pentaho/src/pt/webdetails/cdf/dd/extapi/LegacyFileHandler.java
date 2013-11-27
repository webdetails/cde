package pt.webdetails.cdf.dd.extapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.utils.CharsetHelper;

public class LegacyFileHandler implements IFileHandler {

  @Override
  public boolean saveDashboardAs( String path, String title, String description, String cdfdeJsText ) throws Exception {

    // 1. Read empty wcdf file
    InputStream wcdfFile =
        CdeEnvironment.getPluginSystemReader().getFileInputStream(
            DashboardStructure.SYSTEM_PLUGIN_EMPTY_WCDF_FILE_PATH );

    String wcdfContentAsString = IOUtils.toString( wcdfFile, CharsetHelper.getEncoding() );
    
    // [CDE-130] CDE Dash saves file with name @DASHBOARD_TITLE@
    if(CdeConstants.DASHBOARD_TITLE_TAG.equals( title ) ){ title = FilenameUtils.getBaseName( path ); }
    if(CdeConstants.DASHBOARD_DESCRIPTION_TAG.equals( description ) ){ description = FilenameUtils.getBaseName( path ); }

    // 2. Fill-in wcdf file title and description
    wcdfContentAsString = wcdfContentAsString.replaceFirst( CdeConstants.DASHBOARD_TITLE_TAG, title );
    wcdfContentAsString = wcdfContentAsString.replaceFirst( CdeConstants.DASHBOARD_DESCRIPTION_TAG, description );

    // 3. Publish new wcdf file

    ByteArrayInputStream bais = new ByteArrayInputStream( wcdfContentAsString.getBytes( CharsetHelper.getEncoding() ) );

    return CdeEnvironment.getUserContentAccess().saveFile( path, bais );
  }

}
