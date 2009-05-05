package eu.planets_project.services.shotgun;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.ImmutableContent;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.modify.ModifyResult;
import eu.planets_project.services.shotgun.FileShotgun.Action;
import eu.planets_project.services.shotgun.FileShotgun.Key;
import eu.planets_project.services.utils.FileUtils;

/**
 * Tests and sample usage for the ShotgunModify service.
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */
public class ShotgunModifyTests {
    private static final File INPUT_FILE = new File(
            "tests/test-files/images/bitmap/test_tiff/2274192346_4a0a03c5d6.tif");
    private static final DigitalObject INPUT_DIGITAL_OBJECT = new DigitalObject.Builder(
            ImmutableContent.byReference(INPUT_FILE)).build();
    private static final byte[] INPUT_BYTES = FileUtils
            .readFileIntoByteArray(INPUT_FILE);
    private static final byte[] WRITE_RESULT = shotgun(Action.CORRUPT);
    private static final byte[] DELETE_RESULT = shotgun(Action.DROP);

    /**
     * Sample usage for the ShotgunModify service.
     * @param shotgunAction The action to apply to the input format
     * @return The ModifyResult object (see below on how to use it)
     */
    private static ModifyResult sampleUsage(final Action shotgunAction) {
        /* Configure the shotgun: */
        Parameter count = new Parameter(Key.SEQ_COUNT.toString(), "5");
        Parameter length = new Parameter(Key.SEQ_LENGTH.toString(), "15");
        Parameter action = new Parameter(Key.ACTION.toString(), shotgunAction
                .toString());
        /* Instantiate the shotgun and modify the file: */
        ModifyResult modify = new ShotgunModify().modify(INPUT_DIGITAL_OBJECT,
                null, null, Arrays.asList(count, length, action));
        return modify;
    }

    /**
     * @param shotgunAction The action to apply to the input format
     * @return The bytes of the resulting file; used below in the tests
     */
    private static byte[] shotgun(final Action shotgunAction) {
        Assert.assertTrue(INPUT_FILE != null && INPUT_FILE.exists());
        ModifyResult modify = sampleUsage(shotgunAction);
        /* Check that we have a result: */
        Assert.assertNotNull("Result object is null", modify);
        DigitalObject outputDigitalObject = modify.getDigitalObject();
        Assert.assertNotNull("Result digital object is null", modify);
        File resultFile = FileUtils.writeByteArrayToTempFile(FileUtils
                .writeInputStreamToBinary(outputDigitalObject.getContent()
                        .read()));
        Assert.assertNotNull("Result file is null", resultFile);
        Assert.assertTrue("Result file does not exist", resultFile.exists());
        /* Return the bytes of the resulting file (used in the tests below) */
        return FileUtils.readFileIntoByteArray(resultFile);
    }

    @Test
    public void testWriteActionChangedBytes() {
        assertArraysNotEqual(INPUT_BYTES, WRITE_RESULT);
    }

    @Test
    public void testWriteActionDidNotChangeLength() {
        Assert.assertEquals(INPUT_BYTES.length, WRITE_RESULT.length);
    }

    @Test
    public void testDeleteActionChangedBytes() {
        assertArraysNotEqual(INPUT_BYTES, DELETE_RESULT);
    }

    @Test
    public void testDeleteActionChangedLength() {
        Assert.assertNotSame(INPUT_BYTES.length, DELETE_RESULT.length);
    }

    private void assertArraysNotEqual(byte[] one, byte[] two) {
        if (one.length != two.length) {
            return;
        }
        for (int i = 0; i < one.length; i++) {
            if (one[i] != two[i]) {
                return;
            }
        }
        Assert.fail("Byte arrays are equal");
    }
}