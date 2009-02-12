package eu.planets_project.services.migration.imagemagick;

import org.junit.BeforeClass;

import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migration.imagemagick.ImageMagickMigrate;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * Image magick stand alone tests
 *
 */
public class ImageMagickMigrateStandAloneTest extends
		ImageMagickMigrateLocalTest {
	

	/**
	 * set up tests by creating a service and the compression type array
	 */
	@BeforeClass
    public static void setup() {
		System.out.println("Running ImageMagickMigrate STANDALONE tests...");
		System.out.println("*************************************************");
		
		TEST_OUT = ImageMagickMigrateTestHelper.STANDALONE_TEST_OUT;
		
    	System.setProperty("pserv.test.context", "Standalone");

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