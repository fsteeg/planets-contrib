package eu.planets_project.services.migration.ghostscript;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.ifr.core.common.conf.Configuration;
import eu.planets_project.ifr.core.common.conf.ServiceConfig;
import eu.planets_project.ifr.core.services.migration.
    genericwrapper2.GenericMigrationWrapper;
import eu.planets_project.ifr.core.services.migration.
    genericwrapper2.utils.DocumentLocator;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.ServiceUtils;

/**
 * The class GhostscriptMigration migrates from PostScript and PDF
 * to a number of formats.
 * @author <a href="mailto:cjen@kb.dk">Claus Jensen</a>
 */
@Stateless
@MTOM
@StreamingAttachment(
        parseEagerly = true, memoryThreshold =
        ServiceUtils.JAXWS_SIZE_THRESHOLD)
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
    static final String NAME = "GhostscriptMigrationService";

    /**
     *  Used for logging in the Planets framework.
     */
    private final Logger log =
                        Logger.getLogger(GhostscriptMigration.class.getName());

    /**
     * XML service configuration file containing commands and pathways.
     */
    private static final String SERVICE_CONFIG_FILE_NAME =
                        "GhostscriptMigrateConfiguration.xml";

    /** Path for Eclipse, the above path do not work in Eclipse
      * so this path has to be use when running tests in Eclipse.
      * Please be aware that this path work for test:local and
      * test:standalone tests but not for test:server!
      */
    //private static final String SERVICE_CONFIG_FILE_NAME = "PA/ghostscript/"
    //                   + "src/resources/GhostscriptMigrateConfiguration.xml";

    /** The file name of the dynamic run-time configuration. **/
    private static final String RUN_TIME_CONFIGURATION_FILE_NAME =
                        "pserv-pa-ghostscript";

    /**
     * {@inheritDoc}
     *
     * @see eu.planets_project.services.migrate.Migrate#migrate(
     *      eu.planets_project.services.datatypes.DigitalObject,
     *      java.net.URI, java.net.URI,
     *      eu.planets_project.services.datatypes.Parameter)
     */
    public final MigrateResult migrate(final DigitalObject digitalObject,
            final URI inputFormat, final URI outputFormat,
                final List<Parameter> parameters) {

        ServiceReport report = new ServiceReport(
                Type.INFO, Status.SUCCESS, "OK");
        checkMigrateArgs(digitalObject, inputFormat, outputFormat, report);

        try {
            final DocumentLocator documentLocator = new DocumentLocator(
                SERVICE_CONFIG_FILE_NAME);

            final Configuration runtimeConfiguration = ServiceConfig
                .getConfiguration(RUN_TIME_CONFIGURATION_FILE_NAME);

            GenericMigrationWrapper genericWrapper =
                new GenericMigrationWrapper(
                        documentLocator.getDocument(),
                        runtimeConfiguration,
                        this.getClass().getCanonicalName());

            return genericWrapper.migrate(digitalObject, inputFormat,
                outputFormat, parameters);

            } catch (Exception exception) {
            log.log(Level.SEVERE, "Migration failed for object with title '"
                + digitalObject.getTitle() + "' from input format URI: "
                + inputFormat + " to output format URI: " + outputFormat,
                exception);

            ServiceReport serviceReport = new ServiceReport(Type.ERROR,
                Status.TOOL_ERROR, exception.toString());

            return new MigrateResult(null, serviceReport);
        }
    }

    /** Check the arguments of migrate method.
     * @param digitalObject From the migrate method.
     * @param inputFormat From the migrate method.
     * @param outputFormat From the migrate method.
     * @param report Planets ServiceReport.
     */
    private void checkMigrateArgs(final DigitalObject digitalObject,
            final URI inputFormat, final URI outputFormat,
            final ServiceReport report) {

        if (digitalObject == null) {
            this.fail(new ServiceReport(Type.ERROR, Status.TOOL_ERROR,
                    "An empty (null) digital object was given"));
        } else if (digitalObject.getContent() == null) {
            this.fail(new ServiceReport(Type.ERROR, Status.TOOL_ERROR,
                    "The content of the digital object " + "is empty (null)"));
            this.fail(report);
        }

        if (inputFormat == null) {
            this.fail(new ServiceReport(Type.ERROR, Status.TOOL_ERROR,
                    "An empty (null) input format was given"));
        }

        if (outputFormat == null) {
            this.fail(new ServiceReport(Type.ERROR, Status.TOOL_ERROR,
                    "An empty (null) output format was given"));
        }
    }

    /**
     * Handles the failure of a migration.
     * @param report Planets ServiceReport containing a status of the migration.
     * @return MigrateResult.
     */
    private MigrateResult fail(final ServiceReport report) {
        return new MigrateResult(null, report);
    }

    /**
     * @see eu.planets_project.services.migrate.Migrate#describe()
     * @return ServiceDescription
     */
    public final ServiceDescription describe() {

        final DocumentLocator documentLocator = new DocumentLocator(
            SERVICE_CONFIG_FILE_NAME);
        try {
            final Configuration runtimeConfiguration = ServiceConfig
                    .getConfiguration(RUN_TIME_CONFIGURATION_FILE_NAME);

            GenericMigrationWrapper genericWrapper =
                new GenericMigrationWrapper(
                        documentLocator.getDocument(),
                        runtimeConfiguration,
                        this.getClass().getCanonicalName());

            return genericWrapper.describe();

        } catch (Exception e) {
            log.log(Level.SEVERE,
                    "Failed getting service description for service: "
                            + this.getClass().getCanonicalName(), e);
            return null;
        }
    }
}
