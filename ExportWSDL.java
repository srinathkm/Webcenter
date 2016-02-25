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
 * @author Srinath Menon 
 * 
 * Overview :  RIDC based Soap WSDL export utility which would export all the existing "custom only" WSDL's
               This would be the starting point for migration of existing WSDL's from source server. 
               These resultsets are then referenced by ImportWSDL to import on target server.
 * Params :    Source host , source intradoc server , source user name
*/

public class ExportWSDL {
	// RIDC connection information
	private static final String IDC_PROTOCOL = "";
	private static final String RIDC_SERVER_SOURCE = "";
	private static final String RIDC_PORT_SOURCE = "";
        private static IdcClientManager m_idcClientManager;
        private static IntradocClient m_idcClient;

	private static final String UTF8 = "UTF-8";
        
        private static Properties prop = new Properties();
        private static InputStream input = null;
        InputStream fileStream = null;
	
        // Services used for getting and setting the wsdl lists and it's details : 
	private static final String IDC_SERVICE = "IdcService";
	private static final String ExportWsdlName = "GET_SOAP_WSDL_LIST";
        private static final String ExportWsdlService = "GET_SOAP_WSDL_INFO";
        private static final String ExportWsdlServiceParams = "GET_SOAP_SERVICE_INFO";

	
        // User to execute service calls as
	private static final String USER = "";

	// ResultSet to store exported wsdl details:
	private static final String WsdlName = "Wsdls";
        private static final String WsdlService = "Services";
        private static final String WsdlComplex = "ComplexTypes";
        private static final String WsdlServiceParams_request = "RequestParams";
        private static final String WsdlServiceParams_response = "ResponseParams";

        public static DataResultSet WsdlNameResultSet = null;
        public static DataResultSet WsdlServiceResultSet = null;
        public static DataResultSet WsdlServiceParamsResultSet_request = null;
        public static DataResultSet WsdlServiceParamsResultSet_response = null;
        //public static  DataResultSet tempResultSet = null;        
               
         
        public static  List<DataResultSet> wsdlNameList = new ArrayList<DataResultSet>(); 
        public static  List<DataResultSet> wsdlServiceList = new ArrayList<DataResultSet>(); 
        public static  List<DataResultSet> wsdlServiceRequestList = new ArrayList<DataResultSet>();
        public static  List<DataResultSet> wsdlServiceResponseList = new ArrayList<DataResultSet>();
        
