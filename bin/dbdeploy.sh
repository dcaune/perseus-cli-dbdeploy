#!/bin/sh
#
# Copyright (C) 1998-2006 Majormode.  All rights reserved.
#
# This software is the confidential and proprietary information of
# Majormode or one of its subsidiaries.  You shall not disclose this
# confidential information and shall use it only in accordance with
# the terms of the license agreement or other  applicable agreement
# you entered into with Majormode. 
#  
# MAJORMODE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
# SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
# BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  MAJORMODE
# SHALL NOT BE LIABLE FOR ANY LOSSES OR DAMAGES SUFFERED BY LICENSEE
# AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
# ITS DERIVATIVES.

# Set environment variables.
export DB_DEPLOY_HOME=.

export RDBMS_HOSTNAME=localhost
export RDBMS_DATABASE=playlive
export RDBMS_USERNAME=dbo
export RDBMS_PASSWORD=lythikimanh

export RDBMS=postgresql
export JDBC_DRIVER=org.postgresql.Driver
export JDBC_URL=jdbc:postgresql://$RDBMS_HOSTNAME/$RDBMS_DATABASE

# Set the Java class path.
for i in `find $DB_DEPLOY_HOME/lib -name "*.jar"` ; do
  CLASSPATH=$CLASSPATH:$i
done

for i in `find $DB_DEPLOY_HOME/build -name "*.jar"` ; do
  CLASSPATH=$CLASSPATH:$i
done

# Execute the Database Deployment Tool.
JAVA_ARGS="-Xss8192k -classpath $CLASSPATH"
java $JAVA_ARGS com.majormode.tool.dbdeploy.Main \
    --rdbms $RDBMS \
    --jdbc-driver $JDBC_DRIVER \
    --jdbc-url $JDBC_URL \
    --username $RDBMS_USERNAME \
    --password $RDBMS_PASSWORD \
    $@
