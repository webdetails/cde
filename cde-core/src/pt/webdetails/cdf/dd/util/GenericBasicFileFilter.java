package pt.webdetails.cdf.dd.util;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;

public class GenericBasicFileFilter implements IBasicFileFilter{
	
	private String fileName;
	private String[] fileExtensions;
	
	public GenericBasicFileFilter(String fileName, String fileExtension){
		this.fileName = fileName;
		this.fileExtensions = !StringUtils.isEmpty(fileExtension) ? new String[]{cleanDot(fileExtension)} : null;
	}
	
	public GenericBasicFileFilter(String fileName, String[] fileExtensions){
		this.fileName = fileName;
		
		if(fileExtensions != null && fileExtensions.length > 0){
			ArrayList<String> extensions = new ArrayList<String>();
			for(String fileExtension : fileExtensions){
				if(!StringUtils.isEmpty(fileExtension)){
					extensions.add(cleanDot(fileExtension));
				}
			}
			this.fileExtensions = extensions.toArray(new String[extensions.size()]);
		} else {
			this.fileExtensions = null;
		}
	}
	  
	@Override
	public boolean accept(IBasicFile file) {
		
		boolean fileNameOK = false;
		boolean fileExtensionOK = false;
		
		if(file != null && file.getName() != null){
			
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
						fileExtensionOK = fileExtension.equalsIgnoreCase(cleanDot(file.getExtension()));
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