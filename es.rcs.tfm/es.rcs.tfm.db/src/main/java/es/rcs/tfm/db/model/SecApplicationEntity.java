package es.rcs.tfm.db.model;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.crnk.core.queryspec.pagingspec.NumberSizePagingSpec;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = true)
@JsonApiResource(
		type = SecApplicationEntity.RES_NAME,
		resourcePath = SecApplicationEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = SecApplicationEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
				name = SecApplicationEntity.DB_ID_PK, 
				columnNames = { SecApplicationEntity.DB_ID }),
			@UniqueConstraint(
					name = SecApplicationEntity.DB_UID_UK, 
					columnNames = { SecApplicationEntity.DB_UID }),
			@UniqueConstraint(
					name = SecApplicationEntity.DB_CODE_UK, 
					columnNames = { SecApplicationEntity.DB_CODE }),
			@UniqueConstraint(
					name = SecApplicationEntity.DB_APPLICATION_UK, 
					columnNames = { SecApplicationEntity.DB_APPLICATION}) })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class SecApplicationEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(SecApplicationEntity.class);

	public static final String RES_ACTION			= "applications";
	public static final String RES_NAME				= "Application";

	public static final String RES_CODE				= "code";
	public static final String RES_APPLICATION		= "application";
	public static final String RES_URL				= "url";
	public static final String RES_MODULE_IDS		= "moduleIds";
	public static final String RES_MODULES			= "modules";
	public static final String RES_AUTHORITY_IDS	= "authorityIds";
	public static final String RES_AUTHORITIES		= "authorities";
	public static final String RES_ROLE_IDS			= "roleIds";
	public static final String RES_ROLES			= "roles";

	public static final String DB_TABLE 			= "sec_application";
	public static final String DB_ID_PK 			= "sec_app_pk";
	public static final String DB_UID_UK			= "sec_app_uid_uk";
	public static final String DB_CODE_UK			= "sec_app_cod_uk";
	public static final String DB_APPLICATION_UK	= "sec_app_txt_uk";

	public static final String DB_CODE				= "app_cod";
	public static final String DB_APPLICATION		= "app_txt";
	public static final String DB_URL				= "app_url";

	public static final String ATT_MODULE_IDS		= "moduleIds";
	public static final String ATT_MODULES			= "modules";
	public static final String ATT_AUTHORITY_IDS	= "authorityIds";
	public static final String ATT_AUTHORITIES		= "authorities";
	public static final String ATT_ROLES_IDS		= "roleIds";
	public static final String ATT_ROLES			= "roles";

	
	@JsonProperty(
			value = RES_CODE,
			required = true)
	@Column(
			name = DB_CODE, 
			unique = true,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El c�digo no puede ser nulo")
	@Size(
			max = 32, 
			message = "El c�digo no puede sobrepasar los {max} caracteres.")
	private String code;

	
	@JsonProperty(
			value = RES_APPLICATION,
			required = true)
	@Column(
			name = DB_APPLICATION, 
			unique = true,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 32, 
			message = "El nombre puede sobrepasar los {max} caracteres.")
	private String name;

	
	@JsonProperty(
			value = RES_URL,
			required = true)
	@Column(
			name = DB_URL, 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "La url base no puede ser nula")
	@Size(
			max = 64, 
			message = "La url base no puede sobrepasar los {max} caracteres.")
	public String url;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_MODULE_IDS)
	@Transient
	private Set<Long> moduleIds = null;


	@JsonApiRelation(
			idField = RES_MODULE_IDS,
			mappedBy = SecModuleEntity.ATT_APPLICATION,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_MODULES,
			required = false)
	@OneToMany(
			mappedBy = SecModuleEntity.ATT_APPLICATION,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecModuleEntity> modules;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_ROLE_IDS)
	@Transient
	private Set<Long> roleIds = null;

	
	@JsonApiRelation(
			idField = RES_ROLE_IDS, 
			mappedBy = SecRoleEntity.ATT_APPLICATION,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_ROLES,
			required = false)
	@OneToMany(
			mappedBy = SecRoleEntity.ATT_APPLICATION,
			fetch = FetchType.EAGER,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecRoleEntity> roles;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_AUTHORITY_IDS)
	@Transient
	private Set<Long> authorityIds = null;

	
	@JsonApiRelation(
			idField = RES_AUTHORITY_IDS, 
			mappedBy = SecAuthorityEntity.ATT_APPLICATION,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_AUTHORITIES,
			required = false)
	@OneToMany(
			mappedBy = SecAuthorityEntity.ATT_APPLICATION,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecAuthorityEntity> authorities;

	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecApplicationEntity() {
		super();
	}

	public SecApplicationEntity(
			@NotNull(message = "El c�digo no puede ser nulo") @Size(max = 32, message = "El c�digono puede sobrepasar los {max} caracteres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 32, message = "El nombre puede sobrepasar los {max} caracteres.") String name) {
		super();
		this.code = code;
		this.name = name;
	}

	public SecApplicationEntity(
			@NotNull(message = "El c�digo no puede ser nulo") @Size(max = 32, message = "El c�digono puede sobrepasar los {max} caracteres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 32, message = "El nombre puede sobrepasar los {max} caracteres.") String name,
			@NotNull(message = "La url base no puede ser nula") @Size(max = 64, message = "La url base no puede sobrepasar los {max} caracteres.") String url) {
		super();
		this.code = code;
		this.name = name;
		this.url = url;
	}

	public void setModules(Set<SecModuleEntity> items) {
		this.modules = items;
		if (items != null) this.moduleIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setAuthorities(Set<SecAuthorityEntity> items) {
		this.authorities = items;
		if (items != null) this.authorityIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setRoles(Set<SecRoleEntity> items) {
		this.roles = items;
		if (items != null) this.roleIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

}
