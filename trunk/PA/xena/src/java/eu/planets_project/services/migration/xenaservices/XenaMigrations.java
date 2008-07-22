/**
 * This file is based on Xena, but heavily modified.
 * 
 */
package eu.planets_project.services.migration.xenaservices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

import com.sun.star.beans.PropertyValue;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;

import eu.planets_project.ifr.core.common.logging.PlanetsLogger;

/**
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class XenaMigrations {
    PlanetsLogger log = PlanetsLogger.getLogger(XenaMigrations.class);
    /*
     * Code from the Xena project.
     */

    public String ooffice_install_dir;
    // Values like PDF might work, writer_pdf_Export should work.
    //  - http://www.oooforum.org/forum/viewtopic.phtml?p=167815
    //  - List of filters: http://www.oooforum.org/forum/viewtopic.phtml?t=3549
    public final static String EXPORT_FILTER_NONE = "";
    public final static String EXPORT_FILTER_PDF = "writer_pdf_Export";
    public String ooffice_export_filter = EXPORT_FILTER_NONE;
    // Import filters:
    public final static String IMPORT_FILTER_NONE = null;
    public final static String IMPORT_FILTER_DOC = "doc";
    public String ooffice_import_filter = IMPORT_FILTER_DOC;
    
    
    public final static String OPEN_DOCUMENT_PREFIX = "opendocument";
