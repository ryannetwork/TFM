package es.rcs.tfm.srv.services;

import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.repository.TrainRepository;
import es.rcs.tfm.srv.setup.BiocXmlProcessor;
import es.rcs.tfm.srv.setup.Conll2003Writer;
import es.rcs.tfm.srv.setup.PubtatorTxtProcessor;
import es.rcs.tfm.xml.XmlNames;

@Service(value = SrvNames.TRAINING_SRVC)
@DependsOn(value = {
		SrvNames.SPARK_SESSION_TRAIN,
		XmlNames.BIOC_CONTEXT})
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class TrainService {

	private static final Logger LOG = LoggerFactory.getLogger(TrainService.class);

	private @Value("${tfm.training.tmvar.bronco.texts}")	String TRAIN_BRONCO_TEXT =		"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/bronco/BRONCO-A_Abstractss.txt";
	private @Value("${tfm.training.tmvar.bronco.answers}")	String TRAIN_BRONCO_ANSWERS =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/bronco/BRONCO-A_Answers.txt";

	private @Value("${tfm.training.tmvar.train.texts}")		String TRAIN_TMVARS_TEXT =		"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/bronco/corpus[tmVar]_abstracts.txt";
	private @Value("${tfm.training.tmvar.train.answers}")	String TRAIN_TMVARS_ANSWERS =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/bronco/corpus[tmVar]_answers.txt";

	private @Value("${tfm.training.tmvar.train.pubtator}")	String TRAIN_TMVARS_TXT_TRAIN =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/train.PubTator";
	private @Value("${tfm.training.tmvar.test.pubtator}")	String TRAIN_TMVARS_TXT_TEST =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/test.PubTator";

	private @Value("${tfm.training.tmvar.train.bioc}")		String TRAIN_TMVARS_XML_TRAIN =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/train.BioC.txt";
	private @Value("${tfm.training.tmvar.test.bioc}")		String TRAIN_TMVARS_XML_TEST =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/test.Bioc.txt";
			
	private @Value("${tfm.training.ner.directory}")			String TRAINING_NER_DIRECTORY =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner";

	private @Value("${tfm.model.bert_uncased.directory}")	String BERT_UNCASED_MODEL =		"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert_uncased_en_2.0.2_2.4_1556651478920";
	private @Value("${tfm.model.bert_ner.directory}")		String BERT_NER_MODEL =		"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner_dl_bert_en_2.0.2_2.4_1558809068913";
	private @Value("${tfm.model.tfm.directory}")			String TFM_NER_MODEL =			"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tfm_1_.0.0";
	
	
	private static final String TMVAR_TXT_TRAIN =	"tmvar.txt.train" + Conll2003Writer.CONLL_EXT;
	private static final String TMVAR_TXT_TEST =	"tmvar.txt.test" + Conll2003Writer.CONLL_EXT;
	private static final String TMVAR_BIOC_TRAIN =	"tmvar.bioc.train" + Conll2003Writer.CONLL_EXT;
	private static final String TMVAR_BIOC_TEST =	"tmvar.bioc.test" + Conll2003Writer.CONLL_EXT;

	public void trainModel(SparkSession spark) {
		
		try {
			TrainRepository.trainFromConll(
					spark, 
					TMVAR_TXT_TRAIN, 
					TMVAR_TXT_TEST, 
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, "tmvar_pubtator.csv"), 
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, "pipeline_pubtator"), 
					BERT_UNCASED_MODEL, 
					TFM_NER_MODEL);
			LOG.info("TRAIN SERVICE: tmVar pubtator fail OK");
		} catch (Exception ex) {
			LOG.warn("TRAIN SERVICE: tmVar pubtator fail ex:" + ex.toString());
		}
		
		try {
			TrainRepository.trainFromConll(
					spark, 
					TMVAR_BIOC_TRAIN, 
					TMVAR_BIOC_TEST, 
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, "tmvar_bioc.csv"), 
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, "pipeline_bioc"), 
					BERT_UNCASED_MODEL, 
					TFM_NER_MODEL);
			LOG.info("TRAIN SERVICE: tmVar bioc fail OK");
		} catch (Exception ex) {
			LOG.warn("TRAIN SERVICE: tmVar bioc fail ex:" + ex.toString());
		}
		
	}
	
	public void prepareDataForTraining(SparkSession spark) {

		try {
			TrainRepository.getConllFrom(
					spark, 
					new PubtatorTxtProcessor(Paths.get(TRAIN_TMVARS_TXT_TRAIN)), 
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, "pipeline_pubtator_train"), 
					BERT_UNCASED_MODEL, 
					BERT_NER_MODEL,
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, TMVAR_TXT_TRAIN));
			LOG.info("PREPARE DATA SERVICE: tmVar train pubtator OK");
		} catch (Exception ex) {
			LOG.warn("PREPARE DATA SERVICE: tmVar train pubtator fail ex:" + ex.toString());
		}
		
		try {
			TrainRepository.getConllFrom(
					spark, 
					new PubtatorTxtProcessor(Paths.get(TRAIN_TMVARS_TXT_TEST)), 
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, "pipeline_pubtator_test"), 
					BERT_UNCASED_MODEL, 
					BERT_NER_MODEL,
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, TMVAR_TXT_TEST));
			LOG.info("PREPARE DATA SERVICE: tmVar test pubtator OK");
		} catch (Exception ex) {
			LOG.warn("PREPARE DATA SERVICE: tmVar test pubtator fail ex:" + ex.toString());
		}

		try {
			TrainRepository.getConllFrom(
					spark, 
					new BiocXmlProcessor(Paths.get(TRAIN_TMVARS_XML_TRAIN)), 
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, "pipeline_bioc_train"), 
					BERT_UNCASED_MODEL, 
					BERT_NER_MODEL,
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, TMVAR_BIOC_TRAIN));
			LOG.info("PREPARE DATA SERVICE: tmVar train bioc OK");
		} catch (Exception ex) {
			LOG.warn("PREPARE DATA SERVICE: tmVar train bioc fail ex:" + ex.toString());
		}
		
		try {
			TrainRepository.getConllFrom(
					spark, 
					new PubtatorTxtProcessor(Paths.get(TRAIN_TMVARS_XML_TEST)), 
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, "pipeline_bioc_test"), 
					BERT_UNCASED_MODEL, 
					BERT_NER_MODEL,
					FilenameUtils.concat(TRAINING_NER_DIRECTORY, TMVAR_BIOC_TEST));
			LOG.info("PREPARE DATA SERVICE: tmVar test bioc OK");
		} catch (Exception ex) {
			LOG.warn("PREPARE DATA SERVICE: tmVar test bioc fail ex:" + ex.toString());
		}

	}
	
}
