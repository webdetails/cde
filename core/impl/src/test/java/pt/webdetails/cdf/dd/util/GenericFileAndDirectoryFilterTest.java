/*!
 * Copyright 2002 - 2021 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.util;

import org.junit.Test;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenericFileAndDirectoryFilterTest extends GenericBasicFileFilterTest {

  protected static final boolean ACCEPT_DIRECTORIES = true;

  protected static final String[] DIRECTORIES = new String[] { BASE_DIR_PLUS_ONE_EXTRA_DIR };

  protected static final String NON_EXISTING_DIRECTORY = BASE_DIR + "another-folder";


  /**
   * This tests that the file is properly accepted it is a directory, and we are stating we accept those.
   */
  @Test
  public void testExistingDirectoryIsProperlyAccepted() {

    // we call the super ( which uses GenericBasicFileFilter ) to ensure it's behaviour remains unchanged ( read:
    // properly working )
    super.testDirectoryIsProperlyAccepted();

    // so the directory is properly accepted; let's check now if using GenericFileAndDirectoryFilter,
    // it remains as an accepted folder

    // we use the exact same IBasicFile object as the super
    IBasicFile testFolder = new DummyBasicFile( BASE_DIR_PLUS_ONE_EXTRA_DIR );

    // we use the exact same IBasicFileFilter object as the super
    GenericBasicFileFilter superFilter =
      new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS, ACCEPT_DIRECTORIES );

    // we use the new GenericFileAndDirectoryFilter
    IBasicFileFilter filter = new GenericFileAndDirectoryFilter( superFilter, DIRECTORIES,
      GenericFileAndDirectoryFilter.FilterType.FILTER_IN );

    // this directory should now be accepted because it is contained in the DIRECTORIES and we are doing a
    // FILTER_IN logic ( white-list )
    assertTrue( filter.accept( testFolder ) );
  }

  @Test
  public void testExistingDirectoryIsProperlyDiscarded() {

    // we call the super ( which uses GenericBasicFileFilter ) to ensure it's behaviour remains unchanged ( read:
    // properly working )
    super.testDirectoryIsProperlyAccepted();


    // so the directory is properly accepted; let's check now if using GenericFileAndDirectoryFilter,
    // it remains as an accepted folder

    // we use the exact same IBasicFile object as the super
    IBasicFile testFolder = new DummyBasicFile( BASE_DIR_PLUS_ONE_EXTRA_DIR );

    // we use the exact same IBasicFileFilter object as the super
    GenericBasicFileFilter superFilter =
      new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS, ACCEPT_DIRECTORIES );

    // we use the new GenericFileAndDirectoryFilter
    IBasicFileFilter filter = new GenericFileAndDirectoryFilter( superFilter, DIRECTORIES,
      GenericFileAndDirectoryFilter.FilterType.FILTER_OUT );

    // this directory should *not* be accepted, because it is contained in the DIRECTORIES and we are doing a
    // FILTER_OUT logic ( black-list )
    assertFalse( filter.accept( testFolder ) );
  }

  @Test
  public void testNonExistingDirectoryIsProperlyAccepted() {

    // we call the super ( which uses GenericBasicFileFilter ) to ensure it's behaviour remains unchanged ( read:
    // properly working )
    super.testDirectoryIsProperlyAccepted();


    // so the directory is properly accepted; let's check now if using GenericFileAndDirectoryFilter,
    // it remains as an accepted folder

    IBasicFile testFolder = new DummyBasicFile( NON_EXISTING_DIRECTORY );

    // we use the exact same IBasicFileFilter object as the super
    GenericBasicFileFilter superFilter =
      new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS, ACCEPT_DIRECTORIES );

    // we use the new GenericFileAndDirectoryFilter
    IBasicFileFilter filter = new GenericFileAndDirectoryFilter( superFilter, DIRECTORIES,
      GenericFileAndDirectoryFilter.FilterType.FILTER_OUT );

    // this directory should be accepted, because it is *not* contained in the DIRECTORIES and we are doing a
    // FILTER_OUT logic ( black-list )
    assertTrue( filter.accept( testFolder ) );
  }

  @Test
  public void testNonExistingDirectoryIsProperlyDiscarded() {

    // we call the super ( which uses GenericBasicFileFilter ) to ensure it's behaviour remains unchanged ( read:
    // properly working )
    super.testDirectoryIsProperlyAccepted();


    // so the directory is properly accepted; let's check now if using GenericFileAndDirectoryFilter,
    // it remains as an accepted folder

    // we use the exact same IBasicFile object as the super
    IBasicFile testFolder = new DummyBasicFile( NON_EXISTING_DIRECTORY );

    // we use the exact same IBasicFileFilter object as the super
    GenericBasicFileFilter superFilter =
      new GenericBasicFileFilter( ACCEPTED_FILENAME, ACCEPTED_EXTENSIONS, ACCEPT_DIRECTORIES );

    // we use the new GenericFileAndDirectoryFilter
    IBasicFileFilter filter = new GenericFileAndDirectoryFilter( superFilter, DIRECTORIES,
      GenericFileAndDirectoryFilter.FilterType.FILTER_IN );

    // this directory should *not* be accepted, because it is not contained in the DIRECTORIES and we are doing a
    // FILTER_IN logic ( white-list )
    assertFalse( filter.accept( testFolder ) );
  }
}
