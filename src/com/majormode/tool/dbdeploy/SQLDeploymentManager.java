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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SQLDeploymentManager {
  protected static int DEFAULT_MAXIMUM_ATTEMPT_COUNT = 3;
  protected static int DEPLOYMENT_STATUS_FAILURE = 0;
  protected static int DEPLOYMENT_STATUS_SUCCESS = 1;

  /**
   * Regular expression that matches any valid SQL comments such as:
   * <ul>
   * <li>comment that starts with the characters <code>--</code> and
   * followed by any hcaracters until the end of the line.</li>
   * <li>a C-like comment that starts with <code>/*</code> and ends
   * with <code>*&#47;/</code>.</li>
   * </ul>
   */
  protected static final String PATTERN_SQL_COMMENT =
    "(--[^\\n]*[\\n])|(/\\*([^/]|([^*]/))*\\*/)|(/\\*\\*/)";

  /**
   * Regular expression that matches the definition of an SQL constant
   * of the form:
   * <pre>
   * --(defconstant +constant-name+ constant-value)
   * </pre>
   */
  protected static final String PATTERN_SQL_CONSTANT_DECLARATION =
    "--\\s*[(]defconstant\\s+([+][\\w-]+[+])\\s+([^)]*)[)]";

  /**
   * Password of the user on whose behalf the connection is being made
   * to the relational database management system.
   */
  protected String m_accountPassword;

  /**
   * Database user on whose behalf the connection is being made to the
   * relational database management system.
   */
  protected String m_accountUsername;

  /**
   * Indicate whether database objects must be updated even if their
   * current revision correspond to the last deployed.
   */
  protected boolean m_forceUpdate = false;

  /**
   * List of SQL statements which execution has failed because of
   * dependency on some database objects that have not been already
   * created.  These SQL statements should be executed later.
   */
  protected ArrayList m_invalidSQLStatements = new ArrayList();

  /**
   * Java class name of the JDBC driver that is to be used to connect
   * to the relational database management system.
   */
  protected String m_jdbcDriver;

  /**
   * Database URL of the form <code>jdbc:subprotocol:subname</code>
   * identifying the relational database management system.
   */
  protected String m_jdbcUrl;

  /**
   * Maximum number of attempts in executing an SQL statement that
   * might temporarily fail for dependency reason.
   */
  protected int m_maximumAttemptCount = DEFAULT_MAXIMUM_ATTEMPT_COUNT;

  /**
   * Name of the module to be only processed if defined.
   */
  protected String m_moduleNameFilter = null;

  /**
   * Type of database objects to be only processed if defined.
   */
  protected String m_objectTypeFilter = null;

  /**
   * List of database objects, which a given RDBMS supports, declared
   * in their dependency order.  By default, no database object
   * supported.
   */
  protected String[] m_objectTypeNameOrders = new String[0];

  /**
   * Compiled representation of the regular expression that matches
   * any valid SQL comments.
   */
  protected Pattern m_patternSQLComment = Pattern.compile(PATTERN_SQL_COMMENT);

  /**
   * Compiled representation of the regular expression that matches
   * any SQL constant declarations.
   */
  protected Pattern m_patternSQLConstantDeclaration = Pattern.compile(PATTERN_SQL_CONSTANT_DECLARATION);

  /**
   * The revision control system that is used to manage the script
   * files and to store the identification of a revision within the
   * script files.
   */
  protected RevisionControlSystem m_revisionControlSystem;

  protected Collection m_sqlScripts;

  /**
   * Collection of SQL constants declared in SQL script files.
   */
  protected Hashtable m_sqlScriptConstants;

  /**
   * Collection of SQL script files grouped by the type of database
   * objects these files managed.
   */
  protected Hashtable m_sqlScriptGroups;

  /**
   * Indicate whether to display whole information while processing
   * SQL scripts.
   */
  protected boolean m_verbose_enabled = false;

  /**
   * Build a new instance of a SQL deployment manager for a given
   * relational database management system.
   *
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
   * @throws ClassNotFoundException if the Java class name of the JDBC
   *         driver wasn't found.
   * @throws IOException if a read access to a SQL script files failed
   *         for some reason.
   */
  protected SQLDeploymentManager(
      String jdbcDriver,
      String jdbcUrl,
      String accountUsername,
      String accountPassword)
    throws ClassNotFoundException,
           IOException {
    Class.forName(jdbcDriver);
    m_jdbcDriver = jdbcDriver;
    m_jdbcUrl = jdbcUrl;
    m_accountUsername = accountUsername;
    m_accountPassword = accountPassword;
  }

  /**
   * Return a connection to the relational database management system.
   *
   * @return a connection to the relational database management
   *         system.
   *
   * @throws SQLException if the connection to the relational database
   *         management system fails for some reason.
   */
  protected Connection getRBDMSConnection()
    throws SQLException {
    return DriverManager.getConnection(
        m_jdbcUrl,
        m_accountUsername,
        m_accountPassword);
  }

  /**
   * Group the given collection of SQL script files by the type of the
   * database objects these scripts manage.  Each script will be
   * stored in a hashtable that corresponds to the type of the
   * database objects this script manages.
   *
   * @param sqlScripts a collection of <code>SQLScript</code> objects.
   *
   * @return a map of collections, one collection per database object
   *         type.  The class of a key is a <code>String</code> object
   *         identifying a type of database objects.  The class of a
   *         value implements the <code>Collection</code> interface;
   *         each value only contains <code>SQLScript</code> objects,
   *         if any.
   */
  protected Hashtable groupSQLScripts(Collection sqlScripts) {
    Hashtable sqlScriptGroups = new Hashtable();
    for (int i = 0; i < m_objectTypeNameOrders.length; i++)
      sqlScriptGroups.put(m_objectTypeNameOrders[i], new Vector());

    Iterator iterator = sqlScripts.iterator();
    while (iterator.hasNext()) {
      SQLScript script = (SQLScript) iterator.next();
      Collection sqlScriptGroup = (Collection) sqlScriptGroups.get(script.m_objectTypeName);
      if (sqlScriptGroup == null)
        System.out.println(
            "Ignore " + script.m_filePathname + ", object '"
            + script.m_objectTypeName + "' not supported...");
      else
        sqlScriptGroup.add(script);
    }

    return sqlScriptGroups;
  }

  /**
   * Parse the SQL constants declared in the specified SQL scripts.
   *
   * @param sqlScripts a collection of SQL scripts.
   *
   * @return a collection of <code>SQLConstant</code> objects.
   *
   * @throws IOException if a read access to a SQL script files failed
   *         for some reason.
   */
  protected Hashtable parseSQLScriptConstants(Collection sqlScripts)
    throws IOException {
    Hashtable sqlConstants = new Hashtable();

    for (Iterator iterator = sqlScripts.iterator() ; iterator.hasNext(); ) {
      SQLScript sqlScript = (SQLScript) iterator.next();

      System.out.println("Processing file " + sqlScript.m_filePathname);
      File file = new File(sqlScript.m_filePathname);
      FileReader reader = new FileReader(file);
      char[] buffer = new char[(int) file.length()];
      reader.read(buffer);
      reader.close();
      String sqlScriptContent = new String(buffer);

      Matcher matcher = m_patternSQLConstantDeclaration.matcher(sqlScriptContent);
      while (matcher.find()) {
        String constantName = matcher.group(1);
        String constantValue = matcher.group(2);

        SQLConstant sqlConstant = (SQLConstant) sqlConstants.get(constantName);
        if (sqlConstant == null) {
          sqlConstants.put(
              constantName,
              new SQLConstant(sqlScript, constantName, constantValue));
          System.out.println("  (defconstant " + constantName + " " + constantValue + ")");
        } else {
          if (sqlConstant.m_value.compareTo(constantValue) == 0)
            System.out.println("Warning: constant '" + constantName
                + "' is defined first in file '" + sqlConstant.m_sqlScript.m_filePathname
                + "' and in file '" + sqlScript.m_filePathname + "'!");
          else {
            System.out.println("Error: constant '" + constantName
                + "' declaration mismatch between file '" + sqlConstant.m_sqlScript.m_filePathname
                + "' and file '" + sqlScript.m_filePathname + "'!");
            System.exit(0);
          }
        }
      }
    }

    return sqlConstants;
  }

  /**
   * Parse the input stream and return a collection of SQL statements.
   *
   * @param input a stream containing SQL statements.
   * @param objectTypeName the type of database object that these SQL
   *        statements are responsible for creating.
   * @param sqlScript the SQL from which the SQL statements are parsed
   *        from.
   *
   * @return a collection of SQL statements.
   */
  protected abstract Collection<SQLStatement> parseSQLStatements(
      String input,
      String objectTypeName,
      SQLScript sqlScript);

  /**
   * Execute all the SQL statements declared in the SQL script files
   * provided to the SQL deployment manager.
   *
   * @throws IOException if a read access to a SQL script files failed
   *         for some reason.
   * @throws SQLException if the execution of an SQL statement of
   *         these script files failed.
   */
  public void process()
    throws IOException,
           MaximumExecutionAttemptException,
           SQLException,
           UndefinedRevisionNumberException {
    Connection rdbmsConnection = getRBDMSConnection();
    ArrayList sqlStatements = new ArrayList();
    Collection sqlStatementHistory = new Vector();

    for (int i = 0; i < m_objectTypeNameOrders.length; i++) {
      Collection _sqlScripts = (Collection) m_sqlScriptGroups.get(m_objectTypeNameOrders[i]);
      for (Iterator sqlScriptIterator = _sqlScripts.iterator();
           sqlScriptIterator.hasNext(); ) {
        SQLScript sqlScript = (SQLScript) sqlScriptIterator.next();

        if (((m_moduleNameFilter != null) &&
             (sqlScript.m_moduleName.compareTo(m_moduleNameFilter) != 0)) ||
            ((m_objectTypeFilter != null) &&
             (sqlScript.m_objectTypeName.compareTo(m_objectTypeFilter) != 0)))
          continue;

        System.out.println("Parsing " + sqlScript.m_filePathname + "...");

        // Read the content of the script file.
        //
        File file = new File(sqlScript.m_filePathname);
        FileReader reader = new FileReader(file);
        char[] buffer = new char[(int) file.length()];
        reader.read(buffer);
        reader.close();
        String sqlScriptContent = new String(buffer);

        // Parse the revision number stored in this SQL script.
        //
        if (m_revisionControlSystem != null ) {
          try {
            sqlScript.m_revisionNumber =
              m_revisionControlSystem.parseRevisionNumber(sqlScriptContent);
            System.out.println("  revision-number = " + sqlScript.m_revisionNumber);
          } catch (UndefinedRevisionNumberException exception) {
            System.out.println(
                "File " + sqlScript.m_filePathname + " has no revision");
            throw exception;
          }

          // Retrieve the revision number of the last deployment of this
          // SQL script.
          //
          PreparedStatement preparedStatement = rdbmsConnection.prepareStatement(
              "SELECT revision_number, deployment_time, deployment_status" +
              "  FROM revision_control" +
              "  WHERE module_name = ?" +
              "    AND object_type = ?");
          preparedStatement.setString(1, sqlScript.m_moduleName.toLowerCase());
          preparedStatement.setString(2, sqlScript.m_objectTypeName.toLowerCase());
          ResultSet resultSet = preparedStatement.executeQuery();
          if (resultSet.next()) {
            sqlScript.m_lastDeploymentRevisionNumber = resultSet.getInt("revision_number");
            sqlScript.m_lastDeploymentTime = new Date(resultSet.getLong("deployment_time"));
            sqlScript.m_lastDeployementStatus = resultSet.getInt("deployment_status");
            System.out.println("  last-deployment-revision-number = " + sqlScript.m_lastDeploymentRevisionNumber);
            System.out.println("  last-deployment-time = " + sqlScript.m_lastDeploymentTime);
            System.out.println("  last-deployment-status = " + sqlScript.m_lastDeployementStatus);
          }

          if (sqlScript.m_revisionNumber < sqlScript.m_lastDeploymentRevisionNumber) {
            System.out.println("Skipping " + sqlScript.m_filePathname + " as it is too old.");
            continue;
          } else if (sqlScript.m_revisionNumber == sqlScript.m_lastDeploymentRevisionNumber) {
            if (m_forceUpdate)
              System.out.println("Forcing update of " + sqlScript.m_filePathname);
            else {
              System.out.println("Skipping " + sqlScript.m_filePathname + " as it has been already deployed");
              continue;
            }
          }
        }

        // Replace every constant used in the SQL script by its value.
        //
        for (Iterator sqlConstantIterator = m_sqlScriptConstants.values().iterator();
        sqlConstantIterator.hasNext(); ) {
          SQLConstant sqlConstant = (SQLConstant) sqlConstantIterator.next();
          sqlScriptContent = sqlScriptContent.replace(
              sqlConstant.m_name,
              sqlConstant.m_value);
        }

        // Parse every SQL staments defined in the SQL script and
        // store them in some collections.
        //
        Iterator sqlStatementIterator = parseSQLStatements(
            sqlScriptContent,
            sqlScript.m_objectTypeName,
            sqlScript).iterator();
        
        while (sqlStatementIterator.hasNext()) {
          SQLStatement sqlStatement = (SQLStatement) sqlStatementIterator.next();
          sqlStatements.add(sqlStatement);
          sqlStatementHistory.add(sqlStatement);
        }
      }
    }

    // Execute the SQL statements in their given order, until every
    // SQL statement passes or completely fails.
    //
    while (sqlStatements.size() > 0) {
      Iterator iterator = sqlStatements.iterator();
      while (iterator.hasNext()) {
        SQLStatement sqlStatement = (SQLStatement) iterator.next();

        System.out.println(
            "Processing " + sqlStatement.m_sqlScript.m_moduleName
            + " " + sqlStatement.m_sqlScript.m_objectTypeName);

        if (processSQLStatement(sqlStatement)) {
          sqlStatement.m_executionStatus = SQLStatement.EXECUTION_STATUS_SUCCEEDED;
          iterator.remove();
        } else {
          sqlStatement.m_attemptCount++;
          if (sqlStatement.m_attemptCount > m_maximumAttemptCount) {
            sqlStatement.m_executionStatus = SQLStatement.EXECUTION_STATUS_FAILED;
            iterator.remove();
          }
        }
      }
    }

    // Check every SQL statement execution result and update
    // consequently the state of the SQL script it depends.
    //
    if (m_revisionControlSystem != null) {
      Iterator sqlStatementIterator = sqlStatementHistory.iterator();
      while (sqlStatementIterator.hasNext()) {
        SQLStatement sqlStatement = (SQLStatement) sqlStatementIterator.next();
        if (sqlStatement.m_sqlScript.m_executionStatus == SQLScript.EXECUTION_STATUS_NONE)
          sqlStatement.m_sqlScript.m_executionStatus = sqlStatement.m_executionStatus;
        else if (sqlStatement.m_executionStatus == SQLStatement.EXECUTION_STATUS_FAILED)
          sqlStatement.m_sqlScript.m_executionStatus = SQLScript.EXECUTION_STATUS_FAILED;
      }

      Iterator sqlScriptIterator = m_sqlScripts.iterator();
      while (sqlScriptIterator.hasNext()) {
        SQLScript sqlScript = (SQLScript) sqlScriptIterator.next();
        if (sqlScript.m_executionStatus != SQLScript.EXECUTION_STATUS_NONE) {
          if (sqlScript.m_lastDeploymentTime != null) {
            PreparedStatement preparedStatement = rdbmsConnection.prepareStatement(
                "UPDATE revision_control" +
                "  SET revision_number = ?," +
                "      deployment_time = ?," +
                "      deployment_status = ?" +
                "  WHERE module_name = ?" +
                "    AND object_type = ?");
            preparedStatement.setInt(1, sqlScript.m_revisionNumber);
            preparedStatement.setLong(2, new Date().getTime());
            preparedStatement.setInt(3, sqlScript.m_executionStatus);
            preparedStatement.setString(4, sqlScript.m_moduleName);
            preparedStatement.setString(5, sqlScript.m_objectTypeName);
            preparedStatement.execute();
          } else {
            PreparedStatement preparedStatement = rdbmsConnection.prepareStatement(
                "INSERT INTO revision_control(module_name, object_type, revision_number, deployment_time, deployment_status)" +
                " VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setString(1, sqlScript.m_moduleName);
            preparedStatement.setString(2, sqlScript.m_objectTypeName);
            preparedStatement.setInt(3, sqlScript.m_revisionNumber);
            preparedStatement.setLong(4, new Date().getTime());
            preparedStatement.setInt(5, sqlScript.m_executionStatus);
            preparedStatement.execute();
          }
        }
      }
    }

    rdbmsConnection.close();
  }

  /**
   * Execute the specified SQL statement against the relation database
   * management system.
   *
   * @param sqlStatement a SQL statement to be executed.
   *
   * @throws SQLException if the execution of the SQL statement
   *         failed.
   */
  protected abstract boolean processSQLStatement(SQLStatement sqlStatement)
    throws SQLException;

  /**
   * Specify whether database objects must be updated even if their
   * current revision correspond to the last deployed.
   *
   * @param forceUpdate <code>true</code> if the database objects must
   *        be updated even if their current revision correspond to
   *        the last deployed; <code>false</code> otherwise.
   */
  public void setForceUpdate(boolean forceUpdate) {
    m_forceUpdate = forceUpdate;
  }

  /**
   * Define the maximum numbers of attempt executing a SQL statement
   * that might fail because of dependency on some database objects
   * that have not been already created.
   *
   * @param maximumAttemptCount the maximum numbers of attempt before
   *        a fatal error is raised.
   */
  public void setMaximumAttemptCount(int maximumAttemptCount) {
    m_maximumAttemptCount = maximumAttemptCount;
  }

  /**
   * Define the name of the module to be only processed.
   *
   * @param moduleName name of the module to be only processed if
   *        defined.
   */
  public void setModuleNameFilter(String moduleName) {
    m_moduleNameFilter = moduleName;
  }

  /**
   * Define the type of database objects to be only processed.
   *
   * @param objectType type of database objects to be only processed
   *        if defined.
   */
  public void setObjectTypeFilter(String objectType) {
    m_objectTypeFilter = objectType;
  }

  /**
   * Define the revision control system that is used to manage the SQL
   * script files and to store the identification of a revision within
   * these files.
   *
   * @param revisionControlSystem a revision control system.
   */
  public void setRevisionControlSystem(RevisionControlSystem revisionControlSystem) {
    m_revisionControlSystem = revisionControlSystem;
  }

  /**
   *
   * @param sqlScripts a collection of SQL script files that contain
   *        SQL statements to be executed against a relational
   *        database management system.
   */
  public void setSQLScripts(Collection sqlScripts)
    throws IOException {
    m_sqlScripts = sqlScripts;
    m_sqlScriptGroups = groupSQLScripts(sqlScripts);
    m_sqlScriptConstants = parseSQLScriptConstants(sqlScripts);
  }

  /**
   * Indicate whether to display whole information while processing
   * SQL scripts.
   *
   * @param enabled <code>true</code> if whole information must be
   *        displayed while processing SQL scripts; <code>false</code>
   *        otherwiser.
   */
  public void setVerbose(boolean enabled) {
    m_verbose_enabled = enabled;
  }
}
