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
 * Represent an SQL constant corresponding to a name and a value.
 *
 * @author Daniel CAUNE (daniel.caune@majormode.com)
 */
public class SQLConstant
{
  /**
   * Name of the constant.
   */
  public String m_name;
  
  /**
   * SQL script where the constant is declared.
   */
  public SQLScript m_sqlScript;
  
  /**
   * Litteral value of the constant.
   */
  public String m_value;
  
  /**
   * Build an SQL constant.
   *
   * @param sqlScript SQL script where the constant is declared.
   * @param name name of the constant.
   * @param value litteral value of the constant.
   */
  public SQLConstant(SQLScript sqlScript,
                     String name,
                     String value)
  {    
    m_name = name;
    m_value = value;
    m_sqlScript = sqlScript;
  }
}
