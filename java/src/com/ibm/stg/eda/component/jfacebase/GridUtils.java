/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *    DATE: 03/26/2009
 *
 *-PURPOSE---------------------------------------------------------------------
 * Utility methods used for grid layouts.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/26/2009 GFS  Initial coding.
 * 11/17/2009 GFS  Format updates.  Added color getters.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.jfacebase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GridUtils {
    
    /*
     * Getters
     */
    public static Color getWhite(Display display) { 
        return display.getSystemColor(SWT.COLOR_WHITE);
    }
    public static Color getBlack(Display display) { 
        return display.getSystemColor(SWT.COLOR_BLACK);
    }
    public static Color getGray(Display display) { 
        return display.getSystemColor(SWT.COLOR_GRAY);
    }
    public static Color getRed(Display display) { 
        return display.getSystemColor(SWT.COLOR_RED);
    }
    public static Color getGreen(Display display) { 
        return display.getSystemColor(SWT.COLOR_GREEN);
    }
    public static Color getYellow(Display display) { 
        return display.getSystemColor(SWT.COLOR_YELLOW);
    }

    
    /**
     * Define a read only text widget. Great for adding titles.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     */
    public static void setReadonlyText(Composite comp, int gridWidth, String text) {

        Text descriptionText = new Text(comp, SWT.WRAP | SWT.CENTER);
        descriptionText.setEditable(false);
        descriptionText.setText(text);

        // Place it in the grid composite.
        GridData data = new GridData();
        data.horizontalAlignment = SWT.CENTER | SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.horizontalSpan = gridWidth;
        descriptionText.setLayoutData(data);

    }

    
    /**
     * Define a separator line to add to the composite.
     * 
     * @param comp           The parent composite widget which will hold this 
     *                       widget.
     * @param gridWidth      Number of columns in current grid.
     * @param separatorType  SWT.HORIZONTAL or SWT.VERTICAL.
     */
    public static void setSeparator(Composite comp, int gridWidth, int separatorType) {

        // Create the separation line.
        Label separatorLabel = new Label(comp, SWT.SEPARATOR | separatorType);

        // Place it in the grid composite.
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.horizontalSpan = gridWidth;
        separatorLabel.setLayoutData(data);

    }
    

    /**
     * Define an empty label to add space to the composite.
     * 
     * @param comp       The parent composite widget which will hold this widget.
     * @param gridWidth  Number of columns in current grid.
     * @param spaceType  SWT.HORIZONTAL or SWT.VERTICAL.
     */
    public static void setBlankSpace(Composite comp, int gridWidth, int spaceType) {

        // Create the blank line.
        Label blankLabel = new Label(comp, SWT.NONE | spaceType);

        // Place it in the grid composite.
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.horizontalSpan = gridWidth;
        blankLabel.setLayoutData(data);

    }


}