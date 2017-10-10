/* IpinVector.java --  class hades.models.rtlib.io.IpinVector
 *
 * 03.07.02 - implemented assign()
 * 22.02.02 - changed behaviour for mouse clicks: normal/shift/cntl
 * 09.06.00 - request formattedValue in configure()
 * 07.09.98 - schedule initial vector value during elaborate()
 * 28.08.98 - number-formats (dec/hex/bin), separated numerical from UXZ input
 * 05.07.98 - inherit from GenericRtlibObject
 * 26.06.98 - got evaluate() method working
 * 22.06.98 - first version, copied from hades.models.io.Ipin
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.altera.cicloneiiep2c35f672c6n;

import hades.simulator.*;
import hades.models.*;
import hades.models.rtlib.io.*;
import hades.signals.*;
import hades.symbols.*;
import hades.utils.*;

import jfig.objects.*;
import java.awt.Color;
import java.util.Enumeration;


/** 
 * IpinVector - a simple switch to specify bus values from Integer values.
 * The width of the corresponding bus is specified by an IpinVector 
 * attribute and cannot be changed once a Signal (or the matching width)
 * is connected. The default width is 16 bits.
 * <p>
 * Also, a IpinVector object can be used as an input
 * for a hierarchical Design.
 *
 * @author F.N.Hendrich
 * @version 0.2  05.07.98
 */
