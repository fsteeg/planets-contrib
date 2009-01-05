package eu.planets_project.services.migration.soxservices;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.services.utils.ProcessRunner;

/**
 * Common sox invocation class
 * 
 * @author : Thomas Kraemer thomas.kraemer@uni-koeln.de created : 14.07.2008
 * 
 */
public class SoxMigrations {

    /**
     * SOX as a string
     */
    public final String SOX = "sox";
    PlanetsLogger plogger = PlanetsLogger.getLogger(SoxMigrations.class);

    /**
     * the system temp directory
     */
    public static final String SYSTEM_TEMP = System.getProperty("java.io.tmpdir") + File.separator;
    /**
     * the SOX working directory
     */
    public static  String SoX_WORK_DIR = "SOX";
    /**
     * the SOX input dir
     */
    public static  String SoX_IN = "INPUT";
    /**
     * the SOX output dir
     */
    public static  String SoX_OUTPUT_DIR = "OUT";
    /**
     * SOX home dir
     */
    public static String SOX_HOME = null;
    
    /**
     * no arg default constructor, sets up the directories
     */
    public SoxMigrations() {
    	
    	SOX_HOME = System.getenv("SOX_HOME") + File.separator;
    	
    	if(SOX_HOME==null){
    		System.err.println("SOX_HOME is not set! Please create an system variable\n" +
    				"and point it to the SoX installation folder!");
    		plogger.error("SOX_HOME is not set! Please create an system variable\n" +
    				"and point it to the SoX installation folder!");
    	}
		
        plogger.info("Pointed to sox in: " + SOX_HOME);
        System.out.println("Pointed SoX_HOME to: " + SOX_HOME);
    }

	/**
	 * @param input
	 * @return the migrated OGG file
	 */
	public byte[] transformMp3ToOgg(byte[] input) {
        plogger.info("transformMp3ToOgg begin ");
        return genericTransformAudioSrcToAudioDest(input, ".mp3", ".ogg", null);
    }
    
	/**
	 * @param input
	 * @return the migrated AIFF file
	 */
    public byte[] transformWavToAiff(byte[] input) {
    	plogger.info("transformWavToAiff begin ");
    	return genericTransformAudioSrcToAudioDest(input, ".wav", ".aiff", null);
    }

	/**
	 * @param input
	 * @return the migrated WAV file
	 */
    public byte[] transformMp3ToWav(byte[] input) {
        plogger.info("transformMp3ToWav begin ");
        return genericTransformAudioSrcToAudioDest(input, ".mp3", ".wav", null);
    }

	/**
	 * @param input
	 * @return the migrated OGG file
	 */
    public byte[] transformWavToOgg(byte[] input) {
        plogger.info("transformWavToOgg begin ");
        return genericTransformAudioSrcToAudioDest(input, ".wav", ".ogg", null);
    }

	/**
	 * @param input
	 * @return the migrated FLAC file
	 */
    public byte[] transformWavToFlac(byte[] input) {
        plogger.info("transformWavToFlac begin ");
        return genericTransformAudioSrcToAudioDest(input, ".wav", ".flac", null);
    }

	/**
	 * @param input
	 * @return the migrated FLAC file
	 */
    public byte[] transformMp3ToFlac(byte[] input) {
        plogger.info("transformMp3ToFlac begin ");
        return genericTransformAudioSrcToAudioDest(input, ".mp3", ".flac", null);
    }

//    public DataHandler transformMp3ToOggDH(DataHandler input) {
//        log.info("transformMp3ToOggDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".mp3", ".ogg",
//                null);
//    }
//    
//    public DataHandler transformWavToAiffDH(DataHandler input) {
//    	log.info("transformWavToRAW begin ");
//    	return genericTransformAudioSrcToAudioDestDH(input, ".wav", ".aiff", null);
//    }
//
//    public DataHandler transformMp3ToWavDH(DataHandler input) {
//        log.info("transformMp3ToWavDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".mp3", ".wav",
//                null);
//    }
//
//    public DataHandler transformWavToOggDH(DataHandler input) {
//        log.info("transformWavToOggDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".wav", ".ogg",
//                null);
//    }
//
//    public DataHandler transformWavToFlacDH(DataHandler input) {
//        log.info("transformWavToFlacDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".wav", ".flac",
//                null);
//    }
//
//    public DataHandler transformMp3ToFlacDH(DataHandler input) {
//        log.info("transformMp3ToFlacDH begin ");
//        return genericTransformAudioSrcToAudioDestDH(input, ".mp3", ".flac",
//                null);
//    }

