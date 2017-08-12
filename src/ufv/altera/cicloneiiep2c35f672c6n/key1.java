/* Ipin.java --  class hades.models.io.Ipin
 *
 * 02.07.02 - check for visible in assign()
 * 01.07.02 - re-encode states using the StdLogic1164 encoding
 * 01.07.02 - implement assign(String,double) and wakeup()
 * 01.04.01 - speed up evaluate a little bit
 * 22.11.99 - added setValueAtTime( StdLogicVector value, double time )
 * 20.10.98 - inherit from GenericGate, use PortStdLogic1164, recycle events
 * 19.10.98 - recycle events - use createNewSimEvent instead of new SimEvent
 * 01.12.97 - use 'scheduleInteractiveEvent' for user events
 * 14.11.97 - use port_xxx as source in scheduleEvent
 * 03.11.97 - consistent elaborate(): resets to state 'U'
 * 27.10.97 - implements InputConnector
 * 01.07.97 - added four state 0-1-Z-U with 'shift-click'
 * 13.06.97 - added reaction to MouseEvents
 * 12.06.97 - first try a' la diglog
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.altera.cicloneiiep2c35f672c6n;

import hades.simulator.*;
import hades.models.*;
import hades.models.io.*;
import hades.signals.*;
import hades.symbols.*;
import hades.utils.*;

import jfig.objects.*;
import java.awt.Color;
import java.util.Enumeration;


/** 
 * Ipin - a simple subclass of GenericGate that models an switch. 
 * Also, a Ipin object is automatically interpreted as an input
 * for a hierarchical Design - like a standard-CAD 'ipin'.
 *
 * @author F.N.Hendrich
 * @version 0.1  12.06.97
 */
