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
@Author : Srinath Menon
@Date : 01/12/2015
@Description: This class will check in a new content item and along with set annotation for the same based on template (annotation xml file) 
              This functionality is newly introduced with WCC 12.2.1.0.0 where in the new service gives the ability to checkin and set attachments to it with single
              service call . 
@IdcService : CHECKIN_NEW_WITH_RENDITIONS
@Params : 
         InputFile - the primary file to be checked in 
         annotationFile - the annotation template to be used for setting the text , location etc for the placement of annotation .
@Documentation :https://docs.oracle.com/middleware/1221/wcc/CSSRG/GUID-8B4EC3CD-FC12-4459-9AF1-B1E504848BF5.htm#CSSRG-GUID-61CD449A-C91C-4CEA-85B5-A02CFF7565C9 
*/

public class AddAnnotation {

	/**
	 * @param args
	 */
        private static  String Rendition="";

	public static void main(String[] args) {

       

   	        // Create a new IdcClientManager
                                
                IdcClientManager manager = new IdcClientManager ();
                Properties prop = new Properties();
                InputStream input = null;

                try{

                        input = new FileInputStream("config.file");

                        //Properties file for connection and parameter details
                        prop.load(input);

                        // Create a new IdcClient Connection 
                        IdcClient idcClient = manager.createClient (prop.getProperty("IDC_PROTOCOL")+"://" + prop.getProperty("RIDC_SERVER") + ":" + prop.getProperty("RIDC_PORT"));
                        IdcContext userContext = new IdcContext(prop.getProperty("user"));
  
                        
			// Create an HdaBinderSerializer; this is not necessary, but it allows us to serialize the request and response data binders
			HdaBinderSerializer serializer = new HdaBinderSerializer ("UTF-8", idcClient.getDataFactory ());
			
			// Databinder for checkin and setting the annotation 
			DataBinder dataBinder = idcClient.createBinder();

                        /* New service to check in and set annotation - Start*/
                        dataBinder.putLocal("IdcService", "CHECKIN_NEW_WITH_RENDITIONS");
                        dataBinder.putLocal("dDocTitle",prop.getProperty("contentTitle"));
                        dataBinder.putLocal("dDocAuthor",prop.getProperty("user"));
                        dataBinder.putLocal("dSecurityGroup",prop.getProperty("SecGroup"));
                        dataBinder.putLocal("dDocType",prop.getProperty("Type"));
                        dataBinder.addFile("primaryFile",new File(prop.getProperty("InputFile")));
                        /* New service to check in and set annotation - End*/
                                               
                        /* Service to be used if already existing content has to be set with annotation*/
                      
                        //dataBinder.putLocal("IdcService", "EDIT_RENDITIONS"); 
                        //dataBinder.putLocal("dID",prop.getProperty("dID"));
                      
                        dataBinder.putLocal("AuxRenditionType","System");
                        dataBinder.putLocal("addRendition0.action","edit");
                        dataBinder.putLocal("addRendition0.name","annotationRendition");
                        dataBinder.putLocal("renditionKeys","addRendition0");
                        dataBinder.putLocal("addRendition0.description",prop.getProperty("description"));
                        dataBinder.addFile("addRendition0.file",new File(prop.getProperty("annotationFile")));
            
            // Write the data binder for the request to stdout
            serializer.serializeBinder (System.out, dataBinder);
            // Send the request to Content Server
            ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
            // Get the data binder for the response from Content Server
            DataBinder responseData = response.getResponseAsBinder();
            // Write the response data binder to stdout
            serializer.serializeBinder (System.out, responseData);

 
		
		} catch (IdcClientException ice){
			ice.printStackTrace();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

}