//    private final static String OPEN_DOCUMENT_URI = "http://preservation.naa.gov.au/odf/1.0";
    public final static String DOCUMENT_TYPE_TAG_NAME = "type";
    public final static String DOCUMENT_EXTENSION_TAG_NAME = "extension";
    public final static String PROCESS_DESCRIPTION_TAG_NAME = "description";
    private final static String OS_X_ARCHITECTURE_NAME = "mac os x";

    private static Logger logger = Logger.getLogger(DocToODFXena.class.getName());

    /*
    private final static String DESCRIPTION =
        "The following data is a MIME-compliant (RFC 1421) PEM base64 (RFC 1421) representation of an Open Document Format "
                + "(ISO 26300, Version 1.0) document, produced by Open Office version 2.0.";
*/
    
    /**
     * 
     */
    public XenaMigrations() {
        Properties props = new Properties();
        try {
            props.load( this.getClass().getResourceAsStream("/eu/planets_project/services/migration/xenaservices/xena.properties"));
            this.ooffice_install_dir = props.getProperty("openoffice.install.dir");
        } catch( IOException e ) {
            // Use a default for now.
            this.ooffice_install_dir  = "C:/Program Files/OpenOffice.org 2.3";
        }
        log.info("Pointed to OOffice in: "+this.ooffice_install_dir);
    }

    /**
     * 
     * @param fname
     * @param is
     * @param extension
     * @param visible
     * @return
     * @throws Exception
     */
    private XComponent loadDocument(String fname, InputStream is, String extension, boolean visible ) throws Exception {
        File input = File.createTempFile("input", "." + extension);
        try {
            input.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(input);
            byte[] buf = new byte[4096];
            int n;
            while (0 < (n = is.read(buf))) {
                fos.write(buf, 0, n);
            }
            fos.close();
            XComponent rtn = loadDocument(fname, input, visible );
            return rtn;
        } finally {
            input.delete();
        }
    }

    /**
     * 
     * @param fname
     * @param input
     * @param visible
     * @return
     * @throws Exception
     */
    static XComponent loadDocument(String fname, File input, boolean visible) throws Exception {
        /*
         * Bootstraps a servicemanager with the jurt base components registered
         */
        XMultiServiceFactory xmultiservicefactory = com.sun.star.comp.helper.Bootstrap.createSimpleServiceManager();

        /*
         * Creates an instance of the component UnoUrlResolver which supports the services specified by the factory.
         */
        Object objectUrlResolver = xmultiservicefactory.createInstance("com.sun.star.bridge.UnoUrlResolver");

        // Create a new url resolver
        XUnoUrlResolver xurlresolver = (XUnoUrlResolver) UnoRuntime.queryInterface(XUnoUrlResolver.class, objectUrlResolver);

        // Resolves an object that is specified as follow:
        // uno:<connection description>;<protocol description>;<initial object name>
        Object objectInitial = null;
        String address = "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";
        try {
            objectInitial = xurlresolver.resolve(address);
        } catch (com.sun.star.connection.NoConnectException ncex) {
            // Could not connect to OpenOffice, so start it up and try again
            try {
                startOpenOffice(fname);
                objectInitial = xurlresolver.resolve(address);
            } catch (Exception ex) {
                // If it fails again for any reason, just bail
                throw new Exception("Could not start OpenOffice", ex);
            }
        } catch (com.sun.star.uno.RuntimeException rtex) {
            // Could not connect to OpenOffice, so start it up and try again
            try {
                startOpenOffice(fname);
                objectInitial = xurlresolver.resolve(address);
            } catch (Exception ex) {
                // If it fails again for any reason, just bail
                throw new Exception("Could not start OpenOffice", ex);
            }
        }

        // Create a service manager from the initial object
        xmultiservicefactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, objectInitial);

        /*
         * A desktop environment contains tasks with one or more frames in which components can be loaded. Desktop is
         * the environment for components which can instanciate within frames.
         */
        XComponentLoader xcomponentloader =
            (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, xmultiservicefactory.createInstance("com.sun.star.frame.Desktop"));

        PropertyValue[] loadProperties = null;
        if (visible) {
            loadProperties = new PropertyValue[0];
        } else {
            loadProperties = new PropertyValue[1];
            loadProperties[0] = new PropertyValue();
            loadProperties[0].Name = "Hidden";
            loadProperties[0].Value = new Boolean(true);
        }
        return xcomponentloader.loadComponentFromURL("file:///" + input.getAbsolutePath().replace('\\', '/'), "_blank", 0, loadProperties);
    }

    /**
     * 
     * @param fname
     * @throws Exception
     * @throws InterruptedException
     */
    private static void startOpenOffice(String fname ) throws Exception, InterruptedException {
        
        if (fname == null || fname.equals("")) {
            throw new Exception("OpenOffice.org location not configured.");
        }

        // NeoOffice/OpenOffice on OS X has a different program structure than that for Windows and Linux, so we
        // need a special case...
        File sofficeProgram;
        if (System.getProperty("os.name").toLowerCase().equals(OS_X_ARCHITECTURE_NAME)) {
            sofficeProgram = new File(new File(fname, "Contents/MacOS"), "soffice.bin");
        } else {
            sofficeProgram = new File(new File(fname, "program"), "soffice");
        }
        List<String> commandList = new ArrayList<String>();
        commandList.add(sofficeProgram.getAbsolutePath());
        commandList.add("-nologo");
        commandList.add("-nodefault");
        commandList.add("-accept=socket,port=8100;urp;");
        String[] commandArr = commandList.toArray(new String[0]);
        try {
            logger.finest("Starting OpenOffice process");
            Runtime.getRuntime().exec(commandArr);
        } catch (IOException x) {
            throw new Exception("Cannot start OpenOffice.org. Try Checking Office Properties. " + sofficeProgram.getAbsolutePath(), x);
        }

        try {
            int sleepSeconds = 50;
//              Integer.parseInt(propManager.getPropertyValue(OfficeProperties.OFFICE_PLUGIN_NAME, OfficeProperties.OOO_SLEEP_PROP_NAME));
            Thread.sleep(1000 * sleepSeconds);
        } catch (NumberFormatException nfex) {
            throw new Exception("Cannot start OpenOffice.org due to invalid startup sleep time. " + "Try Checking Office Properties. ", nfex);
        }
    }

    /**
     * 
     * @param input
     * @param output
     * @throws SAXException
     * @throws IOException
     */
    public void transform(URI input, URI output) throws SAXException, IOException {
        
        FileInputStream inputStream = new FileInputStream( new File( input ));

        try {
            boolean visible = false;

            // Open our office document...
            XComponent objectDocumentToStore =
                loadDocument( this.ooffice_install_dir, inputStream, this.ooffice_import_filter, visible );

            // Getting an object that will offer a simple way to store a document to a URL.
            XStorable xstorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, objectDocumentToStore);

            if (xstorable == null) {
                throw new SAXException("Cannot connect to OpenOffice.org - possibly something wrong with the input file");
            }

            // Preparing properties for converting the document
            PropertyValue propertyvalue[] = new PropertyValue[2];
            // Setting the flag for overwriting
            propertyvalue[0] = new PropertyValue();
            propertyvalue[0].Name = "Overwrite";
            propertyvalue[0].Value = new Boolean(true);

            // Setting the optional filter name
            if (ooffice_export_filter != null
                    && !"".equals(ooffice_export_filter)) {
                propertyvalue[1] = new PropertyValue();
                propertyvalue[1].Name = "FilterName";

                propertyvalue[1].Value = ooffice_export_filter;
            }
            
            // Storing and converting the document
            try {
                xstorable.storeToURL(output.toURL().toString(), propertyvalue);
            } catch (Exception e) {
                throw new Exception(
                                        "Cannot convert to open document format. Maybe your OpenOffice.org installation does not have installed: "
                                                + ooffice_export_filter
                                                + " or maybe the document is password protected or has some other problem. Try opening in OpenOffice.org manually.",
                                        e);
            }
            // Getting the method dispose() for closing the document
            XComponent xcomponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xstorable);
            // Closing the converted document
            xcomponent.dispose();
