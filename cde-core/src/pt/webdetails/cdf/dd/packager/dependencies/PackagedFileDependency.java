/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.dependencies;

import java.io.IOException;
import java.io.InputStream;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IRWAccess;

/**
 * Base class for concatenated and minified files.
 */
public abstract class PackagedFileDependency extends FileDependency {

  private Iterable<FileDependency> inputFiles;
  private IRWAccess writer;
  private boolean isSaved;

  public PackagedFileDependency(PathOrigin origin, String path, IRWAccess writer, Iterable<FileDependency> inputFiles, IUrlProvider urlProvider) {
    super( null, origin, path, urlProvider );
    this.inputFiles = inputFiles;
    this.writer = writer;
  }

  @Override
  public synchronized InputStream getFileInputStream() throws IOException {
    if ( !isSaved ) {
      isSaved = writer.saveFile( filePath, minifyPackage(inputFiles));
      if ( !isSaved ) {
        throw new IOException("Unable to save file " + filePath);
      }
      else {
        //release refs
        inputFiles = null;
      }
    }

    return super.getFileInputStream();
  }

  protected abstract InputStream minifyPackage(Iterable<FileDependency> inputFiles);

}