public class  sw 
       extends  hades.models.rtlib.GenericRtlibObject
       implements  InputConnector, Assignable {

  protected Port           port_Y, port_toplevel;


  protected int            numberFormat = 0;
  protected double         t_delay  = 1E-9; // 1nsec, might also use 0.0




  /**
   * IpinVector(): simple constructor
   */
  public sw() {
    super();
    setDelay( t_delay );
  }

  //override
  public void setSymbol( Symbol s ) {
    this.symbol = s;

    symbol.setInstanceLabel( "sw0-7" );
    initValueLabel();
    updateSymbol();
  }


  public void constructPorts() {
    ports = new Port[2];
    ports[0] = new Port(this, "Y", Port.OUT, null, 
                        SignalStdLogicVector.class, new Integer(n_bits) );
    ports[1] = new Port(this, name, Port.TOPLEVEL_IN, null,
                        SignalStdLogicVector.class, new Integer(n_bits) );
    port_Y = ports[0];
    port_toplevel = ports[1];
  }





  public void setName( String name ) {
    super.setName( name );
    ports[1].setName( name );
  }




  public void setValue( String s ) {
    if (debug) message( "-#- IpinVector.setValue: " + s );
    //super.setValue( s );  // not necessary here any longer FNH 03.01.00
    vector = new StdLogicVector( n_bits );
    vector.parse( s );
    schedule();
  }






  /**
   * mousePressed: increment (click) or decrement (shift+click) the output
   * value of this IpinVector.  
   * <p>
   * <-- old way of doing things
   * To allow efficient input of the nonstandard values UUU, XXX, ZZZ, 
   * we use the following convention. If the mouse click is in the rightmost
   * part of the IpinVector symbol, the output uses the sequence
   * --> 
   * To allow efficient input of the values UUU, XXX, ZZZ, we check
   * for the <ctnl> modifier key. If this is used for the mouse click,
   * 
   * 0xvalue : 0xZZZ : 0xXXX : 0xUUU : 0xZZZ, and in reverse order
   * for a shift+click: 0xvalue : 0xUUU : 0xXXX : xZZZ : 0xUUU.
   * 
   * <p>
   * A normal mouse click in the IpinVector symbol, however, 
   * increments through the standard numerical values (use <shift>+<click>
   * for decrements): 0x<value> -> 0x<value+1) -> 0x<value+2) ... -> 0x000.
   */
  public void mousePressed( java.awt.event.MouseEvent me ) {
    if (debug) message( "IpinVector mousePressed!, value=" + vector);

    long  maxint = vector.getBitMask();
    long  value  = vector.getValue();

    if (isUXZClick( me )) {
      if (me.isShiftDown()) { // decrement: <any> -> UUU -> XXX -> ZZZ -> UUU
       if (value == StdLogicVector.UNDEFINED_UUU)
          value = StdLogicVector.INVALID_XXX;   
       else if (value == StdLogicVector.INVALID_XXX)
          value = StdLogicVector.TRISTATE_ZZZ;  
       else if (value == StdLogicVector.TRISTATE_ZZZ)
          value = StdLogicVector.UNDEFINED_UUU;  
       else
          value = StdLogicVector.UNDEFINED_UUU;  
      }
      else { // increment: <any> -> ZZZ -> XXX -> UUU -> ZZZ
       if (value == StdLogicVector.TRISTATE_ZZZ)
          value = StdLogicVector.INVALID_XXX;
       else if (value == StdLogicVector.INVALID_XXX)
          value = StdLogicVector.UNDEFINED_UUU;  
       else if (value == StdLogicVector.UNDEFINED_UUU)
          value = StdLogicVector.TRISTATE_ZZZ;
       else 
          value = StdLogicVector.TRISTATE_ZZZ;
      }
    }
    else { // standard click: increment/decrement numerical value, wraparound
      if (me.isShiftDown()) { // value -> value-1
        if (vector.has_UXZ()) value = maxint;
        else                  value = (value - 1) & maxint;
      }
      else {
        if (vector.has_UXZ()) value = 0;
        else                  value = value + 1; 
      }
    }
    vector = new StdLogicVector( n_bits, value );
    schedule();
  }


  public void schedule() {
    if (parent == null) return;

    SimKernel  simulator = parent.getSimulator();
    if (simulator == null) return;

    double     time      = simulator.getSimTime() + delay;

    simulator.scheduleInteractiveEvent(
        new WakeupEvent( this, time, this )); // update value label at time

    Signal signal_Y;
    if ((signal_Y = port_Y.getSignal()) == null) return;
    SimEvent se = 
      new SimEvent( signal_Y, simulator.getSimTime()+delay, vector, port_Y );
    simulator.scheduleInteractiveEvent( se );
  }


  public boolean isUXZClickOLD( java.awt.event.MouseEvent evt ) {
    java.awt.Point pos = symbol.getPos();
    java.awt.Point mousepos = ((jfig.canvas.FigCanvasEvent) evt).
                                               getWorldCoordinatePoint();

    int   y_offset = mousepos.y - pos.y;
    int   x_offset = mousepos.x - pos.x;

    if (x_offset < 0 && x_offset > -1200) return true;
    else                                  return false;
  }


  public boolean isUXZClick( java.awt.event.MouseEvent evt ) {
    return (evt.isControlDown());
  }



  /**
   * elaborate():
   */
  public void elaborate( Object arg ) {
    if (debug) System.err.println( toString() + ".elaborate()" );

    simulator = parent.getSimulator();
    updateSymbol();

    Signal  signal_Y = port_Y.getSignal();
    if (simulator != null && vector != null && signal_Y != null) {
      simulator.scheduleEvent( new SimEvent( signal_Y, 0.0, vector, port_Y ));
    }
  }



  /**
   * evaluate(): for an IpinVector, this method is only called in hierarchical
   * designs on any change on the toplevel input. Propagate the toplevel
   * value to the IpinVector's design.
   *
   * @param arg An arbitrary and unused object
   */
  public void evaluate( Object arg ) {
    if (debug) message( toString() + ".evaluate()" ); 
  
    Signal signal_toplevel, signal_Y;

    if ((signal_toplevel = port_toplevel.getSignal()) == null) {
       message( "-E- " + toString() + ".evaluate: toplevel Signal is null??" );
       return;
    } 

    vector = ((StdLogicVector) signal_toplevel.getValue()).copy();

    SimKernel  simulator = parent.getSimulator();
    double     time      = simulator.getSimTime() + delay;

    if (enableAnimationFlag) {
      simulator.scheduleWakeup( this, time, this ); // update value label 
    }
    if ((signal_Y  = port_Y.getSignal()) != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_Y, time, vector, port_Y ));
    }
  }


  public void assign( String value, double simTime ) {
    try {
      Signal signal_Y = null;
      StdLogicVector tmp = new StdLogicVector( n_bits );
      tmp.parse( value );

      if ((signal_Y = port_Y.getSignal()) != null) {
        simulator.scheduleEvent(
          new SimEvent( signal_Y, simTime, tmp, port_Y ));
      }
      if (visible) simulator.scheduleWakeup( this, simTime, tmp );
    }
    catch( Exception e ) {
      System.err.println( "-E- " + toString() + ".assign: " + value );
    }
  }


  /**
   * wakeup:  react to wakeup events. 
   * We have to override the wakeup method from GenericRtlibObject
   * in order to support WakeupEvents generated by previous calls to
   * assign(). 
   * To this end, assign() schedules a WakeupEvent which contains the
   * new "vector" output value as its optional argument. We extract
   * that value and call setValue() before calling updateSymobl().
   */
  public void wakeup( Object arg ) {
    try {
      WakeupEvent    evt = (WakeupEvent) arg;
      Object         tmp = evt.getArg();

      if (tmp instanceof StdLogicVector) { // called via assign: update vector
        StdLogicVector slv = (StdLogicVector) tmp;
        vector = slv.copy();
      }
      else { // 'traditional' wakeup: do nothing here, just update the symbol
        ;
      }
    }
    catch( Exception e ) {
      System.err.println( "-E- " + toString() + ".wakeup: " + arg );
    }
    if (enableAnimationFlag) updateSymbol();
  }



  /**
   * initialize an IpinVector object from a String,
   * which contains the integer version id, the width of this vector,
   * the start value(e.g. "1001 8 01ZXW001" for an 8 bit vector),
   * the default delay, and the default number format (dec=0,hex=1,bin=2).
   */
  public boolean initialize( String s ) {
    StringTokenizer st = new StringTokenizer( s );
    int n_tokens = st.countTokens();
    try {
      versionId  = Integer.parseInt( st.nextToken() );
      n_bits     = Integer.parseInt( st.nextToken() );

      constructStandardValues();
      constructPorts();

      setValue( st.nextToken() );
      setDelay( st.nextToken() );

      numberFormat = Integer.parseInt( st.nextToken() ); 
    }
    catch( Exception e ) {
      message( "-E- " + toString() + ".initialize(): " + e + " " + s );
    }
    return true;
  }


  /**
   * write the following data to PrintWriter ps:
   * our versionId, the width (n_bits), the current value, the default delay 
   * (propagation delay from the next lower hierarchy level, might be zero),
   * and the default label format (dec=0/hex=1/bin=2)
   */
  public void write( java.io.PrintWriter ps ) {
    ps.print(   " " + versionId
              + " " + n_bits
              + " " + vector.toBinString() 
              + " " + delay
              + " " + numberFormat
    );
  }



  public void configure() {
    if (debug) message( "-I- starting to configure this " + toString() );
    String[] fields = { "instance name:",             "name",
                        "number or bits:    [1 .. 32]:", "width",
                        "output value    [00..0X10_b]:", "formattedValue",
                        "output delay           [sec]:", "delay",
                        "format [dec=0/hex=1/bin=2]:", "numberFormat" };

    propertySheet = hades.gui.PropertySheet.getPropertySheet( this, fields );
    propertySheet.setHelpText(
      "Specify instance name, bus width, delay\n"
     + "and the output value. Recognized formats:\n"
     + "binary: 0100ZXH0_b,\n"
     + "hex:    0x0123 or abcd_h\n"
     + "decimal: 4711\n"
    );
    propertySheet.enableUpdateButton( true );
    propertySheet.setVisible( true );
  }






  /**
   * construct a (short) tool tip message.
   */
  public String getToolTip( java.awt.Point position, long millis ) {
    return getClass().getName() + "   " + getName() + "\n" 
           + "value<" + (n_bits-1) + ":0>= " + vector.toHexString() + "\n"
           + "(mouse click: increment | <shift>+click: decrement)" + "\n"
           + "(<cntrl>+click: toggle UXZ values)";
  }




} 

/* end IpinVector.java */
