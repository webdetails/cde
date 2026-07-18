/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



/*This is to add as a dependency in custom components which use libraries with 
  Immediately-Invoked Function Expressions (IIFE), to prevent cases in which the minification
  would put an unfinished statement before an IIFE - http://jira.pentaho.com/browse/CDE-524. 
*/
;