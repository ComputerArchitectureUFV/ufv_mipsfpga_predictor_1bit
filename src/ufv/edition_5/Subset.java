/* Subset.java -  class hades.models.rtlib.io.Subset
 *
 * 24.08.98 - modified position of subsetLabel
 * 07.07.98 - first try, copied from models.rtlib.arith.Incr
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package ufv.edition_5;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.utils.StringTokenizer;
import hades.models.rtlib.io.*;

import java.awt.Color;
import java.util.Enumeration;



/** 
 * Subset - extract a subset of bits from a SignalStdLogicVector
 */
public class Subset 
       extends  hades.models.rtlib.GenericRtlibObject {

  protected  Port            port_A, port_Y;
  protected  StdLogicVector  value_A;

  protected  Label           subsetLabel;

  protected  int  n_subset;
  protected  int  upper = 3;
  protected  int  lower = 0;


  public Subset() { 
    super();
  }


  public void constructPorts() {
    setBounds();

    ports = new Port[2];
    ports[0] = new Port(this, "A", Port.IN, null, 
                        SignalStdLogicVector.class, new Integer(n_bits) );
    ports[1] = new Port(this, "Y", Port.OUT, null, 
                        SignalStdLogicVector.class, new Integer(n_subset) );
    port_A   = ports[0]; 
    port_Y   = ports[1]; 

    vector = new StdLogicVector( n_subset );
  }





  public void setSymbol( Symbol s ) {
    symbol = s;
    symbol.setInstanceLabel( getName() );

    subsetLabel = new Label();
    subsetLabel.initialize( "0 450 2 <3:0>" );
    symbol.addMember( subsetLabel );
    symbol.update_bbox();
  }


  public int getUpper() { 
    return upper;
  }

  public int getLower() {
    return lower;
  }

  public void setUpper( String s) {
    try                  { upper = Integer.parseInt( s ); }
    catch( Exception e ) { upper = n_bits-1; }

    constructPorts();
    updateSymbol();
    evaluate( null );
  }


  public void setLower( String s) {
    try                  { lower = Integer.parseInt( s ); }
    catch( Exception e ) { lower = n_bits-1; }
  
    constructPorts();
    updateSymbol();
    evaluate( null );
  }


  private void setBounds() {
    if (lower < 0)        lower = 0;
    if (lower >= n_bits)  lower = n_bits-1;

    if (upper < lower)    upper = lower;
    if (upper >= n_bits)  upper = n_bits-1;

    n_subset = upper-lower+1;
  }




  /**
   *  evaluate(): called by the simulation engine on all events that
   *  concern this object. The object is responsible for updating its
   *  internal state and for scheduling all pending output events.
   */
  public void evaluate( Object arg ) {
    if (debug) System.err.println( toString() + ".evaluate()" ); 

    Signal signal_A, signal_Y;

    if      ((signal_A = port_A.getSignal()) == null) {
      vector = new StdLogicVector( n_subset );
    }
    else { // extract subset from signal_A

      value_A = (StdLogicVector) signal_A.getValue();
      vector  = value_A.subset( upper, lower );
    }

    SimKernel  simulator = parent.getSimulator();
    double     time      = simulator.getSimTime() + delay; 

    simulator.scheduleWakeup( this, time, this ); // update value label at time
    if ((signal_Y  = port_Y.getSignal()) != null) {
      simulator.scheduleEvent(
         new SimEvent( signal_Y, time, vector, port_Y ));
    }
  }

  
  public void updateSymbol() {
    if (subsetLabel != null) {
      subsetLabel.setText( "<" + upper + ":" + lower + ">" );
    }
  }


  /**
   * write the following data to PrintWriter ps:
   * our versionId, the width (n_bits), the upper and lower subset indices,
   * the default output value (vector), and the default delay.
   * Hopefully, many RTLIB classes won't need to override write().
   */
  public void write( java.io.PrintWriter ps ) {
    ps.print(   " " + versionId
              + " " + n_bits
              + " " + upper + " " + lower
              + " " + vector.toBinString()
              + " " + delay );
  }


 /**
   * initialize a RTLIB object from a String
   * which contains the integer version id, the width of the input vector,
   * the upper and lower subset indices, 
   * a String of the desired start value
   * (e.g. "1001 8 01ZXW001" for an 8 bit vector),
   * and the default delay.
   */
  public boolean initialize( String s ) {
    StringTokenizer st = new StringTokenizer( s );
    int n_tokens = st.countTokens();
    try {
        versionId  = Integer.parseInt( st.nextToken() );
        n_bits     = Integer.parseInt( st.nextToken() );

        upper      = Integer.parseInt( st.nextToken() );
        lower      = Integer.parseInt( st.nextToken() );

        constructStandardValues();
        constructPorts();

        setValue( st.nextToken() );
        setDelay( st.nextToken() );
    }
    catch( Exception e ) {
      message( "-E- " + toString() + ".initialize(): " + e + " " + s );
    }
    return true;
  }




  public void configure() {
    if (debug) message( "-I- starting to configure this " + toString() );
    String[] fields = { "instance name:",             "name",
                        "number or bits:    [1 .. 32]:", "width",
                        "output value    [00..0X10_b]:", "value",
                        "output delay           [sec]:", "delay", 
                        "upper subset index [31..lower]:", "upper",
                        "lower subset index  [upper..0]:", "lower", 
    };

    propertySheet = hades.gui.PropertySheet.getPropertySheet( this, fields );
    propertySheet.setHelpText(
      "Specify instance name, bus width, delay,\n"
      + "output value\n"
      + "and the upper and lower subset indices" 
    );
    propertySheet.setVisible( true );
  }


} 

/* end Subset.java */