        public static DataResultSet.Field wsdlName= new DataResultSet.Field("wsdlName");
        public static DataResultSet.Field wsdlDescription= new DataResultSet.Field("wsdlDescription");
 


	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) {
		try {
		        /* WSDL Export section
                 	*  WSDL Name
                        *  WSDL service details 
                        *  WSDL service parameters - request and response
                        */
                        wsdlNameList=getWsdlName();
                        wsdlServiceList=getWsdlService();
                        wsdlServiceRequestList=getWsdlServiceParams_request();
                        wsdlServiceResponseList=getWsdlServiceParams_response();
                     

                     
                        /*WSDL Import section
                        * Add WSDL
                        * Add Service
                        * Add Complex
                        * Update Service to add request and reponse parameters 
                        */

			ImportWSDL.addWsdlName(wsdlNameList);
                        ImportWSDL.generateWsdl();
                        UpdateWSDLEndPoint.updateUrl(); // To update the wsdl end-point url to target ucm server hostname:port
                        ImportWSDL.addWsdlService(wsdlNameList,wsdlServiceList);
                        ImportWSDL.addWsdlServiceRequest(wsdlNameList,wsdlServiceList,wsdlServiceRequestList);
                        ImportWSDL.addWsdlServiceResponse(wsdlNameList,wsdlServiceList,wsdlServiceResponseList);   

		} catch (final IdcClientException ice){
			ice.printStackTrace(System.out);
		}
	}

	/**
	 * This method executes GET_SOAP_WSDL_LIST and returns the Wsdls ResultSet.
	 * @return Wsdls ResultSet.
         */

	private static List getWsdlName() throws IdcClientException {
		// DataBinder for service call
		DataBinder serviceBinder = getNewDataBinder();
                DataFactory dataFactory =m_idcClient.getDataFactory();
                final DataResultSet tempResultSet = dataFactory.createResultSet();
                tempResultSet.addField(wsdlName,"");
                tempResultSet.addField(wsdlDescription,"");

		// Set service parameters - GET_SOAP_WSDL_LIST
		serviceBinder.putLocal(IDC_SERVICE,ExportWsdlName);

		// Execute service
		serviceBinder = executeService(serviceBinder,prop.getProperty("RIDC_SOURCE_USER"));
                
		// Return Wsdls ResultSet
                WsdlNameResultSet = serviceBinder.getResultSet("Wsdls");
               
                for (DataObject name : WsdlNameResultSet.getRows ()) {
                int isCustom=0;
                int isSystem=Integer.parseInt(name.get("isSystem"));
                     if (isCustom==isSystem){
                System.out.println("inside custom check block" + isSystem);
             
                ArrayList<String> rowList = new ArrayList<String>(2);
                rowList.add(name.get("wsdlName"));
                rowList.add(name.get("wsdlDescription"));

                tempResultSet.addRow(name); 
                wsdlNameList.add(tempResultSet);
                }
              }
                return wsdlNameList;

	}

         /**
         * This method executes GET_SOAP_WSDL_INFO 
         * @return Services ResultSet.
         */
   
         private static List getWsdlService() throws IdcClientException {
                // DataBinder for service call
                DataBinder serviceBinder = getNewDataBinder();
                int i=0;
                // Set service parameters - GET_SOAP_WSDL_LIST
                //for (DataObject dataObject : WsdlNameResultSet.getRows ()) 
                for (DataObject dataObject :  wsdlNameList.get(i).getRows() )
                {
                  //System.out.println("Name result"+ dataObject.get ("wsdlName"));
                  serviceBinder.putLocal(IDC_SERVICE,ExportWsdlService);
                  serviceBinder.putLocal("wsdlName",dataObject.get("wsdlName"));      
                  // Execute service
                  serviceBinder = executeService(serviceBinder,prop.getProperty("RIDC_SOURCE_USER"));
                  WsdlServiceResultSet = serviceBinder.getResultSet("Services");
                  wsdlServiceList.add(WsdlServiceResultSet);

               } i++;
                // Return Services resultset as list:
                return wsdlServiceList;
         }


     /**
         * This method executes GET_SOAP_SERVICE_INFO
         * @return Requests ResultSet.
    */

     private static List getWsdlServiceParams_request() throws IdcClientException {
                System.out.println("Inside the service params_request block");
                // DataBinder for service call
                DataBinder serviceBinder = getNewDataBinder();
               
               int i=0;

                // Set service parameters - GET_SOAP_WSDL_INFO
                for (DataObject name : wsdlNameList.get(i).getRows())
                    { 
                      //System.out.println("Inside first for block");


                        for (DataObject service :  wsdlServiceList.get(i).getRows() )
                          {
                            //System.out.println("Inside second for block-export");
                            serviceBinder.putLocal(IDC_SERVICE,ExportWsdlServiceParams);
                            serviceBinder.putLocal("wsdlName",name.get("wsdlName"));
                            serviceBinder.putLocal("serviceName",service.get("serviceName"));
                            // Execute service
                            serviceBinder = executeService(serviceBinder, prop.getProperty("RIDC_SOURCE_USER"));
                            WsdlServiceParamsResultSet_request = serviceBinder.getResultSet("RequestParams");
                            wsdlServiceRequestList.add(WsdlServiceParamsResultSet_request);
                            /*for (DataObject request :  WsdlServiceParamsResultSet_request.getRows() ){
                            System.out.println("Export - service request details:" + request.get ("dataName"));
                            }*/
                          } i++;
                    }
                // Return RequestParams result set as list:
                return wsdlServiceRequestList;
               }



        /**
         * This method executes GET_SOAP_SERVICE_INFO
         * @return Response ResultSet.
       */

        private static List getWsdlServiceParams_response() throws IdcClientException {
                 System.out.println("Inside the service params_response block");
                 // DataBinder for service call
                  DataBinder serviceBinder = getNewDataBinder();
                 int i=0; 
 
                 // Set service parameters - GET_SOAP_WSDL_INFO
                 for (DataObject name :  wsdlNameList.get(i).getRows() )
                     {
                        System.out.println("Inside first for block response");
                         
                       
                         for (DataObject service :  wsdlServiceList.get(i).getRows() )
                           {


                            serviceBinder.putLocal(IDC_SERVICE,ExportWsdlServiceParams);
                            serviceBinder.putLocal("wsdlName",name.get("wsdlName"));
                            serviceBinder.putLocal("serviceName",service.get("serviceName"));
                            // Execute service
                            serviceBinder = executeService(serviceBinder, prop.getProperty("RIDC_SOURCE_USER"));
                            WsdlServiceParamsResultSet_response = serviceBinder.getResultSet("ResponseParams");
                            wsdlServiceResponseList.add(WsdlServiceParamsResultSet_response);
                            /*for (DataObject response :  WsdlServiceParamsResultSet_response.getRows() ){
                            System.out.println("Export - service response details:" + response.get ("dataName"));
                            }*/
                          } i++;
                    }
                 // Return ResponseParams ResultSet as list:
                // System.out.println("Export - response size is:" + wsdlServiceResponseList.size());
                 return wsdlServiceResponseList;
         }
 

	/**
	 * This method executes a service.
	 * @param binder DataBinder containing service parameters.
	 * @param userName User to execute service as.
	 * @return DataBinder from service call.
	 * @throws IdcClientException
	 * @throws IOException
	 */
	public static DataBinder executeService(DataBinder binder, final String userName) throws IdcClientException {
		final HdaBinderSerializer serializer = new HdaBinderSerializer (UTF8, getIntradocClient().getDataFactory ());

		try {
			System.out.println("Service binder before executing:");
			serializer.serializeBinder (System.out, binder);
		} catch (final IOException ioe) {
			ioe.printStackTrace(System.out);
		}

		// Execute the request
	    final ServiceResponse response = getIntradocClient().sendRequest(new IdcContext(userName), binder);
	    
            // Get the response as a DataBinder
	    binder = response.getResponseAsBinder();
		
                 try {
			System.out.println("Service binder after executing:");
			serializer.serializeBinder (System.out, binder);
		} catch (final IOException ioe) {
			ioe.printStackTrace(System.out);
		}

	    // Return the response as a DataBinder
		return binder;
	}

	public static IdcClientManager getIdcClientManager() {
		if (m_idcClientManager == null) {
			// Needed to create IntradocClient
			m_idcClientManager = new IdcClientManager();
		}
		return m_idcClientManager;
	}


       public static IntradocClient getIntradocClient() throws IdcClientException {
		//if (m_idcClient == null) {
                  try{
                    input = new FileInputStream("wsdlconfig.properties");

                    // load a properties file
                     prop.load(input);

                if (m_idcClient == null) {        // Create a new IdcClient Connection using idc protocol (i.e. socket connection to Content Server)
                        m_idcClient = (IntradocClient)getIdcClientManager().createClient (prop.getProperty("IDC_PROTOCOL")+"://" + prop.getProperty("RIDC_SERVER_SOURCE") + ":" + prop.getProperty("RIDC_PORT_SOURCE"));
                   }
			// Client to talk to WebCenter Content
		//	m_idcClient = (IntradocClient)getIdcClientManager().createClient(IDC_PROTOCOL + RIDC_SERVER_SOURCE + ":" + RIDC_PORT_SOURCE);
		}
		//return m_idcClient;
                   catch (IOException ioe){
                        ioe.printStackTrace();
                }return m_idcClient;

	}
        
	
        /**
	 * This method gets a new DataBinder.
	 * @return A new DataBinder.
	 * @throws IdcClientException
	 */
	public static DataBinder getNewDataBinder() throws IdcClientException {
		return getIntradocClient().createBinder();
	}
}
