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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostgreSQLDeploymentManager extends SQLDeploymentManager {
  /**
   * Regular expression that matches the definition of a PL/pgSQL or
   * SQL function.
   * [TODO] This regular expression is a hack that doesn't cover all
   * the possible use cases.
   */
  protected static final String PATTERN_SQL_FUNCTION_QUERY =
    "(CREATE([^$])+[$][$]([^$][$]{0,1})+[$][$]\\s+LANGUAGE\\s+(PLPGSQL|SQL));";

  /**
   * Regular expression that matches standard SQL query delimited
   * from another standard SQL query by the character <code>;</code>.
   */
  protected static final String PATTERN_SQL_QUERY = "([^;]*);";

  /**
   * Regular expression that matches command used to set a runtime
   * parameter.
   */
  protected static final String PATTERN_RUNTIME_PARAMETER_COMMAND = "(^SET\\s+(SESSION|LOCAL|)\\s*(?<name>\\w+)\\s+(TO|=)[^;]*;)";

  /**
   * Regular expression that matches PostgreSQL exception raised when
   * a database object A is attempted to be created while one other
   * database object B, the object A depends on, is not yet created.
   *
   * PostgreSQL raises an exception such as the following one:
   * <pre>
   * Exception in thread "main" org.postgresql.util.PSQLException: ERROR: function get_job_hierarchy() does not exist
   * </pre>
   */
  protected static final String PATTERN_DEPENDENCY_EXCEPION = "does not exist";

  /**
   * List of the database objects, which PostgreSQL supports, declared
   * in their dependency order.
   */
  protected static final String[] OBJECT_TYPE_NAME_ORDERS = {
      "constant",
      "type",
      "sequence",
      "table",
      "view",
      "constraint",
      "index",
      "function",
      "job",
      "trigger",
      "dataset"
  };

  /**
   * Compiled representation of the regulat expression that maches a
   * command that sets a runtime parameter.
   */
  protected Pattern m_patternRuntimeParameterCommand = Pattern.compile(PATTERN_RUNTIME_PARAMETER_COMMAND, Pattern.CASE_INSENSITIVE);

  /**
   * Compiled representation of the regular expression that matches a
   * standard SQL query.
   */
  protected Pattern m_patternSQLQuery = Pattern.compile(PATTERN_SQL_QUERY, Pattern.CASE_INSENSITIVE);

  /**
   * Compile representation of the regulare expression that matches a
   * definition of a PL/pgSQL function.
   */
  protected Pattern m_patternSQLFunctionQuery = Pattern.compile(PATTERN_SQL_FUNCTION_QUERY, Pattern.CASE_INSENSITIVE);

  /**
   * Build a new instance of a SQL deployment manager for a PostgreSQL
   * relational database management system.
   *
   * @param jdbcDriver Java class name of the JDBC driver that is to
   *        be used to connect to the PostgreSQL relational database
   *        management system.
   * @param jdbcUrl database URL of the form
   *        <code>jdbc:subprotocol:subname</code> identifying the
   *        PostgreSQL relational database management system.
   * @param accountUsername database user on whose behalf the
   *        connection is being made to the PostgreSQL relational
   *        database management system.
   * @param accountPassword password of the user on whose behalf the
   *        connection is being made to the PostgreSQL relational
   *        database management system.
   *
   * @throws ClassNotFoundException if the Java class name of the JDBC
   *         driver has not been found.
   * @throws IOException if a read access to a SQL script files has
   *         failed for some reason.
   */
  public PostgreSQLDeploymentManager(
      String jdbcDriver,
      String jdbcUrl,
      String accountUsername,
      String accountPassword)
    throws ClassNotFoundException,
           IOException {
    super(jdbcDriver,
          jdbcUrl,
          accountUsername,
          accountPassword);
    m_objectTypeNameOrders = OBJECT_TYPE_NAME_ORDERS;
  }

  /**
   * Parse the input stream and return a collection of SQL statements.
   *
   * @param input a stream containing SQL statements.
   * @param objectTypeName the type of database object that these SQL
   *        statements are responsible for creating.
   * @param sqlScript the SQL script which the SQL statements are
   *        parsed from.
   *
   * @return a collection of SQL statements.
   */
  protected Collection<SQLStatement> parseSQLStatements(
      String input,
      String objectTypeName,
      SQLScript sqlScript) {
    HashMap<String, String> runtimeParameterCommands = new HashMap<>();
    Vector<SQLStatement> statements = new Vector<>();

    // Remove all the comment from the script file's content.
    //
    Matcher matcher = m_patternSQLComment.matcher(input);
    input = matcher.replaceAll("").trim();

    // Parse each SQL statement.
    //
    matcher = (objectTypeName.compareTo("function") == 0)
      ? m_patternSQLFunctionQuery.matcher(input)
      : m_patternSQLQuery.matcher(input);

    while (matcher.find()) {
      String sqlExpression = matcher.group(1).trim();
      System.out.println("SQL expression parsed = " + sqlExpression);

      Matcher runtimeParameterCommandMatcher = m_patternRuntimeParameterCommand.matcher(sqlExpression);
      System.out.println("Is runtime parameter command? " + runtimeParameterCommandMatcher.find());

      if (runtimeParameterCommandMatcher.find()) {
        String parameterName = matcher.group("name");
        String command = matcher.group(1).trim();
        runtimeParameterCommands.put(parameterName, command);
      } else if (sqlExpression.length() > 0) {
        statements.add(new SQLStatement(sqlExpression, sqlScript, new ArrayList(runtimeParameterCommands.values())));
      }
    }

    return statements;
  }

  /**
   * Execute the specified SQL statement against the PostgreSQL
   * relation database management system.
   *
   * @param sqlStatement a SQL statement to be executed.
   *
   * @return <code>true</code> if the SQL statement execution
   *         succeeded; <code>false</code> otherwise.
   *
   * @throws SQLException if the execution of the SQL statement
   *         failed.
   */
  protected boolean processSQLStatement(SQLStatement sqlStatement)
    throws SQLException {
    Connection rdbmsConnection = getRBDMSConnection();

    try {
      if (m_verbose_enabled) {
        System.out.println(sqlStatement.m_sqlExpression);
      }

      if (sqlStatement.m_runtimeParameterCommands != null) {
        for (Iterator iterator = sqlStatement.m_runtimeParameterCommands.iterator() ; iterator.hasNext(); ) {
          String runtimeParameterCommand = (String) iterator.next();  
          rdbmsConnection.createStatement().execute(runtimeParameterCommand);
        }
      }

      rdbmsConnection.createStatement().execute(sqlStatement.m_sqlExpression);

      if (m_verbose_enabled) {
        System.out.println("Success.\n");
      }
    } catch (SQLException exception) {
      if (m_verbose_enabled) {
        System.out.println("Failure: " + exception.getSQLState() + " " + exception.getMessage() + "\n");
      }

      String sqlState = exception.getSQLState();

      // 42703 - ERROR: column "..." referenced in foreign key
      //         constraint does not exist
      // 42710 - ERROR: constraint "..." for relation "..." already
      //         exists
      // 42883 - ERROR: function ... does not exist
      // 42P01 - ERROR: relation "..." does not exist
      // 42P07 - ERROR: relation "..." already exists
      // 42P16 - ERROR: multiple primary keys for table "..." are not
      //         allowed
      if ("42883".compareTo(sqlState) == 0) {
        return false;
      } else if ("42P01".compareTo(sqlState) == 0) {
        // Handle case when a table inherits from one other that has
        // not been created yet.  We should attempt another time
        // when all the tables have been created.
        if ((("table".compareTo(sqlStatement.m_sqlScript.m_objectTypeName) != 0) &&
              ("constraint".compareTo(sqlStatement.m_sqlScript.m_objectTypeName) != 0)) ||
            (sqlStatement.m_attemptCount > 0)) {
          throw exception;
        }
      }
      else if ("42703".compareTo(sqlState) == 0) {
        throw exception;
      }
    } finally {
      rdbmsConnection.close();
    }

    return true;
  }
}
