@echo off

:: Copyright (C) 1998-2006 Majormode.  All rights reserved.
::
:: This software is the confidential and proprietary information of
:: Majormode or one of its subsidiaries.  You shall not disclose this
:: confidential information and shall use it only in accordance with
:: the terms of the license agreement or other  applicable agreement
:: you entered into with Majormode. 
::  
:: MAJORMODE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
:: SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
:: BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
:: FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  MAJORMODE
:: SHALL NOT BE LIABLE FOR ANY LOSSES OR DAMAGES SUFFERED BY LICENSEE
:: AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
:: ITS DERIVATIVES.

:: Set the environment variables.
SET RDBMS_HOSTNAME=apollon
SET RDBMS_DATABASE=apollon
SET RDBMS_USERNAME=dbo
SET RDBMS_PASSWORD=lythikimanh

SET RDBMS=postgresql
SET JDBC_DRIVER=org.postgresql.Driver
SET JDBC_URL=jdbc:postgresql://%RDBMS_HOSTNAME%/%RDBMS_DATABASE%

SET DB_DEPLOY_HOME=c:\bin\dbdeploy
SET LOCAL_CLASSPATH="%DB_DEPLOY_HOME%\lib\dbdeploy.jar"
SET LOCAL_CLASSPATH=%LOCAL_CLASSPATH%;"%DB_DEPLOY_HOME%\lib\postgresql-8.2-507.jdbc4.jar"
SET LOCAL_CLASSPATH=%LOCAL_CLASSPATH%;"%DB_DEPLOY_HOME%\lib\jsap-2.1.jar"

java -Xss8192k -classpath "%LOCAL_CLASSPATH%" com.majormode.tool.dbdeploy.Main --rdbms %RDBMS% --jdbc-driver %JDBC_DRIVER% --jdbc-url %JDBC_URL% --username %RDBMS_USERNAME% --password %RDBMS_PASSWORD% %*
