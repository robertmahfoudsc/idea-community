/*
 * Copyright 2003-2005 Dave Griffith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class FormattedTextFieldMacFix{
    private FormattedTextFieldMacFix(){
        super();
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    public static void apply( JFormattedTextField field)
    {
        if(isMacOs()) {
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            final int commandKeyMask = toolkit.getMenuShortcutKeyMask();
            final InputMap inputMap = field.getInputMap();
            final KeyStroke copyKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, commandKeyMask);
            inputMap.put(copyKeyStroke, "copy-to-clipboard");
            final KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, commandKeyMask);
            inputMap.put(pasteKeyStroke, "paste-from-clipboard");
            final KeyStroke cutKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_X, commandKeyMask);
            inputMap.put(cutKeyStroke, "cut-to-clipboard");
        }
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    private static boolean isMacOs(){
        final String osName = System.getProperty("os.name").toLowerCase();
        if(osName == null)
        {
            return false;
        }
        return osName.startsWith("mac os x");
    }
}
