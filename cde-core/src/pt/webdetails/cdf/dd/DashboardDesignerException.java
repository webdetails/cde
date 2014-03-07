/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

@SuppressWarnings("serial")
public class DashboardDesignerException extends Exception {
  public DashboardDesignerException(String message) {
     super(message);
  }
  public DashboardDesignerException(Throwable cause) {
    super(cause);
 }
}