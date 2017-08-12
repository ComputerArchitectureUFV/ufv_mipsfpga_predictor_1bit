 /* ShiftLeft.java - class ufv.edition_5
 * 
 * 24-10-2014 - MK.Computer.Organization.and.Design.4th.Edition.Oct.2011, P.xxx
 *
 * (C) T.T. Almeida, thales.almeida@ufv.edition_5.br
 */

package ufv.edition_5;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.utils.StringTokenizer;

import java.awt.Color;
import java.util.Enumeration;

public class ShiftLeft extends  hades.models.rtlib.GenericRtlibObject {

  protected  PortStdLogicVector  port_A, port_outSL;
  protected  StdLogicVector      value_A;

  public ShiftLeft() { 
    super();
  }

  public void constructPorts() {
    port_A   = new PortStdLogicVector(this, "A", Port.IN,  null, 32 );
    port_outSL = new PortStdLogicVector(this, "OutSL", Port.OUT, null, 32 );
  
    ports    = new Port[2];
    ports[0] = port_A;
    ports[1] = port_outSL;

    vectorOutputPort = port_outSL;
  }

  /**
   *  evaluate(): called by the simulation engine on all events that
   *  concern this object. The object is responsible for updating its
   *  internal state and for scheduling all pending output events.
   */
  public void evaluate( Object arg ) {
    if (debug) System.err.println( toString() + ".evaluate()" ); 

    Signal signal_A, signal_outSL;

    if      ((signal_A = port_A.getSignal()) == null) vector=vector_UUU.copy();
    else { 
		
			value_A = (StdLogicVector) signal_A.getValue();
     
			int result = (int) value_A.getValue() * 4;  	
		
			vector = new StdLogicVector(32, result);

			if (debug) System.err.println( "-I- inputs=" + value_A.toBinString(20)
											+ "  output=" + vector.toBinString(20));
    }

    SimKernel  simulator = parent.getSimulator();
    double     time      = simulator.getSimTime() + delay; 

    if ((signal_outSL  = port_outSL.getSignal()) != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_outSL, time, vector, port_outSL ));
    }
  }



} 

/* end Add.java */
