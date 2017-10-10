

package ufv.edition_5.fig460;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.utils.StringTokenizer;

import java.awt.Color;
import java.util.Enumeration;


public class ControlUnit 
  extends  hades.models.rtlib.GenericRtlibObject {

  protected  PortStdLogicVector  port_opcode, port_Signals;
  protected  PortStdLogic1164    port_Branch, port_IFflush;
  protected  StdLogicVector      vector_opcode, vector_Signals;
  protected  StdLogic1164        value_Branch, value_IFflush;

  protected  PortStdLogic1164    port_reset;

  public ControlUnit() { 
    super();
    constructPorts();
  }

  public void constructPorts() {
    port_opcode   = new PortStdLogicVector(this, "opcode", Port.IN,  null, 6 );
    port_Signals   = new PortStdLogicVector(this, "Signals", Port.OUT,  null, 8 ); 
    port_Branch = new PortStdLogic1164(this, "Branch", Port.OUT, null );
    port_IFflush = new PortStdLogic1164(this, "IFflush", Port.OUT, null );

    port_reset  = new PortStdLogic1164(   this, "R", Port.IN, null );

    ports    = new Port[5];
    ports[0] = port_opcode;
    ports[1] = port_Signals;
    ports[2] = port_Branch;
    ports[3] = port_IFflush;
    ports[4] = port_reset;
  }

  /**
   *  evaluate(): called by the simulation engine on all events that
   *  concern this object. The object is responsible for updating its
   *  internal state and for scheduling all pending output events.
   */
  public void evaluate( Object arg ) {
    if (debug) System.err.println( toString() + ".evaluate()" ); 

    Signal signal_opcode, signal_Signals, signal_Branch, signal_IFflush;

    if      ((signal_opcode = port_opcode.getSignal()) == null) { 
         vector_Signals=vector_000.copy();
         value_Branch=null;
         value_IFflush=null;
    }
    else { // control function

      vector_opcode = (StdLogicVector) signal_opcode.getValue();

      int  code = (int) vector_opcode.getValue();
      
      //signal_reset = port_reset.getSignal();
      //value_reset = (StdLogic1164) signal_reset.getValue();
      //SignalStdLogic1164  reset =  (SignalStdLogic1164) port_reset.getSignal();

      switch ( code ) {
      //Bits sequence
	  //RegDst - OP0 - OP1 - ALUSrc - MemWrite - MemRead - RegWrite - MemToReg
	  //  7  -  6  -   5    -    4     -    3    -   2    -    1     -     0

         //opcode 0 = R
         case 0: 
                /* if(reset.hasRisingEdge()){
                   vector_Signals = new StdLogicVector(8,0);
                   value_Branch = new StdLogic1164(2);
                   value_IFflush = new StdLogic1164(2);
                 }
                 else{*/
                   vector_Signals = new StdLogicVector(8,162);//1 01 0 0 0 1 0
                   value_Branch = new StdLogic1164(2);
                   value_IFflush = new StdLogic1164(2);
                 //}
                 break;
         //opcode 35 = LW
         case 35: 
           vector_Signals = new StdLogicVector(8,23); // 23 = 0 00 1 0111 = Regdst RT=0, OP=00 +, ALUsrc = IM = 1, MW=0, MR=1, RegW=1, MemtoReg=1 
           value_Branch = new StdLogic1164(2);  // 2 = falso, 3 = verdadeiro
           value_IFflush = new StdLogic1164(2);
           break;
 //opcode 8 = ADDI
         case 8: 
           vector_Signals = new StdLogicVector(8,18); // 18 = 0 00 1 0001 = Regdst RT=0, OP=00 +, ALUsrc = IM = 1, MW=0, MR=0, RegW=1, MemtoReg=0 
           value_Branch = new StdLogic1164(2);  // 2 = falso, 3 = verdadeiro
           value_IFflush = new StdLogic1164(2);
           break;
         //opcode 43 = SW
         case 43:
           vector_Signals = new StdLogicVector(8,24);
           value_Branch = new StdLogic1164(2);
           value_IFflush = new StdLogic1164(2);
           break;
         //opcode 4 = BEQ
         case 4: 
           vector_Signals = new StdLogicVector(8,64);
           value_Branch = new StdLogic1164(3);
           value_IFflush = new StdLogic1164(3);
           break;
         //Erro
         default: 
           vector_Signals = new StdLogicVector(8,0);
           value_Branch = new StdLogic1164(2);
           value_IFflush = new StdLogic1164(2);
           break;
      }
    }

    SimKernel  simulator = parent.getSimulator();
    double     time      = simulator.getSimTime() + delay; 

    if ((signal_Signals  = port_Signals.getSignal()) != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_Signals, time, vector_Signals, port_Signals ));
    }
    if ((signal_Branch  = port_Branch.getSignal()) != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_Branch, time, value_Branch, port_Branch ));
    }
    if ((signal_IFflush  = port_IFflush.getSignal()) != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_IFflush, time, value_IFflush, port_IFflush ));
    }
  }
} 

