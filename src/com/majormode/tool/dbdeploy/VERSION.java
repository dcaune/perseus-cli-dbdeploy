/**
 * Copyright (C) 1998-2008 Majormode.  All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Majormode or one of its subsidiaries.  You shall not disclose this
 * confidential information and shall use it only in accordance with
 * the terms of the license agreement or other applicable agreement
 * you entered into with Majormode.
 *
 * MAJORMODE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  MAJORMODE
 * SHALL NOT BE LIABLE FOR ANY LOSSES OR DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */

package com.majormode.tool.dbdeploy;

/**
 * Class used to output product version information when user
 * performs command <code>java -jar</code>.
 * 
 * @author Daniel CAUNE (daniel.caune@majormode.com)
 */
public final class VERSION {
  protected static final String USAGE_INFORMATION =
    "\n";
  
  public static void main(String[] args) {
    System.out.println("Database Deployment Tool " +
        VERSION.class.getPackage().getSpecificationVersion() + "." +
		    VERSION.class.getPackage().getImplementationVersion());
    System.out.println("Copyright (C) 1998-2008 Majormode.  All rights reserved.\n");
    System.out.println(USAGE_INFORMATION);
  }
}
