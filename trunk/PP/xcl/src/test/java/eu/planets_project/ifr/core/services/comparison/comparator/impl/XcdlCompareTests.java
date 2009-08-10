package eu.planets_project.ifr.core.services.comparison.comparator.impl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import eu.planets_project.services.compare.Compare;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * Local and client tests of the comparator XCDL comparison service.
 * @author Fabian Steeg
 */
public final class XcdlCompareTests {

    @Test
    public void testDescribe() {
        Compare c = ServiceCreator.createTestService(XcdlCompare.QNAME,
                XcdlCompare.class, WSDL);
        ServiceDescription sd = c.describe();
        assertTrue("The ServiceDescription should not be NULL.", sd != null);
        System.out.println("test: describe()");
        System.out
                .println("--------------------------------------------------------------------");
        System.out.println();
        System.out.println("Received ServiceDescription from: "
                + c.getClass().getName());
        System.out.println(sd.toXmlFormatted());
        System.out
                .println("--------------------------------------------------------------------");
    }

    private static final String WSDL = "/pserv-xcl/XcdlCompare?wsdl";

    /**
     * Tests PP comparator comparison using the XCDL comparator.
     */
    @Test
    public void testService() {
        byte[] data1 = FileUtils.readFileIntoByteArray(new File(
                ComparatorWrapperTests.XCDL1));
        byte[] data2 = FileUtils.readFileIntoByteArray(new File(
                ComparatorWrapperTests.XCDL2));
        byte[] configData = FileUtils.readFileIntoByteArray(new File(
                ComparatorWrapperTests.COCO_IMAGE));
        testServices(data1, data2, configData);
    }

    /**
     * Tests the services that use the actual value strings.
     * @param data1 The XCDL1 data
     * @param data2 The XCDL2 data
     * @param configData The config data
     */
    protected void testServices(final byte[] data1, final byte[] data2,
            final byte[] configData) {
        Compare c = ServiceCreator.createTestService(XcdlCompare.QNAME,
                XcdlCompare.class, WSDL);
        DigitalObject first = new DigitalObject.Builder(Content.byValue(data1))
                .build();
        DigitalObject second = new DigitalObject.Builder(Content.byValue(data2))
                .build();
        DigitalObject configFile = new DigitalObject.Builder(Content
                .byValue(configData)).build();
        List<Property> properties = c.compare(first, second,
                c.convert(configFile)).getProperties();
        ComparatorWrapperTests.check(properties);
    }
}
