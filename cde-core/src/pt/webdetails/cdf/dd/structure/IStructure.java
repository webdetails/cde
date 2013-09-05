/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.structure;

import java.util.HashMap;

public interface IStructure {
	
	public abstract HashMap<String, String>  save(HashMap<String, Object> parameters) throws Exception;
	
	public abstract Object load(HashMap<String, Object> parameters) throws Exception;
	
	public abstract void delete(HashMap<String, Object> parameters) throws Exception;
	
}
