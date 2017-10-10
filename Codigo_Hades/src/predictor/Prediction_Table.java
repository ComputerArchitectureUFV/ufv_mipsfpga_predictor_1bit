/* RegBank.java - hades.models.rtl.RegBank
 * 
 * standard two-read one-write port register bank, with write-enable
 * and clock input for writing.
 *
 * 27.12.99 - first version (copied from RAM)
 *
 * (C) F.N.Hendrich, hendrich@informatik.uni-hamburg.de
 */ 

package predictor;

import  hades.models.*;
import  hades.signals.*;
import  hades.simulator.*;
import  hades.symbols.*;


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
public class  Prediction_Table 
       extends  GenericMemory {

  protected  PortStdLogicVector  port_PCF, port_PCD, port_DESTCD;
  protected  PortStdLogicVector  port_VP, port_DEST, port_DZ,port_nWE;
  protected  PortStdLogic1164    port_CLK,port_reset;

  public final static double  t_access       = 30.0E-9; // read access time
  public final static double  t_tristate     =  5.0E-9; // outputs -> Z
  public final static double  t_undefined    =  6.0E-9; // outputs -> X
  public final static double  t_setup        =  6.0E-9; // outputs -> X
  public final static double  t_min_we_cycle =  6.0E-9; // outputs -> X


  /**
   * RegBank constructor
   */
  public Prediction_Table() {
    super();
    constructPorts();
  }


  protected void constructPorts() {
    int  na   = getAddressBusWidth();
    port_PCF   = new PortStdLogicVector( this, "PCF", Port.IN,  null, 12 );
    port_PCD   = new PortStdLogicVector( this, "PCD", Port.IN,  null, 12 );
    port_DESTCD   = new PortStdLogicVector( this, "DESTCD", Port.IN,  null, 12 );

    port_VP   = new PortStdLogicVector( this, "VP", Port.OUT, null, 2 );
    port_DEST   = new PortStdLogicVector( this, "PRED", Port.OUT, null, 32 );
    port_DZ   = new PortStdLogicVector( this, "DZ", Port.IN,  null, n_bits );

    port_nWE  = new PortStdLogicVector(this, "nWE", Port.IN, null,2 );
    port_CLK  = new PortStdLogic1164(   this, "CLK", Port.IN, null );
    port_reset = new PortStdLogic1164(this,"R", Port.IN, null);

    ports = new Port[9];
    ports[0] = port_PCF;
    ports[1] = port_PCD;
    ports[2] = port_DESTCD;
    ports[3] = port_VP;
    ports[4] = port_DEST;
    ports[5] = port_DZ;
    ports[6] = port_nWE;
    ports[7] = port_CLK;
    ports[8]=port_reset;
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
   * elaborate(): On elaboration, the inputs of the RegBank will be undefined.
   * Correspondingly, the outputs are, too.
   */
  public void elaborate( Object arg ) {
    if (debug) message( toString() + ".elaborate()" ); 
    simulator = parent.getSimulator();

    if (simulator == null) return;
    if (port_VP.getSignal() != null) {
      simulator.scheduleEvent( 
        new SimEvent(port_VP.getSignal(), 0.0, vector_UUU.copy(), port_VP));
    }
    if (port_DEST.getSignal() != null) {
      simulator.scheduleEvent( 
        new SimEvent(port_DEST.getSignal(), 0.0, vector_UUU.copy(), port_DEST));
    }
//      resize(12, 18);
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

    StdLogicVector  pcf   = port_PCF.getVectorOrUUU();
    StdLogicVector  pcd   = port_PCD.getVectorOrUUU();
    StdLogicVector  destdc   = port_DESTCD.getVectorOrUUU();
    StdLogicVector  vp   = null;
    StdLogicVector  destino   = null;
    StdLogic1164 reset = port_reset.getValueOrU();

    StdLogicVector    value_nWE   = port_nWE.getVectorOrUUU();
    StdLogic1164    value_CLK   = port_CLK.getValueOrU();


//    if (value_nWE.equals(new StdLogicVector( 2, 0 ))) {
//      message( "-W- " + toString() +
//               "nWE undefined: data loss would occur! Ignoring..." );
//    }
//    else 
    if (!value_CLK.is_01()) {
      message( "-W- " + toString() +
               "CLK undefined: data loss would occur! Ignoring..." );
    }
    else if (pcd.has_UXZ()) { 
      message( "-W- " + toString() +
               "AZ address undefined: data loss would occur! Ignoring..." );
    }
    else {
        if (reset.getValue().equals(2)){
//            System.out.println("reset "+reset);
            vp   = new StdLogicVector( 2, 0 );
            destino   = new StdLogicVector( 32, 0 );
            
            schedule( port_VP, vp, time + t_access );
            schedule( port_DEST, destino, time + t_access );
        }
        else if(reset.getValue().equals(3)){
//            System.out.println("reset "+reset);
            SignalStdLogic1164  clk = (SignalStdLogic1164) port_CLK.getSignal();
            if (value_nWE.equals(new StdLogicVector(2,3)) && clk != null && clk.hasRisingEdge()) {

  //         value_nWE = 01 --> para incerir na tablela o que sim desvia
                int  addr_z = (int) pcd.subset(11, 4).getValue();
                long old_z  =       getDataAt( addr_z);

//                long new_tag = (old_z >> 12) & 15 ;
                long new_tag = pcd.subset(3, 0).getValue();
                long new_dest = destdc.getValue();
               
                long data_z = ((((1 << 1 | 1) << 4)| new_tag) << 12) | new_dest ;

                message( "-W- " + pcf );
                setDataAt( addr_z, data_z );
                notifyWriteListeners( addr_z, old_z, data_z );
                
            }else if (value_nWE.equals(new StdLogicVector(2,2))&& clk != null && clk.hasRisingEdge()){
                
  //          value_nWE = 10 --> para incerir na tablela o que nao desvia
                int  addr_z = (int) pcd.subset(11, 4).getValue();
                long old_z  =       getDataAt( addr_z);         
                long data_z = (1 << 1 | 0) | (old_z & 0xffff);

                setDataAt( addr_z, data_z );
                notifyWriteListeners( addr_z, old_z, data_z );
            }else if (value_nWE.equals(new StdLogicVector(2,1))&& clk != null && clk.hasRisingEdge()){
                int  addr_z = (int) pcd.subset(11, 4).getValue();
                long old_z  =       getDataAt( addr_z);
//                long data_z = 0 << 18;
                long data_z = 0 << 18;
                setDataAt( addr_z, data_z );
                notifyWriteListeners( addr_z, old_z, data_z );
            }
            
            if (pcf.has_UXZ()) {
                vp   = vector_UUU.copy();
                destino   = vector_UUU.copy();
            }
            else {
                System.out.println(pcf);
                int   linha = (int) pcf.subset(11, 4).getValue();
                System.out.println("Linha " + linha) ;
                long  data_x = getDataAt( linha );
                System.out.println("Data " + data_x) ;
                long cmp_tag = pcf.subset(3, 0).getValue();
                System.out.println("CPM_TAG " + cmp_tag) ;
                long tag = (data_x >> 12) & 15 ;
                System.out.println("TAG " + tag) ;
                
                    if (cmp_tag == tag){
                        long v_p = data_x >> 16;
                        long dest = data_x & 0xfff;
                        vp = new StdLogicVector( 2, v_p );
                        destino = new StdLogicVector(32,(dest << 2));
                    }else{
                        vp   = new StdLogicVector( 2, 0 );
                        destino = new StdLogicVector(32,0);
                    }

                schedule( port_VP, vp, time + t_access );
                schedule( port_DEST, destino, time + t_access );

                notifyReadListeners( linha, data_x );
            }
        }
    }

    //
    // read two values: DX = regbank[AX], DY = regbank[AY]
    //
    
  }


  public void schedule( Port port, Object value, double time ) {
    Signal signal = port.getSignal();
    if (signal != null) {
      simulator.scheduleEvent(  
        new SimEvent( signal, time, value, port ));
    }
  }
  
  @Override
    public boolean needsDynamicSymbol() {
        return true;
    }

    @Override
    public void constructDynamicSymbol() {
        symbol = new Symbol();
        symbol.setParent(this);

        //retângulo cinza
        BboxRectangle bbr = new BboxRectangle();
        bbr.initialize("0 0 4200 4200");

        //retangulo do componente
        Rectangle rec = new Rectangle();
        rec.initialize("0 0 4200 4200");

        //símbolo para portas de fios
        BusPortSymbol portsymbol0 = new BusPortSymbol();
        portsymbol0.initialize("0 1200 PCF");
        
        BusPortSymbol portsymbol3 = new BusPortSymbol();
        portsymbol3.initialize("3600 0 PRED");
        
        
        BusPortSymbol portsymbol4 = new BusPortSymbol();
        portsymbol4.initialize("2400 0 DESTCD");

        BusPortSymbol portsymbol5 = new BusPortSymbol();
        portsymbol5.initialize("0 3000 PCD");

        BusPortSymbol portsymbol2 = new BusPortSymbol();
        portsymbol2.initialize("4200 3600 VP");
        
        //sínbolo para portas de barramentos
        
        BusPortSymbol portsymbol6 = new BusPortSymbol();
        portsymbol6.initialize("1800 4200 nWE");
        
        PortSymbol portsymbol1 = new PortSymbol();
        portsymbol1.initialize("600 4200 CLK");
        
        PortSymbol portsymbol7 = new PortSymbol();
        portsymbol7.initialize("1200 4200 R");

        FatLabel label_comp_name = new FatLabel();
        FatLabel label_comp_name_1 = new FatLabel();
        label_comp_name_1.initialize("2100 2000 2 " + "Prediction");
        label_comp_name.initialize("2100 2800 2 " + "Table");
        
        
        //adiciona os símbolos para o "desenhista" do programa
        symbol.addMember(bbr);
        symbol.addMember(rec);
        symbol.addMember(label_comp_name_1);
        symbol.addMember(label_comp_name);
        symbol.addMember(portsymbol0);
        symbol.addMember(portsymbol1);
        symbol.addMember(portsymbol2);
        symbol.addMember(portsymbol3);
        symbol.addMember(portsymbol4);
        symbol.addMember(portsymbol5);
        symbol.addMember(portsymbol6);
        symbol.addMember(portsymbol7);
        
        //symbol.addMember(busportsymbol0);
    }
    
} /* RegBank.java */ 
