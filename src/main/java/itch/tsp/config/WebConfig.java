package itch.tsp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${app.ruta.base}")
	private String rutaBase;

	@Value("${app.carpeta.residentes}")
	private String carpetaResidentes;

	@Value("${app.carpeta.asesores.internos}")
	private String carpetaAsesoresInternos;

	@Value("${app.carpeta.asesores.externos}")
	private String carpetaAsesoresExternos;

	@Value("${app.carpeta.proyectos}")
	private String carpetaProyectos;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		registry.addResourceHandler("/residentes-archivos/**")
				.addResourceLocations("file:/" + rutaBase + carpetaResidentes + "/");

		registry.addResourceHandler("/asesores-internos-archivos/**")
				.addResourceLocations("file:/" + rutaBase + carpetaAsesoresInternos + "/");

		registry.addResourceHandler("/asesores-externos-archivos/**")
				.addResourceLocations("file:/" + rutaBase + carpetaAsesoresExternos + "/");

		registry.addResourceHandler("/proyectos-archivos/**")
				.addResourceLocations("file:/" + rutaBase + carpetaProyectos + "/");
	}
}