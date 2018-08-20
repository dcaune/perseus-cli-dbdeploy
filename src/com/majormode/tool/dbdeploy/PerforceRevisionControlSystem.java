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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerforceRevisionControlSystem implements RevisionControlSystem {
  /**
   * Regular expression that matches the definition of a Perforce
   * revision keyword declared in a source code.
   */
  protected static final String PATTERN_REVISION_KEYWORD =
    "[$]Revision:\\s#([\\d]+)\\s[$]";

  /**
   * Compiled representation of the regular expression that matches
   * a Perforce revision keyword declared in a source code.
   */
  protected Pattern m_patternRevisionKeyword = Pattern.compile(PATTERN_REVISION_KEYWORD);
  
  public int parseRevisionNumber(String sourceCode)
    throws UndefinedRevisionNumberException {
    Matcher matcher = m_patternRevisionKeyword.matcher(sourceCode);
    if (matcher.find())
      return Integer.valueOf(matcher.group(1)).intValue();
    
    throw new UndefinedRevisionNumberException();
  }
}