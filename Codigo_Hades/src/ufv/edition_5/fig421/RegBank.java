/* RegBank.java - hades.models.rtl.RegBank
 * 
 * standard two-read one-write port register bank, with write-enable
 * and clock input for writing.
 *
 * 23-10-2014 - MK.Computer.Organization.and.Design.4th.Edition.Oct.2011, P.360
 *
 * (C) T.T. Almeida, thales.almeida@ufv.edition_5.br
 */ 

package ufv.edition_5.fig421;

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
 * RegBank - a generic register band with two-read ports (X,Y)
 * and one-write (Z) port with n words by m bits.
 * It has a global active-low write-enable and an edge-sensitive 
 * clock for writing.
 * <p>
 * Note that the model currently does not check for timing violations
 * during write cycles.
 * <p>
 */
public class  RegBank
       extends  hades.models.rtlib.memory.RegBank {

	protected PortStdLogicVector port_ADebug,port_DDebug;
  
  /**
   * RegBank constructor
   */
  public RegBank() {
    super();
    constructPorts();
  }


  protected void constructPorts() {
int  na   = getAddressBusWidth();
    port_AX   = new PortStdLogicVector( this, "ReadRegister1", Port.IN,  null, na );
    port_AY   = new PortStdLogicVector( this, "ReadRegister2", Port.IN,  null, na );
    port_AZ   = new PortStdLogicVector( this, "WriteRegister", Port.IN,  null, na );
    
    port_ADebug   = new PortStdLogicVector( this, "ReadDebug", Port.IN,  null, na );

    port_DX   = new PortStdLogicVector( this, "ReadData1", Port.OUT, null, n_bits );
    port_DY   = new PortStdLogicVector( this, "ReadData2", Port.OUT, null, n_bits );
    port_DDebug   = new PortStdLogicVector( this, "DataDebug", Port.OUT, null, n_bits );
    port_DZ   = new PortStdLogicVector( this, "WriteData", Port.IN,  null, n_bits );

    port_nWE  = new PortStdLogic1164(   this, "RegWrite", Port.IN, null );
    port_CLK  = new PortStdLogic1164(   this, "CLK", Port.IN, null );

    ports = new Port[10];
    ports[0] = port_AX;
    ports[1] = port_AY;
    ports[2] = port_AZ;
    ports[3] = port_DX;
    ports[4] = port_DY;
    ports[5] = port_DZ;
    ports[6] = port_nWE;
    ports[7] = port_CLK;
    ports[8] = port_ADebug;
    ports[9] = port_DDebug;
  }


  /**
   * evaluate(): 
   * If either port_CLK or port_nWE are undefined, the RegBank data is 
   * invalidated, and the DO1 value is undefined.
   * <p>
   * Warning: The RegBank model currently does not check for timing violations
   * or address glitches during write cycles. Instead, the corresponding
   * memory locations are written.
   */
  public void evaluate( Object arg ) {
    if (debug) System.err.println( toString() + ".evaluate()" );

    double          time        = simulator.getSimTime() + t_access;

    StdLogicVector  vector_AX   = port_AX.getVectorOrUUU();
    StdLogicVector  vector_AY   = port_AY.getVectorOrUUU();
    StdLogicVector  vector_AZ   = port_AZ.getVectorOrUUU();
    StdLogicVector  vector_ADebug   = port_ADebug.getVectorOrUUU();
    
    StdLogicVector  vector_DX   = null;
    StdLogicVector  vector_DY   = null;
    StdLogicVector  vector_DDebug   = null;
    StdLogicVector  vector_DZ   = port_DZ.getVectorOrUUU();

    StdLogic1164    value_nWE   = port_nWE.getValueOrU();
    StdLogic1164    value_CLK   = port_CLK.getValueOrU();


    if (!value_nWE.is_01()) {
      message( "-W- " + toString() +
               "nWE undefined: data loss would occur! Ignoring..." );
    }
    else if (!value_CLK.is_01()) {
      message( "-W- " + toString() +
               "CLK undefined: data loss would occur! Ignoring..." );
    }
    else if (vector_AZ.has_UXZ()) { 
      message( "-W- " + toString() +
               "AZ address undefined: data loss would occur! Ignoring..." );
    }
    else {
      SignalStdLogic1164  clk = (SignalStdLogic1164) port_CLK.getSignal();
      if (value_nWE.is_1() && clk != null && clk.hasRisingEdge()) {
        int  addr_z = (int) vector_AZ.getValue();
        long old_z  =       getDataAt( addr_z);
        long data_z =       vector_DZ.getValue();
 
        setDataAt( addr_z, data_z );
        notifyWriteListeners( addr_z, old_z, data_z );
      }
    }

    //
    // read two values: DX = regbank[AX], DY = regbank[AY]
    //
    if (vector_AX.has_UXZ()) {
      vector_DX   = vector_UUU.copy();
    }
    else {
      int   addr_x = (int) vector_AX.getValue();
      long  data_x = getDataAt( addr_x );
      vector_DX    = new StdLogicVector( n_bits, data_x );
      notifyReadListeners( addr_x, data_x );

      schedule( port_DX, vector_DX, time + t_access );
    }

    if (vector_AY.has_UXZ()) {
      vector_DY    = vector_UUU.copy();
    }
    else {
      int   addr_y = (int) vector_AY.getValue();
      long  data_y = getDataAt( addr_y );
      vector_DY    = new StdLogicVector( n_bits, data_y );
      notifyReadListeners( addr_y, data_y );

      schedule( port_DY, vector_DY, time + t_access );
    }
    
    if (vector_ADebug.has_UXZ()) {
      vector_DDebug    = vector_UUU.copy();
    }
    else {
      int   addr_y = (int) vector_ADebug.getValue();
      long  data_y = getDataAt( addr_y );
      vector_DDebug    = new StdLogicVector( n_bits, data_y );
      notifyReadListeners( addr_y, data_y );

      schedule( port_DDebug, vector_DDebug, time + t_access );
    }
    
  }



    
}/* RegBank.java */ 
