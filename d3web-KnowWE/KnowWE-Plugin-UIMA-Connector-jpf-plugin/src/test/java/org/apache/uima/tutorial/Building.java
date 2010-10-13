

/* First created by JCasGen Wed May 19 08:59:53 CEST 2010 */
package org.apache.uima.tutorial;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed May 19 09:00:13 CEST 2010
 * XML source: C:/Users/ManiaC/UIMASandbox/RoomNumberAnnotator/desc/RoomNumberAnnotator.xml
 * @generated */
public class Building extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Building.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Building() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Building(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Building(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Building(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
}

    