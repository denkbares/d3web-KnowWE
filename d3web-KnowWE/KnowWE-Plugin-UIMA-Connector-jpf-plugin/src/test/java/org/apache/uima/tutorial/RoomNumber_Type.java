
/* First created by JCasGen Wed May 19 08:59:53 CEST 2010 */
package org.apache.uima.tutorial;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Wed May 19 09:00:13 CEST 2010
 * @generated */
public class RoomNumber_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (RoomNumber_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = RoomNumber_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new RoomNumber(addr, RoomNumber_Type.this);
  			   RoomNumber_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new RoomNumber(addr, RoomNumber_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = RoomNumber.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.apache.uima.tutorial.RoomNumber");
 
  /** @generated */
  final Feature casFeat_building;
  /** @generated */
  final int     casFeatCode_building;
  /** @generated */ 
  public int getBuilding(int addr) {
        if (featOkTst && casFeat_building == null)
      jcas.throwFeatMissing("building", "org.apache.uima.tutorial.RoomNumber");
    return ll_cas.ll_getRefValue(addr, casFeatCode_building);
  }
  /** @generated */    
  public void setBuilding(int addr, int v) {
        if (featOkTst && casFeat_building == null)
      jcas.throwFeatMissing("building", "org.apache.uima.tutorial.RoomNumber");
    ll_cas.ll_setRefValue(addr, casFeatCode_building, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public RoomNumber_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_building = jcas.getRequiredFeatureDE(casType, "building", "org.apache.uima.tutorial.Building", featOkTst);
    casFeatCode_building  = (null == casFeat_building) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_building).getCode();

  }
}



    