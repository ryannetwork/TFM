package es.rcs.tfm.srv.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.rcs.tfm.nlp.service.ConllWritter;
import es.rcs.tfm.nlp.service.NerPrepare;
import es.rcs.tfm.nlp.service.NerTrain;
import es.rcs.tfm.srv.model.MarkedText;
import es.rcs.tfm.srv.setup.MarkedTextProcessor;

public class TrainRepository {
	
	private static final Logger LOG = LoggerFactory.getLogger(TrainRepository.class);
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-hhmmss");
	private static final String HADOOP_FILE_PREFIX = "file:///";
	
	/**
	 * Construye un dataset a partir de los datos de entrenamiento en el modelo intermedio
	 * @param list lista de beans del modelo con los documentos de entrenamiento
	 * @return lista de filas con los documentos de entrenamiento
	 */
	public static final List<Row> generateDS(List<MarkedText> list) {
		
		if ((list == null) || (list.isEmpty())) return null;

		List<Row> result = new ArrayList<>();
		try {
		
			list.forEach(doc -> {
				doc.blocks.forEach((e, b) -> {
					List<String> notes = null;
					if ((b.notes != null) && (!b.notes.isEmpty())) {
						notes = b.notes.values().stream().flatMap(n -> n.pos.stream().map(p -> 
							String.format("[%d, %d, %s, %s]", p.offset, p.offset + p.length - 1, n.text, n.type)
						)).collect(Collectors.toList());
					}
					if (notes == null) notes = new ArrayList<String>();
					result.add(RowFactory.create(doc.pmid, b.type, b.text, notes));
				});
			});
			
		} catch (Exception ex) {
			LOG.warn("generateDS FAIL " + ex.toString());
		}
		return result;
		
	}

	/**
	 * Genera una estructura compatible para Spark del conjunto de entrenamiento
	 * @return La estructura para el Dataset
	 */
	public static final StructType  generateStructType() {
		
	    StructField[] structFields = new StructField[]{
	            new StructField("id", DataTypes.StringType, true, Metadata.empty()),
	            new StructField("type", DataTypes.StringType, true, Metadata.empty()),
	            new StructField("text", DataTypes.StringType, true, Metadata.empty()),
	            new StructField("notes", DataTypes.createArrayType(DataTypes.StringType, true), true, Metadata.empty())
	    };

	    return new StructType(structFields);

	}
	
	public static final boolean trainFromConll(
			SparkSession spark,
			String trainFilename,
			String testFilename,
			String resultsFilename,
			String resultsDirectory,
			String modelDirectory,
			String targetModelDirectory) {

		if (spark == null) return false;
		if (StringUtils.isBlank(resultsDirectory)) return false;
		if (StringUtils.isBlank(resultsFilename)) return false;
		if (StringUtils.isBlank(targetModelDirectory)) return false;
		if (StringUtils.isBlank(modelDirectory)) return false;
		if (StringUtils.isBlank(trainFilename)) return false;
		if (StringUtils.isBlank(testFilename)) return false;

		Path results = Paths.get(resultsDirectory);
		if (results.toFile() == null) return false;
		if (!results.toFile().exists()) return false;
		if (!results.toFile().isDirectory()) return false;

		//Path target = Paths.get(targetModelDirectory);

		Path model = Paths.get(modelDirectory);
		if (model.toFile() == null) return false;
		if (!model.toFile().exists()) return false;
		if (!model.toFile().isDirectory()) return false;

		Path train = Paths.get(trainFilename);
		if (train.toFile() == null) return false;
		if (!train.toFile().exists()) return false;
		if (!train.toFile().isFile()) return false;

		Path test = Paths.get(testFilename);
		if (test.toFile() == null) return false;
		if (!test.toFile().exists()) return false;
		if (!test.toFile().isFile()) return false;
		
		
		boolean resultado = true;
		try {
			
			String ini = SIMPLE_DATE_FORMAT.format(new Date());
			
			NerTrain nerTrainer = new NerTrain(
					spark.sparkContext(),
					spark,
					modelDirectory);
			
			nerTrainer.
				saveNerModel (
					nerTrainer.
						measureNerTraining(
							trainFilename,
							testFilename,
							resultsFilename,
							resultsDirectory),
					modelDirectory);
			
			LOG.info(
					"\r\nTRAIN TIME for [" + trainFilename + "] "  +
					"\r\n\tINI:" + ini +
 					"\r\n\tEND:" + SIMPLE_DATE_FORMAT.format(new Date()) );

		} catch (Exception ex) {
			resultado = false;
			LOG.warn("TRAIN FAIL " + ex.toString());
		}

		return resultado;
		
	}

