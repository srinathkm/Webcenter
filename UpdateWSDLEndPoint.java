import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.*;
import java.util.*;

public class UpdateWSDLEndPoint {

 public static void updateUrl() {
 try {
         
        Properties prop = new Properties();
        InputStream input = new FileInputStream("wsdlconfig.properties");
        prop.load(input);
        //File folder = new File("/home/oracle/fmw_ps6/user_projects/domains/fmwps6_domain/ucm/cs/weblayout/groups/secure/wsdl/custom/");
        File folder = new File(prop.getProperty("WSDL_DIR_LOCATION"));
        File[] listOfFiles = folder.listFiles();
         
       //String inputFile = "/home/oracle/fmw_ps6/user_projects/domains/fmwps6_domain/ucm/cs/weblayout/groups/secure/wsdl/custom/samp1.wsdl"; 	    	
          
               // input = new FileInputStream("wsdlconfig.properties");
                // load a properties file
     
        for (int i = 0; i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {

              //File wsdlFile = new File("/home/oracle/fmw_ps6/user_projects/domains/fmwps6_domain/ucm/cs/weblayout/groups/sec"+listOfFiles[i].getName());
              File wsdlFile = new File(prop.getProperty("WSDL_DIR_LOCATION")+listOfFiles[i].getName());
              DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
              DocumentBuilder docBuilder =  docFactory.newDocumentBuilder();
              Document doc = docBuilder.parse(wsdlFile);
              
              Node wsdls = doc.getFirstChild();
              Node soaplocation = doc.getElementsByTagName("soap:address").item(0);
              //read the soap:address node
              NamedNodeMap hostname= soaplocation.getAttributes();
              //read and store the location tag
              Node nodeAttr = hostname.getNamedItem("location");
              // nodeAttr.setTextContent("http://linas5.idc.oracle.com:16218/_dav/cs/idcplg");
              // update the location tag with the target server's endpoint url
              nodeAttr.setTextContent(prop.getProperty("TARGET_ENDPOINT"));
              
              // save the hostname change to the wsdl file on the disk location
              TransformerFactory transformerFactory = 
              TransformerFactory.newInstance();
              Transformer transformer = transformerFactory.newTransformer();
              DOMSource source = new DOMSource(doc);
              StreamResult result = new StreamResult(wsdlFile);
              transformer.transform(source, result);
            }
        }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