public class key1 
       extends hades.models.gates.GenericGate 
       implements InputConnector, Assignable, Wakeable {

  protected int            state = StdLogic1164._U;

  protected PortStdLogic1164  port_Y, port_toplevel;
  protected StdLogic1164      output_1, output_0, output_Z, output_X, output_U;
  protected StdLogic1164      startValue;

  protected double         delay = 0.0; // same time for toplevel and local

  // Graphics stuff
  protected ColoredCircle   showOnOff;


  /**
   * Ipin(): simple constructor
   */
  public key1() {
    super();

    port_Y        = new PortStdLogic1164( this, "Y",  Port.OUT, null );
    port_toplevel = new PortStdLogic1164( this, name, Port.TOPLEVEL_IN, null );

    ports         = new Port[2];
    ports[0]      = port_Y;
    ports[1]      = port_toplevel;

    startValue    = new StdLogic1164();
    output_U      = Const1164.__U;
    output_X      = Const1164.__X;
    output_0      = Const1164.__0;
    output_1      = Const1164.__1;
    output_Z      = Const1164.__Z;
  }


  public double getDelay() { 
    return delay; 
  }

  public void setDelay( String s ) {
    try {
      delay = Double.valueOf( s ).doubleValue();
    }
    catch( Exception e ) {
      message( "-E- Illegal delay value '" + s + "', using 0.0 sec..." );
      delay = 0.0;
    }
  }

  public String getStartValue() {
    return "" + startValue.getChar();
  }

  public void setStartValue( String s ) {
    if (s != null && s.length() > 0) {
      startValue = new StdLogic1164( s.charAt(0) );
    }
  }


  /** 
   * initialize an Ipin from a String, which contains the integer version id,
   * and optionally the start value of this Ipin (the latter encoded via
   * StdLogic1164.getChar() values, 'U' 'X' '0' ... 'D'), e.g.
   * "1001 U"
   */
  public boolean initialize( String s ) {
    StringTokenizer st = new StringTokenizer( s );
    int n_tokens = st.countTokens();
    try {
      if (n_tokens == 0) { 
        versionId = 1001;
      }
      else if (n_tokens == 1) {
        versionId  = Integer.parseInt( st.nextToken() );
        startValue = output_U;
      }
      else if (n_tokens == 2) {
        versionId = Integer.parseInt( st.nextToken() );

        char c = st.nextToken().charAt( 0 );
        startValue    = new StdLogic1164( c );
        setStartState();
      }
      else {
        throw new Exception( "invalid number of arguments" );
      }
    }
    catch( Exception e ) {
      message( "-E- Ipin.initialize(): " + e + " " + s );
    }
    return true;
  }


  /** 
   * write the versionID and the start value of this Ipin (the latter
   * as StdLogic1164.getChar() value), e.g. "1001 Z"
   */
  public void write( java.io.PrintWriter ps ) {
    ps.print( " " + versionId 
              + " " + startValue.getChar() );
  }




  public void setSymbol( Symbol s ) {
    this.symbol = s;
    symbol.setInstanceLabel( "key1" );
    initDisplay();
  }


  public void setName( String name ) {
    super.setName( name );
    ports[1].setName( name );
  }



  protected void initDisplay() {
    try {
      jfig.objects.FigObject tmp = null; FigAttribs attr; double rand;
      if (symbol == null)  return;

    
      for( Enumeration e=symbol.elements(); e.hasMoreElements(); ) {
        tmp = (jfig.objects.FigObject) e.nextElement();
        if (tmp instanceof ColoredCircle) {
          showOnOff = (ColoredCircle) tmp;
        }
      }

      setStartState();
      showState();
    }
    catch( Exception e ) {
      message( "-E- Ipin.initDisplay(): " + e );
      e.printStackTrace();
    }
  }

  public void setStartState() {
    state = startValue.intValue();   // we use StdLogic1164 encoding now

//    switch( startValue.intValue() ) {
//      case StdLogic1164._U : state = 3; break;
//      case StdLogic1164._0 : state = 0; break;
//      case StdLogic1164._1 : state = 1; break;
//      case StdLogic1164._Z : state = 2; break;
//      default              : state = 3; break;
//    }
  }


  public void showState() {
    if (!visible) return;

    FigAttribs attr = showOnOff.getAttributes();
    attr.fillStyle = attr.SOLID_FILL;
    if      (state == StdLogic1164._0) { 
                           attr.fillColor = output_0.getColor();
                           attr.fig_fill_color = 0; 
                           attr.fig_area_fill  = 5;
                         }
    else if (state == StdLogic1164._1) { 
                           attr.fillColor = output_1.getColor();
                           attr.fig_fill_color = 4; 
                           attr.fig_area_fill  = 20;
                         }
    else if (state == StdLogic1164._Z) { 
                           attr.fillColor = output_Z.getColor();
                           attr.fig_fill_color = 6; 
                           attr.fig_area_fill  = 20;
                         }
    else if (state == StdLogic1164._U) { 
                           attr.fillColor = output_U.getColor();
                           attr.fig_fill_color = 3; 
                           attr.fig_area_fill  = 20;
                         }
    else {
       message( "-E- Ipin.showState(): unsupported state=" + state );
       attr.fillColor = output_U.getColor();
    }
    showOnOff.setAttributes( attr );

    if (showOnOff.painter != null) showOnOff.painter.paint( showOnOff );
    //else message( "ObjectPainter is still null..." );
  }


  /**
   * handle a mouse click on this Ipin's graphical symbol.
   * Toggle the Ipins output value, schedule a corresponding output event
   * with the new output value, and update the graphical symbol.
   * For a normal mouse-click, the output value toggles between "0-1-0...",
   * while a shift-mouse-click toggles between "0-1-Z-U-0..." states.
   */
  public void mousePressed( java.awt.event.MouseEvent me ) {
    //message( "Ipin mousePressed!, state=" + state );
    if (me.isShiftDown()) { // 0-1-Z-U-0 transitions
      if      (state == StdLogic1164._0) state = StdLogic1164._1;
      else if (state == StdLogic1164._1) state = StdLogic1164._Z;
      else if (state == StdLogic1164._Z) state = StdLogic1164._U;
      else                               state = StdLogic1164._0;
    }
    else { // normal 0-1-0 transitions, other values map to 0
      if      (state == StdLogic1164._0) state = StdLogic1164._1;
      else                               state = StdLogic1164._0;
    }

    showState();                   // update graphical symbol now
    schedule( state, 5E-9, true ); // schedule interactive event after 5 nsec.
  }




  /** 
   *  elaborate(): This method is called by the simulation engine
   *  to initialize the simulation object.
   * @param arg This parameter is ignored by Ipin
   */
  public void elaborate( Object arg ) {
    if (debug) message( toString() + ".elaborate()" ); 

    simulator = parent.getSimulator();
    setStartState();
    schedule( state, 0.0E-9, false );
    showState();
  }



  protected void schedule( int state, double delay, boolean interactive ) {

    Signal signal_Y;
    if ((signal_Y = port_Y.getSignal()) == null) return;

    StdLogic1164 output;
    if      (state == StdLogic1164._0) output = output_0;
    else if (state == StdLogic1164._1) output = output_1;
    else if (state == StdLogic1164._Z) output = output_Z;
    else                               output = output_U;

    if (simulator == null) return;
    else {
      double time = simulator.getSimTime() + delay;
      SimEvent1164 se = SimEvent1164.
                       createNewSimEvent( signal_Y, time, output, port_Y );

      if (interactive) simulator.scheduleInteractiveEvent( se );
      else             simulator.scheduleEvent( se );
    }
  }


  /**
   * used to specify a new Ipin output value from an external source 
   * (e.g. a script). Note that this method only schedules an output
   * value, but does _not_ update the graphical representation of the
   * Ipin. This should probably be corrected...
   * @deprecated please use assign(String,time) instead
   */
  public void setValueAtTime( StdLogic1164 value, double time ) {

    Signal signal_Y = port_Y.getSignal();
    if (signal_Y == null) return;
    if (simulator == null) return;
    else {
      simulator.scheduleEvent( 
        SimEvent1164.createNewSimEvent( signal_Y, time, value, port_Y ));

    }
  }


  /**
   * used to specify a new Ipin output value from an external source 
   * (e.g. a script). 
   * Calling this method will result both in the corresponding output event
   * and an update/repaint of the Ipin graphical symbol at the specified
   * simulation time.
   * The 'value' String is expected to have a one-character encoding
   * of the StdLogic1164 value, namely "U" "X" "0" "1" "Z" "H" "L" "W" "D".
   * <p>
   * Internally, the latter graphical symbol update is implemented via
   * a Wakeup event for this Ipin.
   */
  public void assign( String value, double time ) {
    try {
      StdLogic1164 tmp = new StdLogic1164( value.charAt(0) );
      
      scheduleOutputValue( port_Y, tmp, time );
      if (visible) simulator.scheduleWakeup( this, time, tmp );
    }
    catch( Exception e ) {
      System.err.println( "-E- internal in Ipin.assign: " + e );
    }
  } 



  /**
   * evaluate: as interactive Ipin events are handled by mousePressed(),
   * this method is only called by the simulation engine whenever our
   * (hidden) toplevel signal changes.
   * Take the toplevel signal value and propagate to our local signal.
   */
  public void evaluate( Object arg ) {
    // if (port_Y.getSignal() == null) return; // output not connected

    StdLogic1164 value_Y = port_toplevel.getValueOrU();
    scheduleOutputValueAfter( port_Y, value_Y, delay );   
  }


  /**
   * wakeup: this method is currently used to handle "external" events
   * on this Ipin via the assign call,  which schedules a
   * Wakeup event to update the Ipin symbol at the correct simulation time.
   * To this end, the "arg" argument is expected to hold a WakeupEvent
   * whose arg member is a StdLogic1164 object.
   */
  public void wakeup( Object arg ) {
    // System.err.println( "-#- Ipin.wakeup: " + arg );
    try {
      WakeupEvent  we = (WakeupEvent) arg;
      StdLogic1164 sl = (StdLogic1164) we.getArg();
      state           = sl.intValue();
      showState();
    }
    catch( Exception e ) {
      System.err.println( "-E- " + toString() + ".wakeup: " + e );
    }
  }


  /**
   * configure: display a dialog to specify the Ipin instance name,
   * initial output value, and gate propagation delay.
   */
  public void configure() {
    if (debug) message( "-I- starting to configure this " + toString() );
    String[] fields = { "instance name:",          "name",
                        "start value  [U,0,1,Z]:", "startValue",
                        "output delay [sec]:",     "delay" };

    propertySheet = hades.gui.PropertySheet.getPropertySheet( this, fields );
    propertySheet.setHelpText("Specify instance name, start value, and delay:"); 
    propertySheet.setVisible( true );
  }

} 
