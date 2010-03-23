/**
 * 
 */
package eu.planets_project.services.migration.dia.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * @author Thomas Skou Hansen &lt;tsh@statsbiblioteket.dk&gt;
 */
public class DiaMigrationServiceTest extends TestCase {

    /**
     * The location of this service when deployed.
     */
    private String wsdlLocation = "/pserv-pa-dia/DiaMigrationService?wsdl";

    /**
     * A holder for the object to be tested.
     */
    private Migrate migrationService = null;

    /**
     * File path to the dia test files used by this test class.
     */
    private final File DIA_TEST_FILE_PATH = new File(
	    "tests/test-files/images/vector/dia");

    /**
     * File path to the Xfig test files used by this test class.
     */
    private final File FIG_TEST_FILE_PATH = new File(
	    "tests/test-files/images/vector/fig");

    private Set<URI> inputFormatURIs;

    private Set<URI> outputFormatURIs;

    /**
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {
	migrationService = ServiceCreator.createTestService(Migrate.QNAME,
		DiaMigrationService.class, wsdlLocation);
	initialiseInputFormatURIs();
	initialiseOutputFormatURIs();
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    public void tearDown() throws Exception {
    }

    /**
     * Test migration from Dia to SVG version 1.1
     * 
     * Test method for
     * {@link eu.planets_project.services.migration.dia.impl.DiaMigrationService#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)}
     * .
     */
    @Test
    public void testMigrationDiaToSvg() throws Exception {

	final String diaTestFileName = "Arrows_doublestraight_arrow2.dia";

	/**
	 * Full path to the Dia test file to use.
	 */
	final File diaTestFile = new File(DIA_TEST_FILE_PATH, diaTestFileName);

	// Dia file format URI
	final URI diaFormatURI = new URI("info:pronom/x-fmt/381");

	// SVG version 1.0 format URI
	final URI svgFormatURI = new URI("info:pronom/fmt/91");

	final DigitalObject.Builder digitalObjectBuilder = new DigitalObject.Builder(
		Content.byValue(diaTestFile));
	digitalObjectBuilder.format(diaFormatURI);
	digitalObjectBuilder.title(diaTestFileName);
	final DigitalObject digitalObject = digitalObjectBuilder.build();

	final List<Parameter> testParameters = new ArrayList<Parameter>();
	MigrateResult migrationResult = migrationService.migrate(digitalObject,
		diaFormatURI, svgFormatURI, testParameters);

	final ServiceReport serviceReport = migrationResult.getReport();
	final ServiceReport.Status migrationStatus = serviceReport.getStatus();
	assertEquals(ServiceReport.Status.SUCCESS, migrationStatus);

	// TODO: Can we make some meaningful tests on the output from
	// migrationResult.getDigitalObject()?
    }

    /**
     * Test migration from Xfig to Dia.
     * 
     * Test method for
     * {@link eu.planets_project.services.migration.dia.impl.DiaMigrationService#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)}
     * .
     */
    @Test
    public void testMigrationFigToDia() throws Exception {

	final String figTestFileName = "z80pio.fig";

	/**
	 * Full path to the Fig test file to use.
	 */
	final File figTestFile = new File(FIG_TEST_FILE_PATH, figTestFileName);

	// Fig Planets (pseudo) format URI
	final URI figFormatURI = new URI("planets:fmt/ext/fig");

	// Dia (unspecified version) PRONOM format URI
	final URI diaFormatURI = new URI("info:pronom/x-fmt/381");

	final DigitalObject.Builder digitalObjectBuilder = new DigitalObject.Builder(
		Content.byValue(figTestFile));
	digitalObjectBuilder.format(figFormatURI);
	digitalObjectBuilder.title(figTestFileName);

	final DigitalObject digitalObject = digitalObjectBuilder.build();

	final List<Parameter> testParameters = new ArrayList<Parameter>();
	final MigrateResult migrationResult = migrationService.migrate(
		digitalObject, figFormatURI, diaFormatURI, testParameters);

	final ServiceReport serviceReport = migrationResult.getReport();
	final ServiceReport.Status migrationStatus = serviceReport.getStatus();
	assertEquals(ServiceReport.Status.SUCCESS, migrationStatus);

	// TODO: Can we make some meaningful tests on the output from
	// migrationResult.getDigitalObject()?
    }

