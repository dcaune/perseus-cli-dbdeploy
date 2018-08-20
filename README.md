# Database Deployment Tool

## What is the Database Deployment Tool?

The Database Deployment Tool is a command line Java application that   collects a series of SQL script files, located in a given directory and sub-directories, parses the SQL statements declared in these script files, orders them according to database object dependencies, and executes them against a specified relational database management system (RDBMS).  Each script file can contain a sequence of standard SQL or proprietary SQL (such as PL/SQL, PL/pgSQL, Transact-SQL, etc.).

The tool accepts a simple list of arguments:

* `rdbms`: name of the relational database management system (RDBMS)
    that the SQL scripts might be specific to;

* `jdbc-driver`: name of the Java class implementing the RDBMS driver;

* `jdbc-url`: URL specific to the RDBMS driver, which defines     connection properties to the RDBMS;

* `module-name`: name of the module which database objects have to be     created or updated.  If not specified, all existing modules are     creating or updating;

* `object-type`: type of database objects that have to be created or     updated.  If not specified, all database objects are creating or updating.

* `maximum-attempt-count`: maximal number of attempts to executes a given SQL statement that fails, before raising a fatal error. This argument is optional.  The default value is `3`.

* `password`: password associated to the account;

* `path`: absolute path of the directory where the tool starts scanning recursively for SQL script files.  This argument is optional.  The default value is the working directory;

* `username`: username of an account to connect to the RDBMS.

## Naming Convention

The current version of the Database Deployment Tool expects that a given SQL script file groups SQL statements responsible for creating or updating database objects of a same type, such as sequence, table, index, function, and so on.  It does not embed any SQL grammar analyzer, and so it cannot determine the type of a database object a SQL statement creates or updates.

A given SQL script file might contain SQL statements responsible for creating or updating database objects for a particular domain, activity, or business logic, also called module.  Therefore a SQL script file is expected to contain SQL statements that create or update database objects of a same type for a particular module.

The name of a SQL script file must respect the following naming convention:

``` text
create_<module-name>_<object-type>.sql
```

where:

* `<module-name>` refers to the name of a module for which the SQL script creates or updates database objects;

* `<object-type>` refers to the type of database objects this script creates or updates, such as "constraint", "function", "index", "job", "package", "procedure", "sequence", "table", "trigger", "type", "view", "constant".  The list of object types might be RDBMS dependent.

Once the SQL scripts collected, the Database Deployment Tool orders their execution according to the database object types these scripts are said to manage.  The order choosen reflects the possible dependency of database objects on each other:

* type*

* table*

* constraint*

* function

* index*

* sequence*

* package

* job

* trigger

(* only if the option "create" has been set)

## Constant Declaration and Usage

The Database Deployment Tool allows to declare literal constants in any SQL script file.  Constants can be declared anywhere in a SQL script file.  A constant is declared by the following expression:

``` sql
    --(defconstant +<constant-name>+ <constant-value>)
```

`<constant-name>` and `<constant-value>` correspond to alphabetical and numerical strings, which mustn’t include any space character; `<constant-name>` mustn’t include any "+" character.  Example:

``` sql
    --(defconstant +western-chess-white-color+ 1)
    --(defconstant +western-chess-black-color+ 0)
```

The Database Deployment Tool parses constant declarations from every SQL script files before executing any SQL statements defined in these scripts files.  If a constant is defined more than once, the Database Deployment Tool checks whether the constant’s value stays the same; if so, the tool just warns the user, otherwise it raises an error and stops.

When executing an SQL statement, the Database Deployment Tool replaces in the SQL code constant names by their respective values.

## SQL Comment

SQL script files can include comments such as:

* comment that starts with the characters `--` and follows by any characters until the end of the line;

* C-like comment that starts with `/*` and ends with `*/`.

The Database Deployment Tools cleanses SQL statements from comments when executing these statements against the specified RDBMS. However special comment which refers to a hint (cf. Oracle hint, a special comment starting with /*+) are not removed from a SQL statement, indeed.

## Database Object Creation

This task uses a naive approach to create the database objects. Each time it executes a script file, this task checks then from database wether or not some database objects are invalid.  This may occur when these database objects depend on some others not yet created by the task.  If so the task add the script file in a special collection to remind this script file must be executed one time more later, after that all the other script files are passed.

## Requirements

The Database Deploy Ant Task is a Java application that requires the Java 2 Standard Edition SDK 1.5 or higher (http://java.sun.com). Make sure that you download the SDK and not the JRE!

## Compilation

This step is not really needed actually, only if you need to change some parts of the code.

Copy the source distribution file, this that better fits your operating system, into a directory and uncompress it from this directory.  On a Linux platform, you uncompress the TAR GZ source distribution file by the following command:

``` shell
tar xvfz dbdeploy-x.y.z-b-src.tar.gz
```

On a Windows platform, you may use the WinZip tool to uncompress the ZIP source distribution file.

To compile properly the Database Ant Task for your own project, you must setup some properties declared in the "build.properties" file:

* `url.repository.library`: Unified Resource Locator of the repository where the library archives, which the database Ant task library depends on, are stored.

## Installation

Copy the binary distribution file, this that better fits your operating system, into a directory and uncompress it from this directory.

On a Linux platform, you uncompress the TAR GZ binary distribution file by the following command:

``` shell
tar xvfz database-ant-task-x.y.z-b-bin.tar.gz
```

On a Windows platform, you may use the WinZip tool to uncompress the ZIP binary distribution file.

Once the archive decompressed, copy the JAR file `dbdeploy-ant-task.jar` located in the `lib` directory to the `CLASSPATH` of the Ant tool.

## Troubleshooting

The Database Deploy Ant Task may crash on Solaris system, while it correctly works on Linux system:

``` java
java.lang.StackOverflowError
  at java.util.regex.Pattern$NotSingle.match(Pattern.java:2956)
  at java.util.regex.Pattern$Branch.match(Pattern.java:3860)
  at java.util.regex.Pattern$GroupHead.match(Pattern.java:3900)
  at java.util.regex.Pattern$Loop.match(Pattern.java:4028)
  at java.util.regex.Pattern$GroupTail.match(Pattern.java:3959)
  at java.util.regex.Pattern$NotSingle.match(Pattern.java:2956)
  at java.util.regex.Pattern$Branch.match(Pattern.java:3860)
  at java.util.regex.Pattern$GroupHead.match(Pattern.java:3900)
  at java.util.regex.Pattern$Loop.match(Pattern.java:4028)
  (...)
```

The exception `StackOverflowError` occurs when we try to match the SQL
  script file content on the regular expression:

``` java
protected static final String PATTERN_SQL_COMMENT =
  "(--([\\n]|([^#][^\\n]*[\\n])))|(/\\*([^/]|([^*]/))*\\*/)";
protected Pattern m_patternSQLComment =
  Pattern.compile(PATTERN_SQL_COMMENT);

    Matcher matcher = m_patternSQLComment.matcher(content);
```

The problem comes from the initial configuration of the JVM installed on Solaris.  The default stack size seems to be too small. The JVM must be started with the option `-Xss8192k`.
