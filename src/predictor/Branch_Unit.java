/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package predictor;

//import hades.models.PortStdLogic1164;
//import hades.models.PortStdLogicVector;
//import hades.models.StdLogic1164;
//import hades.models.StdLogicVector;
//import hades.signals.Signal;
//import hades.simulator.Port;
//import hades.simulator.SimEvent;
//import hades.simulator.SimKernel;
//import hades.symbols.BboxRectangle;
//import hades.symbols.BusPortSymbol;
//import hades.symbols.Label;
//import hades.symbols.PortSymbol;
//import hades.symbols.Rectangle;
//import hades.symbols.Symbol;


import hades.simulator.*;//sao os objetos responsáveis pela simulação
import hades.symbols.*;//São componentes responsáveis pela criação dos símbolos
import hades.signals.*;//Componentes responsáveis para acesso aos valores das portas
import hades.models.*;//são os componentes que já existem no hades e fios
import hades.utils.StringTokenizer;

/**
 *
 * @author hector
 */
public class Branch_Unit extends hades.models.rtlib.GenericRtlibObject{
    
    protected PortStdLogicVector port_VP, port_VP_D, port_Mux,port_Wrdata;
    protected PortStdLogic1164 port_fluch,port_cmp, port_BEQ,port_reset;

    public Branch_Unit() {
        super();
    }

