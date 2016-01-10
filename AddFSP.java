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
 * @Details: This RIDC class will Add a new FSP rule and provision it as per the requirement. For eg: if we want to change
 * Dispersion rule or way Webpath is constructed etc .   
 * External Config file with the parameters and content server connection details 
 */

public class AddFSP {

               private static final String Name = "";
               private static final String QueryTest = "";

               // RIDC connection information
               private static final String IDC_PROTOCOL = "";
               private static final String RIDC_SERVER = "";
               private static final String RIDC_PORT = "";
               private static IdcClientManager m_idcClientManager;
               private static IntradocClient m_idcClient;
               private static final String UTF8 = "UTF-8";

               // User to execute service calls as
               private static final String userName = "";

               private static Properties prop = new Properties();
               private static InputStream input = null;
               private static final String resultSetFolders  = "SEARCH_RESULTS";
               private static final String resultSetContents = "SearchResults";

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

                     // load a properties file
                     prop.load(input);

                if (m_idcClient == null) {
                        // Client to talk to WebCenter Content
                    //    m_idcClient = (IntradocClient)getIdcClientManager().createClient(IDC_PROTOCOL + RIDC_SERVER + ":" + RIDC_PORT);
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
                System.out.println("Inside get new data binder");
                return getIntradocClient().createBinder();
        }


       /**
         * This method gets a new Resultset.
         * @return A new resultset.
         * @throws IdcClientException
         */
       /* public static ResultSet resultSet(String name) throws IdcClientException {
               return getIntradocClient.getResultSet(); 
        }*/



/* 
     *Main class to call the methods for Adding the FSP rule and editing it to set the parameters 
     *First method is for Adding the rule since that rule name has to be passed for subsequest edit method to apply the changes
*/

       public static void main(final String[] args){
	
                       addProvider(Name);
                       editAddRule(Name) ;
                  }       
      
       /* Editing the the FSP Rule and setting the parameters as per Requirement */
             
       private static void editAddRule(String Name)           
               {
                  try {            
                        System.out.println("Inside Content Search method");
                        DataBinder serviceBinder = getNewDataBinder();
                        serviceBinder.putLocal("IdcService", "FS_EDIT_STORAGE_RULE");
                        serviceBinder.putLocal("pName","DefaultFileStore");
                        serviceBinder.putLocal("StorageRule",prop.getProperty("RuleName"));
                        serviceBinder.putLocal("StorageType",prop.getProperty("StorageType"));
                        serviceBinder.putLocal("IsWeblessStore",prop.getProperty("WeblessorNot"));
                        serviceBinder.putLocal("isRuleEdit","");
                        serviceBinder.putLocal("vault",prop.getProperty("vault"));
                        serviceBinder.putLocal("FsWeblayoutDir",prop.getProperty("FsWeblayoutDir"));
                        serviceBinder.putLocal("dispersion",prop.getProperty("dispersion"));
                        serviceBinder.putLocal("endDispMarker","~edisp");
                        serviceBinder.putLocal("web",prop.getProperty("web"));
                        serviceBinder.putLocal("FsHttpWebRoot","$HttpWebRoot$");
                        serviceBinder.putLocal("weburl.file",prop.getProperty("weburl.file"));
                        serviceBinder.putLocal("Submit","OK");
                        // Execute service
                        serviceBinder=executeService(serviceBinder, prop.getProperty("user"));
              } catch (final IdcClientException ice){
                        ice.printStackTrace(System.out);
               }
          }
      
      /* Adding a new FSP Rule to the system */

      private static void addProvider(String Name)
               {
                  try {
                        System.out.println("Inside Content Search method");
                        DataBinder serviceBinder = getNewDataBinder();
                        serviceBinder.putLocal("IdcService", "ADD_EDIT_PROVIDER");
                        serviceBinder.putLocal("IsEdit","1");
                        serviceBinder.putLocal("pName","DefaultFileStore");
                        serviceBinder.putLocal("pType","FileStore");
                        serviceBinder.putLocal("storageRules",prop.getProperty("RuleName"));
                        serviceBinder.putLocal("pDescription",prop.getProperty("Description"));
                        serviceBinder.putLocal("ProviderClass","intradoc.filestore.BaseFileStore");
                        serviceBinder.putLocal("Submit","Update");
                        // Execute service
                        serviceBinder=executeService(serviceBinder, prop.getProperty("user"));
          } catch (final IdcClientException ice){
                        ice.printStackTrace(System.out);
           }
        }
      }
