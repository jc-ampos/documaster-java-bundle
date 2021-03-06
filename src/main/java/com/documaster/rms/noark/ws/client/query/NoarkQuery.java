package com.documaster.rms.noark.ws.client.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.documaster.rms.noark.ws.client.Bean;
import com.documaster.rms.noark.ws.noarkentities.NoarkEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NoarkQuery<TEntity extends NoarkEntity> implements Bean {

	private final int limit;
	private final String query;
	private final String type;
	private final List<SortOrder> sortOrder = new ArrayList<>();
	private int offset;
	private boolean publicUse = true;

	@JsonIgnore
	private final List<QueryParam> parameters = new ArrayList<>();

	@JsonIgnore
	private final List<Join> joins = new ArrayList<>();

	public NoarkQuery(Class<TEntity> entityClass, int limit) {

		this(entityClass, null, limit);
	}

	public NoarkQuery(Class<TEntity> entityClass, String query, int limit) {

		this.limit = limit;
		this.query = query;
		this.type = entityClass.getSimpleName();
	}

	public int getLimit() {

		return limit;
	}

	public String getQuery() {

		return query;
	}

	public int getOffset() {

		return offset;
	}

	public void setOffset(int offset) {

		this.offset = offset;
	}

	public String getType() {

		return type;
	}

	public List<SortOrder> getSortOrder() {

		return sortOrder;
	}

	public List<QueryParam> getParameters() {

		return parameters;
	}

	@JsonProperty("parameters")
	public Map<String, Object> getParametersDict() {

		if (parameters == null) {

			return null;
		}

		Map<String, Object> parametersDict = new LinkedHashMap<>();
		parameters.stream().forEach(p -> parametersDict.put(p.getParamName(), p.getParamValue()));

		return parametersDict;
	}

	public List<Join> getJoins() {

		return joins;
	}

	@JsonProperty("joins")
	public Map<String, String> getJoinsDict() {

		if (joins == null) {

			return null;
		}

		Map<String, String> joinsDict = new LinkedHashMap<>();
		joins.stream().forEach(join -> joinsDict.put(join.getAlias(), join.getReference()));

		return joinsDict;
	}

	public boolean getPublicUse() {

		return publicUse;
	}

	public void setPublicUse(boolean publicUse) {

		this.publicUse = publicUse;
	}
}
