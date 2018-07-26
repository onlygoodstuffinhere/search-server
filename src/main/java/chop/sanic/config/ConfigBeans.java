package chop.sanic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import chop.sanic.model.ScoredDocument;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;

@Configuration
public class ConfigBeans {

	@Bean
	public HierarchicalNameMapper hierarchicalNameMapper () {
		return new HierarchicalNameMapper() {

			@Override
			public String toHierarchicalName(Id id, NamingConvention convention) {
				return "sanicdev."+HierarchicalNameMapper.DEFAULT.toHierarchicalName(id, convention);
			}
			
		};
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule sm = new SimpleModule();
		sm.addSerializer(new ScoredDocumentSerializer(ScoredDocument.class, mapper));
		mapper.registerModule(sm);
		return mapper;
	}

}
