/* 
 * Alu.java - class ufv.Alu
 * 
 * 24.10.2014 - MK.Computer.Organization.and.Design.4th.Edition.Oct.2011, Page 316;
 *  
 * (C) Racyus Delano - racyusdelanoo@gmail.com
 */

package ufv.edition_5.fig456;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.utils.StringTokenizer;

import java.awt.Color;
import java.util.Enumeration;


public class Alu extends  hades.models.rtlib.GenericRtlibObject {

  protected  PortStdLogicVector  port_A, port_B, port_opCode, port_OutAluResult;
  protected  PortStdLogic1164  port_zero;
  
  protected  StdLogicVector      value_A, value_B, value_opCode;
  protected  StdLogic1164   output_0, output_1, value;
  
	
  public Alu() { 
    super();
  }

  public void constructPorts() {
    port_A = new PortStdLogicVector(this, "A", Port.IN,  null, n_bits);
    port_B = new PortStdLogicVector(this, "B", Port.IN,  null, n_bits);
    port_opCode = new PortStdLogicVector(this, "port_opCode", Port.IN,  null, 4);
    port_zero = new PortStdLogic1164(   this, "port_zero",  Port.OUT, null );
    port_OutAluResult = new PortStdLogicVector(this, "port_OutAluResult", Port.OUT,  null, n_bits);
    
    ports    = new Port[5];
    ports[0] = port_A;
    ports[1] = port_B;
    ports[2] = port_opCode;
    ports[3] = port_zero;
    ports[4] = port_OutAluResult;

    output_0 = new StdLogic1164( StdLogic1164._0 );
    output_1 = new StdLogic1164( StdLogic1164._1 );

    vectorOutputPort = port_OutAluResult;
  }

  /**
   *  evaluate(): called by the simulation engine on all events that
   *  concern this object. The object is responsible for updating its
   *  internal state and for scheduling all pending output events.
   */
  
  public void evaluate( Object arg ) {
    if (debug) System.err.println( toString() + ".evaluate()" ); 

    Signal signal_A, signal_B, signal_opCode, signal_OutAluResult, signal_zero;

    if      ((signal_A = port_A.getSignal()) == null) vector=vector_UUU.copy();
    else if ((signal_B = port_B.getSignal()) == null) vector=vector_UUU.copy();
    else if ((signal_opCode = port_opCode.getSignal()) == null) vector=vector_UUU.copy();
    else {

      value_A = (StdLogicVector) signal_A.getValue();
      value_B = (StdLogicVector) signal_B.getValue();
      value_opCode = (StdLogicVector) signal_opCode.getValue();
     
	  int opcode = (int) value_opCode.getValue();
      
      operacao(value_A, value_B, opcode);
// zero flag if output == 0
      if (((int) vector.getValue()) == 0) value = Const1164.__1; 
      else                           value = Const1164.__0;	
     
      if (debug) System.err.println( "-I- inputs=" + value_A.toBinString(20)
                                     + " "         + value_B.toBinString(20)
                                     + " "         + value_opCode.toBinString(20)
                                     + "  output=" + vector.toBinString(20) );
    }

    SimKernel  simulator = parent.getSimulator();
    double     time      = simulator.getSimTime() + delay; 

    if ((signal_OutAluResult  = port_OutAluResult.getSignal()) != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_OutAluResult, time, vector, port_OutAluResult ));
    }
    
    if ((signal_zero  = port_zero.getSignal()) != null) {
      simulator.scheduleEvent(
         SimEvent1164.createNewSimEvent( signal_zero , time, value, port_zero ));
    }

  }
   
  public void operacao(StdLogicVector A, StdLogicVector B, int opcode) {
	
	//OBS: opcode based in page 317 the book
	vector   = new StdLogicVector( n_bits, 0 );	
	switch(opcode)
	{
		//(Operation AND - Instruction AND)
		case 0: 
			  {
				vector = A.and_bitwise( B );
				
			  }	
			break;
		
		//(Operation OR - Instruction OR)
		case 1: 
			  {
				vector = A.or_bitwise( B );
				
			  }	
			break;
				
		//(Operation ADD - Instructions Type R and Lw/Sw)
		case 2: 
			  {
				vector = A.add( B );
			  }	
			break;
		
		//(Operation Sub - Instructions Beq and SUB)
		case 6: 
			  {
				vector = A.sub( B );
				
				int result = (int) vector.getValue();
				
			  }	
			break;	
		
		//(Operation A Lower B - Instruction SLT)
		case 7: 
			  {
				if (A.getSignedValue() < B.getSignedValue()) 
				{	
					vector = new StdLogicVector(n_bits, 1);
				}
				else
					{
						vector = new StdLogicVector(n_bits, 0);
					}		
			  }	
			break;	
	}
  }  
} 

/* end Alu.java */
