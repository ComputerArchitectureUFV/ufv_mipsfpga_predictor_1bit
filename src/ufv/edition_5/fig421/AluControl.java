/* 
 * AluControl.java - class ufv.AluControl
 * 
 * 23.10.2014 - MK.Computer.Organization.and.Design.4th.Edition.Oct.2011, Page 316;
 *  
 * (C) Racyus Delano - racyusdelanoo@gmail.com
 */
 
package ufv.edition_5.fig421; 

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.utils.StringTokenizer;

import java.awt.Color;
import java.util.Enumeration;


public class AluControl extends  hades.models.rtlib.GenericRtlibObject {

  protected  PortStdLogicVector  port_Funct,  port_ALUOp,  port_Out;
  protected  StdLogicVector      value_Funct, value_ALUOp;


  public AluControl() { 
    super();
  }

  public void constructPorts() {
    port_Funct  = new PortStdLogicVector(this, "Funct", Port.IN,  null, 6);
    port_ALUOp  = new PortStdLogicVector(this, "ALUOp", Port.IN,  null, 2);
    port_Out = new PortStdLogicVector(this, "OutALUcontrol", Port.OUT, null, 4);
  
    ports    = new Port[3];
    ports[0] = port_Funct;
    ports[1] = port_ALUOp;
    ports[2] = port_Out;
    
    vectorOutputPort = port_Out;
  }

  /**
   *  evaluate(): called by the simulation engine on all events that
   *  concern this object. The object is responsible for updating its
   *  internal state and for scheduling all pending output events.
   */
  
  public void evaluate( Object arg ) {
    if (debug) System.err.println( toString() + ".evaluate()" ); 
 
    Signal signal_Funct, signal_ALUOp, signal_Out;
 
    if      ((signal_Funct = port_Funct.getSignal()) == null) vector=vector_UUU.copy();
    else if ((signal_ALUOp = port_ALUOp.getSignal()) == null) vector=vector_UUU.copy();
    else {
 
      value_Funct = (StdLogicVector) signal_Funct.getValue();
      value_ALUOp = (StdLogicVector) signal_ALUOp.getValue();
 
      int  funct = (int) value_Funct.getValue();
      int  aluOp = (int) value_ALUOp.getValue();
 
      operacao(funct, aluOp);
 
      if (debug) System.err.println( "-I- inputs=" + value_Funct.toBinString(20)
                                     + " "         + value_ALUOp.toBinString(20)
                                     + "  output=" + vector.toBinString(20) );
    }
 
    SimKernel  simulator = parent.getSimulator();
    double     time      = simulator.getSimTime() + delay; 
 
    if ((signal_Out  = port_Out.getSignal()) != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_Out, time, vector, port_Out ));
    }
  }
  
  
  public void operacao(int funct, int aluOp) {
	
	//OBS: aluOp and Funct based in page 317 the book
			
	switch(aluOp)
	{
		//(Instructions Type I - Lw e Sw) 
		//ADD is an operation that will be sent to ALU
		case 0:  
			  {
				
					vector = new StdLogicVector(4, 2);
			  }	
			break;
			
		//(Instruction Tipo I - Beq) 
		//SUB is an operation that will be sent to ALU
		case 1: 
				
					vector = new StdLogicVector(4, 6);
			break;
			
		//(Instruction Type R) 
		//Operations on Type R		
		case 2:  					
			  {
				switch(funct)
				{	
						//Operation ADD
						case 32: 
								vector = new StdLogicVector(4, 2);
							break;
						
						//Operation SUB		 
						case 34:
								vector = new StdLogicVector(4, 6);
							break;
							
						//Operation AND			
						case 36:
								vector = new StdLogicVector(4, 0);
							break;
							
						//Operation OR		 
						case 37:
								vector = new StdLogicVector(4, 1);
							break;
							
						//Operation SLT		 
						case 42:
								vector = new StdLogicVector(4, 7);
							break;								
				 }
			  }
			break;
	}
  }
} 

/* end AluControl.java */
