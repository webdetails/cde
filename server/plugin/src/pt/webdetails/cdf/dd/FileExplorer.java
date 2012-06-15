package pt.webdetails.cdf.dd;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;

import pt.webdetails.cpf.repository.RepositoryAccess;
import pt.webdetails.cpf.repository.RepositoryAccess.FileAccess;


public class FileExplorer {
	
	private static FileExplorer fileExplorer = null;
	//private static Log logger = LogFactory.getLog(FileExplorer.class);
	
	static FileExplorer getInstance() {
		if(fileExplorer == null) {
			fileExplorer = new FileExplorer();
		}
		return fileExplorer;
	}
	
	private ISolutionFile[] getFileList(String dir, final String fileExtensions, String access, IPentahoSession userSession) {
	  
      ArrayList<String> extensionsList = new ArrayList<String>();
      String[] extensions = StringUtils.split(fileExtensions, ".");
      if(extensions != null){
        for(String extension : extensions){
          // For some reason, in 4.5 filebased rep started to report a leading dot in extensions
          // Adding both just to be sure we don't break stuff
          extensionsList.add("." + extension);
          extensionsList.add(extension);
        }
      }
      FileAccess fileAccess = FileAccess.parse(access);
      if(fileAccess == null) fileAccess = FileAccess.READ;
      return RepositoryAccess.getRepository(userSession).listSolutionFiles(dir, fileAccess, true, extensionsList);
	}
	
	
	public String toJQueryFileTree(String baseDir, ISolutionFile[] files) {
	  StringBuilder out = new StringBuilder();
      out.append("<ul class=\"jqueryFileTree\" style=\"display: none;\">");
      
      for (ISolutionFile file : files) {
          if (file.isDirectory()) {
              out.append("<li class=\"directory collapsed\"><a href=\"#\" rel=\"" + baseDir + file.getFileName() + "/\">"+ file.getFileName() + "</a></li>");
          }
      }
      
      for (ISolutionFile file : files) {
          if (!file.isDirectory()) {
              int dotIndex = file.getFileName().lastIndexOf('.');
              String ext = dotIndex > 0 ? file.getFileName().substring(dotIndex + 1) : "";
              out.append("<li class=\"file ext_" + ext + "\"><a href=\"#\" rel=\"" + baseDir + file.getFileName() + "\">"+ file.getFileName() + "</a></li>");
          }
      }
      out.append("</ul>");
      return out.toString();
	}
	
	public String getJqueryFileTree(final String dir, final String fileExtensions, final String access,  IPentahoSession userSession){
	  ISolutionFile[] files = getFileList(dir, fileExtensions, access, userSession);
	  return toJQueryFileTree(dir, files);
	}


}
