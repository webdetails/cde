/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * @deprecated will be deleted! \o/
 * @author pdpi
 */
class Concatenate {
	private static final Log logger = LogFactory.getLog(Concatenate.class);

	public static InputStream concat(IBasicFile[] files) {
		ListOfFiles mylist = new ListOfFiles(files);

		return new SequenceInputStream(mylist);
	}

	public static InputStream concat(IBasicFile[] files, IReadAccess access) {
		if (access == null) {
			return concat(files);
		}

		StringBuffer buffer = new StringBuffer();
		for (IBasicFile file : files) {
			// TODO: review this!
			BufferedReader fr = null;
			try {
				
				//rootpath = rootpath.replaceAll("\\\\", "/").replaceAll("/+", "/");
				
				// Quick and dirty hack: if the path aims at the custom
				// components, we point at getResource, else we point at the
				// static resource folders

				// Fix windows slashes'
				String filePath = file.getPath().replaceAll("\\\\", "/"); 
				
				String fileLocation = "";
				if (filePath.contains("resources/custom")) {
					// Remove this file's name
					fileLocation = filePath.replaceAll(file.getName(), "")/* .replaceAll(rootpath, "../")*/;
					
				} else if (filePath.matches(".*pentaho-cdf-dd/css/.*/.*$")) {
					
					// Remove this file's name
					fileLocation = filePath.replaceAll(file.getName(), "")/*.replaceAll(rootpath, "../")*/;
					
				} else if (filePath.matches(".*cde/components/.*/.*$")) {
					
					fileLocation = "../../res/" + filePath.substring( filePath.indexOf("cde/components/")).replaceAll(file.getName() + "$", "");
				
				} else if (filePath.matches(".*system/c\\w\\w.*")){
					
					fileLocation = "../" + filePath.substring(filePath.indexOf("system/")) .replaceAll(file.getName() + "$", "");
				}
					
				String tmp = IOUtils.toString(file.getContents());
				
				// We need to replace all the URL formats
				buffer.append(tmp.replaceAll("(url\\(['\"]?)", "$1" + fileLocation.replaceAll("/+", "/"))); // Standard URLs
			
			} catch (FileNotFoundException e) {
				logger.error("concat: File " + file.getFullPath() + " doesn't exist! Skipping...");
			} catch (Exception e) {
				logger.error("concat: Error while attempting to concatenate file " + file.getFullPath() + ". Trying to continue...", e);
			} finally {
				IOUtils.closeQuietly(fr);
			}

		} try {
			return new ByteArrayInputStream(buffer.toString().getBytes("UTF8"));
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
			return null;
		}
	}
}

class ListOfFiles implements Enumeration<InputStream> {

	private IBasicFile[] listOfFiles;
	private int current = 0;

	public ListOfFiles(IBasicFile[] listOfFiles) {
		this.listOfFiles = listOfFiles;
	}

	public boolean hasMoreElements() {
		if (current < listOfFiles.length) {
			return true;
		} else {
			return false;
		}
	}

	public InputStream nextElement() {

		InputStream in = null;
		
		if (!hasMoreElements()) {
			throw new NoSuchElementException("No more files.");
		} else {
			IBasicFile nextElement = listOfFiles[current];
			current++;
			try {
				in = nextElement.getContents();
			} catch (IOException e) {
				System.err.println("ListOfFiles: Can't open " + nextElement);
			}
		}
		return in;
	}
}
