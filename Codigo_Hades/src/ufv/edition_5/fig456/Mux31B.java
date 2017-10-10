/* Mux41.java -  class hades.models.rtlib.muxes.Mux41
 *
 * 29.10.99 - renoved unused wakeup call
 * 05.07.98 - first try, copied from hades.models.rtlib.muxes.Mux21
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 *  @Christopher Gull (edited as 3:1 mux)
 */

package ufv.edition_5.fig456;

import hades.models.rtlib.*;
import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.utils.StringTokenizer;

import java.awt.Color;
import java.util.Enumeration;


/**
 * Mux31 - a simple three-input n-bit multiplexer.
 * Signals are expected to be SignalStdLogicVector objects.
 *
 */
public class Mux31B
    extends  hades.models.rtlib.GenericRtlibObject
{

    protected  PortStdLogicVector   port_A2, port_A1, port_A0, port_Y, port_S1;

    public Mux31B()
    {
        super();
    }

    public void constructPorts()
    {
        port_A2  = new PortStdLogicVector( this, "AluD", Port.IN, null, 32 );
        port_A1  = new PortStdLogicVector( this, "MemD", Port.IN, null, 32 );
        port_A0  = new PortStdLogicVector( this, "RegD", Port.IN, null, 32 );
        port_S1  = new PortStdLogicVector( this, "fwdB", Port.IN, null, 2 );
        port_Y   = new PortStdLogicVector( this, "Y",  Port.OUT, null, 32 );

        ports = new Port[5];
        ports[0] = port_A0;
        ports[1] = port_A1;
        ports[2] = port_A2;
        ports[3] = port_S1;
        ports[4] = port_Y;

        vectorOutputPort = port_Y;
    }

    /**
     *  evaluate(): called by the simulation engine on all events that
     *  concern this object. The object is responsible for updating its
     *  internal state and for scheduling all pending output events.
     */
    public void evaluate( Object arg )
    {
        if (debug) System.err.println( toString() + ".evaluate()" );

        Signal   signal_A0 = null, signal_A1 = null;
        Signal   signal_A2 = null;
        Signal   signal_S1 = null, signal_Y = null;
        boolean  isX = false;

        if      ((signal_A0 = port_A0.getSignal()) == null) isX = true;
        else if ((signal_A1 = port_A1.getSignal()) == null) isX = true;
        else if ((signal_A2 = port_A2.getSignal()) == null) isX = true;
        else if ((signal_S1 = port_S1.getSignal()) == null) isX = true;
        if (isX)
        {
            vector = vector_UUU.copy();
        }
        else   // MUX function: select a0 or a1
        {

            StdLogicVector  a0   = (StdLogicVector) signal_A0.getValue();
            StdLogicVector  a1   = (StdLogicVector) signal_A1.getValue();
            StdLogicVector  a2   = (StdLogicVector) signal_A2.getValue();
            StdLogicVector  s1   = (StdLogicVector) signal_S1.getValue();

            if (s1.equals(new StdLogicVector( 2, 2 )))        vector = a2.copy();
            else if (s1.equals(new StdLogicVector( 2, 1 )))   vector = a1.copy();
            else if (s1.equals(new StdLogicVector( 2, 0 )))   vector = a0.copy();
            else                                              vector = vector_UUU.copy();
        }

        SimKernel simulator = parent.getSimulator();
        double    time      = simulator.getSimTime() + delay;

        //simulator.scheduleWakeup( this, time, this );

        if ((signal_Y  = port_Y.getSignal()) == null) return; // no output
        simulator.scheduleEvent( new SimEvent( signal_Y, time, vector, port_Y ));
    }

}

/* end Mux41.java */
