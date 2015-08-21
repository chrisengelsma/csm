/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.awt;

import javax.swing.*;
import java.awt.*;

/**
 * Special renderer for JLists.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.06.28
 */
public class ColorListRenderer extends JLabel implements ListCellRenderer {
  public ColorListRenderer() {
    setOpaque(true);
    setVerticalAlignment(CENTER);
    setHorizontalAlignment(CENTER);
  }

  public Component getListCellRendererComponent(
    JList list, Object value, int index,
    boolean isSelected, boolean cellHasFocus)
  {
    JPanel currentPanel = (JPanel)value;
    Color bg = currentPanel.getBackground();

    int r = bg.getRed();
    int g = bg.getGreen();
    int b = bg.getBlue();

    if (isSelected) {
      setBackground(bg);
      setForeground(list.getForeground());
    } else {
      setBackground(bg);
      setForeground(list.getForeground());
    }
    
    setText(r+"."+g+"."+b);
    return this;
  }
}
