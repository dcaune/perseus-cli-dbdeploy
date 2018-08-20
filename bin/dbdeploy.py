#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2010 Majormode.  All rights reserved.
#
# This software is the confidential and proprietary information of
# Majormode or one of its subsidiaries.  You shall not disclose this
# confidential information and shall use it only in accordance with
# the terms of the license agreement or other applicable agreement you
# entered into with Majormode.
#
# MAJORMODE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
# SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
# BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  MAJORMODE
# SHALL NOT BE LIABLE FOR ANY LOSSES OR DAMAGES SUFFERED BY LICENSEE
# AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
# DERIVATIVES.
#
# @version $Revision$

import argparse
import fnmatch
import os
import subprocess


def build_classpath(directory_name):
    path = os.path.join(script_root_path, directory_name)
    try:
        return [ os.path.join(path, file_name)
            for file_name in fnmatch.filter(os.listdir(path), '*.jar') ]
    except: # No such directory
        return []


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Automatic database deployment using SQL scripts of the project.')
    parser.add_argument('--hostname', default='localhost')
    parser.add_argument('--database', required=True)
    parser.add_argument('--username', required=True)
    parser.add_argument('--password', required=False)
    parser.add_argument('--rdbms', default='postgresql')
    parser.add_argument('--jdbc_driver', default='org.postgresql.Driver')
    parser.add_argument('--jdbc_url', default='jdbc:postgresql://%(hostname)s/%(database)s')
    parser.add_argument('--java_args', default='-Xss8192k')
    parser.add_argument('--debug', action='store_true')
    arguments = parser.parse_args()

    script_root_path = os.path.normpath(os.path.join(os.path.dirname(os.path.realpath(__file__)), '..'))

    java_classpath = build_classpath('lib') + build_classpath('build') + [ os.path.join(script_root_path, 'build') ]

    java_args = [ arguments.java_args, '-classpath %s' % os.pathsep.join(java_classpath) ]

    tool_args = {
        '--rdbms': arguments.rdbms,
        '--jdbc-driver': arguments.jdbc_driver,
        '--jdbc-url': arguments.jdbc_url % {
                'database': arguments.database,
                'hostname': arguments.hostname },
        '--username': arguments.username,
        '--password': arguments.password
    }

    command_line = 'java %(java_args)s com.majormode.tool.dbdeploy.Main %(tool_args)s' % {
            'java_args': ' '.join(java_args),
            'tool_args': ' '.join([ '%s %s' % (key, value) for (key, value) in tool_args.iteritems() ])
        }

    if arguments.debug:
        print command_line
        command_line += ' --verbose'

    print command_line
    subprocess.call(command_line.split(' '))