    @Override
    protected void constructPorts() {
//        super.constructPorts(); //To change body of generated methods, choose Tools | Templates.
                        
        port_BEQ = new PortStdLogic1164(this, "BEQ", Port.IN, null);
        port_cmp = new PortStdLogic1164(this, "CMP", Port.IN, null);
        port_VP = new PortStdLogicVector(this, "VP_F", Port.IN, null, 2);
        port_VP_D = new PortStdLogicVector(this, "VP_D", Port.IN, null, 2);
        
        port_Mux = new PortStdLogicVector(this, "Mux", Port.OUT, null, 2);        
//        port_UPDT = new PortStdLogicVector(null, "PTUdt", Port.OUT, null, 18);
        port_fluch = new PortStdLogic1164(this, "FLUCH", Port.OUT, null);
        port_Wrdata =  new PortStdLogicVector(this, "Wr", Port.OUT, null,2);
        port_reset = new PortStdLogic1164(this,"R", Port.IN, null);
        
        ports = new Port[8];
        ports[0]=port_BEQ;
        ports[1]=port_cmp;
        ports[2]=port_VP;
        ports[3]=port_VP_D;
        ports[4]=port_Mux;
//        ports[5]=port_UPDT;
        ports[5]=port_fluch;
        ports[6]=port_Wrdata;
        ports[7]=port_reset;
        
    }
public void schedule( Port port, Object value, double time ) {
    Signal signal = port.getSignal();
    if (signal != null) {
      simulator.scheduleEvent(  
        new SimEvent( signal, time, value, port ));
    }
  }
    @Override
    public void evaluate(Object arg) {
//        super.evaluate(arg); //To change body of generated methods, choose Tools | Templates.
        simulator = getParent().getSimulator();
        double time;
        
        StdLogic1164 beq = port_BEQ.getValueOrU();
        StdLogic1164 cmp = port_cmp.getValueOrU();
        StdLogic1164 fluch = null;
        StdLogic1164 reset = port_reset.getValueOrU();
        
        StdLogicVector WR = null;
        StdLogicVector vp_f = port_VP.getVectorOrUUU();
        StdLogicVector vp_d = port_VP_D.getVectorOrUUU();
        StdLogicVector mux = null;
        
        
        boolean isX = false;
        
        if(port_Wrdata.getSignal() == null){
            isX = true;
        }else
            if(port_Mux.getSignal() == null){
            isX = true;
        } else if(port_BEQ.getSignal() == null){
            isX = true;
        }  else if(port_VP.getSignal() == null){
            isX = true;
        } else if(port_VP_D.getSignal() == null){
            isX = true;
        } else if(port_cmp.getSignal() == null){
            isX = true;
        } else if(port_fluch.getSignal() == null){
            isX = true;
        }
//            System.out.println("r"+reset.getValue());
        if (reset.getValue().equals(2)){
            time = simulator.getSimTime() + delay;
            fluch = new StdLogic1164(2);            
            mux = new StdLogicVector( 2, 0 );
            WR = new StdLogicVector( 2, 0 );
            schedule(port_fluch, fluch, time);
            schedule(port_Mux, mux, time);
            schedule(port_Wrdata, WR, time);
        }
        else if(reset.getValue().equals(3)){
            WR = new StdLogicVector( 2, 0 );
            fluch = new StdLogic1164(2);
            time = simulator.getSimTime() + delay;
            schedule(port_Wrdata, WR, time);
            schedule(port_fluch, fluch, time);
            
            if (vp_f.equals(new StdLogicVector(2,3))){
                mux = new StdLogicVector(2,1);
//                System.out.println("VP_F "+ vp_f);
            }else{
                mux = new StdLogicVector( 2, 0 );
            }
            time = simulator.getSimTime()+delay;
//            System.out.println("MUX "+ mux);
            schedule(port_Mux, mux, time);
//            
//            System.out.println("vp_f "+vp_f);
            if(beq.is_1() && (vp_d.equals(new StdLogicVector(2,1)) || vp_d.equals(new StdLogicVector(2,0)))) {  
//                System.out.println("BEQ e nao cadastrado");
                if ( cmp.is_1()){   
//                    System.out.println("Calculado 1");
                    WR = new StdLogicVector(2,3);
                    mux = new StdLogicVector( 2, 3 );
                    fluch = new StdLogic1164(3);
                }
                    else{
                    WR = new StdLogicVector(2,2);
                    mux = new StdLogicVector(2, 0);
                    
                }
                
                time = simulator.getSimTime()+delay;
                schedule(port_Mux, mux, time);
                schedule(port_fluch, fluch, time);
                schedule(port_Wrdata, WR, time);
            }
            else if (beq.is_1() && vp_d.equals(new StdLogicVector(2,3))){
                if (! cmp.is_1()){
                    mux = new StdLogicVector(2,2);
                    WR = new StdLogicVector(2,2);
                    fluch = new StdLogic1164(3);
                    time = simulator.getSimTime()+delay;
                    schedule(port_Mux, mux, time);
                    schedule(port_fluch, fluch, time);
                    schedule(port_Wrdata, WR, time);
                    
                }
            }
            else if(beq.is_1() && vp_d.equals(new StdLogicVector(2,2))){
                if ( cmp.is_1()){
                    mux = new StdLogicVector(2, 3);
                    WR = new StdLogicVector(2,3);
                    fluch = new StdLogic1164(3);
                    time = simulator.getSimTime()+delay;
                    schedule(port_Mux, mux, time);
                    schedule(port_fluch, fluch, time);
                    schedule(port_Wrdata, WR, time);
                    System.out.println("Aqui");    
                }
            }
            else if(vp_d.equals(new StdLogicVector(2,3))){
                if (beq.is_0()){
                    fluch = new StdLogic1164(3);
                    mux = new StdLogicVector(2,2);
                    WR = new StdLogicVector(2,1);
                    time = simulator.getSimTime()+delay;
                    schedule(port_Mux, mux, time);
                    schedule(port_fluch, fluch, time);
                    schedule(port_Wrdata, WR, time);
                }
            }                
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
        bbr.initialize("0 0 4200 3000");

        //retangulo do componente
        Rectangle rec = new Rectangle();
        rec.initialize("0 0 4200 3000");

        //símbolo para portas de fios
        BusPortSymbol portsymbol0 = new BusPortSymbol();
        portsymbol0.initialize("2400 0 VP_F");

        BusPortSymbol portsymbol1 = new BusPortSymbol();
        portsymbol1.initialize("0 600 Wr");
        
        BusPortSymbol portsymbol2 = new BusPortSymbol();
        portsymbol2.initialize("0 1800 Mux");

        PortSymbol portsymbol3 = new PortSymbol();
        portsymbol3.initialize("3600 3000 BEQ");

        PortSymbol portsymbol4 = new PortSymbol();
        portsymbol4.initialize("4200 600 FLUCH");
        
        BusPortSymbol portsymbol5 = new BusPortSymbol();
        portsymbol5.initialize("4200 1800 VP_D");
        
        PortSymbol portsymbol6 = new PortSymbol();
        portsymbol6.initialize("4200 2400 CMP");
        
        PortSymbol portsymbol7 = new PortSymbol();
        portsymbol7.initialize("600 3000 R");
        //sínbolo para portas de barramentos
        //BusPortSymbol busportsymbol0 = new BusPortSymbol();
        //busportsymbol0.initialize("0 1200 D_IN1");

        FatLabel label_comp_name = new FatLabel();
        FatLabel label_comp_name_1 = new FatLabel();
        label_comp_name.initialize("2200 1400 2 " + "Branch");
        label_comp_name_1.initialize("2200 2600 2 " + "Unit");
        
        
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
    
    @Override
    public void write(java.io.PrintWriter ps) {
        ps.print(" " + versionId
                + " " + n_bits
                + " " + delay);
    }

    //método de leitura das configurações do arquivo do hades (.hds)
    @Override
    public boolean initialize(String s) {
        StringTokenizer st = new StringTokenizer(s);
        int n_tokens = st.countTokens();
        try {
            if (n_tokens == 0) {
                versionId = 1001;
                n_bits = 16;
                constructStandardValues();
                constructPorts();
            } else if (n_tokens == 1) {
                versionId = Integer.parseInt(st.nextToken());
                n_bits = 16;
                constructStandardValues();
                constructPorts();
            } else if (n_tokens == 2) {
                versionId = Integer.parseInt(st.nextToken());
                n_bits = Integer.parseInt(st.nextToken());
                constructStandardValues();
                constructPorts();
            } else if (n_tokens == 3) {
                versionId = Integer.parseInt(st.nextToken());
                n_bits = Integer.parseInt(st.nextToken());
                constructStandardValues();
                constructPorts();
                setDelay(st.nextToken());
            } else {
                throw new Exception("invalid number of arguments");
            }
        } catch (Exception e) {
            message("-E- " + toString() + ".initialize(): " + e + " " + s);
            e.printStackTrace();
        }
        return true;
    }
    
    
}
