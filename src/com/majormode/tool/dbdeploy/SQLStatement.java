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
import java.util.Collection;

/**
 * Represent an SQL statement to be executed against a relational
 * database management system (RDBMS).
 *
 * @author Daniel CAUNE (daniel.caune@majormode.com)
 */
public class SQLStatement implements Cloneable, Serializable
{
  public static final int EXECUTION_STATUS_UNDEFINED = 0;
  public static final int EXECUTION_STATUS_SUCCEEDED = 1;
  public static final int EXECUTION_STATUS_FAILED = 2;

  public int m_attemptCount;
  public int m_executionStatus;

  /**
   * Collection of commands that need to be executed before every SQL
   * statement to change run-time configuration parameters.
   */
  protected Collection m_runtimeParameterCommands;

  public String m_sqlExpression;
  public SQLScript m_sqlScript;
  
  
  /**
   * Builds an SQL statement.
   *
   * @param sqlExpression SQL expression that needs to be executed
   * @param sqlScript reference of the SQL script file that the SQL
   *        statement comes from.
   */
  public SQLStatement(String sqlExpression,
                      SQLScript sqlScript)
  {
    this(sqlExpression, sqlScript, null);
  }

  public SQLStatement(String sqlExpression,
                      SQLScript sqlScript,
                      Collection runtimeParameterCommands) 
  {
    m_attemptCount = 0;
    m_executionStatus = EXECUTION_STATUS_UNDEFINED;
    m_runtimeParameterCommands = runtimeParameterCommands;
    m_sqlExpression = sqlExpression;
    m_sqlScript = sqlScript;
  }
}
