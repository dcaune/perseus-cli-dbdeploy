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

public abstract class SQLDeploymentManagerFactory {
  /**
   * Build and return a SQL deployment manager specific to the
   * specified relational database management system.
   * 
   * @param rdbmsName the name of a relational database management
   *        system.
   * @param jdbcDriver Java class name of the JDBC driver that is to
   *        be used to connect to the relational database management
   *        system.
   * @param jdbcUrl database URL of the form
   *        <code>jdbc:subprotocol:subname</code> identifying the
   *        relational database management system.
   * @param accountUsername database user on whose behalf the
   *        connection is being made to the relational database
   *        management system.
   * @param accountPassword password of the user on whose behalf the
   *        connection is being made to the relational database
   *        management system.
   * 
   * @return a SQL deployment manager specific to the specified
   *         relational database management system.
   *
   * @throws ClassNotFoundException if the Java class name of the JDBC
   *         driver wasn't found.
   * @throws IOException if a read access to a SQL script files failed
   *         for some reason.
   * @throws UnsupportedRDBMSException if the specified relational
   *         database management system is not supported.
   */
  public static SQLDeploymentManager getSQLDeploymentManager(
      String rdbmsName,
      String jdbcDriver,
      String jdbcUrl,
      String accountUsername,
      String accountPassword) 
    throws ClassNotFoundException,
           IOException,
           UnsupportedRDBMSException {
    if (rdbmsName.compareTo("postgresql") == 0)
      return new PostgreSQLDeploymentManager(
          jdbcDriver,
          jdbcUrl,
          accountUsername,
          accountPassword);
    
    throw new UnsupportedRDBMSException(rdbmsName);
  }
}