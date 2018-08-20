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

import java.io.IOException;
import java.util.Collection;

public abstract class RevisionControlSystemFactory {
  /**
   * Build and return a SQL deployment manager specific to the
   * specified relational database management system.
   * 
   * @param revisionControlSystemName the name of a revision control
   *        system.
   * 
   * @return .
   *
   * @throws UnsupportedRevisionControlSystemException if the
   *         specified revision control system is not supported.
   */
  public static RevisionControlSystem getRevisionControlSystem(
      String revisionControlSystemName) 
    throws UnsupportedRevisionControlSystemException {
    if (revisionControlSystemName.compareTo("perforce") == 0)
      return new PerforceRevisionControlSystem();
    else if (revisionControlSystemName.compareTo("subversion") == 0)
      return new SubversionRevisionControlSystem();
    
    throw new UnsupportedRevisionControlSystemException(revisionControlSystemName);
  }
}