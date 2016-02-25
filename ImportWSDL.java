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
 * RIDC based utility to import all "custom" WSDL's to a new target server. 
 * Params :    Target host , Target intradoc server ,Target user name

 */

public class ImportWSDL {
	// RIDC connection information
	private static final String IDC_PROTOCOL = "";
        private static final String RIDC_SERVER_TARGET = "";
        private static final String RIDC_PORT_TARGET = "";
	private static IdcClientManager m_idcClientManager;
	private static IntradocClient m_idcClient;

	private static final String UTF8 = "UTF-8";
        InputStream fileStream = null;
        private static InputStream input = null;
        private static Properties prop = new Properties();



        // Services used to add soap / add soap service / soap service parameters : 
	private static final String IDC_SERVICE = "IdcService";
        private static final String AddWsdlName = "ADD_SOAP_WSDL";
        private static final String AddWsdlService ="ADD_SOAP_SERVICE" ;
        private static final String AddWsdlServiceParams = "UPDATE_SOAP_SERVICE_PARAMS";
        private static final String GenerateWsdl ="GENERATE_SOAP_WSDLS";	
        
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
        
        public static  List<DataResultSet> wsdlNameList = new ArrayList<DataResultSet>(); 
        public static  List<DataResultSet> wsdlServiceList = new ArrayList<DataResultSet>(); 
        public static  List<DataResultSet> wsdlServiceRequestList = new ArrayList<DataResultSet>();
        public static  List<DataResultSet> wsdlServiceResponseList = new ArrayList<DataResultSet>();
        
        public static final String dataIdcName="dataIdcName";        
 
     

       
/*Function to add wsdl */ 
	public static void addWsdlName(final List<DataResultSet> wsdlNameList) throws IdcClientException {
		// DataBinder for service call
		final DataBinder serviceBinder = getNewDataBinder();
                int i=0;
                for (DataObject name :  wsdlNameList.get(i).getRows()){
  	        // Set service parameters for import of WSDL Name along with the description
		        serviceBinder.putLocal(IDC_SERVICE, AddWsdlName);
                        serviceBinder.putLocal("wsdlName",name.get("wsdlName"));
                        serviceBinder.putLocal("wsdlDescription",name.get("wsdlDescription"));               
                //Execute Service 
		executeService(serviceBinder, prop.getProperty("RIDC_TARGET_USER"));
             }i++;
	}

/*End*/

/* Generate wsdl is needed after add_wsdl_name service to refresh the list. Otherwiser add_wsdl_service would fail.*/
       public static void generateWsdl() throws IdcClientException {
                // DataBinder for service call
                final DataBinder serviceBinder = getNewDataBinder();
                // Generate WSDL service is called to refresh the wsdl's added 
                        serviceBinder.putLocal(IDC_SERVICE,GenerateWsdl);
                //Execute Service 
                executeService(serviceBinder, prop.getProperty("RIDC_TARGET_USER"));
             }

/*End*/

/* Function to add services per wsdl */
        public static void addWsdlService(final List<DataResultSet> wsdlNameList, final List<DataResultSet> wsdlServiceList) throws IdcClientException {
               System.out.println("Inside add service block");
                // DataBinder for service call
                final DataBinder serviceBinder = getNewDataBinder();
                int i=0;
                for (DataObject name:wsdlNameList.get(i).getRows()){
                //System.out.println("inside wsdl name block"+i);
                for (DataObject service :  wsdlServiceList.get(i).getRows()){
                //System.out.println("count in service block "+ i);
                
                // Set service parameters for adding Idc Services per WSDL
                        serviceBinder.putLocal(IDC_SERVICE, AddWsdlService);
                        serviceBinder.putLocal("wsdlName",name.get("wsdlName"));
                        serviceBinder.putLocal("serviceName",service.get("serviceName"));
                        serviceBinder.putLocal("idcServiceName",service.get("idcServiceName"));
                //Execute Service 
                executeService(serviceBinder, prop.getProperty("RIDC_TAREGT_USER"));
              }i++;
           }i++;
        }
/*End*/    


/* Function to add the request parameters to the services */

