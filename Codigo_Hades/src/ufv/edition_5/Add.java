/* Add.java -  class hades.models.rtlib.arith.Add
 *
 * 28.07.99 - added vectorOutputPort for setValue()
 * 10.07.98 - minor cleanups
 * 04.07.98 - first try, copied from models.rtlib.Incr
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.edition_5;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.utils.StringTokenizer;
import hades.models.rtlib.arith.*;

import java.awt.Color;
import java.util.Enumeration;



/** 
 * Add - a simple adder without carry input or output.
 *         Signals are expected to be SignalStdLogicVector objects.
 *
 */
public class Add 
       extends  hades.models.rtlib.arith.Add {


  public Add() { 
    super();
  }


} 

/* end Add.java */