	/**
	 * Construye un fichero conll en un directorio parquet con el conjunto de documentos de entrenamiento
	 * aplicandoles la localización de entidades del modelo 
	 * @param spark Sesion de Spark donde se ejecutará la preparación de datos
	 * @param processor Generador de anotaciones
	 * @param resultsDirectory Directorio donde se deja el modelo con los resultados
	 * @param bertModelDirectory Directorio con el modelo utilizado para el marcado de palabras
	 * @param bertNerModelDirectory Directorio con el modelo NER utilizado para la localización de entidades genéricas
	 * @param targetFilename Directorio parquet de salida de la preparación de datos
	 * @return
	 */
	public static final boolean getConllFrom(
			SparkSession spark,
			MarkedTextProcessor processor,
			String resultsDirectory,
			String bertModelDirectory,
			String bertNerModelDirectory,
			String targetFilename) {

		if (spark == null) return false;
		if (processor == null) return false;
		if (StringUtils.isBlank(resultsDirectory)) return false;
		if (StringUtils.isBlank(bertModelDirectory)) return false;
		if (StringUtils.isBlank(bertNerModelDirectory)) return false;
		if (StringUtils.isBlank(targetFilename)) return false;

		Path bertModel = Paths.get(bertModelDirectory);
		if (bertModel.toFile() == null) return false;
		if (!bertModel.toFile().exists()) return false;
		if (!bertModel.toFile().isDirectory()) return false;

		Path nerBertModel = Paths.get(bertModelDirectory);
		if (nerBertModel.toFile() == null) return false;
		if (!nerBertModel.toFile().exists()) return false;
		if (!nerBertModel.toFile().isDirectory()) return false;

		Path target = Paths.get(targetFilename);
		if (target.toFile() == null) return false;
		//if (!target.toFile().exists()) return false;
		//if (!target.toFile().isFile()) return false;
		
		boolean resultado = true;
		try {

			String ini = SIMPLE_DATE_FORMAT.format(new Date());
			
			Stream<MarkedText> stream = StreamSupport.stream(
					Spliterators.spliteratorUnknownSize(
							processor, 
							Spliterator.DISTINCT), 
					false);
			//TODO 
			List<MarkedText> data = stream.collect(Collectors.toList());
			//List<MarkedText> data = stream.limit(2).collect(Collectors.toList());
			
			if ((data == null) || (data.size() == 0)) {
				
				resultado = false;
				
			} else {

				List<Row> rows = TrainRepository.generateDS(data);
				StructType structType = TrainRepository.generateStructType();
				
				String build = SIMPLE_DATE_FORMAT.format(new Date());

				NerPrepare generator = new NerPrepare(
						spark.sparkContext(),
						spark);

				String prepare = SIMPLE_DATE_FORMAT.format(new Date());

				ConllWritter writter = new ConllWritter(spark);
				double tasa = writter.
					exportConllFiles(
						generator.
							execute(rows, structType, HADOOP_FILE_PREFIX + FilenameUtils.separatorsToUnix(resultsDirectory)),
						targetFilename);

				LOG.info(
						"\r\nCONLL2003 TIME for [" + rows.size() + "] documents. PRECISSION = " + tasa * 100 +
						"\r\n\tINI:" + ini +
						"\r\n\tDAT:" + build +
						"\r\n\tPRE:" + prepare +
						"\r\n\tEND:" + SIMPLE_DATE_FORMAT.format(new Date()) );

			}
		} catch (Exception ex) {
			resultado = false;
			LOG.warn("CONLL2003 FAIL " + ex.toString());
		}

		return resultado;
		
	}
	    
}
