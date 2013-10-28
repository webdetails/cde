package pt.webdetails.cdf.dd.extapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import pt.webdetails.cdf.dd.PentahoCdeEnvironment;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.impl.FileContent;
import pt.webdetails.cpf.utils.CharsetHelper;

public class FileHandler implements IFileHandler {

  @Override
  public boolean saveDashboardAs( String path, String title, String description, String cdfdeJsText ) throws Exception {
    // 1. Read empty wcdf file
    InputStream wcdfFile =
        CdeEnvironment.getPluginSystemReader().getFileInputStream(
            DashboardStructure.SYSTEM_PLUGIN_EMPTY_WCDF_FILE_PATH );

    String wcdfContentAsString = IOUtils.toString( wcdfFile, CharsetHelper.getEncoding() );

    // 2. Fill-in wcdf file title and description
    wcdfContentAsString = wcdfContentAsString.replaceFirst( "@DASBOARD_TITLE@", title );
    wcdfContentAsString = wcdfContentAsString.replaceFirst( "@DASBOARD_DESCRIPTION@", description );

    // 3. Publish new wcdf file
    ByteArrayInputStream bais = new ByteArrayInputStream( wcdfContentAsString.getBytes( CharsetHelper.getEncoding() ) );

    FileContent file = new FileContent();
    file.setPath( path );
    file.setContents( bais );
    file.setTitle( title );
    file.setDescription( description );

    return PentahoCdeEnvironment.getInstance().getContentAccessFactory().getUserContentAccess( null ).saveFile( file );
  }

}