/*          if (output.length() == 0) {
                throw new Exception("OpenOffice open document file is empty. Do you have OpenOffice Java integration installed?");
            }*/
        } catch (Exception e) {
            logger.log(Level.FINEST, "Problem normalisting office document", e);
            throw new SAXException(e);
        }
        // Check file was created successfully by opening up the zip and checking for at least one entry
        // Base64 encode the file and write out to content handler
        try {
/*
            ContentHandler ch = getContentHandler();
            AttributesImpl att = new AttributesImpl();
            String tagURI = OPEN_DOCUMENT_URI;
            String tagPrefix = OPEN_DOCUMENT_PREFIX;
            ZipFile openDocumentZip = new ZipFile(output);
            // Not sure if this is even possible, but worth checking I guess...
            if (openDocumentZip.size() == 0) {
                throw new IOException("An empty document was created by OpenOffice");
            }
            att.addAttribute(OPEN_DOCUMENT_URI, PROCESS_DESCRIPTION_TAG_NAME, PROCESS_DESCRIPTION_TAG_NAME, "CDATA", DESCRIPTION);
            att.addAttribute(OPEN_DOCUMENT_URI, DOCUMENT_TYPE_TAG_NAME, DOCUMENT_TYPE_TAG_NAME, "CDATA", type.getName());
            att.addAttribute(OPEN_DOCUMENT_URI, DOCUMENT_EXTENSION_TAG_NAME, DOCUMENT_EXTENSION_TAG_NAME, "CDATA", officeType.fileExtension());

            InputStream is = new FileInputStream(output);
            ch.startElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix, att);
            InputStreamEncoder.base64Encode(is, ch);
            ch.endElement(tagURI, tagPrefix, tagPrefix + ":" + tagPrefix);
        } catch (ZipException ex) {
            throw new IOException("OpenOffice could not create the open document file");
            */
        } finally {
//          output.delete();
        }
    }
    
    
    /**
     * @return the ooffice_export_filter
     */
    public String getOoffice_export_filter() {
        return ooffice_export_filter;
    }

    /**
     * @param ooffice_export_filter the ooffice_export_filter to set
     */
    public void setOoffice_export_filter(String ooffice_export_filter) {
        this.ooffice_export_filter = ooffice_export_filter;
    }
    
    /**
     * @return the ooffice_import_filter
     */
    public String getOoffice_import_filter() {
        return ooffice_import_filter;
    }

    /**
     * @param ooffice_import_filter the ooffice_import_filter to set
     */
    public void setOoffice_import_filter(String ooffice_import_filter) {
        this.ooffice_import_filter = ooffice_import_filter;
    }

    /**
     * This is the actual class that does the work.
     * 
     * @param binary
     * @return
     */
    public byte[] basicMigrateOneBinary ( byte[] binary ) {

        File input, output;
        try {
            input = File.createTempFile("input","0");
            input.deleteOnExit();
            output = File.createTempFile("output","0");
            output.deleteOnExit();
        } catch ( IOException e ) {
            log.error("Could not create temporary files! "+e);
            return null;
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(input);
            fos.write(binary);
            fos.flush();
            fos.close();
        } catch( FileNotFoundException e ) {
            log.error("Creating "+input.getAbsolutePath()+" :: " +e);
            return null;
        } catch( IOException e ) {
            log.error("Creating "+input.getAbsolutePath()+" :: " +e);
            return null;
        }
        try {
            this.transform(input.toURI(), output.toURI());
        } catch( SAXException e ) {
            log.error("Transforming "+input.getAbsolutePath()+" :: " +e);
            return null;
        } catch( IOException e ) {
            log.error("Transforming "+input.getAbsolutePath()+" :: " +e);
            return null;
        }
        
        
        byte[] result = null;
        try {
            result = XenaMigrations.getByteArrayFromFile(output);
        } catch( IOException e ) {
            log.error("Returning "+input.getAbsolutePath()+" :: " +e);
            return null;
        }
        
        // Delete the temporaries:
        input.delete();
        output.delete();
        
        // Return the result.
        return result; 
    }
    
    /**
     *     // FIXME Refactor this into common.
     * @param file
     * @return
     * @throws IOException
     */
    private static byte[] getByteArrayFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // throw new
            // IllegalArgumentException("getBytesFromFile@JpgToTiffConverter::
            // The file is too large (i.e. larger than 2 GB!");
            System.out.println("Datei ist zu gross (e.g. groesser als 2GB)!");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
    
}
