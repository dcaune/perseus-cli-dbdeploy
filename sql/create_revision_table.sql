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

--(defconstant +deployment-status-failed+ 0)
--(defconstant +deployment-status-succeeded+ 1)

/**
 * Contain the last revision number of object types of modules that
 * have deployed in the relation database management system
 * (RDBMS).<p>
 *
 * It is most commonly used in engineering and software development to
 * manage ongoing development of application source code that may be
 * worked on by a team of people.  Changes to these source codes are
 * identified by incrementing an associated number, termed the
 * "revision number".  A simple form of revision control, for example,
 * has the initial issue of a drawing assigned the revision number
 * "1".  When the first change is made, the revision number is
 * incremented to "2" and so on.
 *
 * @param module_name a particular domain, activity, or business
 *        logic.
 * @param object_type a particular type of database objects of the
 *        module, such as "constraint", "function", "index", "job",
 *        "package", "procedure", "sequence",  "table", "trigger",
 *        "type", "view".  The list of object types might be RDBMS
 *        dependent. 
 * @param revision_number last revision number of these database
 *        objects deployed in the RDBMS.
 * @param deployment_type date and time when these database objects
 *        have been deployed in the RDBMS.
 * @param deployment_status status of the last deployment of these
 *        database objects:
 *        <ul>
 *        <li><code>+deployment-status-failed+</code></li>
 *        <li><code>+deployment-status-succeeded+</code></li>
 *        </ul>
 */
CREATE TABLE revision_control
(
  module_name       varchar(64) NOT NULL,
  object_type       varchar(32) NOT NULL,
  revision_number   int         NOT NULL,
  deployment_time   bigint      NOT NULL,
  deployment_status smallint    NOT NULL
);
