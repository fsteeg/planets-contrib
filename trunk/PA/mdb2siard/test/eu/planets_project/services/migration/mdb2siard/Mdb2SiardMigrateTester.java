/*== Mdb2SiardMigrate.java ===========================================
JUnit test of class Mdb2SiardMigrate.
Version     : $Id$
Application : PLANETS migration services
Description : Mdb2SiardMigrate implements the PLANETS 
              (v. www.planets-project.eu) Integration Framework's (IF)
              Migrate interface.
------------------------------------------------------------------------
Copyright  : Swiss Federal Archives, Berne, Switzerland 
             (pending PLANETS copyright agreements)
Created    : 05.12.2008, Hartwig Thomas, Enter AG, Zurich
Sponsor    : Swiss Federal Archives, Berne, Switzerland
======================================================================*/

package eu.planets_project.services.migration.mdb2siard;

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.test.ServiceCreator;

public class Mdb2SiardMigrateTester
{
	// private static String sINPUT_FILE = "PA/mdb2siard/test/testfiles/testin.mdb";
	private static String sINPUT_FILE = "test/testfiles/testin.mdb";
  // private static String sOUTPUT_FILE = "PA/mdb2siard/test/testfiles/testout.siard";
  private static String sOUTPUT_FILE = "test/testfiles/testout.siard";
  /* The location of this service when deployed. */
  private static String sWSDL_LOC = "/pserv-pa-mdb2siard/Mbd2SiardMigrate?wsdl";

  /* A holder for the object to be tested */
  Migrate dom = null;

	/*--------------------------------------------------------------------*/
	@Before
	public void setUp() throws Exception
	{
    dom = ServiceCreator.createTestService(Migrate.QNAME, Mdb2SiardMigrate.class, sWSDL_LOC );
	} /* setUp */

	/*--------------------------------------------------------------------*/
	@After
	public void tearDown() throws Exception
	{
	} /* tearDown */

	/*--------------------------------------------------------------------*/
	@Test
	public void testDescribe()
	{
    ServiceDescription desc = dom.describe();
    System.out.println("Received service description: " + desc);
    assertTrue("The ServiceDescription should not be NULL.", desc != null );
	} /* testDescribe */

	/*--------------------------------------------------------------------*/
	@Test
	public void testWriteByteArrayToTmpFile()
	{
    File fileOutput = new File(sOUTPUT_FILE);
    if (fileOutput.exists())
    	fileOutput.delete();
    try
    {
      byte[] buffer = new byte[] { 0, 1, 2, 3};
      Mdb2SiardMigrate.writeByteArrayToTmpFile(buffer,fileOutput);
  		assertTrue(fileOutput.exists());
  		assertTrue(fileOutput.length() == buffer.length);
    }
    catch(IOException ie)
    {
    	fail(ie.getClass().getName()+": "+ie.getMessage());
    }
	} /* testWriteByteArrayToTmpFile */

	/*--------------------------------------------------------------------*/
	@Test
	public void testReadByteArrayFromTmpFile()
	{
    File fileInput = new File(sINPUT_FILE);
    try
    {
      byte[] buffer = Mdb2SiardMigrate.readByteArrayFromTmpFile(fileInput);
  		assertTrue(buffer.length == fileInput.length());
  		assertTrue(buffer[0] == 0);
  		assertTrue(buffer[1] == 1);
  		assertTrue(buffer[2] == 0);
  		assertTrue(buffer[3] == 0);
    }
    catch(IOException ie)
    {
    	fail(ie.getClass().getName()+": "+ie.getMessage());
    }
	} /* testReadByteArrayFromTmpFile */

	/*--------------------------------------------------------------------*/
	@Test
	public void testMigrateFileFileServiceReport()
	{
    File fileOutput = new File(sOUTPUT_FILE);
    if (fileOutput.exists())
      fileOutput.delete();
    File fileInput = new File(sINPUT_FILE);
		ServiceReport sr = new ServiceReport();
		sr = Mdb2SiardMigrate.migrate(fileInput, fileOutput, sr);
		assertTrue((sr.getErrorState() == ServiceReport.ERROR) || fileOutput.exists());
	} /* testMigrateFileFileServiceReport */

	/*--------------------------------------------------------------------*/
	@Test
	public void testMigrateDigitalObjectURIURIParameters()
	{
    try
    {
      /*
			 * To test usability of the digital object instance in web
			 * services, we simply pass one into the service and expect one
			 * back:
			 */
      File fileOutput = new File(sOUTPUT_FILE);
      if (fileOutput.exists())
        fileOutput.delete();
      File fileInput = new File(sINPUT_FILE);
      DigitalObject doInput = new DigitalObject.Builder(
      		Content.byValue(
      				Mdb2SiardMigrate.readByteArrayFromTmpFile(fileInput))).build();
      MigrateResult mr = dom.migrate(doInput, null, null, null);
      DigitalObject doOutput = mr.getDigitalObject();
      assertTrue("Resulting digital object is null.", doOutput != null);
      Mdb2SiardMigrate.writeByteArrayToTmpFile(doOutput.getContent().getValue(), fileOutput);
  		assertTrue((mr.getReport().getErrorState() == ServiceReport.ERROR) || fileOutput.exists());
    }
    catch (Exception e) 
    {
      fail(e.getClass().getName()+": "+e.getMessage());
	  }
	} /* testMigrateDigitalObjectURIURIParameters */

} /* Mdb2SiardMigrateTester */
