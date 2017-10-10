/* RegR.java --  class hades.models.rtlib.register.RegR
 *
 * an edge-triggered n-bit D-type register with reset (active low)
 *
 * 28.07.99 - added vectorOutputPort for setValue()
 * 10.09.98 - use scheduleAfter() etc.
 * 03.09.98 - first version, copied from RegE
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.edition_5.fig465;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.gui.*;
import hades.utils.StringTokenizer;

import java.awt.*;
import java.util.Enumeration;


/** 
 * RegR - an edge-triggered n-bit d-type register with active-low reset.
 * Signals are expected to be of type SignalStdLogicVector for D and Q,
 * and SignalStdLogic1164 for CLK and NR.
 * <p>
 * The RegR model will not check for setup-violations. 
 * <p>
 * @author F.N.Hendrich
 * @version 0.3  03.09.98
 */
public class  RegPC 
       extends  hades.models.rtlib.register.RegRE 
       implements  Assignable
{

  protected   PortStdLogicVector  port_D, port_Q;
  protected   PortStdLogic1164    port_CLK, port_NR, port_ENA;

  protected   double t_setup = 5.0E-9;
  protected   StdLogic1164  ieee_U = new StdLogic1164();



  public RegPC() { 
    super();
  }


  public void constructPorts() {
    port_D   = new PortStdLogicVector( this, "D",   Port.IN,  null, n_bits );
    port_Q   = new PortStdLogicVector( this, "Q",   Port.OUT, null, n_bits );
    port_CLK = new PortStdLogic1164(   this, "CLK", Port.IN,  null );
    port_NR  = new PortStdLogic1164(   this, "NR",  Port.IN,  null );
    port_ENA = new PortStdLogic1164(   this, "ENA", Port.IN,  null );

    ports = new Port[5];
    ports[0] = port_CLK;
    ports[1] = port_NR;
    ports[2] = port_ENA;
    ports[3] = port_D;
    ports[4] = port_Q;

    vectorOutputPort = port_Q;
  }



  public void evaluate( Object arg ) {
    if (debug) message( "-I- RegR.evaluate..." );

    StdLogic1164    value_CLK = port_CLK.getValueOrU();
    StdLogic1164    value_ENA = port_ENA.getValueOrU();
    StdLogic1164    value_NR  = port_NR.getValueOrU();
    StdLogicVector  value_D   = port_D.getVectorOrUUU();

    if (value_NR.is_0()) {                                     // reset active
      vector = vector_000.copy();
    }
    else if (!value_NR.is_1()) {                            // reset undefined
      vector = vector_XXX.copy();
    }
    else if (!value_ENA.is_01()) {                         // enable undefined
      vector = vector_XXX.copy();
    }
    else if (!value_CLK.is_01()) {                          // clock undefined
      vector = vector_XXX.copy();
    }
    else if (port_CLK.hasEvent() && value_CLK.is_1()) {   // check rising edge
      if (value_ENA.is_1()) {
         if (value_D.has_UXZ())  vector = vector_XXX.copy();
         else                    vector = value_D.copy();
      }
      else {
         ; // not enabled     
      }
    }
    else {
        // store value: do nothing
    }
    if (enableAnimationFlag) wakeupAfter( delay );
    scheduleAfter( delay, port_Q, vector );
  }


} 


/* end RegR.java */
