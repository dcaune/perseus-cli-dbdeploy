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

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
  protected static final String LONG_FLAG_OPTION_FORCE_UPDATE = "force-update";
  protected static final String LONG_FLAG_OPTION_JDBC_DRIVER = "jdbc-driver";
  protected static final String LONG_FLAG_OPTION_JDBC_URL = "jdbc-url";
  protected static final String LONG_FLAG_OPTION_MAXIMUM_ATTEMPT_COUNT = "maximum-attempt-count";
  protected static final String LONG_FLAG_OPTION_MODULE_NAME = "module-name";
  protected static final String LONG_FLAG_OPTION_OBJECT_TYPE = "object-type";
  protected static final String LONG_FLAG_OPTION_PASSWORD = "password";
  protected static final String LONG_FLAG_OPTION_PATH = "path";
  protected static final String LONG_FLAG_OPTION_RDBMS = "rdbms";
  protected static final String LONG_FLAG_OPTION_REVISION_CONTROL_SYSTEM = "revision-control-system";
  protected static final String LONG_FLAG_OPTION_USERNAME = "username";
  protected static final String LONG_FLAG_OPTION_VERBOSE = "verbose";

  protected static final char SHORT_FLAG_OPTION_FORCE_UPDATE = 'f';
  protected static final char SHORT_FLAG_OPTION_JDBC_DRIVER = 'r';
  protected static final char SHORT_FLAG_OPTION_JDBC_URL = 'l';
  protected static final char SHORT_FLAG_OPTION_MAXIMUM_ATTEMPT_COUNT = 'a';
  protected static final char SHORT_FLAG_OPTION_MODULE_NAME = 'm';
  protected static final char SHORT_FLAG_OPTION_OBJECT_TYPE = 't';
  protected static final char SHORT_FLAG_OPTION_PASSWORD = 'p';
  protected static final char SHORT_FLAG_OPTION_PATH = 'd';
  protected static final char SHORT_FLAG_OPTION_RDBMS = 'b';
  protected static final char SHORT_FLAG_OPTION_REVISION_CONTROL_SYSTEM = 's';
  protected static final char SHORT_FLAG_OPTION_USERNAME = 'u';
  protected static final char SHORT_FLAG_OPTION_VERBOSE = 'v';

  /**
   * Regular expression that matches the name of a script file. This name must
   * respect the following naming convention: <code>
   * create_<object-group>_<object-type>.sql</code>.
   */
  protected static final String PATTERN_SCRIPT_FILE_NAME =
    "create_([a-zA-Z0-9\\-]+)_([a-zA-Z0-9\\-]+)\\.sql";

  /**
   * Compiled representation of the regular expression that matches the name of
   * a script file.
   */
  protected static Pattern m_patternScriptFileName =
    Pattern.compile(PATTERN_SCRIPT_FILE_NAME);
  
  /**
   * Execute the database deployment tool.
   * 
   * @param arguments a list of arguments that the tool supports:
   *        <ul>
   *        <li><code>--jdbc_driver</code>: defines the Java class
   *         name of the JDBC driver that is to be used to connect to
   *         the relational database management system.</li>
   *        <li><code>--jdbc_url</code>: defines a database URL of the
   *         form <code>jdbc:subprotocol:subname</code> identifying
   *         the relational database management system.</li>
   *        <li><code>--maximum_attempt_count</code>: specifies the
   *         maximum number of attempts in executing an SQL statement
   *         that might temporarily fail for dependency reason.</li>
   *        <li><code>--module-name</code>: specifies the module which
   *         database objects have to be created or updated.  If not
   *         specified, all existing modules are creating or
   *         updating.</li>
   *        <li><code>--object-type</code>: specifies the type of
   *         database objects that have to be created or updated.  If
   *         not specified, all database objects are creating or
   *         updating.</li>
   *        <li><code>--password</code>: defines the password of the
   *         user on whose behalf the connection is being made to the
   *         relational database management system.</li>
   *        <li><code>--path</code>: specifies the path where to find
   *         SQL script files.  If not defined, the path corresponds
   *         to the working directory.</li>
   *        <li><code>--rdbms</code>: defines the name the relational
   *         database management system which the SQL statements might
   *         be specific to.</li>
   *        <li><code>--username</code>: defines database user on
   *         whose behalf the connection is being made to the
   *         relational database management system.</li>
   *        </ul>
   * 
   * @throws Exception if an unexpected exception occurs.
   */
  public static void main(String[] arguments)
    throws Exception {
    SimpleJSAP jsap = new SimpleJSAP( 
        "dbdeploy "
        + Main.class.getPackage().getSpecificationVersion() + "."
        + Main.class.getPackage().getImplementationVersion()
        + "\nCopyright (C) 1998-2008 Majormode.  All rights reserved.\n", 
        "Deploy database objects on a relational database management system (RDBMS).",        
        new com.martiansoftware.jsap.Parameter[] {
            new Switch(LONG_FLAG_OPTION_FORCE_UPDATE, SHORT_FLAG_OPTION_FORCE_UPDATE, LONG_FLAG_OPTION_FORCE_UPDATE,
                "Specifies that database objects must be updated even if their current revision correspond to the last deployed."),
            new FlaggedOption(LONG_FLAG_OPTION_JDBC_DRIVER, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, SHORT_FLAG_OPTION_JDBC_DRIVER, LONG_FLAG_OPTION_JDBC_DRIVER,
                "Defines the Java class name of the JDBC driver that is to be used to connect to the relational database management system."),
            new FlaggedOption(LONG_FLAG_OPTION_JDBC_URL, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, SHORT_FLAG_OPTION_JDBC_URL, LONG_FLAG_OPTION_JDBC_URL,
                "Defines a database URL of the form jdbc:subprotocol:subname identifying the relational database management system."),
            new FlaggedOption(LONG_FLAG_OPTION_MAXIMUM_ATTEMPT_COUNT, JSAP.INTEGER_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, SHORT_FLAG_OPTION_MAXIMUM_ATTEMPT_COUNT, LONG_FLAG_OPTION_MAXIMUM_ATTEMPT_COUNT,
                "Specifies the maximum number of attempts in executing an SQL statement that might temporarily fail for dependency reason.  The default value is 3."),
            new FlaggedOption(LONG_FLAG_OPTION_MODULE_NAME, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, SHORT_FLAG_OPTION_MODULE_NAME, LONG_FLAG_OPTION_MODULE_NAME,
                "Specifies the module which database objects have to be created or updated.  If not specified, all existing modules are creating or updating."),
            new FlaggedOption(LONG_FLAG_OPTION_OBJECT_TYPE, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, SHORT_FLAG_OPTION_OBJECT_TYPE, LONG_FLAG_OPTION_OBJECT_TYPE,
                "Specifies the type of database objects that have to be created or updated.  If not specified, all database objects are creating or updating."),
            new FlaggedOption(LONG_FLAG_OPTION_PASSWORD, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, SHORT_FLAG_OPTION_PASSWORD, LONG_FLAG_OPTION_PASSWORD,
                "Defines the password of the user on whose behalf the connection is being made to the relational database management system."),
            new FlaggedOption(LONG_FLAG_OPTION_PATH, JSAP.STRING_PARSER, ".", JSAP.NOT_REQUIRED, SHORT_FLAG_OPTION_PATH, LONG_FLAG_OPTION_PATH,
            	"Specifies the path where to find SQL script files.  If not defined, the path corresponds to the working directory."),
            new FlaggedOption(LONG_FLAG_OPTION_RDBMS, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, SHORT_FLAG_OPTION_RDBMS, LONG_FLAG_OPTION_RDBMS,
                "Defines the name the relational database management system which the SQL statements might be specific to."),
            new FlaggedOption(LONG_FLAG_OPTION_REVISION_CONTROL_SYSTEM, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, SHORT_FLAG_OPTION_REVISION_CONTROL_SYSTEM, LONG_FLAG_OPTION_REVISION_CONTROL_SYSTEM,
                "Specifies the name of the revision control system that stores the identification of a revision within source files."),
            new FlaggedOption(LONG_FLAG_OPTION_USERNAME, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, SHORT_FLAG_OPTION_USERNAME, LONG_FLAG_OPTION_USERNAME,
                "Defines database user on whose behalf the connection is being made to the relational database management system."),
            new Switch(LONG_FLAG_OPTION_VERBOSE, SHORT_FLAG_OPTION_VERBOSE, LONG_FLAG_OPTION_VERBOSE,
            	"Displays whole information while processing SQL scripts.")
        }
    );
    
    JSAPResult configuration = jsap.parse(arguments);
    if (jsap.messagePrinted())
      System.exit(1);

    // Seeks any SQL script files from the specified path, or the
    // working directory, and from all the subdirectories.
    //
    System.out.println("Collecting script files...");
    Collection sqlScripts = collectSQLScripts(
        new File(configuration.getString(LONG_FLAG_OPTION_PATH)),
        new Vector());

    //
    //
    SQLDeploymentManager sqlDeploymentManager =
      SQLDeploymentManagerFactory.getSQLDeploymentManager(
          configuration.getString(LONG_FLAG_OPTION_RDBMS),
          configuration.getString(LONG_FLAG_OPTION_JDBC_DRIVER),
          configuration.getString(LONG_FLAG_OPTION_JDBC_URL),
          configuration.getString(LONG_FLAG_OPTION_USERNAME),
          configuration.getString(LONG_FLAG_OPTION_PASSWORD));
    
    if (configuration.contains(LONG_FLAG_OPTION_MAXIMUM_ATTEMPT_COUNT))
      sqlDeploymentManager.setMaximumAttemptCount(
          configuration.getInt(LONG_FLAG_OPTION_MAXIMUM_ATTEMPT_COUNT));
    
    if (configuration.contains(LONG_FLAG_OPTION_REVISION_CONTROL_SYSTEM))
      sqlDeploymentManager.setRevisionControlSystem(
          RevisionControlSystemFactory.getRevisionControlSystem(
              configuration.getString(LONG_FLAG_OPTION_REVISION_CONTROL_SYSTEM)));
    
    if (configuration.getBoolean(LONG_FLAG_OPTION_FORCE_UPDATE))
      sqlDeploymentManager.setForceUpdate(true);
    
    if (configuration.getBoolean(LONG_FLAG_OPTION_VERBOSE))
        sqlDeploymentManager.setVerbose(true);
    
    sqlDeploymentManager.setSQLScripts(sqlScripts);
    
    String moduleName = configuration.getString(LONG_FLAG_OPTION_MODULE_NAME);
    if (moduleName != null)
      sqlDeploymentManager.setModuleNameFilter(moduleName);
    
    String objectType = configuration.getString(LONG_FLAG_OPTION_OBJECT_TYPE);
    if (objectType != null)
      sqlDeploymentManager.setObjectTypeFilter(objectType);

    sqlDeploymentManager.process();
  }

  /**
   * Collect the SQL script files located in the given directory and
   * subdirectories.
   *
   * @param directory directory where to find the SQL script files.
   * @param scripts collection used to insert the references of the
   *        SQL script files found in this directory and the
   *        subdirectories.
   *
   * @return the specified collection where the collected scripts have
   *         been inserted in.
   */
  static protected Collection collectSQLScripts(
      File directory,
      Collection scripts) {
    File[] files = directory.listFiles();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      
      if (file.isDirectory())
        collectSQLScripts(file, scripts);
      else {
        Matcher matcher = m_patternScriptFileName.matcher(file.getName());
        if (matcher.matches())
          scripts.add(new SQLScript(
              matcher.group(2).toLowerCase(),
              matcher.group(1),
              file.getPath()));
      }
    }
    
    return scripts;
  }  
}