        public static void addWsdlServiceRequest(final List<DataResultSet> wsdlNameList, final List<DataResultSet> wsdlServiceList,final List<DataResultSet> wsdlServiceRequestList) throws IdcClientException {
               System.out.println("Inside add request block");
                // DataBinder for service call
                final DataBinder serviceBinder = getNewDataBinder();
                
                int i=0; 
               
               for(DataObject name:wsdlNameList.get(i).getRows()){
             
               for(DataObject service :  wsdlServiceList.get(i).getRows()){
              
               int j=0; 
               List<oracle.stellent.ridc.model.DataObject> reqlist =  wsdlServiceRequestList.get(i).getRows();
               int size = reqlist.size();
               //int j=0; 
               //int temp = size;

               final DataBinder newBinder = getNewDataBinder();
               // Update request parameters per Service for every WSDL
               while(j<size) { 
                                                               
                   for (DataObject element : reqlist) {

                       // System.out.println("inside sub for loop " + service.get("serviceName"));

                        newBinder.putLocal(IDC_SERVICE, AddWsdlServiceParams);
                        newBinder.putLocal("wsdlName",name.get("wsdlName"));
                        newBinder.putLocal("serviceName",service.get("serviceName"));
                        newBinder.putLocal("isRequest","1");
                        newBinder.putLocal("dataName_"+j,element.get("dataName"));
                        newBinder.putLocal("dataIdcName_"+j, element.get("dataIdcName"));
                        newBinder.putLocal("dataType_"+j,element.get("dataType"));
                        newBinder.putLocal("isEnabled_"+j,element.get("isEnabled"));
                        newBinder.putLocal("numElements","6");
                        executeService(newBinder,prop.getProperty("RIDC_TARGET_USER"));
                        j++;
                  } i++;    // this is innermost for loop
                }           // this is while loop
             }              // this is service list for loop
          }                 // this is name list loop
       }                    // main function

/* End */




/* Function to add response parameters to service */
           
        public static void addWsdlServiceResponse(final List<DataResultSet> wsdlNameList, final List<DataResultSet> wsdlServiceList,final List<DataResultSet> wsdlServiceResponseList) throws IdcClientException {
               System.out.println("Inside add response block");
                // DataBinder for service call
                final DataBinder serviceBinder = getNewDataBinder();

                int i=0;
               
               for(DataObject name:wsdlNameList.get(i).getRows()){
       
               for(DataObject service :  wsdlServiceList.get(i).getRows()){
       
               int j=0;
       
               //List<oracle.stellent.ridc.model.DataObject> resplist =  wsdlServiceResponseList.get(i).getRows();
       
       
               //int size = resplist.size();
               int size = wsdlServiceResponseList.get(i).getRows().size();
               //System.out.println("inside response size is :  " + service.get("serviceName"));
                 
               final DataBinder newBinder = getNewDataBinder();
              // while(j<size) {

              // Update response parameters per Service for every WSDL

                     for (DataObject element : wsdlServiceResponseList.get(i).getRows()){
                        //System.out.println("value of i is: " + i + "value of j is : " + j);
                        //System.out.println("inside while loop - response " + service.get("serviceName"));

                        newBinder.putLocal(IDC_SERVICE, AddWsdlServiceParams);
                        newBinder.putLocal("wsdlName",name.get("wsdlName"));
                        newBinder.putLocal("serviceName",service.get("serviceName"));
                        newBinder.putLocal("isRequest","0");
                        newBinder.putLocal("dataName_"+j,element.get("dataName"));
                        newBinder.putLocal("dataIdcName_"+j, element.get("dataIdcName"));
                        newBinder.putLocal("dataType_"+j,element.get("dataType"));
                        newBinder.putLocal("isEnabled_"+j,element.get("isEnabled"));
                        newBinder.putLocal("numElements","8");
                        executeService(newBinder, prop.getProperty("RIDC_TARGET_USER"));
                        j++;
                       
                       // System.out.println("response value of size is:" + size) ;
                  } i++;    // this is innermost for loop
             }              // this is service list for loop
          }                 // this is name list loop
       }                   //  main function
 

/* end */ 





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

                     try{
                       input = new FileInputStream("wsdlconfig.properties");

                     // load a properties file
                     prop.load(input);

                if (m_idcClient == null) {
                        // Client to talk to WebCenter Content
                    //    m_idcClient = (IntradocClient)getIdcClientManager().createClient(IDC_PROTOCOL + RIDC_SERVER + ":" + RIDC_PORT);
                          m_idcClient = (IntradocClient)getIdcClientManager().createClient(prop.getProperty("IDC_PROTOCOL")+"://" + prop.getProperty("RIDC_SERVER_TARGET") + ":" + prop.getProperty("RIDC_PORT_TARGET"));
                }
               } catch (IOException ioe){
                        ioe.printStackTrace();
                      }
              return m_idcClient;
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
