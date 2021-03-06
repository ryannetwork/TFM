package es.rcs.tfm.main.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.setup.SparkSessionFactory;
import es.rcs.tfm.nlp.NlpNames;
import es.rcs.tfm.srv.SrvNames;

@Configuration(
		AppNames.SPARK_CONFIG )
@PropertySource( 
		{"classpath:/META-INF/spark.properties"} )
public class SparkConfig {

	@Value("${hadoop.home}") private String hadoopHome;
	@Value("${spark.home}") private String sparkHome;
	@Value("${spark.master}") private String sparkMaster;
	@Value("${spark.driver.cores}") private String sparkCores;
	@Value("${spark.driver.memory}") private String sparkDriverMemory;
	@Value("${spark.executor.instances}") private String sparkExcutorInstances;
	@Value("${spark.executor.cores}") private String sparkExcutorCores;
	@Value("${spark.executor.memory}") private String sparkExcutorMemory;
	@Value("${spark.ui.enabled}") private String sparkUiEnabled;
	@Value("${spark.ui.port}") private String sparkUiPort;

    @Bean( name = NlpNames.SPARK_SESSION_CONFIG )
    public SparkConf getSparkConf() {
    	
		Path hadoop = Paths.get(hadoopHome);
		if (	hadoop.toFile().exists() &&
				hadoop.toFile().isDirectory()) {
			System.setProperty("hadoop.home.dir", hadoop.toFile().getAbsolutePath());
		}
		
		//https://spark.apache.org/docs/latest/configuration.html#application-properties
        SparkConf bean = new SparkConf().
        		setAppName(AppNames.SPARK_APPNAME).
        		setMaster(sparkMaster). // Locahost with 2 threads
        		//set("fs.file.impl", LocalFileSystem.class.getName()).
        		set("spark.rpc.askTimeout", "480").
        		set("spark.driver.cores", sparkCores).
        		set("spark.driver.memory", sparkDriverMemory).
        		set("spark.driver.maxResultSize", "4G").
        		set("spark.executor.instances", sparkExcutorInstances).
        		set("spark.executor.cores", sparkExcutorCores).
        		set("spark.executor.memory", sparkExcutorMemory).
        		set("spark.ui.enabled", sparkUiEnabled).
        		set("spark.ui.port", sparkUiPort).
        		set("spark.local.dir", sparkHome + "/tmp").
        		set("spark.kryoserializer.buffer.max", "500m").
        		set("spark.jars.packages", "JohnSnowLabs:spark-nlp:2.5.2");
        return bean;
    }

	@Bean( name = NlpNames.SPARK_SESSION_FACTORY )
	@DependsOn( value = NlpNames.SPARK_SESSION_CONFIG )
    public SparkSessionFactory getSparkSessionFactory() {
        
		SparkSessionFactory factory = new SparkSessionFactory(getSparkConf());
        return factory;
        
    }
	
	@Bean( name = SrvNames.SPARK_SESSION_TRAIN )
	@DependsOn( value = NlpNames.SPARK_SESSION_FACTORY )
	public SparkSession getSparkTrainSession() {
	    
	    SparkSession spark = null;
		try {
			spark = getSparkSessionFactory().getObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    return spark;
	    
	}
	
    @Bean( name = AppNames.SPARK_SESSION_TEST )
    @DependsOn( value = NlpNames.SPARK_SESSION_FACTORY )
    public SparkSession getSparkSession() {
        
        SparkSession spark = null;
		try {
			spark = getSparkSessionFactory().getObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        return spark;
        
    }

}