    /**
     * @param input
     * @param srcSuffix
     * @param destSuffix
     * @param soxCliParams
     * @return the migrated byte[]
     */
    public byte[] genericTransformAudioSrcToAudioDest(byte[] input,
            String srcSuffix, String destSuffix, ArrayList<String> soxCliParams) {
    	
        if (!srcSuffix.startsWith("."))
            srcSuffix = "." + srcSuffix;
        
        if (!destSuffix.startsWith("."))
            destSuffix = "." + destSuffix;
        
        plogger.info("genericTransformAudioSrcToAudioDest begin: Converting from "
                + srcSuffix + " to " + destSuffix);
        File workFolder = FileUtils.createWorkFolderInSysTemp(SoX_WORK_DIR);
        
        File outputFolder = FileUtils.createFolderInWorkFolder(workFolder, SoX_OUTPUT_DIR);
        
        String outputFilePath = outputFolder.getAbsolutePath() + File.separator + "SoX_OUTPUT_FILE" + destSuffix; 
        
        File inputFolder = FileUtils.createFolderInWorkFolder(workFolder, SoX_IN);
        
        String inputFilePath = inputFolder.getAbsolutePath() + File.separator + "SoX_INPUT_FILE" + srcSuffix;
        File inputFile = ByteArrayHelper.writeToDestFile(input, inputFilePath);
        
        
        try {
            List<String> commands = Arrays.asList(SOX_HOME + SOX, inputFile
                    .getAbsolutePath(), outputFilePath);
            if(soxCliParams!=null) {
            	commands.addAll(soxCliParams);
            }
            
            ProcessRunner pr = new ProcessRunner(commands);
            
            pr.setStartingDir(new File(SOX_HOME));
            
            plogger.info("Executing: " + commands);
            
            pr.run();

            plogger.info("SOX call output: " + pr.getProcessOutputAsString());
            plogger.error("SOX call error: " + pr.getProcessErrorAsString());
            
            plogger.debug("Executing: " + commands + " finished.");

        } catch (Exception ex) {
            plogger.error("SoX could not create the output file");
        }
        plogger.info("genericTransformAudioSrcToAudioDest end");
        File processOutputFile = new File(outputFilePath);
        byte[] outputFileData = null;
        
        if(processOutputFile.canRead()) {
        	outputFileData = ByteArrayHelper.read(new File(outputFilePath));
            plogger.info(outputFileData.length);
        }
        else {
        	outputFileData = null;
        	plogger.error("SoX didn't create an output file!");
        }
        
        FileUtils.deleteTempFiles(workFolder, plogger);
        
        return outputFileData;
    }

    
    
//    public DataHandler genericTransformAudioSrcToAudioDestDH(DataHandler input,
//            String srcSuffix, String destSuffix, ArrayList<String> soxCliParams) {
//        log
//                .info("genericTransformAudioSrcToAudioDestDH begin : Converting from "
//                        + srcSuffix + " to " + destSuffix);
//        File f = FileUtils.tempFile("tempout.audio", destSuffix);
//        byte[] raw = null;
//        try {
//            FileOutputStream fos = new FileOutputStream(f);
//            log.info(input.getContentType());
//            log.info(raw);
//            log.info(input.getContent().toString());
//            log.info(raw);
//            fos.write(raw);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        File inputFile = FileUtils.tempFile(raw, "tempin.audio", srcSuffix);
////        if (!f.canRead() || !inputFile.exists() || !f.exists() || !f.canWrite()
////                || !inputFile.canRead() || !inputFile.canWrite()) {
////            throw new IllegalStateException("Can't read from or write to: "
////                    + f.getAbsolutePath());
////        }
//        try {
//            List<String> commands = Arrays.asList(SOX_HOME + File.separator + SOX, inputFile
//                    .getAbsolutePath(), f.getAbsolutePath());
//            File home = new File(SOX_HOME);
//            if (soxCliParams != null) {
//                commands.addAll(soxCliParams);
//            }
//            ProcessRunner pr = new ProcessRunner(commands);
//            pr.setStartingDir(home);
//            log.info("Executing: " + commands);
//            pr.run();
//            log.info("SOX call output: " + pr.getProcessOutputAsString());
//            log.error("SOX call error: " + pr.getProcessErrorAsString());
//            log.debug("Executing: " + commands + " finished.");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            log.error("SoX could not create the open document file");
//        }
//        log.info("genericTransformAudioSrcToAudioDestDH end");
//        log.info(f.length());
//        return new DataHandler(new ByteArrayDataSource(ByteArrayHelper.read(f),
//                "application/octet-stream"));
//    }
}
