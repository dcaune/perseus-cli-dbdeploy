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

import java.io.Serializable;
import java.util.Date;

/**
 * Represent a reference to an SQL script file, including the
 * following information:
 * <ul>
 *  <li>the pathname of the SQL script file</li>
 *  <li>the type of database objects this script creates or
 *   updates</li>
 *  <li>the name of the module or package corresponding to a
 *   particular domain, activity, business logic, for which this SQL
 *   script creates database objects.</li>
 * </ul>
 *
 * @author Daniel CAUNE (daniel.caune@majormode.com)
 */
public class SQLScript implements Cloneable, Serializable {
  public static final int EXECUTION_STATUS_NONE = 0;
  public static final int EXECUTION_STATUS_SUCCEEDED = 1;
  public static final int EXECUTION_STATUS_FAILED = 2;
  
  public int m_executionStatus;
  public String m_filePathname;
  public int m_lastDeploymentRevisionNumber;
  public int m_lastDeployementStatus;
  public Date m_lastDeploymentTime;
  public String m_moduleName;
  public String m_objectTypeName;
  public int m_revisionNumber;

  /**
   * Builds the reference of a script file.
   *
   * @param objectTypeName name of the database object type this
   *        script manages.
   * @param moduleName name of the module or package corresponding to
   *        a particular domain or activity, for which this script
   *        creates database objects.
   * @param filePathname complete pathname of the script file.
   */
  public SQLScript(String objectTypeName,
                   String moduleName,
                   String filePathname) {
    m_executionStatus = EXECUTION_STATUS_NONE;
    m_filePathname = filePathname;
    m_moduleName = moduleName;
    m_objectTypeName = objectTypeName;
  }
  
  public void setRevisionNumber(int revisionNumber) {
    m_revisionNumber = revisionNumber;
  }
}
