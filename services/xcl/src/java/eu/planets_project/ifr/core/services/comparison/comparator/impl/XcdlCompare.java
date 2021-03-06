package eu.planets_project.ifr.core.services.comparison.comparator.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.io.FileUtils;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.ifr.core.services.characterisation.extractor.impl.CoreExtractor;
import eu.planets_project.ifr.core.services.characterisation.extractor.xcdl.XcdlParser;
import eu.planets_project.ifr.core.services.comparison.comparator.config.ComparatorConfigCreator;
import eu.planets_project.ifr.core.services.comparison.comparator.config.ComparatorConfigParser;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.characterise.CharacteriseResult;
import eu.planets_project.services.compare.Compare;
import eu.planets_project.services.compare.CompareResult;
import eu.planets_project.services.compare.PropertyComparison;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.utils.DigitalObjectUtils;
import eu.planets_project.services.utils.ServiceUtils;

/**
 * XCL Comparator service. Compares image, text or XCDL files wrapped as digital objects.
 * @see {@link eu.planets_project.ifr.core.services.AbstractSampleXclUsage}
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */
@WebService( name = XcdlCompare.NAME, serviceName = Compare.NAME, targetNamespace = PlanetsServices.NS, endpointInterface = "eu.planets_project.services.compare.Compare" )
@Stateless
@MTOM
@StreamingAttachment( parseEagerly=true, memoryThreshold=ServiceUtils.JAXWS_SIZE_THRESHOLD )
public final class XcdlCompare implements Compare {
    private static Logger log = Logger.getLogger(XcdlCompare.class.getName());
    
