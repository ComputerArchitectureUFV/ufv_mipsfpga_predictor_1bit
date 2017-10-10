/* RAM.java - hades.models.rtl.RAM
 *
 * 29.08.98 - first version (copied from ROM)
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.edition_5;

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
public class  DataMemory 
       extends  GenericMemory {

  protected  PortStdLogicVector  port_Address, port_WriteData, port_ReadData;
  protected  PortStdLogic1164    port_MemWrite, port_MemRead, port_CLK;

  public final static double  t_access       = 30.0E-9; // read access time
  public final static double  t_tristate     =  5.0E-9; // outputs -> Z
  public final static double  t_undefined    =  6.0E-9; // outputs -> X
  public final static double  t_setup        =  6.0E-9; // outputs -> X
  public final static double  t_min_we_cycle =  6.0E-9; // outputs -> X


  /**
   * RAM constructor
   */
  public DataMemory() {
    super();
    constructPorts();
  }


  protected void constructPorts() {
    port_Address    = new PortStdLogicVector( this, "Address", Port.IN, null, 12);
    port_WriteData  = new PortStdLogicVector( this, "WriteData", Port.IN, null, 32 );
    port_ReadData = new PortStdLogicVector( this, "ReadData", Port.OUT, null, 32 );
    port_MemWrite  = new PortStdLogic1164(   this, "MemWrite", Port.IN, null );
    port_MemRead  = new PortStdLogic1164(   this, "MemRead", Port.IN, null );
    port_CLK = new PortStdLogic1164(   this, "CLK", Port.IN, null );

    ports = new Port[6];
    ports[0] = port_Address;
    ports[1] = port_WriteData;
    ports[2] = port_ReadData;
    ports[3] = port_MemWrite;
    ports[4] = port_MemRead;
    ports[5] = port_CLK;
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

    if (simulator != null && port_ReadData.getSignal() != null) {
      simulator.scheduleEvent( 
        new SimEvent(port_ReadData.getSignal(), 0.0, vector_UUU.copy(), port_ReadData));
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

    StdLogicVector  vector_Address    = port_Address.getVectorOrUUU();
    StdLogicVector  vector_WriteData  = port_WriteData.getVectorOrUUU();
    StdLogicVector  vector_ReadData = null;
    StdLogic1164    value_MemWrite   = port_MemWrite.getValueOrU();
    StdLogic1164    value_MemRead   = port_MemRead.getValueOrU();
    SignalStdLogic1164  clk = (SignalStdLogic1164) port_CLK.getSignal();
    Signal signal_MemRead = port_MemRead.getSignal();
    //value_MemRead = (StdLogic1164) signal_MemRead.getValue();
    //int MemRead = (int) value_MemRead.getValue();

    int   addr = 0;
    long  data = 0, old_data = 0;
     message("MemRead:"+value_MemRead.is_1()+" - clk:"+clk);
    if(value_MemRead.is_1()){ //write cycle
      message("leitura");
        addr = (int)vector_Address.getValue();
        data = getDataAt(addr);
        vector_ReadData = new StdLogicVector(32, data);
        notifyReadListeners( addr, data );
      }
      if(value_MemWrite.is_1()){ //write cycle
      message("Escrita");
        addr = (int)vector_Address.getValue();
        data = (int)vector_WriteData.getValue();
        
        old_data = getDataAt(addr);
        setDataAt(addr, data);
        notifyWriteListeners( addr, old_data, data );
      }

    Signal  signal_ReadData = port_ReadData.getSignal();
    if (signal_ReadData != null) {
      simulator.scheduleEvent( 
        new SimEvent( signal_ReadData, time, vector_ReadData, port_ReadData ));
    }
  }
    
} /* RAM.java */ 
