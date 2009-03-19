package eu.planets_project.services.migration.ghostscript;

import eu.planets_project.ifr.core.techreg.api.formats.Format;
import eu.planets_project.ifr.core.techreg.impl.formats.FormatRegistryImpl;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;
import eu.planets_project.services.utils.cli.CliMigrationPaths;

import org.xml.sax.SAXException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingType;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The class GhostscriptMigration migrates from PostScript and PDF
 * to a number of formats.
 * @author <a href="mailto:cjen@kb.dk">Claus Jensen</a>
 */
@Local(Migrate.class)
@Remote(Migrate.class)
@Stateless
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
@WebService(
        name = GhostscriptMigration.NAME,
        serviceName = Migrate.NAME,
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.migrate.Migrate")
public class GhostscriptMigration implements Migrate, Serializable {

    /**
     *  Used for serialization.
     */
    private static final long serialVersionUID = 4771511174207268891L;

    /**
     * The service name.
     */
    static final String NAME = "GhostscriptMigration";

    /**
     *  Used for logging in the Planets framework.
     */
    private final PlanetsLogger log = PlanetsLogger.getLogger(
            GhostscriptMigration.class);

    /**
     * XML configuration file containing commands and pathways.
     */
    private final String configfile = "PA/ghostscript/src/resources"
        + "/ghostscript.paths.xml";

    /**
     * The migration paths of the service.
     */
    private CliMigrationPaths migrationPaths = null;

    /**
     * Format extension of the input stream.
     */
    private String inputFmtExt = null;

    /**
     * Format extension of the output stream.
     */
    private String outputFmtExt = null;

    /**
     * Formats of the input and output streams.
     */
    private List<String> inputFormats = null;
    private List<String> outputFormats = null;
    private HashMap<String, String>  formatMapping = null;

    /**
     * {@inheritDoc}
     *
     * @see eu.planets_project.services.migrate.Migrate#migrate(
     * eu.planets_project.services.datatypes.DigitalObject, java.net.URI,
     * java.net.URI, eu.planets_project.services.datatypes.Parameters)
     */
    public final MigrateResult migrate(final DigitalObject digitalObject,
            final URI inputFormat, final URI outputFormat,
                final Parameters parameters) {

        final ServiceReport report = new ServiceReport();

        try {
            this.init();
        } catch (URISyntaxException e) {
            log.error("[GhostscriptMigration] "
                + "Invalid URI in the paths file", e);
            return this.fail(null);
        }

        // Checking arguments.
        if (digitalObject.equals(null)) {
            report.setError("An empty (null) digital object was given");
            return this.fail(report);
        }

        if (digitalObject.getContent().equals(null)) {
            report.setError("The content of the digital object "
                + "is empty (null)");
            return this.fail(report);
        }

        if (outputFormat.equals(null)) {
            report.setError("An empty (null) output format was given");
            return this.fail(report);
        }

        this.getExtensions(inputFormat, outputFormat);

        final String command = migrationPaths.findMigrationCommand(
                inputFormat, outputFormat);

        if (command == null) {
            report.setError("Could not find the command associated with the "
                    + "migrationPath for the input and output formats");
            return this.fail(report);
        }

        InputStream inStream = digitalObject.getContent().read();

        // This should not be the way to do things, but this works.    
        final File workfolder = FileUtils
        .createWorkFolderInSysTemp("Temp_ghostscript");

        final File tmpInFile = FileUtils.writeInputStreamToFile(
                inStream, workfolder, "planets." + inputFmtExt);

        /* This do not work, but should work.
        // Write input stream to temporary file
        final File tmpInFile = FileUtils.writeInputStreamToTmpFile(
                inStream, "planets", inputFmtExt);
        */

        tmpInFile.deleteOnExit();

        if ( !(tmpInFile.exists() && tmpInFile.isFile()
                && tmpInFile.canRead()) ) {
            log.error("[GhostscriptMigration] Unable to create/use "
                + "temporary input file!");
            return null;
        }
        log.info("[GhostscriptMigration] Temporary input file created: "
                + tmpInFile.getAbsolutePath());
       
        ProcessRunner runner = new ProcessRunner();

        runner.setCommand(Arrays.asList("cmd",
                "/c", command, tmpInFile.getAbsolutePath()));

        log.info("[GhostscriptMigration] Executing command: "
                + command.toString() + " ...");

        runner.setInputStream(inStream);
        runner.setCollection(true);
        runner.setOutputCollectionByteSize(-1);
      
        runner.run();

        int return_code = runner.getReturnCode();

        if (return_code != 0) {
            report.setErrorState(return_code);
            report.setError(runner.getProcessOutputAsString() + "\n"
                    + runner.getProcessErrorAsString());
            return fail(report);
        }

        InputStream newFileStream = runner.getProcessOutput();
        byte[] outbytes = FileUtils.writeInputStreamToBinary(newFileStream);

        DigitalObject outputFile = new DigitalObject.Builder(
                Content.byValue(outbytes)).build();
        return new MigrateResult(outputFile, report);
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     * @return ServiceDescription
     */
    public final ServiceDescription describe() {

        ServiceDescription.Builder builder =
            new ServiceDescription.Builder(NAME,
                Migrate.class.getName());
        try {
            this.init();
            builder.paths(migrationPaths.getAsPlanetsPaths());
        } catch (URISyntaxException e) {
            log.warn("[GhostscriptMigration] Invalid URI in the paths file", e);
        }

        builder.author("Claus Jensen <cjen@kb.dk>");
        builder.classname(this.getClass().getCanonicalName());
        builder.description("Converts from PostScript and PDF to a number "
                + "of other formats");

        builder.version("0.1");
        return builder.build();
    }

    /**
     * Initialize the migration.
     * @throws URISyntaxException which can be thrown by URI in the call to
     * CliMigrationPaths.initialiseFromFile
     */
    private void init() throws URISyntaxException {

        // Input formats.
        inputFormats = new ArrayList<String>();
        inputFormats.add("ps");
        inputFormats.add("pdf");

        // Output formats and associated output parameters.
        outputFormats = new ArrayList<String>();
        outputFormats.add("ps");
        outputFormats.add("pdf");

        // Disambiguation of extensions, e.g. {"JPG","JPEG"} to {"JPEG"}
        // FIXIT This should be supported by the FormatRegistryImpl class, but
        // it does not provide the complete set at the moment.
        formatMapping = new HashMap<String, String>();
        formatMapping.put("ps", "ps");
        formatMapping.put("pdf", "pdf");

        // Gets the migrationPaths from the XML configuration file.
        try {
            if (migrationPaths == null) {
                migrationPaths = CliMigrationPaths.initialiseFromFile(
                        configfile);
            }
        } catch (ParserConfigurationException e) {
            throw new Error("Not supposed to happen", e);
        } catch (IOException e) {
            throw new Error("Not supposed to happen", e);
        } catch (SAXException e) {
            throw new Error("Not supposed to happen", e);
        }
    }

    /**
     * Handles the failure of a migration.
     * @param report Planets ServiceReport containing a status of the migration.
     * @return MigrateResult.
     */
    private MigrateResult fail(final ServiceReport report) {
        report.setErrorState(1);
        return new MigrateResult(null, report);
    }

    /**
     * Get the file extensions for the URIs.
     * @param inputFormat The format of the input.
     * @param outputFormat The format of the output.
     */
    private void getExtensions(final URI inputFormat, final URI outputFormat) {
        if (inputFormat != null && outputFormat != null) {
            inputFmtExt = this.getFormatExt(inputFormat, false);
            outputFmtExt = this.getFormatExt(outputFormat, true);
        }
    }

    /**
     * Gets one extension from a set of possible extensions for the incoming
     * request planets URI (e.g. planets:fmt/ext/jpeg) which matches with
     * one format of the set of Ghostscript's supported input/output formats. If
     * isOutput is false, it checks against the input formats ArrayList,
     * otherwise it checks against the output formats HashMap.
     *
     * @param formatUri Planets URI (e.g. planets:fmt/ext/jpeg)
     * @param isOutput Is the format an input or an output format
     * @return Format extension (e.g. "JPEG")
     */
    private String getFormatExt(final URI formatUri, final boolean isOutput) {
        String fmtStr = null;
        // status variable which indicates if an input/out format has been found
        // while iterating over possible matches
        boolean fmtFound = false;
        // Extensions which correspond to the format
        // planets:fmt/ext/jpg -> { "JPEG", "JPG" }
        // or can be found in the list of supported formats
        FormatRegistryImpl fmtRegImpl = new FormatRegistryImpl();
        Format uriFormatObj = fmtRegImpl.getFormatForURI(formatUri);
        Set<String> reqInputFormatExts = uriFormatObj.getExtensions();
        Iterator<String> itrReq = reqInputFormatExts.iterator();
        // Iterate either over input formats ArrayList or over output formats
        // HasMap
        Iterator<String> itrGhostscript =
            (isOutput) ? outputFormats.iterator() : inputFormats.iterator();
        // Iterate over possible extensions that correspond to the request
        // planets uri.
        while (itrReq.hasNext()) {
            // Iterate over the different extensions of the planets:fmt/ext/jpg
            // format URI, note that the relation of Planets-format-URI to
            // extensions is 1 : n.
            String reqFmtExt = this.normalizeExt((String) itrReq.next());
            while (itrGhostscript.hasNext()) {
                // Iterate over the formats that Ghostscript
                // offers either as input or as output format.
                // See input formats in the this.init() method to see the
                // Ghostscript input/output formats offered by this service.
                String gsFmtStr = (String) itrGhostscript.next();
                if (reqFmtExt.equalsIgnoreCase(gsFmtStr)) {
                    // select the Ghostscript supported format
                    fmtStr = gsFmtStr;
                    fmtFound = true;
                    break;
                }
                if (fmtFound) {
                    break;
                }
            }
        }
        return fmtStr;
    }

    /**
     * Disambiguation (e.g. JPG -> JPEG) according to the formatMapping
     * datas structure defined in this class.
     *
     * @param ext Extension.
     * @return Uppercase disambiguized extension string
     */
    private String normalizeExt(final String ext) {
        String normExt = ext.toUpperCase();
        return ((formatMapping.containsKey(normExt))
            ? (String) formatMapping.get(normExt) : normExt);
    }
}