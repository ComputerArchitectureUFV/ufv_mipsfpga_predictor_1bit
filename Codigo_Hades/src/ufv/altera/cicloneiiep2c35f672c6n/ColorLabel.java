/* ColoredValueLabel.java --  hades.symbols.ColoredValueLabel
 *
 * 06.12.98 - use attribs.getClone() in copy(), faster setColor()
 * 22.06.98 - moved from hades.models.ruge to hades.symbols
 * 03.04.98 - first version, copied from InstanceLabel
 *
 * (C) 1997-1998 by F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.altera.cicloneiiep2c35f672c6n;

import jfig.canvas.FigTrafo2D;
import jfig.objects.FigAttribs;
import jfig.objects.FigObject;
import jfig.objects.FigText;

//import hades.symbols.*;

import java.awt.*;
import java.io.*;
import java.util.StringTokenizer;


/** 
 * ColorLabel: a colored and animated label on a SimObject symbol
 */
public class ColorLabel 
       extends hades.symbols.Label {


  private Color color = new Color( 64, 164, 164 );


  /**
   * construct an empty ColorLabel.
   */
  public ColorLabel() {
    super(); // initialize the text
  }


  protected void build_attribs() {
    FigAttribs attribs = getAttributes().getClone();
    attribs.lineColor = attribs.fillColor = color;
    attribs.fillStyle = attribs.NO_FILL;
    attribs.currentLayer = 10;
    attribs.fontSize = 17; 
    attribs.fig_font = FigAttribs.FONT_HELVETICA;
    setAttributes( attribs );
  }

  public void setColor( Color _color ) {
    color = _color;
    attribs.lineColor = attribs.fillColor = color;
  }



  public FigObject copy() {
    ColorLabel carbon = new ColorLabel();
    carbon.setAttributes( this.getAttributes().getClone() );
    carbon.setText( this.getText() );
    Point pp[] = this.getPoints();
    carbon.move( pp[0].x, pp[0].y );
    return carbon;
  }


  
  /** 
   *  toString() - the usual info method 
   */
  public String toString() {
    return "ColorLabel[" + super.toString() + "]";
  }
} 

/* end ColorLabel.java */
