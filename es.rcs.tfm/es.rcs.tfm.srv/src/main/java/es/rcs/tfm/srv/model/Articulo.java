package es.rcs.tfm.srv.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import es.rcs.tfm.db.model.PubArticleEntity;
import es.rcs.tfm.solr.model.PubArticleIdx;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Articulo {

	public static final String SEPARATOR = ";";
	
	public static final String PUBMED_ID_NAME = "pmid";
	public static final String PMC_ID_NAME = "pmc";
	public static final String DOI_ID_NAME = "doi";
	public static final String PII_ID_NAME = "pii";
	public static final String ISBN_ID_NAME = "isbn";
	public static final String ISSN_ID_NAME = "issn";
	public static final String NLM_ID_NAME = "nlm";
	
	public static final String FECHA_REVISION = "REVISION_DATE";
	public static final String FECHA_PUBLICACION = "PUBLICATION_DATE";
	public static final String FECHA_COMPLETA = "COMPLETE_DATE";
	public static final String FECHA_INICIO = "BEGIN_DATE";
	public static final String FECHA_CONTRIBUCION = "CONTRIBUTION_DATE";
	public static final String FECHA_EDICION = "EDITION_DATE";

	public static final String INVESTIGADOR = "investigators";
	public static final String AUTOR = "authors";
	public static final String GRUPO = "group";
	public static final String EDITOR = "editors";
	public static final String EDITORIAL = "editorGroup";

	public static final String DESCRIPTORES_MEDLINE = "MEDLINE_DESC";
	public static final String REVISTA = "REVISTA";
	public static final String LIBRO = "LIBRO";

	public static final String TITLE = "title";
	public static final String VERNACULAR_TITLE = "vernacular";
	public static final String ABSTRACT = "abstract";
	public static final String OTHER_ABSTRACT = "other";
	public static final String OBSERVATIONS = "observations";

	public static final Integer TITLE_START = 1;
	public static final Integer VERNACULAR_TITLE_START = 6;
	public static final Integer OBSERVATIONS_START = 10;
	public static final Integer ABSTRACT_START = 20;
	public static final Integer OTHER_ABSTRACT_START = 60;

	/*
	public static final String COPYRIGHT = "copyright";
	public static final String LANGUAGE = "language";
	public static final String LABEL = "label";
	public static final String CATEGORY = "category";
	public static final String CONTENT_TYPE = "contentType";
	public static final String ABSTRACT_TYPE = "abstract";
	public static final Object OTHER_TYPE = "other";

	public static final String CONTENT = "content";
	public static final String TEXT = "text";
	*/

	private String pmid = new String();
	private Titulo titulo;
	private String propietario;
	
	private String idioma;
	private String version;
	private LocalDate versionFecha;
	private String medio; // Print | Print-Electronic | Electronic | Electronic-Print | Electronic-eCollection
	private String estado; // Completed | In-Process | PubMed-not-MEDLINE | In-Data-Review | Publisher | MEDLINE | OLDMEDLINE
	private Revista revista;
	private Fasciculo fasciculo;
	private Libro libro;

	private Map<String, String> ids = new HashMap<>(); // NASA | KIE | PIP | POP | ARPL | CPC | IND | CPFH | CLML | NRCBL | NLM | QCIM
	private Map<String, Map<String, String>> properties = new HashMap<>();
	
	private List<Seccion> secciones;
	private List<Localizacion> localizaciones = new ArrayList<>();
	private List<Fecha> fechas = new ArrayList<>(); // pubmed | medline | entrez
	private List<Autor> autores = new ArrayList<>(); // 	investigators | authors | editors
	private List<Permiso> permisos = new ArrayList<>();
	private List<Referencia> referencias = new ArrayList<>();
	private List<Termino> terminos = new ArrayList<>();
	private List<Texto> textos = new ArrayList<>();
	private List<BloqueAnotado> blocks = new ArrayList<>();
	private List<Descriptor> datos = new ArrayList<>();
	private List<Descriptor> items = new ArrayList<>();
	private List<Descriptor> keywords = new ArrayList<>();
	private List<Descriptor> genes = new ArrayList<>();
	private List<Descriptor> notas = new ArrayList<>();
	private List<Descriptor> vuelos = new ArrayList<>();

	private boolean hayCambiosEnBD = false;
	private boolean hayCambiosEnIDX = false;
	
	private Fichero ficheroPmc;
	private Fichero ficheroPubmed;
	
	private PubArticleEntity entidad;
	private PubArticleIdx indice;
	private Object xml;

	public Articulo() {
		super();
	}
	
	public void addBlocks(List<BloqueAnotado> items) {
		if ((items!= null) && (!items.isEmpty())) this.blocks.addAll(items);
	}

	public void addBlock(BloqueAnotado item) {
		if (item != null) blocks.add(item);
	}

	public boolean containsBlockOfType(String type) {
		if (StringUtils.isBlank(type)) return false;
		boolean result = false;
		for (BloqueAnotado block: this.blocks) {
			if (type.equals(block.getType())) {
				result = true;
				break;
			}
		}
		return result;
	}

	public BloqueAnotado getBlocksOfType(String type) {
		if (StringUtils.isBlank(type)) return null;
		BloqueAnotado result = null;
		for (BloqueAnotado block: this.blocks) {
			if (type.equals(block.getType())) {
				result = block;
				break;
			}
		}
		return result;
	}

	public List<BloqueAnotado> generateBlocks() {
		
		List<BloqueAnotado> result = new ArrayList<>();
		if (this.getBlocks() != null) result.addAll(this.getBlocks());

		int offset = 0;
		String str = null;
		if ((this.getTextos() != null) && !this.getTextos().isEmpty()) {
			for (Texto texto: this.getTextos()) {
				String type = texto.getTipo();
				if (Articulo.TITLE.equals(type)) {
					type = BloqueAnotado.PASSAGE_TYPE_TITLE;
				} else if (Articulo.ABSTRACT.equals(type)) {
					type = BloqueAnotado.PASSAGE_TYPE_ABSTRACT;
				} else if (Articulo.OTHER_ABSTRACT.equals(type)) {
					type = BloqueAnotado.PASSAGE_TYPE_OTHER_ABSTRACT;
				}
				String text = texto.getTexto();
				if (StringUtils.isNotBlank(str)) {
					BloqueAnotado block = new BloqueAnotado();
					block.setType(type);
					block.setOffset(offset);
					block.setText(text);
					offset += text.length();
					result.add(block);
				}
			}
		}

		return result;
		
	}

	public final String generateTitle() {
		
		if (this.getTitulo() != null) {
			if (StringUtils.isNotBlank(this.getTitulo().getLibroId())) { 
				System.out.print(" L-" + this.getTitulo().getLibroId());
			}
			if (StringUtils.isNotBlank(this.getTitulo().getParteId())) { 
				System.out.print(" P-" + this.getTitulo().getParteId());
			}
			if (StringUtils.isNotBlank(this.getTitulo().getSeccionId())) { 
				System.out.print(" S-" + this.getTitulo().getSeccionId());
			}
			if (StringUtils.isNotBlank(this.getTitulo().getTitulo())) { 
				System.out.println(" T(" +  this.getPmid() + ") ]" + this.getTitulo().getTitulo() + "[");
			}
		} 
		
		String result = "";
		if (this.getTitulo() != null) result = this.getTitulo().getTitulo();
		if (StringUtils.isBlank(result)) result = "";
		
		return result;
		
	}

	public void addIds(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) this.ids.putAll(items);
		String str = this.ids.get(PUBMED_ID_NAME);
		if (StringUtils.isNotBlank(str)) this.pmid = str;
	}

	public void addIds(Entry<String, String> item) {
		if (item != null) this.ids.put(item.getKey(), item.getValue());
	}

	public void addIds(List<Entry<String, String>> items) {
		if ((items!= null) && (!items.isEmpty())) {
			items.forEach(entry -> {
				this.ids.put(entry.getKey(), entry.getValue());
			});
		}
		String str = this.ids.get(PUBMED_ID_NAME);
		if (StringUtils.isNotBlank(str)) this.pmid = str;
	}

	public void addAutores(List<Autor> items) {
		if ((items!= null) && (!items.isEmpty())) this.autores.addAll(items);
	}
	
	public void addFechas(List<Fecha> items) {
		if ((items!= null) && (!items.isEmpty())) this.fechas.addAll(items);
	}

	public void addFecha(Fecha item) {
		if ((item!= null)) this.fechas.add(item);
	}

	public void addLocalizaciones(List<Localizacion> items) {
		if ((items!= null) && (!items.isEmpty())) this.localizaciones.addAll(items);
	}

	public void addLocalizacion(Localizacion item) {
		if ((item!= null)) this.localizaciones.add(item);
	}

	public void addReferencias(List<Referencia> items) {
		if ((items!= null) && (!items.isEmpty())) this.referencias.addAll(items);
	}

	public void addSecciones(List<Seccion> items) {
		if ((items!= null) && (!items.isEmpty())) this.secciones.addAll(items);
	}

	public void addPermisos(List<Permiso> items) {
		if ((items!= null) && (!items.isEmpty())) this.permisos.addAll(items);
	}

	public void addDatos(List<Descriptor> items) {
		if ((items!= null) && (!items.isEmpty())) this.datos.addAll(items);
	}

	public void addTerminos(List<Termino> items) {
		if ((items!= null) && (!items.isEmpty())) this.terminos.addAll(items);
	}

	public void addKeywords(List<Descriptor> items) {
		if ((items!= null) && (!items.isEmpty())) this.keywords.addAll(items);
	}

	public void addItems(List<Descriptor> items) {
		if ((items!= null) && (!items.isEmpty())) this.items.addAll(items);
	}

	public void addNotas(List<Descriptor> items) {
		if ((items!= null) && (!items.isEmpty())) this.notas.addAll(items);
	}

	public void addVuelos(List<Descriptor> items) {
		if ((items!= null) && (!items.isEmpty())) this.vuelos.addAll(items);
	}

	public void addGenes(List<Descriptor> items) {
		if ((items!= null) && (!items.isEmpty())) this.genes.addAll(items);
	}

	public void addPropiedades(Map<String, Map<String, String>> items) {
		if ((items != null) && !items.isEmpty()) {
			properties.putAll(items);
		}
	}

	public void addTexto(Texto item) {
		if ((item!= null)) this.textos.add(item);
	}

	public void addTextos(List<Texto> items) {
		if ((items!= null) && (!items.isEmpty())) this.textos.addAll(items);
	}

	public void mergeRevista(Revista revista) {
		
		if (revista == null) return;
		
		if (this.getRevista() == null) {
			this.setRevista(revista);
		} else {
			Revista old = this.getRevista();
			if (StringUtils.isNotBlank(revista.getAbreviatura()))			old.setAbreviatura(revista.getAbreviatura());
			if (StringUtils.isNotBlank(revista.getMedio()))					old.setMedio(revista.getMedio());
			if (StringUtils.isNotBlank(revista.getNombre()))				old.setNombre(revista.getNombre());
			if (StringUtils.isNotBlank(revista.getPais()))					old.setPais(revista.getPais());
			if (StringUtils.isNotBlank(revista.getTipo()))					old.setTipo(revista.getTipo());
			if ((revista.getIds() != null) && !revista.getIds().isEmpty())	old.addIds(revista.getIds());
		}
		
	}

	public void mergeFasciculo(Fasciculo fasciculo) {
		if (fasciculo == null) return;
		
		if (this.getFasciculo() == null) {
			this.setFasciculo(fasciculo);
		} else {
			Fasciculo old = this.getFasciculo();
			if (fasciculo.getFecha() != null)								old.setFecha(fasciculo.getFecha());
			if (StringUtils.isNotBlank(fasciculo.getMedio()))				old.setMedio(fasciculo.getMedio());
			if (StringUtils.isNotBlank(fasciculo.getNumero()))				old.setNumero(fasciculo.getNumero());
			if (StringUtils.isNotBlank(fasciculo.getTipo()))				old.setTipo(fasciculo.getTipo());
		}
	}

}
