/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/
package pt.webdetails.cdf.dd.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;

import java.io.IOException;
import java.io.InputStream;

public class GenericBasicFileFilterTest {

  protected static final String BASE_DIR = "/this/is/the/image/folder/";
  protected static final String BASE_DIR_PLUS_ONE_EXTRA_DIR = BASE_DIR + "plus/one/folder/";

  protected static final String ACCEPTED_FILENAME = "valid_filename";
  protected static final String[] ACCEPTED_EXTENSIONS = new String[] { ".jpg", ".png", ".svg" };

  protected static final String NOT_ACCEPTED_FILENAME = "some_other_name";
  protected static final String NOT_ACCEPTED_EXTENSION = ".gif";


  @Before
  public void setUp() {
    /* do nothing */
  }

  /**
   * This tests that the file is properly accepted because of having both and accepted filename and an accepted file
   * extension.
   */
  @Test
  public void testFileIsProperlyAcceptedByNameAndExtension() {

    String filename = ACCEPTED_FILENAME + ACCEPTED_EXTENSIONS[ 0 ];

    IBasicFile testFile = new DummyBasicFile( BASE_DIR + filename );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS );

    Assert.assertTrue( filter.accept( testFile ) );
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

    Assert.assertTrue( filter.accept( testFile ) );
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

    Assert.assertTrue( filter.accept( testFile ) );
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

    Assert.assertTrue( filter.accept( testFile ) );
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

    Assert.assertTrue( !filter.accept( testFile ) );

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

    Assert.assertTrue( !filter.accept( testFile ) );

  }

  /**
   * This tests that the file is properly accepted it is a directory, and we are stating we accept those.
   */
  @Test
  public void testDirectoryIsProperlyAccepted() {

    boolean acceptDirectories = true;

    IBasicFile testFolder = new DummyBasicFile( BASE_DIR_PLUS_ONE_EXTRA_DIR );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS, acceptDirectories );

    Assert.assertTrue( filter.accept( testFolder ) );
  }

  @Test
  public void testDirectoryIsProperlyDiscarded() {

    boolean acceptDirectories = false;

    IBasicFile testFolder = new DummyBasicFile( BASE_DIR_PLUS_ONE_EXTRA_DIR );

    IBasicFileFilter filter = new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS, acceptDirectories );

    Assert.assertTrue( !filter.accept( testFolder ) );

  }

  @After
  public void tearDown() {
    /* do nothing */
  }


  protected class DummyBasicFile implements IBasicFile {

    private String path;
    private boolean directory;

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
