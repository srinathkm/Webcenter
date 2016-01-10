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
 * Date : 20-11-2015
 * RIDC Class to add image / video renditions to Content Basket - this mimics the renditions being added to Rendition clipboard from Content UI
 * IdcService : ADD_TO_BASKET
 * params : ResultSet "BasketItems" to be passed
 */

public class AddToBasket {
	// RIDC connection information
	private static final String IDC_PROTOCOL = "";
	private static final String RIDC_SERVER = "";
	private static final String RIDC_PORT = "";
      
	private static IdcClientManager m_idcClientManager;
	private static IntradocClient m_idcClient;
        
        private static Properties prop = new Properties();
        private static InputStream input = null;
      
        private static final String UTF8 = "UTF-8";

	// Service parameters
	private static final String IDC_SERVICE = "IdcService";
        // To add renditions :
	private static final String ADD_TO_BASKET = "ADD_TO_BASKET";
        // To get ResultSet where add action will set items 
        private static final String SHOW_BASKET = "SHOW_BASKET";
	
        // User to execute service calls
	private static final String USER = "";

        // Result set 
        private static final String BasketItems = "BasketItems";


        /* 
        * 2 params to be passed in the result set : 
        * dID - of the content item
        * extraRenditionName - which rendition to be associated with basket eg : TIFF / JPG / FLV
        */
        public static DataResultSet.Field dID= new DataResultSet.Field("dID");
        public static DataResultSet.Field RenditionName= new DataResultSet.Field("extraRenditionName");
         

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) {
		try {

                       // Get ResultSet BasketItems 
                        final DataResultSet templateResultSet = getBasketItems();
			
                      // Initialize the result set and add the renditions to it :

                        DataFactory dataFactory =m_idcClient.getDataFactory();
                        final DataResultSet BasketItems = dataFactory.createResultSet();
			AddtoBasket(BasketItems);

		} catch (final IdcClientException ice){
			ice.printStackTrace(System.out);
		}
	}

        /**
         * This method executes SHOW_BASKET
         * @return BasketItems ResultSet.
         * @throws IdcClientException
         * @throws IOException
         */

        private static DataResultSet getBasketItems() throws IdcClientException {
                // DataBinder for service call
                DataBinder serviceBinder = getNewDataBinder();

                // Set service parameters - SHOW_BASKET
                serviceBinder.putLocal(IDC_SERVICE, SHOW_BASKET);

                // Execute service
                serviceBinder = executeService(serviceBinder,prop.getProperty("user"));

                // Return BasketItems  ResultSet
                return serviceBinder.getResultSet(BasketItems);


        }


        /**
	 * This method executes ADD_TO_BASKET
	 * @param ResultSet BasketItems
	 * @throws IdcClientException
	 */
	private static void AddtoBasket(final DataResultSet BasketItems) throws IdcClientException {
		// DataBinder for service call
		final DataBinder serviceBinder = getNewDataBinder();

		// Initialize Idc Service 
		serviceBinder.putLocal(IDC_SERVICE, ADD_TO_BASKET);

               /** 
                 * Adding the 2 parameters which need to be passed to BasketItems result set
               */
                
               BasketItems.addField(dID,"");
               BasketItems.addField(RenditionName,"");

                 /**
                 * Array list initialized to pass the values to 2 parameters in BasketItems resultset 
                 */
          
                ArrayList<String> rowList = new ArrayList<String>(2);
                rowList.add(prop.getProperty("dID"));
                rowList.add(prop.getProperty("RenditionName"));
                
                BasketItems.addRow(rowList);
                
                serviceBinder.addResultSet("BasketItems",BasketItems);
                
                // Execute service
		executeService(serviceBinder, prop.getProperty("user"));
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
       
        try{
            input = new FileInputStream("config.file");
 
            //load the properties file
            prop.load(input);
      
		if (m_idcClient == null) {
	   // Client to talk to WebCenter Content
           m_idcClient = (IntradocClient)getIdcClientManager().createClient(prop.getProperty("IDC_PROTOCOL")+"://" + prop.getProperty("RIDC_SERVER") + ":" + prop.getProperty("RIDC_PORT"));
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
