package ufv;

import hades.models.gates.*;
import hades.models.*;
import hades.signals.*;
import hades.simulator.*;
import hades.symbols.*;



import hades.manager.DesignManager;
import hades.gui.*;
import hades.utils.NameMangler;
import hades.utils.StringTokenizer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.Vector;


/*import java.lang.*;
import java.util.*;
*/

import java.io.*;
import sun.audio.*;

/**
 *  Description of the Class
 *
 *@author     Ricardo Ferreira / Geraldo Fontes
 *@created    2015
 */

public class q_a extends hades.models.gates.GenericGate {
	protected  String filename;	
	public q_a(){
		super();
    filename = "null";
  }
  
 
  

  public void selectFile() {
    DesignManager DM = DesignManager.getDesignManager();
    String name = DM.selectFileOrURLName(
                        "Select the question and answer file:",
                        filename,
                        new String[] { ".txt" }, 
                        java.awt.FileDialog.LOAD );

    
    if (name == null) { // Cancel'ed
      return;
    }
    else {
      filename = name;
    }
  }



 
  public void mousePressed( MouseEvent me ){
    // inserir sua classe
   try{
	
	Vector<question> perguntas = new Vector<question>();
	questionReader q= new questionReader();
	q.reader(filename,perguntas);
	Vector<questionFrame> frames = new Vector<questionFrame>();
	question qt = new question();
	
	
	for(int i=0; i<perguntas.size();i++){
	  qt = perguntas.get(i);
	  frames.add(new questionFrame(qt,frames));
	  qt = new question();
	}
    
	frames.get(0).setVisible(true);
    
    }catch(FileNotFoundException ex){}
  } 




  public void configure() {
    selectFile();
  }



	
}

