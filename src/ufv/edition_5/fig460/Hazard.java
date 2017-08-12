/* 
 * alucontrol.java -  class ufv.alucontrol
 *
 * 23.10.2014 - livro, pagina descreve o componente 
 * (C) Nome e email
 */ 

package ufv.edition_5.fig460;

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
public class Hazard 
       extends  hades.models.rtlib.GenericRtlibObject {

  protected  PortStdLogicVector  port_rs, port_rt, port_IDEXrt;
  protected  PortStdLogic1164    port_IDEXmemread, port_PCwrite, port_IFIDwrite, port_stall;
  //protected  StdLogicVector      vector_rs, vector_rt, vector_IDEXrt;
  //protected  StdLogic1164        value_IDEXmemread, value_PCwrite, value_IFIDwrite, value_stall;
  
  public Hazard() { 
    super();
    constructPorts();
  }

  public void constructPorts() {
    port_rs   = new PortStdLogicVector(this, "rs", Port.IN,  null, 5 );
    port_rt   = new PortStdLogicVector(this, "rt", Port.IN,  null, 5 );
    port_IDEXrt   = new PortStdLogicVector(this, "IDEXrt", Port.IN,  null, 5 );
    port_IDEXmemread  = new PortStdLogic1164(   this, "IDEXmemread", Port.IN, null );
    port_PCwrite  = new PortStdLogic1164(   this, "PCwrite", Port.OUT, null );
    port_IFIDwrite  = new PortStdLogic1164(   this, "IFIDwrite", Port.OUT, null );
    port_stall  = new PortStdLogic1164(   this, "stall", Port.OUT, null );

    ports    = new Port[7];
    ports[0] = port_rs;
    ports[1] = port_rt;
    ports[2] = port_IDEXrt;
    ports[3] = port_IDEXmemread;
    ports[4] = port_PCwrite;
    ports[5] = port_IFIDwrite;
    ports[6] = port_stall;
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
    StdLogicVector vector_IDEXrt = port_IDEXrt.getVectorOrUUU();
    StdLogic1164 value_IDEXmemread = port_IDEXmemread.getValueOrU();
    StdLogic1164 value_PCwrite = new StdLogic1164(3);
    StdLogic1164 value_IFIDwrite = new StdLogic1164(3);
    StdLogic1164 value_stall = new StdLogic1164(2);
    
    int erro = 0;
    
    //comportamento
    if(vector_rs.has_UXZ()){
      message( "-W- " + toString() +
               "rs undefined: data loss would occur! Ignoring..." );
               erro = 1;
    }
    if(vector_rt.has_UXZ()){
      message( "-W- " + toString() +
               "rt undefined: data loss would occur! Ignoring..." );
               erro = 1;
    }
    if(vector_IDEXrt.has_UXZ()){
      message( "-W- " + toString() +
               "rt LW undefined: data loss would occur! Ignoring..." );
               erro = 1;
    }
    if(!value_IDEXmemread.is_01()){
      message( "-W- " + toString() +
               "MemRead LW undefined: data loss would occur! Ignoring..." );
               erro = 1;
    }
    if (erro == 0){
      long rs = vector_rs.getValue();
      long rt = vector_rt.getValue();
      long IDEXrt = vector_IDEXrt.getValue();
      int IDEXmemread = (int)value_IDEXmemread.getValue(); //o valor de IDEXmemread vem como 2 ou 3

      if(IDEXmemread==3 && (rs==IDEXrt || rt==IDEXrt)){
        value_PCwrite = new StdLogic1164(2);
        value_IFIDwrite = new StdLogic1164(2);
        value_stall = new StdLogic1164(3);
      }
      else{
        value_PCwrite = new StdLogic1164(3);
        value_IFIDwrite = new StdLogic1164(3);
        value_stall = new StdLogic1164(2);
      }                                   
    }

    SimKernel  simulator = parent.getSimulator();
    double     time      = simulator.getSimTime() + delay; 
    
    
    Signal signal_stall = port_stall.getSignal();
    Signal signal_PCwrite = port_PCwrite.getSignal();
    Signal signal_IFIDwrite = port_IFIDwrite.getSignal();

    if (signal_stall != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_stall, time, value_stall, port_stall ));
    }                                                
    if (signal_PCwrite != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_PCwrite, time, value_PCwrite, port_PCwrite ));
    }
    if (signal_IFIDwrite != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_IFIDwrite, time, value_IFIDwrite, port_IFIDwrite ));
    }
  }
}
/* end alucontrol.java example */
