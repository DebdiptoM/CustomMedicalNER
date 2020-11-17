package edu.geisinger.ptnotes.ctakes.test;

import edu.geisinger.ptnotes.ctakes.CASAEDescLoader;
import edu.geisinger.ptnotes.ctakes.note.LungNoduleExtractor;
import edu.geisinger.ptnotes.ctakes.note.LungNoduleNLPRecord;
import edu.geisinger.ptnotes.ctakes.note.NoteCUIDetails;
import edu.geisinger.ptnotes.ctakes.note.NoteNoduleDetails;
import edu.geisinger.ptnotes.ctakes.util.CASUtil;
import edu.geisinger.ptnotes.ctakes.util.MeasurementUnit;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.impl.AnalysisEngineImplBase;
import org.apache.uima.util.XMLInputSource;

import java.io.File;
import java.util.List;
import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 

/**
 * Created by rattam on 2/10/2017.
 */
public class TestLungNoduleResults {
    private static boolean initted = false;

    private static AnalysisEngineImplBase aeBefDict;
    private static AnalysisEngineImplBase aeDict;
    private static AnalysisEngineImplBase aeSimpleLung;
    private static AnalysisEngineImplBase aeAfterDict;
    private static CASAEDescLoader casaeDescLoader;

    public synchronized static String getLNResults(String fullNote) throws Exception {
        if(!initted) {
            aeBefDict = getAE("ctakes-clinical-pipeline/desc/analysis_engine/FAST_UMLS_FULLDICT_Before_Dict.xml");
            aeDict = getAE("ctakes-clinical-pipeline/desc/analysis_engine/FAST_UMLS_FULLDICT_Dict_nodule_only.xml");
            aeSimpleLung = getAE("ctakes-clinical-pipeline/desc/analysis_engine/FAST_UMLS_FULLDICT_SimpleLung_only.xml");
            aeAfterDict = getAE("ctakes-clinical-pipeline/desc/analysis_engine/FAST_UMLS_FULLDICT_SimpleLung_After_Dict.xml");
            String aeLungPolDescXml =
                    "ctakes-clinical-pipeline/desc/analysis_engine/FAST_UMLS_FULLDICT_SimpleLung_After_Dict.xml";

            casaeDescLoader = new CASAEDescLoader(aeLungPolDescXml);
            initted = true;
        }

        String nlpBefDictCasXml = CASUtil.runAEOnNoteToCASXml(fullNote, aeBefDict);

        String nlpDictCasXml = CASUtil.runAEOnCASXml(nlpBefDictCasXml, aeDict);

        String nlpSimpleLungCasXml = CASUtil.runAEOnCASXml(nlpDictCasXml, aeSimpleLung);

        String nlpAfterDictCasXml = CASUtil.runAEOnCASXml(nlpSimpleLungCasXml, aeAfterDict);


        StringBuilder output = new StringBuilder();

        LungNoduleNLPRecord lungNoduleNLPRecords = LungNoduleExtractor.getLungNoduleNLPRecords(casaeDescLoader, "noteId", "noteType", new String(CASAEDescLoader.compress(nlpAfterDictCasXml)));

        List<NoteCUIDetails> cuiDetailsList = lungNoduleNLPRecords.getCuiDetailsList();

        List<NoteNoduleDetails> noduleDetailsList = lungNoduleNLPRecords.getNoduleDetailsList();

        output.append(lungNoduleNLPRecords.getLungRadsScore());
        output.append('\n');
        for(NoteCUIDetails cuiDetails : cuiDetailsList) {
            String details = cuiDetails.getCode() + "|" + cuiDetails.getPolarity() + "|" + cuiDetails.getBegin() + "|" + cuiDetails.getEnd();
            output.append(details);
            output.append('\n');
        }

        for (NoteNoduleDetails noduleDetails : noduleDetailsList) {
            String wholeRecord = getNoduleRecord(noduleDetails);
            output.append(wholeRecord);
            output.append('\n');
        }



//        List<String> lungNoduleRecordsList = LungNoduleExtractor.extractLungNoduleNLPRecord(casaeDescLoader, "noteId", "noteType",
//                new String(CASAEDescLoader.compress(nlpAfterDictCasXml)));
//
//        for(String lungNoduleRecord: lungNoduleRecordsList) {
//            System.out.println(lungNoduleRecord);
//        }

//        String aeBefDictDescXml =
//                "ctakes-clinical-pipeline/desc/analysis_engine/FAST_UMLS_FULLDICT_Before_Dict.xml";
//        String casBefDictXml = "simplelung_note1_bef_dict.xml";
//
//        new TestAEDescFlow().runCTakes(NOTE_FILE, aeBefDictDescXml, casBefDictXml);
//
//        String aeDictDescXml =
//                "ctakes-clinical-pipeline/desc/analysis_engine/FAST_UMLS_FULLDICT_Dict_nodule_only.xml";
//
//        new TestAEDescFlow().runCTakesOnCas(casBefDictXml, aeDictDescXml, XML_CAS_DICT_FILE);

        return output.toString();
    }
static String DELIM_BAR = "|";
    private static String getNoduleRecord(NoteNoduleDetails noduleDetails) {
        StringBuilder record = new StringBuilder();
        record.append(noduleDetails.getSizeLength());
        record.append(DELIM_BAR);
        record.append(noduleDetails.getSizeWidth());
        record.append(DELIM_BAR);
        record.append(noduleDetails.getUnits() == null ? MeasurementUnit.MM : noduleDetails.getUnits());
        record.append(DELIM_BAR);
        record.append(noduleDetails.getCalculatedSize());
        record.append(DELIM_BAR);
        record.append(noduleDetails.getDescription());
        record.append(DELIM_BAR);
        record.append(noduleDetails.getLocation());
        record.append(DELIM_BAR);
//        record.append(noduleDetails.getReadingDate());
//        record.append(DELIM_BAR);
        record.append(noduleDetails.isPastReading());
        //record.append(DELIM_BAR);
//        record.append(noduleDetails.getScore());
//        record.append(DELIM_BAR);
        record.append(DELIM_BAR);
        record.append(noduleDetails.getSizeBeginIndex());
        record.append(DELIM_BAR);
        record.append(noduleDetails.getSizeEndIndex());
        record.append(DELIM_BAR);
        record.append(noduleDetails.getScore());
        return record.toString();
    }

    private synchronized static AnalysisEngineImplBase getAE(String aeDescXmlFileName) throws Exception {
        String aeNlpDictDescXmlFile = new TestLungNoduleResults().getClass().getClassLoader().getResource(aeDescXmlFileName).getFile();
        AnalysisEngineDescription analysisEngineDescription = UIMAFramework.getXMLParser().parseAnalysisEngineDescription
                (new XMLInputSource(new File(aeNlpDictDescXmlFile)));
        AnalysisEngineImplBase analysisEngine = (AnalysisEngineImplBase) UIMAFramework.produceAnalysisEngine(analysisEngineDescription);

        return analysisEngine;
    }

    public static void main(String[] args) throws Exception {
        String output = getLNResults(TestUtils.readTextFile("notes_lung"));
        System.out.println("output" + output);

        // creating JSONObject 
        JSONObject jo = new JSONObject();
        PrintWriter pw = new PrintWriter("../data/archive/ner_260.json"); 
        pw.write(jo.toJSONString());  

    }
}
