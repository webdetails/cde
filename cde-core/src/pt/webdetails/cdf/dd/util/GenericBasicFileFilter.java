package pt.webdetails.cdf.dd.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;

public class GenericBasicFileFilter implements IBasicFileFilter{
	
	private String fileName;
	private String[] fileExtensions;
	private boolean acceptDirectories;
	
	public GenericBasicFileFilter(String fileName, String fileExtension){
		this.fileName = fileName;
		this.fileExtensions = !StringUtils.isEmpty(fileExtension) ? new String[]{cleanDot(fileExtension)} : null;
	}
	
	public GenericBasicFileFilter(String fileName, String[] fileExtensions){
		this.fileName = fileName;
		this.fileExtensions = fileExtensions;
	}
	
	public GenericBasicFileFilter(String fileName, String[] fileExtensions, boolean acceptDirectories){
		this.fileName = fileName;
		this.fileExtensions = fileExtensions;
		this.acceptDirectories = acceptDirectories;
	}
	  
	@Override
	public boolean accept(IBasicFile file) {
		
		boolean fileNameOK = false;
		boolean fileExtensionOK = false;
		
		if(file != null && file.getName() != null){
			
			if(acceptDirectories && file.isDirectory()){
				return true;
			}
			
			// file name is equal ?
			if(!StringUtils.isEmpty(fileName)){
				fileNameOK = fileName.equalsIgnoreCase(FilenameUtils.getBaseName(file.getName()));
			}else{
				fileNameOK = true; //filename was not placed as filter
			}
		
			if(fileExtensions != null && fileExtensions.length > 0){
				// is file extension one of the allowed extensions ?	
				for(String fileExtension : fileExtensions){
					if(!StringUtils.isEmpty(fileExtension)){
						fileExtensionOK = cleanDot(fileExtension).equalsIgnoreCase(cleanDot(file.getExtension()));
						if(fileExtensionOK){
							break; //found a match
						}
					}
				}
			}else{
				fileExtensionOK = true; //file extension was not placed as filter
			}
		}
		
		return fileNameOK && fileExtensionOK;
	}
	
	private static String cleanDot(String extension){
		return !StringUtils.isEmpty(extension) && extension.startsWith(".") ? extension.substring(1) : extension;
	}
}