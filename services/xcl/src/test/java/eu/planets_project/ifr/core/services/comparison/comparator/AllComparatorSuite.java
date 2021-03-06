package eu.planets_project.ifr.core.services.comparison.comparator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.planets_project.ifr.core.services.SampleXclUsageImageCore;
import eu.planets_project.ifr.core.services.SampleXclUsageImageMagic;
import eu.planets_project.ifr.core.services.SampleXclUsageTextPdfPdf;
import eu.planets_project.ifr.core.services.comparison.comparator.config.ComparatorConfigCreatorTests;
import eu.planets_project.ifr.core.services.comparison.comparator.config.ComparatorConfigParserTests;
import eu.planets_project.ifr.core.services.comparison.comparator.impl.ComparatorWrapperTests;
import eu.planets_project.ifr.core.services.comparison.comparator.impl.ResultPropertiesReaderTests;
import eu.planets_project.ifr.core.services.comparison.comparator.impl.XcdlComparePropertiesTests;
import eu.planets_project.ifr.core.services.comparison.comparator.impl.XcdlCompareTests;

/**
 * Suite to run all tests in the comparator component.
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( { ComparatorWrapperTests.class, XcdlCompareTests.class, ResultPropertiesReaderTests.class,
        ComparatorConfigCreatorTests.class, ComparatorConfigParserTests.class, XcdlComparePropertiesTests.class,
        /* Overall samples, but in the end Comparator: */
        SampleXclUsageImageCore.class, SampleXclUsageImageMagic.class, SampleXclUsageTextPdfPdf.class
        /* TODO: enable when tool issues are resolved */
        /* , SampleXclUsageTextDocxPdf.class */})
public class AllComparatorSuite {}
