/**
 * 
 */
package eu.planets_project.services.migration.dioscuri;


import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * @author melmsp
 *
 */
public class DioscuriArjMigrationTest {
	
	public static String wsdlLoc = "/pserv-pa-dioscuri/DioscuriArjMigration?wsdl";
	
	public static Migrate DIOSCURI_MIGRATE = null;
	
	public static File DIOSCURI_TEST_OUT = null;
	
	public static File ARJ_TEST_FILE = new File("tests/test-files/archives/TEST.ARJ");
	
	public static FormatRegistry format = FormatRegistryFactory.getFormatRegistry();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
//		System.setProperty("pserv.test.context", "server");
//        System.setProperty("pserv.test.host", "localhost");
//        System.setProperty("pserv.test.port", "8080");
		DIOSCURI_MIGRATE = ServiceCreator.createTestService(Migrate.QNAME, DioscuriArjMigration.class, wsdlLoc);
		DIOSCURI_TEST_OUT = new File(System.getProperty("java.io.tmpdir"), "DIOSCURI_ARJ_TEST_OUT");
		FileUtils.forceMkdir(DIOSCURI_TEST_OUT);
	}
	
	@Test
	public void testDescribe() {
		ServiceDescription sd = DIOSCURI_MIGRATE.describe();
		assertTrue("The ServiceDescription should not be NULL.", sd != null );
    	System.out.println("test: describe()");
    	System.out.println("--------------------------------------------------------------------");
    	System.out.println();
    	System.out.println("Received ServiceDescription from: " + DIOSCURI_MIGRATE.getClass().getName());
    	System.out.println(sd.toXmlFormatted());
    	System.out.println("--------------------------------------------------------------------");
	}
	
	@Test
	public void testMigrate() {
		DigitalObject inputDigOb = new DigitalObject.Builder(Content.byReference(ARJ_TEST_FILE)).title(ARJ_TEST_FILE.getName()).format(format.createExtensionUri(FilenameUtils.getExtension(ARJ_TEST_FILE.getName()))).build();
		MigrateResult result = DIOSCURI_MIGRATE.migrate(inputDigOb, format.createExtensionUri(FilenameUtils.getExtension(ARJ_TEST_FILE.getName())), format.createExtensionUri("EXE"), null);
		
		assertTrue("MigrateResult should not be NULL", result!=null);
        System.out.println("DEBUG ServiceReport: " + result.getReport() );
		assertTrue("ServiceReport should be SUCCESS", result.getReport().getStatus()==Status.SUCCESS);
		
		System.out.println(result.getReport());
		
		File resultFile = new File(DIOSCURI_TEST_OUT, result.getDigitalObject().getTitle());
		DigitalObjectUtils.toFile(result.getDigitalObject(), resultFile);
//		String content = FileUtils.readTxtFileIntoString(resultFile);
//		System.out.println("Archive Content:\r\n");
//		System.out.println(content);
		System.out.println("Please find the converted file here: " + resultFile.getAbsolutePath());
	}
}
