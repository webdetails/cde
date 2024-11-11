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


load("${project.build.directory}/requireCfg.js");
var output = "require.config("+JSON.stringify(requireCfg)+");";

out = new java.io.FileWriter( "${project.build.directory}/requireCfg.js" );

// Write the code to the file
out.write( output, 0, output.length );
out.flush();
out.close();
