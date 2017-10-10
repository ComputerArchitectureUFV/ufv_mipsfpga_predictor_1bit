/* Mux21.java -  class hades.models.rtlib.muxes.Mux21
 *
 * 01.06.99 - enableAnimation
 * 07.09.98 - use Port-subclasses, avoid NullPointers in evaluate()
 * 28.08.98 - add visual clue to the "active input
 * 05.07.98 - first try, copied from hades.models.ruge.Mux
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.edition_5;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.utils.StringTokenizer;

import jfig.objects.FigObject;

import java.awt.Color;
import java.awt.Point;
import java.util.Enumeration;


/** 
 * Mux21 - a simple two-input n-bit multiplexer.
 * Signals are expected to be SignalStdLogicVector objects.
 *
 */
public class Mux21 
       extends  hades.models.rtlib.muxes.Mux21 {

  public Mux21() { 
    super();
  }

  public void initDisplay() {
   
  }
 private void setMarkers( int i ) {
    
   }

 public void updateSymbol() {
  
  }



} 

/* end Mux21.java */
