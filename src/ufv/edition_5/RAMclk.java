/* RAM.java - hades.models.rtl.RAM
 *
 * 29.08.98 - first version (copied from ROM)
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.edition_5;

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
 * RAM - a generic RAM with n words by m bits with asynchronous (level 
 * sensitive) write enable and separate active-low chip select.
 * When chip-select is high, the data output of the RAM is tristated
 * and writes are ignored.
 * This component models a standard MSI/LSI RAM with asynchronous write
 * enable - the adress is never latched. As long as write enable is low, 
 * the input data is written to the currently selected address.
 * <p>
 * Note that the model currently does not check for timing violations
 * during write cycles.
 * <p>
 *
 * @author  F.N.Hendrich
 * @version 0.1  29.08.98
 */
public class  RAMclk 
       extends  hades.models.rtlib.memory.GenericMemory {

  protected  PortStdLogicVector  port_A, port_DIN, port_DOUT,port_ADebug, port_DDebug;
  protected  PortStdLogic1164    port_WR, port_RD, port_CLK;

  public final static double  t_access       = 30.0E-9; // read access time
  public final static double  t_tristate     =  5.0E-9; // outputs -> Z
  public final static double  t_undefined    =  6.0E-9; // outputs -> X
  public final static double  t_setup        =  6.0E-9; // outputs -> X
  public final static double  t_min_we_cycle =  6.0E-9; // outputs -> X


  /**
   * RAM constructor
   */
  public RAMclk() {
    super();
    constructPorts();
  }

    


  protected void constructPorts() {
    port_A    = new PortStdLogicVector( this, "Address", Port.IN, null, 
                                        getAddressBusWidth() );
    port_ADebug    = new PortStdLogicVector( this, "ReadDebug", Port.IN, null, 
                                        getAddressBusWidth() );
    port_DIN  = new PortStdLogicVector( this, "WriteData", Port.IN, null, n_bits );
    port_DOUT = new PortStdLogicVector( this, "ReadData", Port.OUT, null, n_bits );
    port_DDebug = new PortStdLogicVector( this, "DataDebug", Port.OUT, null, n_bits );
    port_WR  = new PortStdLogic1164(   this, "MemWrite", Port.IN, null );
    port_RD  = new PortStdLogic1164(   this, "MemRead", Port.IN, null );
    port_CLK = new PortStdLogic1164(   this, "CLK", Port.IN, null );

    ports = new Port[8];
    ports[0] = port_A;
    ports[1] = port_DIN;
    ports[2] = port_DOUT;
    ports[3] = port_WR;
    ports[4] = port_RD;
    ports[5] = port_CLK;
    ports[6] = port_ADebug;
    ports[7] = port_DDebug;

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
   * elaborate(): On elaboration, the inputs of the RAM will be undefined.
   * Correspondingly, the outputs are, too.
   */
  public void elaborate( Object arg ) {
    if (debug) message( toString() + ".elaborate()" ); 
    simulator = parent.getSimulator();

    if (simulator != null && port_DOUT.getSignal() != null) {
      simulator.scheduleEvent( 
        new SimEvent(port_DOUT.getSignal(), 0.0, vector_UUU.copy(), port_DOUT));
    }
  }


  /**
   * evaluate(): 
   * If either port_nCS or port_nWE are undefined, the RAM data is invalidated,
   * and the DOUT value is undefined.
   * If port_nCS is high, the RAM is inactive. That is, the outputs are
   * tristated, and all write requests are ignored.
   * If port_nCS is low, the output is driven with the memory contents of
   * the currently active address. Also, as long as port_nWE is low, the
   * input data is written to the active address.
   * <p>
   * Warning: The RAM model currently does not check for timing violations
   * or address glitches during write cycles. Instead, the corresponding
   * memory locations are written.
   */
  public void evaluate( Object arg ) {
    if (debug) System.err.println( toString() + ".evaluate()" );

    double          time        = simulator.getSimTime() + t_access;

    StdLogicVector  vector_A    = port_A.getVectorOrUUU();
    StdLogicVector  vector_ADebug    = port_ADebug.getVectorOrUUU();
    StdLogicVector  vector_DIN  = port_DIN.getVectorOrUUU();
    StdLogicVector  vector_DOUT = null;
    StdLogicVector  vector_DDebug = null;
    StdLogic1164    value_RD   = port_RD.getValueOrU();
    StdLogic1164    value_WR   = port_WR.getValueOrU();
    StdLogic1164    value_CLK   = port_CLK.getValueOrU();

    int   addr = 0;
    long  data = 0, old_data = 0;

    if (value_RD.is_0()) {         // read memory inactive: tristate outputs
      vector_DOUT = vector_ZZZ.copy();
    }
    else if (!value_RD.is_1()) {        // read memory undefined: output UUU
      vector_DOUT = vector_UUU.copy();
    }


     if (value_RD.is_1() ) {                                   // read cycle
        if (vector_A.has_UXZ()) {                    // but address undefined
          vector_DOUT = vector_UUU.copy();
        }
        else {
          addr        = (int) vector_A.getValue();
          data        = getDataAt( addr );
          vector_DOUT = new StdLogicVector( n_bits, data );
          notifyReadListeners( addr, data );
        }
     }
     
     SignalStdLogic1164  clk = (SignalStdLogic1164) port_CLK.getSignal();
     if (value_WR.is_1() && clk != null && clk.hasRisingEdge()) {                             // write cycle
        if (vector_A.has_UXZ()) {                    // but address undefined
          message( "-W- RAM.evaluate(): write enable active but address"
                 + " undefined: data would be lost. Ignored!" );
          vector_DOUT = vector_UUU.copy();
        }
        else if (vector_DIN.has_UXZ()) {                // but data undefined
          message( "-W- RAM.evaluate(): write enable active but data input"
                 + " undefined: data would be lost. Ignored!" );
          vector_DOUT = vector_UUU.copy();
        }
        else {                                           // valid write cycle
          addr     = (int) vector_A.getValue();
          data     = (int) vector_DIN.getValue();

          old_data = getDataAt( addr );
          setDataAt( addr, data );
          notifyWriteListeners( addr, old_data, data );

          vector_DOUT = new StdLogicVector( n_bits, data );
        }
      }
      
    Signal  signal_DOUT = port_DOUT.getSignal();
    if (signal_DOUT != null) {
      simulator.scheduleEvent( 
        new SimEvent( signal_DOUT, time, vector_DOUT, port_DOUT ));
    }
    
    if (vector_ADebug.has_UXZ()) {                    // but address undefined
      vector_DDebug = vector_UUU.copy();
    }
    else {
      addr        = (int) vector_ADebug.getValue();
      data        = getDataAt( addr );
      vector_DDebug = new StdLogicVector( n_bits, data );
      //notifyReadListeners( addr, data );
    }
    
    Signal  signal_DDebug = port_DDebug.getSignal();
    if (signal_DDebug != null) {
      simulator.scheduleEvent( 
        new SimEvent( signal_DDebug, time, vector_DDebug, port_DDebug ));
    }
    
  }
    
} /* RAM.java */ 
