/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenericBasicFileFilterTest {

  protected static final String BASE_DIR = "/this/is/the/image/folder/";
  protected static final String BASE_DIR_PLUS_ONE_EXTRA_DIR = BASE_DIR + "plus/one/folder/";

  protected static final String ACCEPTED_FILENAME = "valid_filename";
  protected static final String[] ACCEPTED_EXTENSIONS = new String[] { ".jpg", ".png", ".svg" };

  protected static final String NOT_ACCEPTED_FILENAME = "some_other_name";
  protected static final String NOT_ACCEPTED_EXTENSION = ".gif";

  /**
   * This tests that the file is properly accepted because of having both and accepted filename and an accepted file
   * extension.
   */
  @Test
  public void testFileIsProperlyAcceptedByNameAndExtension() {

    String filename = ACCEPTED_FILENAME + ACCEPTED_EXTENSIONS[ 0 ];

    IBasicFile testFile = new DummyBasicFile( BASE_DIR + filename );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS );

    assertTrue( filter.accept( testFile ) );
  }

  /**
   * This tests that the file is properly accepted because of the accepted filename. Note we purposely create the
   * testFile with a file extension other than those that are marked as accepted
   */
  @Test
  public void testFileIsProperlyAcceptedByNameOnly() {

    String filename = ACCEPTED_FILENAME + NOT_ACCEPTED_EXTENSION;

    IBasicFile testFile = new DummyBasicFile( BASE_DIR + filename );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, new String[] { } );

    assertTrue( filter.accept( testFile ) );
  }

  /**
   * This tests that the file is properly accepted because of it having a subset of the accepted filename. Note we
   * purposely create the testFile with a file extension other than those that are marked as accepted
   */
  @Test
  public void testFileIsProperlyAcceptedByNameSubsetOnly() {

    String filename = "sample." + ACCEPTED_FILENAME + NOT_ACCEPTED_EXTENSION;

    IBasicFile testFile = new DummyBasicFile( BASE_DIR + filename );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, new String[] { } );

    assertTrue( filter.accept( testFile ) );
  }

  /**
   * This tests that the file is properly accepted because of it having a file extension of the accepted kind. Note we
   * purposely create the testFile with a filename other than the one that is marked as accepted
   */
  @Test
  public void testFileIsProperlyAcceptedByExtensionOnly() {

    String filename = NOT_ACCEPTED_FILENAME + ACCEPTED_EXTENSIONS[ 0 ];

    IBasicFile testFile = new DummyBasicFile( BASE_DIR + filename );

    IBasicFileFilter filter = new GenericBasicFileFilter( null, ACCEPTED_EXTENSIONS );

    assertTrue( filter.accept( testFile ) );
  }

  /**
   * This tests that the file is properly discarded, given that it has an invalid filename, even though is has a valid
   * file extension.
   */
  @Test
  public void testFileIsProperlyDiscardedBecauseOfName() {

    String filename = NOT_ACCEPTED_FILENAME + ACCEPTED_EXTENSIONS[ 0 ];

    IBasicFile testFile = new DummyBasicFile( BASE_DIR + filename );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS );

    assertFalse( filter.accept( testFile ) );
  }

  /**
   * This tests that the file is properly discarded, given that it has a valid filename but does not hold a valid file
   * extension.
   */
  @Test
  public void testFileIsProperlyDiscardedBecauseOfExtension() {

    String filename = ACCEPTED_FILENAME + NOT_ACCEPTED_EXTENSION;

    IBasicFile testFile = new DummyBasicFile( BASE_DIR + filename );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS );

    assertFalse( filter.accept( testFile ) );
  }

  /**
   * This tests that the file is properly accepted it is a directory, and we are stating we accept those.
   */
  @Test
  public void testDirectoryIsProperlyAccepted() {

    boolean acceptDirectories = true;

    IBasicFile testFolder = new DummyBasicFile( BASE_DIR_PLUS_ONE_EXTRA_DIR );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS, acceptDirectories );

    assertTrue( filter.accept( testFolder ) );
  }

  @Test
  public void testDirectoryIsProperlyDiscarded() {

    boolean acceptDirectories = false;

    IBasicFile testFolder = new DummyBasicFile( BASE_DIR_PLUS_ONE_EXTRA_DIR );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS, acceptDirectories );

    assertFalse( filter.accept( testFolder ) );
  }

  protected static class DummyBasicFile implements IBasicFile {

    private final String path;
    private final boolean directory;

    public DummyBasicFile( String fullPath ) throws IllegalArgumentException {
      this.path = fullPath;
      this.directory = StringUtils.isEmpty( FilenameUtils.getExtension( path ) );
    }

    @Override public InputStream getContents() throws IOException {
      return null;
    }

    @Override public String getName() {
      return directory ? StringUtils.EMPTY : FilenameUtils.getName( path );
    }

    @Override public String getFullPath() {
      return path;
    }

    @Override public String getPath() {
      return path;
    }

    @Override public String getExtension() {
      return directory ? StringUtils.EMPTY : FilenameUtils.getExtension( path );
    }

    @Override public boolean isDirectory() {
      return directory;
    }
  }
}
