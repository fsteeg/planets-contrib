package eu.planets_project.services.migration.imagemagick;

import org.junit.BeforeClass;

import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migration.imagemagick.ImageMagickMigrate;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * Image magick migration server tests
 *
 */
public class ImageMagickMigrateServerTest extends
		ImageMagickMigrateLocalTest {
	
	/**
	 * set up the server tests by creating a test service and the array of compression types
	 */
	@BeforeClass
    public static void setup() {
		System.out.println("Running ImageMagickMigrate SERVER tests...");
		System.out.println("*********************************************");
		
		TEST_OUT = ImageMagickMigrateTestHelper.SERVER_TEST_OUT;
		
    	System.setProperty("pserv.test.context", "server");
        System.setProperty("pserv.test.host", "localhost");
        System.setProperty("pserv.test.port", "8080");
        
    	imageMagick = ServiceCreator.createTestService(Migrate.QNAME, ImageMagickMigrate.class, wsdlLocation);
    	
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
    }
	
}