/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

/**
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.03.05
 */
public interface Model {
  float[] getVertices();
  void drawModel();
}
