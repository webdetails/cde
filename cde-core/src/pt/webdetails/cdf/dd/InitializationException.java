/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;


public class InitializationException extends Exception {

	private static final long serialVersionUID = 1089220229330479839L;

	public InitializationException(final String s, final Exception cause) {
		super(s,cause);
	}  

}
