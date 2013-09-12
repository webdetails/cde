package pt.webdetails.cdf.dd.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class GenericBasicFileFilter implements IBasicFileFilter{
	
	private static Log logger = LogFactory.getLog(GenericBasicFileFilter.class);
	
	private String fileName;
	private String[] fileExtensions;
	private boolean checkReadability;
	private IReadAccess access;
	
	public GenericBasicFileFilter(String fileName, String fileExtension){
		this.fileName = fileName;
		this.fileExtensions = new String[]{fileExtension};
	}
	
	public GenericBasicFileFilter(String fileName, String[] fileExtensions){
		this.fileName = fileName;
		this.fileExtensions = fileExtensions;
	}
	
	public GenericBasicFileFilter(String fileName, String fileExtension, boolean checkReadability, IReadAccess access){
		this.fileName = fileName;
		this.fileExtensions = new String[]{fileExtension};
		this.checkReadability = checkReadability;
		this.access = access;
		
	}
	
	public GenericBasicFileFilter(String fileName, String[] fileExtensions, boolean checkReadability, IReadAccess access){
		this.fileName = fileName;
		this.fileExtensions = fileExtensions;
		this.checkReadability = checkReadability;
		this.access = access;
		
	}
	  
	@Override
	public boolean accept(IBasicFile file) {
		
		boolean fileNameOK = false;
		boolean fileExtensionOK = false;
		boolean fileReadabilityOK = false;
		
		if(file != null){
			
			// file name is equal ?
			if(!StringUtils.isEmpty(fileName)){
				fileNameOK = fileName.equalsIgnoreCase(file.getName());
			}
		
			// file extension is one of the allowed extensions ?	
			for(String fileExtension : fileExtensions){
				if(!StringUtils.isEmpty(fileExtension) && fileExtension.equalsIgnoreCase(file.getExtension())){
					fileExtensionOK = true;
					break;
				}
			}			
			
			// does the file actually exist (i.e. not a bogus path) ?
			if(checkReadability){ 
				try{
					fileReadabilityOK = access.fileExists(file.getFullPath()) && access.fetchFile(file.getFullPath()) != null;
				}catch(Exception e){
					logger.error("checkReadability", e);
				}
			}
		}
		
		return fileNameOK && fileExtensionOK && fileReadabilityOK;
	}
}