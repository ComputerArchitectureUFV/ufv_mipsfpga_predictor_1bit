/* ROM.java - hades.models.rtl.ROM
 *
 * 29.08.98 - check isConnected() before resize()ing, new Symbol
 * 27.08.98 - allow resize()ing of the memory array
 * 26.08.98 - first version
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */

package ufv.edition_5.fig465;

import  hades.models.rtlib.memory.*;
import  hades.models.*;
import  hades.models.memory.*;
import  hades.signals.*;
import  hades.simulator.*;
import  hades.symbols.*;
import  hades.utils.StringTokenizer;

import  java.io.*;
import  java.util.Hashtable;
import  java.util.Enumeration;


/**
 * ROM - a generic ROM with n words by m bits.
 * <p>
 * This class models a standard LSI ROM without clocking or enable.
 * <p>
 *
 * @author  F.N.Hendrich
 * @version 0.4  23.02.98
 */
public class  InstructionMemory
       extends  GenericMemory {

  protected  PortStdLogicVector  port_Address, port_Instruction;


  public final static double  t_access    = 30.0E-9; // read access time
  public final static double  t_tristate  =  5.0E-9; // outputs -> Z
  public final static double  t_undefined =  6.0E-9; // outputs -> X


  /**
   * InstructionMemory constructor
   */
  public InstructionMemory() {
    super();
    constructPorts();
  }


  protected void constructPorts() {
    port_Address = new PortStdLogicVector( this, "Addr", Port.IN, null, 12 );
    port_Instruction = new PortStdLogicVector( this, "Inst", Port.OUT, null, 32 );


    ports = new Port[2];
    ports[0] = port_Address;
    ports[1] = port_Instruction;
  }






  public boolean canChangeSize() {
    return !isConnected();
  }


  public void setSize( int n_words ) {
    resize( n_words, getBitsPerWord() );
  }


  public void setBitsPerWord( int n_bits ) {
    resize( getSize(), n_bits );
  }


  public boolean resize( int n_words, int n_bits ) {
    this.n_words = n_words;
    this.n_bits  = n_bits;

    this.data = new long[n_words];
    initializeWithZeroes();

    constructStandardValues();
    constructPorts();
    return true;
  }


  /**
   * elaborate(): On elaboration, the inputs of the ROM will be undefined.
   * Correspondingly, the outputs are, too.
   */
  public void elaborate( Object arg ) {
    if (debug) message( toString() + ".elaborate()" );
    simulator = parent.getSimulator();

    if (simulator != null && port_Instruction.getSignal() != null) {
      simulator.scheduleEvent(
        new SimEvent( port_Instruction.getSignal(), 0.0, vector_UUU.copy(), port_Instruction ));
    }
  }


  /**
   *  evaluate(): Return the data at address after t_access
   */
  public void evaluate( Object arg ) {
    if (debug) System.err.println( toString() + ".evaluate()" );

    Signal  signal_D = port_Instruction.getSignal();
    if (signal_D == null) return;

    double          time     = simulator.getSimTime() + t_access;

    StdLogicVector  vector_A = port_Address.getVectorOrUUU();
    StdLogicVector  vector_D = null;
    int   addr;
    long  data;

    if (vector_A.has_UXZ()) {
       vector_D = vector_UUU.copy();
       addr     = UNDEFINED;
       data     = UNDEFINED;
    }
    else {
       addr     = (int) vector_A.getValue();
       data     = getDataAt( addr );
       vector_D = new StdLogicVector( 32, data );
       notifyReadListeners( addr, data );
    }

    simulator.scheduleEvent(
      new SimEvent( signal_D, time, vector_D, port_Instruction ));
  }

} /* ROM.java */
