/* RegR.java --  class hades.models.rtlib.register.RegR
 *
 * an edge-triggered n-bit D-type register with reset (active low)
 *
 * 28.07.99 - added vectorOutputPort for setValue()
 * 10.09.98 - use scheduleAfter() etc.
 * 03.09.98 - first version, copied from RegE
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.edition_5;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.gui.*;
import hades.utils.StringTokenizer;

import java.awt.*;
import java.util.Enumeration;


/** 
 * RegR - an edge-triggered n-bit d-type register with active-low reset.
 * Signals are expected to be of type SignalStdLogicVector for D and Q,
 * and SignalStdLogic1164 for CLK and NR.
 * <p>
 * The RegR model will not check for setup-violations. 
 * <p>
 * @author F.N.Hendrich
 * @version 0.3  03.09.98
 */
public class  RegPC 
       extends  hades.models.rtlib.register.RegR 
       implements  Assignable
{

  protected   PortStdLogicVector  port_D, port_Q;
  protected   PortStdLogic1164    port_CLK, port_NR;

  protected   double t_setup = 5.0E-9;


  public RegPC() { 
    super();
  }


} 

/* end RegR.java */
