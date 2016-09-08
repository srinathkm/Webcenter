import java.io.*;
import oracle.stellent.ridc.*;
import oracle.stellent.ridc.model.*;
import oracle.stellent.ridc.protocol.*;
import oracle.stellent.ridc.protocol.intradoc.*;
import oracle.stellent.ridc.common.log.*;
import oracle.stellent.ridc.model.serialize.*;
import oracle.stellent.ridc.protocol.http.*;
import java.util.List;
import java.util.*;
import java.text.*;

/*
 * @Author : Srinath Menon
 * @Date : 21-Aug-2015 
 * @Details : RIDC code for doing mass update of Physical Items based on specific search criteria or a generic blank
 * search.This would be RM compliant because it will trigger Audit Trail update to track the changes done.
 */

public class PhysicalUpdate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


         

   	        // Create a new IdcClientManager
		IdcClientManager manager = new IdcClientManager ();
		try{
			// Create a new IdcClient Connection using idc protocol (i.e. socket connection to Content Server)
			IdcClient idcClient = manager.createClient ("idc://<urm hostname>:<port>");

                                IdcContext userContext = new IdcContext("<username>", "<password>");          
                                //for using web connection - end
                                  
                        
			// Create an HdaBinderSerializer; this is not necessary, but it allows us to serialize the request and response data binders
			HdaBinderSerializer serializer = new HdaBinderSerializer ("UTF-8", idcClient.getDataFactory ());
			
			// Service to execute Physixal Items Search - In this case it is a blank search 
                      
			DataBinder dataBinder = idcClient.createBinder();
                        dataBinder.putLocal("IdcService", "GET_EXTERNAL_ITEM_SEARCH_RESULTS");
                        dataBinder.putLocal("ErmSearchTable","EXTERNAL_ITEM");
                        dataBinder.putLocal("SearchEngineName","Database");
                        dataBinder.putLocal("SearchQueryFormat","Universal");
                        dataBinder.putLocal("ExternalSearchText","");
                        dataBinder.putLocal("dSource","Physical");
                        dataBinder.putLocal("AdvSearch","True");
                       
                     
           
                        // Send the request to Content Server
                        ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
           

                        // Get the data binder for the response from Content Server
                        DataBinder responseData = response.getResponseAsBinder();
          
                        // Retrieve the Result set 
                        DataResultSet resultSet = responseData.getResultSet("SearchResults_Physical");
                        
                        /*Iterate over the result set retrieved from the above method and in that loop run the Edit / Update service call */
                        
                        for (DataObject dataObject : resultSet.getRows ()) {

                          //System.setOut(out);
                          System.out.println ("Content ID  is  :  " + dataObject.get ("dDocName"));
                          
                          DataBinder dataBinder1 = idcClient.createBinder(); 
                          dataBinder1.putLocal("IdcService","EDIT_EXTERNAL_ITEM");
                          dataBinder1.putLocal("dID",dataObject.get ("dID"));
                          dataBinder1.putLocal("dDocName",dataObject.get ("dDocName"));
                          /*Metadata which needs to be updated */
                          dataBinder1.putLocal("xClassificationGuideRemarks","Looping works"); 
                          dataBinder1.putLocal("xCategoryID","Test1");                          
                          dataBinder1.putLocal("dExtObjectType","Box");
                          dataBinder1.putLocal("dMediaType","Box");
                          /*Metadat which needs to be updated - End */
 
                        // Write the data binder for the request to stdout
                        serializer.serializeBinder (System.out, dataBinder1);

                        // Send the request to Content Server
                        ServiceResponse response1 = idcClient.sendRequest(userContext,dataBinder1);

                    } 
                  

		} catch (IdcClientException ice){
			ice.printStackTrace();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

   }