    /***/
    static final String NAME = "XcdlCompare";

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.compare.Compare#compare(eu.planets_project.services.datatypes.DigitalObject[],
     *      eu.planets_project.services.datatypes.DigitalObject)
     */
    public CompareResult compare(final DigitalObject first, final DigitalObject second,
            final List<Parameter> config) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Digital objects to compare must not be null");
        }
        String pcr = null;
        if( config != null && config.size() > 0 ) {
        	pcr = new ComparatorConfigCreator(config).getComparatorConfigXml();
        }
        // Set up the default config:
        /*
        if( pcr == null || "".equals(pcr.trim())) {
            pcr = ComparatorWrapper.read(ComparatorWrapper.DEFAULT_CONFIG);
        }
        */
        String xcdl1 = xcdlFor(first);
        //log.info("Got XCDL1: "+xcdl1);
        String xcdl2 = xcdlFor(second);
        //log.info("Got XCDL2: "+xcdl2);
        String result = ComparatorWrapper.compare(xcdl1, Arrays.asList(xcdl2), pcr);
        log.info("Got Result: "+result);
        // Build the comparison, using properties from extraction and comparison. 
        List<List<PropertyComparison>> props = propertiesFrom(xcdl1, xcdl2, result);
        // Create the result object from the properties.
        return compareResult(props);
    }

    private String xcdlFor(DigitalObject object) {
        // Try using the extractor to create an XCDL for the input file:
        File xcdl = new CoreExtractor(getClass().getName())
                .extractXCDL(object, null, null, null);
        // Return either the extracted XCDL (if it exists) or assume the file is an XCDL:
        String xcdlString = null;
        try {
        	xcdlString = xcdl != null && xcdl.exists() ? read(new DigitalObject.Builder(Content.byReference(xcdl)).build()) : read(object);
        } catch ( IllegalArgumentException e ) {
        	log.severe("ERROR when reading XCDL file. "+e);
        	xcdlString = "";
        }
        return xcdlString;
    }

    /**
     * @param props The 1-level nested result properties
     * @return A compare result object with either top-level properties only or without top-level
     *         properties but embedded results
     */
    static CompareResult compareResult(final List<List<PropertyComparison>> props) {
        if (props.size() == 1) {
            return new CompareResult(props.get(0), new ServiceReport(Type.INFO, Status.SUCCESS,
                    "Top-level comparison result without embedded results"));
        } else {
            List<CompareResult> embedded = new ArrayList<CompareResult>();
            for (List<PropertyComparison> list : props) {
                embedded.add(new CompareResult(list, new ServiceReport(Type.INFO, Status.SUCCESS,
                        "Embedded comparison result")));
            }
            return new CompareResult(new ArrayList<PropertyComparison>(), new ServiceReport(Type.INFO,
                    Status.SUCCESS, "Top-level comparison result with embedded results"), embedded);
        }
    }

    /**
     * @param result The comparator result
     * @return The properties found in the result XML
     */
    private List<List<PropertyComparison>> propertiesFrom(final String xcdl1, final String xcdl2, final String result) {
        File file = null;
        try {
            file = File.createTempFile("xcl", null);
            FileUtils.writeStringToFile(file, result);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        List<List<PropertyComparison>> props = null;
        try {
        	props = new ResultPropertiesReader(file).getProperties();
        } catch( IllegalArgumentException e ) {
        	log.severe("Could not parse properties from string "+result+"\n "+e);
        	props = new ArrayList<List<PropertyComparison>>();
        }
        // Also grab properties from the XCDL files and merge in with the results:
        CharacteriseResult c1 = new XcdlParser(new StringReader(xcdl1)).getCharacteriseResult();
        CharacteriseResult c2 = new XcdlParser(new StringReader(xcdl2)).getCharacteriseResult();
        if (props.size() >= 1) {
            patchInProps(props.get(0), c1.getProperties(), c2.getProperties() );
        }
        if( props.size() > 1 ) {
            for( int i = 1; i < props.size(); i++ ) {
                patchInProps(props.get(i), c1.getResults().get(i-1).getProperties(),
                        c2.getResults().get(i-1).getProperties());
            }
        }
        return props;
    }

    /**
     * @param list
     * @param properties
     * @param properties2
     */
    private void patchInProps(List<PropertyComparison> pcs,
            List<Property> props1, List<Property> props2) {
        for( PropertyComparison pc : pcs ) {
            for( Property p : props1 ) {
                if( pc.getComparison().getUri().equals( p.getUri() )) {
                    pc.getFirstProperties().add(p);
                }
            }
            for( Property p : props2 ) {
                if( pc.getComparison().getUri().equals( p.getUri() )) {
                    pc.getSecondProperties().add(p);
                }
            }
        }
        
    }

    /**
     * @param digitalObject The digital objects
     * @return A string representing the content of the digital objects
     */
    private String read(final DigitalObject digitalObject) {
        if (digitalObject == null) {
            throw new IllegalArgumentException("Digital object is null!");
        }
        InputStream stream = digitalObject.getContent().getInputStream();
        String xcdl = null;
        try {
            xcdl = FileUtils.readFileToString(DigitalObjectUtils.toFile(digitalObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!xcdl.toLowerCase().contains("<xcdl")) {
            throw new IllegalArgumentException("Digital object given is not XCDL: " + xcdl.substring(0,100));
        }
        return stream == null ? null : xcdl;
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.compare.Compare#describe()
     */
    public ServiceDescription describe() {
        /* TODO: Set input formats to XCDL PUID when there is one. */
        return new ServiceDescription.Builder(NAME, Compare.class.getCanonicalName())
                .classname(this.getClass().getCanonicalName())
                .author("Fabian Steeg, Andrew Jackson")
                .furtherInfo(URI.create("http://planetarium.hki.uni-koeln.de/"))
                .version("0.4")
                .description(
                        "XCDL Comparison Service, which compares two Xcdl files generated by the Extractor tool."
                                + "This services is a wrapper for the Comparator command line tool developed at the UzK."
                                + "The Comparator allows to check how much information has been lost during a migration process."
                                + "To use the Comparator with a list of properties instead of .xcdl files, please use the XcdlCompareProperties service!")
                .logo(URI.create("http://www.planets-project.eu/graphics/Planets_Logo.png"))
                .serviceProvider("The Planets Consortium").inputFormats(
                        ComparatorWrapper.getSupportedInputFormats().toArray(new URI[] {})).build();
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.compare.Compare#convert(eu.planets_project.services.datatypes.DigitalObject)
     */
    public List<Parameter> convert(final DigitalObject configFile) {
        InputStream inputStream = configFile.getContent().getInputStream();
        return new ComparatorConfigParser(inputStream).getProperties();
    }
}
