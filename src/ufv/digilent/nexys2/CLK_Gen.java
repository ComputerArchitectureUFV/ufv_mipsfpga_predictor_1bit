/* StringDisplay.java --  class hades.models.string.StringDisplay
 *
 * a stub class to display Strings in its Symbol
 *
 * 30.03.98 - first version
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 * 
 * 24/05/2016
 * String Debug Component. - UFV MG Brasil
 * Jeronimo Costa Penha - jeronimopenha@gmail.com
 */ 

package ufv.digilent.nexys2;

import hades.simulator.*;
import hades.signals.*;
import hades.models.*;
import hades.symbols.*;
import hades.gui.*;
import hades.utils.StringTokenizer;

import java.awt.event.*;


/** 
 * StringDisplay - a SimObject to display Strings in its Symbol
 *               Signals are expected to be StringSignals.
 *
 * @author F.N.Hendrich
 * @version 0.1  25.03.98
 */
public class CLK_Gen 
       extends SimObject
       implements Simulatable, java.io.Serializable {


  protected   Port       port_A;
  protected   Port       port_EN;
  protected   Port       port_RES;
  protected   Port       port_CLK;
  
  protected   String     string;

  protected   Label      stringLabel;
  protected   Rectangle  background;
  
  protected   StdLogicVector value_A;
  
  protected   double     t_delay;
  protected   int        reseted, total,done;

  public CLK_Gen() { 
    super();
    
    port_A = new PortStdLogicVector(this, "A", Port.IN,  null, 8);
    port_EN = new PortStdLogic1164(this, "EN",  Port.IN, null );
    port_RES = new PortStdLogic1164(this, "RES",  Port.IN, null );
    port_CLK = new PortStdLogic1164(this, "CLK",  Port.OUT, null );
    
    
    ports = new Port[4];
    ports[0] = port_A;
    ports[1] = port_EN;
    ports[2] = port_RES;
    ports[3] = port_CLK;
    
    string = "0_D";
    
    t_delay = 0.5;
    
    reseted = 0;
    
    total = 0;
    
    done = 0;
  }

  //OVERRIDE
  public void setSymbol( Symbol symbol ) {
    this.symbol = symbol;
    this.symbol.setInstanceLabel( "CLK_GEN" );
  }

  public boolean needsDynamicSymbol() {
    return true;
  }


  public void constructDynamicSymbol() {
      symbol = new Symbol();
      symbol.setParent( this );

      BboxRectangle bbr = new BboxRectangle();
      bbr.initialize( "0 0 4200 1200" );
      
      InstanceLabel ilabel = new InstanceLabel();
      ilabel.initialize( "4400 800 " + getName() );
      
      //símbolos dos ports
      PortSymbol portsymbola = new PortSymbol();
      portsymbola.initialize( "2400 1200 A" );
      
      PortSymbol portsymbolclk = new PortSymbol();
      portsymbolclk.initialize( "4200 600 CLK" );
      
      PortSymbol portsymbolen = new PortSymbol();
      portsymbolen.initialize( "1200 1200 EN" );
      
      PortSymbol portsymbolres = new PortSymbol();
      portsymbolres.initialize( "1800 1200 RES" );
      
      //nomes dos ports no desenho
      PortLabel  portlabelA = new PortLabel();
      portlabelA.initialize( "2600 1600  A" );
      
      PortLabel  portlabelclk = new PortLabel();
      portlabelclk.initialize( "4400 1000 CLK" );
      
      PortLabel  portlabelen = new PortLabel();
      portlabelen.initialize( "1000 1600 EN" );
      
      PortLabel  portlabelres = new PortLabel();
      portlabelres.initialize( "1800 2000 RES" );
      
      stringLabel = new ColorLabel();
      stringLabel.initialize( "400 800 " + getString() );
     
      background = new Rectangle();
      background.initialize( "0 0 4200 1200" );
      jfig.objects.FigAttribs attr = background.getAttributes();
      attr.currentLayer = 50;
      attr.lineColor = null;
      attr.fillColor = java.awt.Color.white;
      attr.fillStyle = attr.SOLID_FILL;
      background.setAttributes( attr );

      Polyline border = new Polyline();
      border.initialize( "5 0 0 4200 0 4200 1200 0 1200 0 0" );

    
      symbol.addMember( ilabel );
      symbol.addMember( portsymbola );
      symbol.addMember( portsymbolclk );
      symbol.addMember( portsymbolen );
      symbol.addMember( portsymbolres );
      symbol.addMember( portlabelA );
      symbol.addMember( portlabelclk );
      symbol.addMember( portlabelen );
      symbol.addMember( portlabelres );
      symbol.addMember( stringLabel );
      symbol.addMember( background );
      symbol.addMember( border );
      symbol.addMember( bbr );
  }
  
  public void setString( String s ) {
    this.string = s;
    stringLabel.setText( s + "_D" );
    getSymbol().painter.paint( getSymbol(), 100 );
  }

  public String getString() {
    return this.string;
  }


  
  public void elaborate( Object arg ) {
  }
  
  private int bin_to_int(String im, int l){//converte binario para decimal
    int soma = 0,cont = 0;
    for (int i = l; i >0 ; i--) {
      if (im.substring(i-1, i).equals("1")) {
	soma += (int) Math.pow(2, cont);
      }
            cont ++;
    }
    return soma;
  }
  
  public void evaluate( Object arg ) {
    if (debug) message( "-I- StringDisplay.evaluate: " + arg );
    
    Signal signal_A = port_A.getSignal();
    Signal signal_EN = port_EN.getSignal();
    Signal signal_RES = port_RES.getSignal();
    
    if (signal_RES == null) return;
    
    
    StdLogic1164 reset = (StdLogic1164) signal_RES.getValue();
    
    int posicao=0, i, tamanho;
    String temp,str="";
      
    int reset_val = (int) reset.getValue();
    
    if (reset_val == 2){
      reseted = 1;
      total = 0;
      
      
      Signal signalOut = port_CLK.getSignal();
      if (signalOut == null) return;
      double time = simulator.getSimTime() + t_delay;
      
      StdLogic1164 next_Out = new StdLogic1164(2);
      
      simulator.scheduleEvent( SimEvent1164.createNewSimEvent( signalOut, time, next_Out, port_CLK ) );
      str = Integer.toString(total);
      setString( str );
      return;
    }
    
    if (signal_A == null) return;
    if (signal_EN == null) return;
    
    StdLogic1164 enable = (StdLogic1164) signal_EN.getValue();
    SignalStdLogic1164  signal_temp;
    
    int en_val = (int) enable.getValue();
    
    signal_temp = (SignalStdLogic1164) port_EN.getSignal(); 
    
    if (en_val == 3 && reseted == 1 && done == 0){
      done = 1;
      
      value_A = (StdLogicVector) signal_A.getValue();
      
      if (value_A.has_UXZ()){
	return;
      }else{
	temp = value_A.toString();
      }
      tamanho = temp.length();
      for(i = 0; i < tamanho; i++){  //busca o índice do primeiro bit na string
	if (temp.substring(i,i+1).equals(":")){  
	  posicao = i+1;  
	  break;
	} 
      }
      i++;
      
      int vezes = bin_to_int(temp.substring(i,i+8),8);
      StdLogic1164 next_Out;
      Signal signalOut;
      double time;
      next_Out = new StdLogic1164();
      
      total += vezes;
      
      str = Integer.toString(total);
      
      time = simulator.getSimTime() + t_delay;
      
      signalOut = port_CLK.getSignal();
      if (signalOut == null) return;
      
      for(i = 0; i < vezes; i++){
	
	next_Out = new StdLogic1164(3);
	simulator.scheduleEvent( SimEvent1164.createNewSimEvent( signalOut, time, next_Out, port_CLK ) );
	time += t_delay;
	
	next_Out = new StdLogic1164(2);
	simulator.scheduleEvent( SimEvent1164.createNewSimEvent( signalOut, time, next_Out, port_CLK ) );
	time += t_delay;
	
      }
      
      setString( str );
      
    }else if (en_val == 2 && reseted == 1 && done == 1){
      done = 0;
    }
  }



  public void configure() {
    if (debug) message( "-I- starting to configure this " + toString() );
    String[] fields = { "instance name:", "name",
                        "current value: ",  "string",
                      }; 

    propertySheet = hades.gui.PropertySheet.getPropertySheet( this, fields );
    propertySheet.setHelpText(  "Specify instance name and gate delay:" );
    propertySheet.setVisible( true );
  }




  /**
   * initialize a StringDisplay from a String that contains the integer 
   * version id of this gate and its propagation delay (in seconds), e.g.
   * '1001 0.5E-8'. 
   */
  public boolean initialize( String s ) {
    return true;
  }


  public void write( java.io.PrintWriter ps ) {
    ps.print( " " + versionId );
  }




  /** 
   *  toString() - the usual info method 
   */
  public String toString() {
    return "StringDisplay: " + getFullName();
  }
} 

/* end StringDisplay.java */
