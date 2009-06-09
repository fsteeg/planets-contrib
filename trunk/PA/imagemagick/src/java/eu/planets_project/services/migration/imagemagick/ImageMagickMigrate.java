package eu.planets_project.services.migration.imagemagick;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.identification.imagemagick.utils.ImageMagickHelper;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.ServiceUtils;


/**
 * @author Peter Melms
 *
 */
@Stateless()
@Local(Migrate.class)
@Remote(Migrate.class)

@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@WebService(
        name = ImageMagickMigrate.NAME, 
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class ImageMagickMigrate implements Migrate, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1999759257332654952L;

    private static Log plogger = LogFactory.getLog(ImageMagickMigrate.class);
    
    /**
     * the service name
     */
    public static final String NAME = "ImageMagickMigrate";

    /** Array of compression type strings */
    public static String[] compressionTypes = new String[11];
    private static final String COMPRESSION_TYPE = "compressionType";
    private static final int COMPRESSION_TYPE_PARAM_DEFAULT = 1; 
    private static final String COMPRESSION_QUALITY_LEVEL = "compressionQuality";
    private static final int COMPRESSION_QUALITY_LEVEL_DEFAULT = 100;
    private static final String IMAGEMAGICK_TEMP = "ImageMagickMigrate";
    private static final String DEFAULT_INPUT_FILE_NAME = "imageMagickInput";
    private static final String DEFAULT_OUTPUT_FILE_NAME = "imageMagickOutput";
    private static final String IMAGE_MAGICK_URI = "http://www.imagemagick.org";
    private static final FormatRegistry formatRegistry = FormatRegistryFactory.getFormatRegistry();
//    private long startTime = 0;
//    private long endTime = 0;

    /**
     * default no arg constructor
     */
    public ImageMagickMigrate() {
        System.setProperty("jmagick.systemclassloader","no"); // Use the JBoss-Classloader, instead of the Systemclassloader.

        compressionTypes[0] = "Undefined Compression";
        compressionTypes[1] = "No Compression";
        compressionTypes[2] = "BZip Compression";
        compressionTypes[3] = "Fax Compression";
        compressionTypes[4] = "Group4 Compression";
        compressionTypes[5] = "JPEG Compression";
        compressionTypes[6] = "JPEG2000 Compression";
        compressionTypes[7] = "LosslessJPEG Compression";
        compressionTypes[8] = "LZW Compression";
        compressionTypes[9] = "RLE Compression";
        compressionTypes[10] = "Zip Compression";

        plogger.info("Hello! Initializing ImageMagickMigrate service...");
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
        ServiceDescription.Builder sd = new ServiceDescription.Builder(NAME,Migrate.class.getCanonicalName());
        sd.author("Peter Melms, mailto:peter.melms@uni-koeln.de");
        sd.description("A wrapper for ImageMagick file format conversions. Using ImageMagick v6.3.9-Q8 and JMagick v6.3.9-Q8.\n" +
                "This service accepts input and target formats of this shape: 'planets:fmt/ext/[extension]'\n" +
        "e.g. 'planets:fmt/ext/tiff' or 'planets:fmt/ext/tif'");

        sd.classname(this.getClass().getCanonicalName());
        sd.version("0.1");

        List<Parameter> parameterList = new ArrayList<Parameter>();
        Parameter compressionTypeParam = new Parameter.Builder("compressionType", "0-10").description( 
                "Allowed int values: 0 - 10").build();
        parameterList.add(compressionTypeParam);

        Parameter compressionLevelParam = new Parameter.Builder("compressionQuality", "0-100").description(
                "This should be an int value between: 0 - 100, representing the compression quality in percent.").build();
        parameterList.add(compressionLevelParam);

        sd.parameters(parameterList);
        
        sd.tool( Tool.create(null, "ImageMagick", "6.3.9-Q8", null, IMAGE_MAGICK_URI) );
        
        
        
        // Migration Paths: List all combinations:
//        List<String> inputFormats = new ArrayList<String> ();
//
//        inputFormats.add("JPEG");
//        inputFormats.add("JP2");
//        inputFormats.add("TIFF");
//        inputFormats.add("GIF");
//        inputFormats.add("PNG");
//        inputFormats.add("BMP");
//        //		inputFormats.add("RAW");
//        inputFormats.add("PCX");
//        inputFormats.add("TGA");
//        inputFormats.add("PDF");
//
//        List<String> outputFormats = new ArrayList<String> ();
//
//        outputFormats.add("TIFF");
//        outputFormats.add("PNG");
//        outputFormats.add("JPEG");
//        outputFormats.add("JP2");
//        outputFormats.add("GIF");
//        outputFormats.add("PDF");
//        //		outputFormats.add("RAW");
//        outputFormats.add("PCX");
//        outputFormats.add("TGA");
//        outputFormats.add("BMP");
        
        // Checks the installed extensions and supported formats on the fly and creates Migration paths matching the systems capabilities.
        if( ImageMagickHelper.getSupportedInputFormats() != null ) {
          sd.paths(ServiceUtils.createMigrationPathways(ImageMagickHelper.getSupportedInputFormats(), ImageMagickHelper.getSupportedOutputFormats()));
//        sd.paths(createMigrationPathwayMatrix(inputFormats, outputFormats));
        }
        return sd.build();
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)
     */
    public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, List<Parameter> parameters) {

        plogger.info("...and ready! Checking input...");
//        startTime = System.currentTimeMillis();
        URI inputFormatFromDigObj = digitalObject.getFormat();
        String inputExt = null;

        if(inputFormatFromDigObj==null) {
            plogger.info("No file format specified for this DigitalObject, using interface inputFormat URI!");
            inputExt = formatRegistry.getFirstExtension(inputFormat);
        }
        else {
            inputExt = formatRegistry.getFirstExtension(inputFormatFromDigObj);
        }

        String outputExt = formatRegistry.getFirstExtension(outputFormat);

        String inputError = null;
        String outputError = null;
        String outputFilePath = null;

        File imageMagickTmpFolder = FileUtils.createWorkFolderInSysTemp(IMAGEMAGICK_TEMP);
        plogger.info("Created tmp folder: " + imageMagickTmpFolder.getAbsolutePath());

        // test if the wanted migrationpath is supported by this service...for the input file
        if(inputExt == null){
            plogger.error("The Format: " + inputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!");
            inputError = "The Format: " + inputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!";
        }

        // test if the wanted migrationpath is supported by this service...for the output file
        if(outputExt == null) {
            plogger.error("The Format: " + outputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!");
            outputError = "The Format: " + outputFormat.toASCIIString() + " is NOT supported by this ImageMagick-Service!";
        }

        // src and target format aren't supported by this service...
        if((inputError != null) && (outputError != null)) {
            StringBuffer allError = new StringBuffer();
            allError.append(inputError);
            allError.append("\n");
            allError.append(outputError);
            return this.returnWithErrorMessage(allError.toString(), null);
        }

        // only the input format is not supported...
        if((inputError != null) && (outputError == null)) {
            return this.returnWithErrorMessage(inputError, null);
        }

        // only the target format isn't supported...
        if((inputError == null) && (outputError != null)) {
            return this.returnWithErrorMessage(outputError, null);
        }

        plogger.info("Getting content from DigitalObject as InputStream...");
        File inputFile;
        File outputFile;

        try {
            plogger.info("Writing content to temp file.");
            inputFile = File.createTempFile( DEFAULT_INPUT_FILE_NAME, "."+inputExt, imageMagickTmpFolder );
            FileUtils.writeInputStreamToFile( digitalObject.getContent().read(), inputFile );
            plogger.info("Temp file created for input: " + inputFile.getAbsolutePath());

            // Also define the output file:
            outputFile = File.createTempFile( DEFAULT_OUTPUT_FILE_NAME, "."+outputExt, imageMagickTmpFolder );
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return this.returnWithErrorMessage("Could not create temporary files!", e1);
        }

        plogger.info("Starting ImageMagick Migration from " + inputExt + " to " + outputExt + "...");

        try {
            plogger.debug("Initialising ImageInfo Object");
            ImageInfo imageInfo = new ImageInfo(inputFile.getAbsolutePath());
            MagickImage image = new MagickImage(imageInfo);
            plogger.info("ImageInfo object created for file: " + inputFile.getAbsolutePath());
            String actualSrcFormat = image.getMagick();
            plogger.info("ImageMagickMigrate: Given src file format extension is: " + inputExt);
            plogger.info("ImageMagickMigrate: Actual src format is: " + actualSrcFormat);
            
            // Has the input file the format it claims it has?
            if(extensionsAreEqual(inputExt, actualSrcFormat) == false) {
                // if NOT just return without doing anything...
                return this.returnWithErrorMessage("The passed input file format (" + inputExt.toUpperCase() + ") does not match the actual format (" + actualSrcFormat + ") of the file!\n" +
                        "This could cause unpredictable behaviour. Nothing has been done!", null);
            }

            // Are there any additional parameters for us?
            if(parameters != null) {
                plogger.info("Got additional parameters:");
                int compressionType;
                int compressionQuality; 
                for (Iterator<Parameter> iterator = parameters.iterator(); iterator.hasNext();) {
                    Parameter parameter = (Parameter) iterator.next();
                    String name = parameter.getName();
                    plogger.info("Got parameter: " + name + " with value: " + parameter.getValue());
                    if(!name.equalsIgnoreCase(COMPRESSION_QUALITY_LEVEL) && !name.equalsIgnoreCase(COMPRESSION_TYPE)) {
                        plogger.info("Invalid parameter with name: " + parameter.getName());

                        plogger.info("Setting compressionQualilty to Default value: " + COMPRESSION_QUALITY_LEVEL_DEFAULT);
                        imageInfo.setQuality(COMPRESSION_QUALITY_LEVEL_DEFAULT);

                        plogger.info("Setting Compression Type to Default value: " + COMPRESSION_TYPE_PARAM_DEFAULT + compressionTypes[COMPRESSION_TYPE_PARAM_DEFAULT]);
                        image.setCompression(COMPRESSION_TYPE_PARAM_DEFAULT);
                    }


                    if(name.equalsIgnoreCase(COMPRESSION_QUALITY_LEVEL)) {
                        compressionQuality = Integer.parseInt(parameter.getValue());
                        if(compressionQuality >=0 && compressionQuality <=100) {
                            plogger.info("Setting compressionQualilty to: " + compressionQuality);
                            imageInfo.setQuality(compressionQuality);
                        }
                        else {
                            plogger.info("Invalid value for compressionQualilty: " + parameter.getValue());
                            plogger.info("Setting compressionQualilty to Default value: " + COMPRESSION_QUALITY_LEVEL_DEFAULT);
                            imageInfo.setQuality(COMPRESSION_QUALITY_LEVEL_DEFAULT);
                        }

                    }

                    if(name.equalsIgnoreCase(COMPRESSION_TYPE)) {
                        compressionType = Integer.parseInt(parameter.getValue());
                        if(compressionType >= 0 && compressionType <= 10) {
                            plogger.info("Trying to set Compression type to: " + compressionTypes[compressionType]);
                            image.setCompression(compressionType);
                        }
                        else {
                            plogger.info("Invalid value for Compression type: " + parameter.getValue());
                            plogger.info("Setting Compression Type to Default value: " + COMPRESSION_TYPE_PARAM_DEFAULT + compressionTypes[COMPRESSION_TYPE_PARAM_DEFAULT]);
                            image.setCompression(COMPRESSION_TYPE_PARAM_DEFAULT);
                        }
                    }
                }
            }
            else {
                plogger.info("No parameters passed! Setting default values for compressionType and compressionQuality");
                image.setCompression(COMPRESSION_TYPE_PARAM_DEFAULT);
                imageInfo.setQuality(COMPRESSION_QUALITY_LEVEL_DEFAULT);
            }

            image.setImageFormat(outputExt);
            plogger.info("Setting new file format for output file to: " + outputExt);

            outputFilePath = outputFile.getAbsolutePath();
            plogger.info("Starting to write result file to: " + outputFilePath);

            image.setFileName(outputFilePath);
            boolean imageMagickSuccess = image.writeImage(imageInfo);
            if(imageMagickSuccess)
                plogger.info("Successfully created result file at: " + outputFilePath);
            else 
                return this.returnWithErrorMessage("Something went terribly wrong with ImageMagick: No output file created!!!", null);

        } catch (MagickException e) {
            e.printStackTrace();
            return this.returnWithErrorMessage("Something went terribly wrong with ImageMagick: ", e);
        }

        DigitalObject newDigObj = null;
        ServiceReport report = null;

        //*******************************************************************************************************
        // commented out the event handling, as this should be handled by L2 workflows and not by services themselves.
        /*String id = Long.toString(serialVersionUID);
        String name = ImageMagickMigrate.NAME;
        String type = "Migrate";


        
        String datetime = ServiceUtils.getSystemDateAndTimeFormatted();
        String summary = "Image migration from " + inputExt.toUpperCase() + " to " + outputExt.toUpperCase() + ".\nUsed tool: ImageMagick.";
        endTime = System.currentTimeMillis();
        double duration = ServiceUtils.calculateDuration(startTime, endTime);


        Agent agent = new Agent(id,name,type);            
        Event event = new Event(summary,datetime,duration,agent,null);*/
        //*******************************************************************************************************

        newDigObj = new DigitalObject.Builder(Content.byValue(outputFile))
        .format(outputFormat)
        .title(outputFile.getName())
        .permanentUri(URI.create("http://planets.services.migration.ImageMagickMigrate"))
//        .events(event)
        .build();

        plogger.info("Created new DigitalObject for result file...");

        report = new ServiceReport(Type.INFO, Status.SUCCESS, "OK");
        plogger.info("Created Service report...");

        plogger.info("Success!! Returning results! Goodbye!");
        return new MigrateResult(newDigObj, report);
    }

    private MigrationPath[] createMigrationPathwayMatrix (List<String> inputFormats, List<String> outputFormats) {
        List<MigrationPath> paths = new ArrayList<MigrationPath>();

        for (Iterator<String> iterator = inputFormats.iterator(); iterator.hasNext();) {
            String input = iterator.next();

            for (Iterator<String> iterator2 = outputFormats.iterator(); iterator2.hasNext();) {
                String output = iterator2.next();
                MigrationPath path = new MigrationPath(formatRegistry.createExtensionUri(input), formatRegistry.createExtensionUri(output), null);
                // Debug...
                //				System.out.println(path.getInputFormat() + " --> " + path.getOutputFormat());
                paths.add(path);
            }

        }

        return paths.toArray(new MigrationPath[]{});
    }


    private boolean compareExtensions(String extension1, String extension2) {

        plogger.info("Starting to compare these two extensions: " + extension1 + " and " + extension2);

        Set <URI> ext1FormatURIs = formatRegistry.getUrisForExtension(extension1.toLowerCase());
        plogger.info("Got list of URIs for " + extension1);

        Set <URI> ext2FormatURIs = formatRegistry.getUrisForExtension(extension2.toLowerCase());
        plogger.info("Got list of URIs for " + extension2);

        boolean success = false;

        plogger.info("Trying to match URIs...");
        for(URI currentUri: ext1FormatURIs) {
            plogger.info("current URI: " + currentUri.toASCIIString());
            if(ext2FormatURIs.contains(currentUri)) {
                success = true;
                break;
            }
            else {
                success = false;
            }
        }
        if(success) {
            plogger.info("Success!");
        }
        else {
            plogger.info("No success.");
        }

        return success;
    }
    
    private boolean extensionsAreEqual(String extension1, String extension2) {
    	if(extension1.contains(".")) {
    		extension1 = extension1.replace(".", "");
    	}
    	if(extension2.contains(".")) {
    		extension2 = extension2.replace(".", "");
    	}
//        plogger.info("Starting to compare these two extensions: " + extension1 + " and " + extension2);

        Set <URI> ext1FormatURIs = formatRegistry.getUrisForExtension(extension1.toLowerCase());
//        plogger.info("Got list of URIs for " + extension1);

        Set <URI> ext2FormatURIs = formatRegistry.getUrisForExtension(extension2.toLowerCase());
//        plogger.info("Got list of URIs for " + extension2);

        boolean success = false;
        
        if(ext1FormatURIs==null || ext2FormatURIs==null) {
        	if(extension1.equalsIgnoreCase(extension2)) {
        		return true;
        	}
        	else {
        		return false;
        	}
        }

//        plogger.info("Trying to match URIs...");
        for(URI currentUri: ext1FormatURIs) {
//            plogger.info("current URI: " + currentUri.toASCIIString());
            if(ext2FormatURIs.contains(currentUri)) {
                success = true;
                break;
            }
            else {
                success = false;
            }
        }
        if(success) {
            plogger.info("Success!");
        }
        else {
            plogger.info("No success.");
        }

        return success;
    }

    private MigrateResult returnWithErrorMessage(String message, Exception e ) {
        if( e == null ) {
            return new MigrateResult(null, ServiceUtils.createErrorReport(message));
        } else {
            return new MigrateResult(null, ServiceUtils.createExceptionErrorReport(message, e));
        }
    }


}
