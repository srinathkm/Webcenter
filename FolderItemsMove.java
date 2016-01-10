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
 * @Details : This RIDC class searches for all (or based on query text) contents that are present in a ParentFolder 
 * and it's sub-folders. Then it moves all those items and it's sub-folders to a target folder .
 * This is mimicing the functionality provided from UI - Folder - Action- Move
 */

public class FolderItemsMove {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


         

   	        // Create a new IdcClientManager
		IdcClientManager manager = new IdcClientManager ();
		try{
			// Create a new IdcClient Connection using idc protocol (i.e. socket connection to Content Server)
			IdcClient idcClient = manager.createClient ("idc://<hostname>:<intradoc port>");

                                //for using the web connection - start          
                                    //  IdcClient idcClient = manager.createClient("http://<hostname>:<webport>/cs/idcplg/");
                                IdcContext userContext = new IdcContext("weblogic", "welcome1");          
                                //for using web connection - end
                                  
                        

                 
                        
			// Create an HdaBinderSerializer; this is not necessary, but it allows us to serialize the request and response data binders
			HdaBinderSerializer serializer = new HdaBinderSerializer ("UTF-8", idcClient.getDataFactory ());
			
			//Search Request for listing all the content items that are under a specific Parent Folder
			DataBinder dataBinder = idcClient.createBinder();
                        dataBinder.putLocal("IdcService", "GET_SEARCH_RESULTS");
                        dataBinder.putLocal("QueryText","");
                        dataBinder.putLocal("SearchQueryFormat" ,"Universal");
                        dataBinder.putLocal("ResultCount","20");
                        dataBinder.putLocal("AdvSearch","True");  
                        /*This parameter instructs Content server to look for the contents under sub-folder of Parent collection ID */
                        dataBinder.putLocal("folderChildren","<xCollectionID of Parent>");
  





            // Write the data binder for the request to stdout
            serializer.serializeBinder (System.out, dataBinder);
           
            // Send the request to Content Server
            ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
           

            // Get the data binder for the response from Content Server
            DataBinder responseData = response.getResponseAsBinder();
          
            //Write the response data binder to stdout - only needed to view the entire result set 
            // serializer.serializeBinder (System.out, responseData);
           
           // Retrieve the SearchResults ResultSet from the response
           DataResultSet resultSet = responseData.getResultSet("SearchResults");



          /*Iterating the result set obtained from earlier request and using that moving the folders and it's contents to a target folder*/

            for (DataObject dataObject : resultSet.getRows ()) {
                   
                   DataBinder dataBinder1 = idcClient.createBinder();
                   dataBinder1.putLocal("IdcService", "COLLECTION_MOVE_LOT");
                   dataBinder1.putLocal("tohasCollectionID","true");
                   dataBinder1.putLocal("todCollectionID","<xCollectionID of Target Folder>");
                   dataBinder1.putLocal("fromhasCollectionID0","true");
                   dataBinder1.putLocal("fromdCollectionID0",dataObject.get("xCollectionID"));
                   dataBinder1.putLocal("fromdDocName0" ,dataObject.get("dDocName"));
                   /*If this is set to true then the folders are not moved , only the contents are moved*/
                   //dataBinder1.putLocal("contentselect0","true");
                   /*If this is set to true then the folders and it's contents are moved*/
                   dataBinder1.putLocal("collectionselect0","true");
         
                  
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

