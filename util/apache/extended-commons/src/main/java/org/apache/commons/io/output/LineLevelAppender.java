/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.output;

import java.io.IOException;

/**
 * @author Lyor G.
 * @since Aug 13, 2013 7:36:41 AM
 */
public interface LineLevelAppender {
    /**
     * A typical line length used in many textual standards
     */
    static final int TYPICAL_LINE_LENGTH=80;

    /**
     * @return TRUE if OK to accumulate data in work buffer
     */
    boolean isWriteEnabled ();
    /**
     * Called by the implementation once end of line is detected. 
     * @param lineData The &quot;pure&quot; line data - excluding any CR/LF(s).
     * @throws IOException If failed to write the data
     */
    void writeLineData (CharSequence lineData) throws IOException;

}
