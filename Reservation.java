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
import static java.lang.Math.abs;

/*
 * @Author : Srinath Menon
 * @Date : 21-Aug-2015
 * @Details : This is RIDC class which will Reserve a Storage.
 *            From the WCCREC UI this action is done from Physical Item actions menu (from search result)-Create Reservation
*/

public class Reservation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


         

   	        // Create a new IdcClientManager
		IdcClientManager manager = new IdcClientManager ();
		try{
			// Create a new IdcClient Connection using idc protocol (i.e. socket connection to Content Server)
			 IdcClient idcClient = manager.createClient ("idc://<urm hostname>:<intradoc port>");

                                 //for using the web connection - start          
                                      //  IdcClient idcClient = manager.createClient("http://<urm hostname>:<webport>/urm/idcplg/");
                                IdcContext userContext = new IdcContext("weblogic", "welcome1");          
                                //for using web connection - end
                                  
                        
			// Create an HdaBinderSerializer; this is not necessary, but it allows us to serialize the request and response data binders
			HdaBinderSerializer serializer = new HdaBinderSerializer ("UTF-8", idcClient.getDataFactory ());
			
			/*Create Reservation ID with this service 
                            *dIDList corresponds to the dID of the Physical item for which reservation is to be done.
                        */ 
 
			DataBinder dataBinder = idcClient.createBinder();
                        dataBinder.putLocal("IdcService", "CREATE_RESERVATION_FORM");
                        dataBinder.putLocal("dIDList","<value for the Physical Item>"); 
                        dataBinder.putLocal("dSource","Physical");

                             

 
            // Write the data binder for the request to stdout
            serializer.serializeBinder (System.out, dataBinder);
           
            // Send the request to Content Server
            ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);


            // Get the data binder for the response from Content Server
            DataBinder responseData = response.getResponseAsBinder();

           /* Method for getting the Reservation request id and post-processing it */
           DataBinder binder1 = response.getResponseAsBinder();
           String requestID=binder1.getLocal("dRequestID");
           /*Retrieving the absolute value for the request ID since from the above service it comes as Negative value*/
           Long RequestID = Math.abs(Long.parseLong(requestID));
           System.out.println(requestID);
           System.out.println(RequestID);


           /*Reserving the Physical Item based on the request ID retrieved from the above service */
            DataBinder dataBinder1 = idcClient.createBinder();
            dataBinder1.putLocal("IdcService", "CREATE_RESERVATION");
            dataBinder1.putLocal ("dRequestID",String.valueOf(RequestID));
            dataBinder1.putLocal ("dRequestName", "<Name of the Reservation to be created>");
            dataBinder1.putLocal ("dRequestDate", "2015-03-08 06:00:00Z"); //Date Format to be used
            dataBinder1.putLocal ("dRequestor", "<username>");
            dataBinder1.putLocal ("dSecurityGroup", "<Sec group to be assigned>");
            dataBinder1.putLocal ("dTransferMethod", "wwPickup"); // Pick up method
            dataBinder1.putLocal ("dRequestPriority", "wwNoPriority"); // Priority detail
            dataBinder1.putLocal ("dIDList","<dID of the Physical Item>");
            dataBinder1.putLocal("dIsComplete", "0");
            dataBinder1.putLocal("isDelete","0");
          
         //Write the data binder for the request to stdout
           serializer.serializeBinder (System.out, dataBinder1);

         //Send the request to Content Server
           ServiceResponse response1 = idcClient.sendRequest(userContext,dataBinder1);


         //Get the data binder for the response from Content Server
           DataBinder responseData1 = response1.getResponseAsBinder();


  	} catch (IdcClientException ice){
			ice.printStackTrace();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

   }

