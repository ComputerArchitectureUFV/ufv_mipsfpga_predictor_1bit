/* 
 * alucontrol.java -  class ufv.alucontrol
 *
 * 23.10.2014 - livro, pagina descreve o componente 
 * (C) Nome e email
 */ 

package ufv.edition_5.fig465;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.utils.StringTokenizer;

import java.awt.Color;
import java.util.Enumeration;



/** 
 * 
 *
 */
public class Comp 
       extends  hades.models.rtlib.GenericRtlibObject {

  protected  PortStdLogicVector  port_rs, port_rt;
  protected  PortStdLogic1164    port_equal;
  //protected  StdLogicVector      vector_rs, vector_rt, vector_IDEXrt;
  //protected  StdLogic1164        value_IDEXmemread, value_PCwrite, value_IFIDwrite, value_stall;
  
  public Comp() { 
    super();
    constructPorts();
  }

  public void constructPorts() {
    port_rs   = new PortStdLogicVector(this, "rs", Port.IN,  null, 32 );
    port_rt   = new PortStdLogicVector(this, "rt", Port.IN,  null, 32 );
    port_equal  = new PortStdLogic1164(   this, "equal", Port.IN, null );

    ports    = new Port[3];
    ports[0] = port_rs;
    ports[1] = port_rt;
    ports[2] = port_equal;
  }

/**
   *  evaluate(): called by the simulation engine on all events that
   *  concern this object. The object is responsible for updating its
   *  internal state and for scheduling all pending output events.
   */
  public void evaluate( Object arg ) {
    if (debug) System.err.println( toString() + ".evaluate()" ); 

    StdLogicVector vector_rs = port_rs.getVectorOrUUU();
    StdLogicVector vector_rt = port_rt.getVectorOrUUU();
    StdLogic1164 value_equal = null;
    
    //comportamento
    if(vector_rs.has_UXZ()){
      message( "-W- " + toString() +
               "rs undefined: data loss would occur! Ignoring..." );
    }
    else if(vector_rt.has_UXZ()){
      message( "-W- " + toString() +
               "rt undefined: data loss would occur! Ignoring..." );
    }
    else{
      long rs = vector_rs.getValue();
      long rt = vector_rt.getValue();

      if(rs==rt){
        value_equal = new StdLogic1164(3);
      }
      else{
        value_equal = new StdLogic1164(2);
      }                                   
    }

    SimKernel  simulator = parent.getSimulator();
    double time = simulator.getSimTime();
    
    Signal signal_equal = port_equal.getSignal();

    if (signal_equal != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_equal, time, value_equal, port_equal));
    }
  }
}
/* end alucontrol.java example */
