package itch.tsp.config;

import java.io.File;

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
				.addResourceLocations(resolverUbicacionArchivo(carpetaResidentes));

		registry.addResourceHandler("/asesores-internos-archivos/**")
				.addResourceLocations(resolverUbicacionArchivo(carpetaAsesoresInternos));

		registry.addResourceHandler("/asesores-externos-archivos/**")
				.addResourceLocations(resolverUbicacionArchivo(carpetaAsesoresExternos));

		registry.addResourceHandler("/proyectos-archivos/**")
				.addResourceLocations(resolverUbicacionArchivo(carpetaProyectos));
	}

	private String resolverUbicacionArchivo(String carpeta) {
		File directorioBase = new File(rutaBase);
		File directorio = new File(directorioBase, carpeta);
		return directorio.getAbsoluteFile().toURI().toString();
	}
}
