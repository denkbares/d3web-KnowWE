

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
public class RoomNumber extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(RoomNumber.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected RoomNumber() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public RoomNumber(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public RoomNumber(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public RoomNumber(JCas jcas, int begin, int end) {
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
     
 
    
  //*--------------*
  //* Feature: building

  /** getter for building - gets 
   * @generated */
  public Building getBuilding() {
    if (RoomNumber_Type.featOkTst && ((RoomNumber_Type)jcasType).casFeat_building == null)
      jcasType.jcas.throwFeatMissing("building", "org.apache.uima.tutorial.RoomNumber");
    return (Building)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((RoomNumber_Type)jcasType).casFeatCode_building)));}
    
  /** setter for building - sets  
   * @generated */
  public void setBuilding(Building v) {
    if (RoomNumber_Type.featOkTst && ((RoomNumber_Type)jcasType).casFeat_building == null)
      jcasType.jcas.throwFeatMissing("building", "org.apache.uima.tutorial.RoomNumber");
    jcasType.ll_cas.ll_setRefValue(addr, ((RoomNumber_Type)jcasType).casFeatCode_building, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    