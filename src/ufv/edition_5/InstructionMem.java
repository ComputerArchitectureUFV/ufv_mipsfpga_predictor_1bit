/* ROM.java - hades.models.rtl.ROM
 *
 * 29.08.98 - check isConnected() before resize()ing, new Symbol
 * 27.08.98 - allow resize()ing of the memory array
 * 26.08.98 - first version 
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.edition_5;

import  hades.models.*;
import  hades.simulator.*;



/**
 * ROM - a generic ROM with n words by m bits.
 * <p>
 * This class models a standard LSI ROM without clocking or enable.
 * <p>
 *
 * @author  F.N.Hendrich
 * @version 0.4  23.02.98
 */
public class  InstructionMem 
       extends  hades.models.rtlib.memory.ROM {


  /**
   * ROM constructor
   */
  public InstructionMem() {
    super();
    constructPorts();
  }


  protected void constructPorts() {
    port_D = new PortStdLogicVector( this, "Inst", Port.OUT, null, n_bits );
    port_A = new PortStdLogicVector( this, "Addr", Port.IN, null, 
                                     getAddressBusWidth() );

    ports = new Port[2];
    ports[0] = port_A;
    ports[1] = port_D;
  }




} /* ROM.java */ 
