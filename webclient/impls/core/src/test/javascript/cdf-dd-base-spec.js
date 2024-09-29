/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


describe("CDF-DD-BASE tests", function() {

  	it("Check if previewMode generated the correct data", function() {
  		cdfdd.previewMode();
    	expect(PreviewRequests.status).toBeTruthy();
  	});
});
