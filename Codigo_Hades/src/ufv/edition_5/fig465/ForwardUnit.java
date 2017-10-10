/* ForwardUnit.java -  class ufv.ForwardUnit
 *
 *
 * @ Christopher Gull - UFV
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
 * ForwardUnit detects Register usages between pipeline stages.
 * In the event of problem, either/both forward Muxes before ALU receive
 * data from previous EX result or from Memory/EX result from two CCs back.
 *
 */
public class ForwardUnit
    extends  hades.models.rtlib.GenericRtlibObject
{

    protected  PortStdLogicVector  port_IDEXARS1, port_IDEXART1, port_EXMEMRD1, port_MEMWBRD1, port_fA, port_fB;
    protected  PortStdLogic1164  port_EXMEMRegWrite, port_MEMWBRegWrite;
    protected  StdLogicVector  IDEXARS1, IDEXART1, EXMEMRD1, MEMWBRD1, fA, fB;
    protected  StdLogic1164  EXMEMRegWrite, MEMWBRegWrite;


    public ForwardUnit()
    {
        super();
    }

    public void constructPorts()
    {
        port_IDEXARS1  = new PortStdLogicVector( this, "IDEX_ARS1", Port.IN, null, 5 ); //port
        port_IDEXART1  = new PortStdLogicVector( this, "IDEX_ART1", Port.IN, null, 5 );
        port_EXMEMRD1  = new PortStdLogicVector( this, "EXMEM_RD1", Port.IN, null, 5 );
        port_MEMWBRD1  = new PortStdLogicVector( this, "MEMWB_RD1", Port.IN, null, 5 );
        port_EXMEMRegWrite  = new PortStdLogic1164( this, "EXMEM_RW", Port.IN, null );
        port_MEMWBRegWrite  = new PortStdLogic1164( this, "MEMWB_RW", Port.IN, null );
        port_fA   = new PortStdLogicVector( this, "fwdA",  Port.OUT, null, 2 );
        port_fB   = new PortStdLogicVector( this, "fwdB",  Port.OUT, null, 2 );

        ports = new Port[8];
        ports[0] = port_IDEXARS1;
        ports[1] = port_IDEXART1;
        ports[2] = port_EXMEMRD1;
        ports[3] = port_MEMWBRD1;
        ports[4] = port_EXMEMRegWrite;
        ports[5] = port_MEMWBRegWrite;
        ports[6] = port_fA;
        ports[7] = port_fB;
    }

    /**
     *  evaluate(): called by the simulation engine on all events that
     *  concern this object. The object is responsible for updating its
     *  internal state and for scheduling all pending output events.
     */
    public void evaluate( Object arg )
    {
        if (debug) System.err.println( toString() + ".evaluate()" );

        Signal   signal_IDEXARS1 = null, signal_IDEXART1 = null, signal_EXMEMRD1 = null, signal_MEMWBRD1 = null,
                 signal_EXMEMRegWrite = null, signal_MEMWBRegWrite = null, signal_fA = null, signal_fB = null;
        boolean  isX = false;

        if      ((signal_IDEXARS1 = port_IDEXARS1.getSignal()) == null) isX = true;
        else if ((signal_IDEXART1 = port_IDEXART1.getSignal()) == null) isX = true;
        else if ((signal_EXMEMRD1 = port_EXMEMRD1.getSignal()) == null) isX = true;
        else if ((signal_MEMWBRD1 = port_MEMWBRD1.getSignal()) == null) isX = true;
        else if ((signal_EXMEMRegWrite = port_EXMEMRegWrite.getSignal()) == null) isX = true;
        else if ((signal_MEMWBRegWrite = port_MEMWBRegWrite.getSignal()) == null) isX = true;
        if (isX)
        {
            vector = vector_UUU.copy();
        }
        else
        {
            IDEXARS1 = (StdLogicVector) signal_IDEXARS1.getValue();
            IDEXART1 = (StdLogicVector) signal_IDEXART1.getValue();
            EXMEMRD1 = (StdLogicVector) signal_EXMEMRD1.getValue();
            MEMWBRD1 = (StdLogicVector) signal_MEMWBRD1.getValue();
            EXMEMRegWrite = (StdLogic1164)   signal_EXMEMRegWrite.getValue();
            MEMWBRegWrite = (StdLogic1164)   signal_MEMWBRegWrite.getValue();

            forwardA(EXMEMRegWrite, MEMWBRegWrite, EXMEMRD1, MEMWBRD1, IDEXARS1);
            forwardB(EXMEMRegWrite, MEMWBRegWrite, EXMEMRD1, MEMWBRD1, IDEXART1);
        }

        SimKernel simulator = parent.getSimulator();
        double    time      = simulator.getSimTime() + delay;

        if ((signal_fA  = port_fA.getSignal()) == null) return;
            simulator.scheduleEvent( new SimEvent( signal_fA, time, fA, port_fA ));
        if ((signal_fB  = port_fB.getSignal()) == null) return;
            simulator.scheduleEvent( new SimEvent( signal_fB, time, fB, port_fB ));

    }

    public void forwardA(StdLogic1164 exmem_RegWrite, StdLogic1164 memwb_RegWrite, StdLogicVector exmem_RD1, StdLogicVector memwb_RD1, StdLogicVector idex_ARS1)
    {
        if((exmem_RegWrite.is_1()) && ((!exmem_RD1.is_000()) && (exmem_RD1.equals(idex_ARS1))))
        {
            fA = new  StdLogicVector( 2, 2 );

        }
        //else if ((memwb_RegWrite.is_1()) && (!memwb_RD1.is_000()) && !(exmem_RegWrite.is_1() && (!exmem_RD1.is_000()) && !exmem_RD1.equals(idex_ARS1)) && memwb_RD1.equals(idex_ARS1))
	else if (((memwb_RegWrite.is_1()) && (!memwb_RD1.is_000()) &&  (exmem_RegWrite.is_1() && (!exmem_RD1.is_000())) && (!exmem_RD1.equals(idex_ARS1)) && (memwb_RD1.equals(idex_ARS1)))||((memwb_RegWrite.is_1()) && (!memwb_RD1.is_000()) && !(exmem_RegWrite.is_1() && (!exmem_RD1.is_000()) && !exmem_RD1.equals(idex_ARS1)) && memwb_RD1.equals(idex_ARS1)))
        {
            fA = new  StdLogicVector( 2, 1 );
        }
        else
        {
            fA = new  StdLogicVector( 2, 0 );
        }
    }

    public void forwardB(StdLogic1164 exmem_RegWrite, StdLogic1164 memwb_RegWrite, StdLogicVector exmem_RD1, StdLogicVector memwb_RD1, StdLogicVector idex_ART1)
    {
        if((exmem_RegWrite.is_1()) && ((!exmem_RD1.is_000()) && (exmem_RD1.equals(idex_ART1))))
        {
            fB = new  StdLogicVector( 2, 2 );
        }
        //else if ((memwb_RegWrite.is_1()) && (!memwb_RD1.is_000()) && !(exmem_RegWrite.is_1() && (!exmem_RD1.is_000()) && !exmem_RD1.equals(idex_ART1)) && memwb_RD1.equals(idex_ART1))
	else if (((memwb_RegWrite.is_1()) && (!memwb_RD1.is_000()) &&  (exmem_RegWrite.is_1() && (!exmem_RD1.is_000())) && (!exmem_RD1.equals(idex_ART1)) && (memwb_RD1.equals(idex_ART1)))||((memwb_RegWrite.is_1()) && (!memwb_RD1.is_000()) && !(exmem_RegWrite.is_1() && (!exmem_RD1.is_000()) && !exmem_RD1.equals(idex_ART1)) && memwb_RD1.equals(idex_ART1)))
        {
            fB = new  StdLogicVector( 2, 1 );
        }
        else
        {
            fB = new  StdLogicVector( 2, 0 );
        }
    }


    public void write( java.io.PrintWriter ps ) {
	      ps.print(   " " + delay );
      }
    
    public boolean initialize( String s ) { 
    
        StringTokenizer st = new StringTokenizer( s );
	int n_tokens = st.countTokens();
	try {
	    
	    constructPorts();
	    constructStandardValues();
	    

	    setDelay( delay );
	}
	catch( Exception e ) {
	  message( "-E- " + toString() + ".initialize(): " + e + " " + s );
	}
	return true;
    
    }
}