    /**
     * Test migration from Dia to PNG.
     * 
     * Test method for
     * {@link eu.planets_project.services.migration.dia.impl.DiaMigrationService#migrate(eu.planets_project.services.datatypes.DigitalObject, java.net.URI, java.net.URI, eu.planets_project.services.datatypes.Parameter)}
     * .
     */
    @Test
    public void testMigrationDiaToPng() throws Exception {

	final String diaTestFileName = "CompositeAction.dia";

	/**
	 * Full path to the Fig test file to use.
	 */
	final File figTestFile = new File(DIA_TEST_FILE_PATH, diaTestFileName);

	// Dia (unspecified version) PRONOM format URI
	final URI diaFormatURI = new URI("info:pronom/x-fmt/381");

	// PNG version 1.2 PRONOM format URI
	final URI pngFormatURI = new URI("info:pronom/fmt/13");

	final DigitalObject.Builder digitalObjectBuilder = new DigitalObject.Builder(
		Content.byValue(figTestFile));
	digitalObjectBuilder.format(diaFormatURI);
	digitalObjectBuilder.title(diaTestFileName);

	final DigitalObject digitalObject = digitalObjectBuilder.build();

	final List<Parameter> testParameters = new ArrayList<Parameter>();
	final MigrateResult migrationResult = migrationService.migrate(
		digitalObject, diaFormatURI, pngFormatURI, testParameters);

	final ServiceReport serviceReport = migrationResult.getReport();
	final ServiceReport.Status migrationStatus = serviceReport.getStatus();
	assertEquals(ServiceReport.Status.SUCCESS, migrationStatus);

	// TODO: Can we make some meaningful tests on the output from
	// migrationResult.getDigitalObject()?
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !! Warning! You may turn blind if you look at the code below this line.!!
    // !! !!
    // !! Please leave it, I'll clean up later - Thomas (SB) !!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    /**
     * Test method for
     * {@link eu.planets_project.services.migration.dia.impl.DiaMigrationService#describe()}
     * .
     */
    @Test
    public void testDescribe() throws Exception {

	final ServiceDescription diaServiceDescription = migrationService
		.describe();

	assertEquals("Un-expected author (creator) information.",
		"\"Thomas Skou Hansen <tsh@statsbiblioteket.dk>\"",
		diaServiceDescription.getAuthor());

	assertNotNull("The migration service does not provide a description.",
		diaServiceDescription.getDescription());

	final URI expectedFurtherInfoURI = new URI("http://live.gnome.org/Dia");
	assertEquals("Un-expected text returned by getFurtherInfo().",
		expectedFurtherInfoURI, diaServiceDescription.getFurtherInfo());

	assertNotNull("The migration service does not provide an identifier.",
		diaServiceDescription.getIdentifier());

	// verifyInputFormats(diaServiceDescription.getInputFormats());

	assertNotNull(
		"The migration service does not provide instructions for the use of this service.",
		diaServiceDescription.getInstructions());

	assertNotNull("The migration service does not provide a name.",
		diaServiceDescription.getName());

	// verifyMigrationPaths(diaServiceDescription.getPaths());

	assertNotNull(
		"The migration service does not provide a list of properties.",
		diaServiceDescription.getProperties());

	assertNotNull("The migration service does not provide a tool URI.",
		diaServiceDescription.getTool());

	assertNotNull(
		"The migration service does not provide version information.",
		diaServiceDescription.getVersion());

	// System.out.println("@%^@^%@%^ GNARF>>> "
	// + diaServiceDescription.getServiceProvider());
	// FIXME! Enable when the end-point is correctly configured...
	// assertEquals("Un-expected end-point URL.",
	// "FNaaaaa", diaServiceDescription.getEndpoint());

	assertEquals("Un-expected service provider information.",
		"Dia Migration Service Test Provider", diaServiceDescription
			.getServiceProvider());

	assertEquals("Un-expected interface type.",
		"eu.planets_project.services.migrate.Migrate",
		diaServiceDescription.getType());

	// FIXME! test code, kill!
	// String[] suffixes = new String[] { "bmp", "gif", "jpg", "png", "pnm",
	// "ras", "tif" };
	// HashMap<String, Set<URI>> formatURIMap = new HashMap<String,
	// Set<URI>>();
	// final FormatRegistry fm = FormatRegistryFactory.getFormatRegistry();
	// for (String suffix : suffixes) {
	// formatURIMap.put(suffix, fm.getURIsForExtension(suffix));
	// System.out.println(suffix + " : " + formatURIMap.get(suffix));
	// }

    }

    @SuppressWarnings("unused")
    private void verifyMigrationPaths(List<MigrationPath> migrationPaths) {
	// TODO: More intelligent test needed.
	assertNotNull(
		"The migration service does not provide a list of migration paths.",
		migrationPaths);

	final List<MigrationPath> testPaths = MigrationPath.constructPaths(
		inputFormatURIs, outputFormatURIs);
	for (MigrationPath migrationPath : testPaths) {
	    System.out.println("///" + migrationPath.toString());
	}

    }

    private void verifyInputFormats(List<URI> inputFormats) {
	// TODO: More intelligent test needed.
	assertNotNull(
		"The migration service does not provide a list of possible input formats.",
		inputFormats);

	// Check if the tool allows input formats that are not expected by this
	// test class.
	for (URI uri : inputFormats) {
	    assertTrue(
		    "Unexpected allowed input format URI reported by the tool: "
			    + uri, inputFormatURIs.contains(uri));
	}

	// Check that the tool allows all input formats expected by this test.
	for (URI uri : inputFormatURIs) {
	    assertTrue("Input format URI is not supported by the tool: " + uri,
		    inputFormats.contains(uri));
	}
    }

    private void initialiseInputFormatURIs() throws URISyntaxException {
	inputFormatURIs = new HashSet<URI>();

	// DIA URI
	inputFormatURIs.add(new URI("info:pronom/x-fmt/381"));

	// SVG URIs
	inputFormatURIs.add(new URI("info:pronom/fmt/91"));
	inputFormatURIs.add(new URI("info:pronom/fmt/92"));

	// PNG URIs
	inputFormatURIs.add(new URI("info:pronom/x-fmt/11"));
	inputFormatURIs.add(new URI("info:pronom/x-fmt/12"));
	inputFormatURIs.add(new URI("info:pronom/x-fmt/13"));

	// DXF URIs
	inputFormatURIs.add(new URI("info:pronom/fmt/63"));
	inputFormatURIs.add(new URI("info:pronom/fmt/64"));
	inputFormatURIs.add(new URI("info:pronom/fmt/65"));
	inputFormatURIs.add(new URI("info:pronom/fmt/66"));
	inputFormatURIs.add(new URI("info:pronom/fmt/67"));
	inputFormatURIs.add(new URI("info:pronom/fmt/68"));
	inputFormatURIs.add(new URI("info:pronom/fmt/69"));
	inputFormatURIs.add(new URI("info:pronom/fmt/70"));
	inputFormatURIs.add(new URI("info:pronom/fmt/71"));
	inputFormatURIs.add(new URI("info:pronom/fmt/72"));
	inputFormatURIs.add(new URI("info:pronom/fmt/73"));
	inputFormatURIs.add(new URI("info:pronom/fmt/74"));
	inputFormatURIs.add(new URI("info:pronom/fmt/75"));
	inputFormatURIs.add(new URI("info:pronom/fmt/76"));
	inputFormatURIs.add(new URI("info:pronom/fmt/77"));
	inputFormatURIs.add(new URI("info:pronom/fmt/78"));
	inputFormatURIs.add(new URI("info:pronom/fmt/79"));
	inputFormatURIs.add(new URI("info:pronom/fmt/80"));
	inputFormatURIs.add(new URI("info:pronom/fmt/81"));
	inputFormatURIs.add(new URI("info:pronom/fmt/82"));
	inputFormatURIs.add(new URI("info:pronom/fmt/83"));
	inputFormatURIs.add(new URI("info:pronom/fmt/84"));
	inputFormatURIs.add(new URI("info:pronom/fmt/85"));

	// TODO: FIG URIs are not provided by PRONOM. Add these when possible.

	// BMP URIs
	inputFormatURIs.add(new URI("info:pronom/x-fmt/25"));
	inputFormatURIs.add(new URI("info:pronom/fmt/114"));
	inputFormatURIs.add(new URI("info:pronom/fmt/115"));
	inputFormatURIs.add(new URI("info:pronom/fmt/116"));
	inputFormatURIs.add(new URI("info:pronom/fmt/117"));
	inputFormatURIs.add(new URI("info:pronom/fmt/118"));
	inputFormatURIs.add(new URI("info:pronom/fmt/119"));
	inputFormatURIs.add(new URI("info:pronom/x-fmt/270"));

	// GIF URIs
	inputFormatURIs.add(new URI("info:pronom/fmt/3"));
	inputFormatURIs.add(new URI("info:pronom/fmt/4"));

	// TODO: PNM URIs are not provided by PRONOM. Add these when possible.

	// RAS URI
	inputFormatURIs.add(new URI("info:pronom/x-fmt/184"));

	// TIF URIs
	inputFormatURIs.add(new URI("info:pronom/fmt/7"));
	inputFormatURIs.add(new URI("info:pronom/fmt/8"));
	inputFormatURIs.add(new URI("info:pronom/fmt/9"));
	inputFormatURIs.add(new URI("info:pronom/fmt/10"));
	inputFormatURIs.add(new URI("info:pronom/fmt/152"));
	inputFormatURIs.add(new URI("info:pronom/fmt/153"));
	inputFormatURIs.add(new URI("info:pronom/fmt/154"));
	inputFormatURIs.add(new URI("info:pronom/fmt/155"));
	inputFormatURIs.add(new URI("info:pronom/fmt/156"));
	inputFormatURIs.add(new URI("info:pronom/x-fmt/399"));
	inputFormatURIs.add(new URI("info:pronom/x-fmt/387"));
	inputFormatURIs.add(new URI("info:pronom/x-fmt/388"));
    }

    private void initialiseOutputFormatURIs() throws URISyntaxException {

	outputFormatURIs = new HashSet<URI>();

	// CGM URI
	outputFormatURIs.add(new URI("info:pronom/x-fmt/142"));

	// DIA URI
	outputFormatURIs.add(new URI("info:pronom/x-fmt/381"));

	// TODO: SHAPE URI is not provided by PRONOM. Add when possible.

	// PNG URIs
	outputFormatURIs.add(new URI("info:pronom/fmt/11"));
	outputFormatURIs.add(new URI("info:pronom/fmt/12"));
	outputFormatURIs.add(new URI("info:pronom/fmt/13"));

	// DXF URIs
	outputFormatURIs.add(new URI("info:pronom/fmt/63"));
	outputFormatURIs.add(new URI("info:pronom/fmt/64"));
	outputFormatURIs.add(new URI("info:pronom/fmt/65"));
	outputFormatURIs.add(new URI("info:pronom/fmt/66"));
	outputFormatURIs.add(new URI("info:pronom/fmt/67"));
	outputFormatURIs.add(new URI("info:pronom/fmt/68"));
	outputFormatURIs.add(new URI("info:pronom/fmt/69"));
	outputFormatURIs.add(new URI("info:pronom/fmt/70"));
	outputFormatURIs.add(new URI("info:pronom/fmt/71"));
	outputFormatURIs.add(new URI("info:pronom/fmt/72"));
	outputFormatURIs.add(new URI("info:pronom/fmt/73"));
	outputFormatURIs.add(new URI("info:pronom/fmt/74"));
	outputFormatURIs.add(new URI("info:pronom/fmt/75"));
	outputFormatURIs.add(new URI("info:pronom/fmt/76"));
	outputFormatURIs.add(new URI("info:pronom/fmt/77"));
	outputFormatURIs.add(new URI("info:pronom/fmt/78"));
	outputFormatURIs.add(new URI("info:pronom/fmt/79"));
	outputFormatURIs.add(new URI("info:pronom/fmt/80"));
	outputFormatURIs.add(new URI("info:pronom/fmt/81"));
	outputFormatURIs.add(new URI("info:pronom/fmt/82"));
	outputFormatURIs.add(new URI("info:pronom/fmt/83"));
	outputFormatURIs.add(new URI("info:pronom/fmt/84"));
	outputFormatURIs.add(new URI("info:pronom/fmt/85"));

	// PLT URI
	outputFormatURIs.add(new URI("info:pronom/x-fmt/83"));

	// HPGL URI
	outputFormatURIs.add(new URI("info:pronom/fmt/293"));

	// EPS URIs
	outputFormatURIs.add(new URI("info:pronom/fmt/122"));
	outputFormatURIs.add(new URI("info:pronom/fmt/123"));
	outputFormatURIs.add(new URI("info:pronom/fmt/124"));

	// TODO: EPSI URI is not provided by PRONOM. Add when possible.

	// SVG URIs
	outputFormatURIs.add(new URI("info:pronom/fmt/91"));
	outputFormatURIs.add(new URI("info:pronom/fmt/92"));

	// SVGZ URI
	outputFormatURIs.add(new URI("info:pronom/x-fmt/109"));

	// TODO: MP URI is not provided by PRONOM. Add when possible.

	// TODO: TEX URI is not provided by PRONOM. Add when possible.

	// WPG URI
	outputFormatURIs.add(new URI("info:pronom/x-fmt/395"));

	// TODO: FIG URI is not provided by PRONOM. Add when possible.

	// TODO: CODE URI is not provided by PRONOM. Add when possible.
    }

}
