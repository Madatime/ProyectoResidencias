package itch.tsp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import itch.tsp.security.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	public SecurityConfig(CustomUserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider =
				new DaoAuthenticationProvider(userDetailsService);

		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
			.authenticationProvider(authenticationProvider())

			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
						"/",
						"/login",
						"/residencias-publicas/**",
						"/portal-publico/**",
						"/calendario-actividades/**",
						"/css/**",
						"/js/**",
						"/img/**",
						"/assets/**",
						"/webjars/**",
						"/residentes-archivos/**",
						"/docentes-archivos/**",
						"/asesores-externos-archivos/**",
						"/proyectos-archivos/**"
				).permitAll()

				.requestMatchers(
						"/usuarios/**",
						"/perfiles/**",
						"/residentes/delete/**",
						"/residentes/inactivos/**",
						"/residentes/recuperar/**",
						"/estudiantes/delete/**",
						"/estudiantes/inactivos/**",
						"/estudiantes/recuperar/**",
						"/docentes/delete/**",
						"/docentes/inactivos/**",
						"/docentes/recuperar/**",
						"/directivos/delete/**",
						"/directivos/inactivos/**",
						"/directivos/recuperar/**",
						"/empresas/delete/**",
						"/empresas/inactivos/**",
						"/empresas/recuperar/**",
						"/asesores-internos/delete/**",
						"/asesores-internos/inactivos/**",
						"/asesores-internos/recuperar/**",
						"/asesores-externos/delete/**",
						"/asesores-externos/inactivos/**",
						"/asesores-externos/recuperar/**",
						"/residencias/delete/**",
						"/residencias/inactivos/**",
						"/residencias/recuperar/**",
						"/banco-proyectos/delete/**",
						"/banco-proyectos/inactivos/**",
						"/banco-proyectos/recuperar/**",
						"/carreras/delete/**",
						"/carreras/inactivos/**",
						"/carreras/recuperar/**"
				).hasRole("ADMINISTRADOR")

				.requestMatchers("/dashboard", "/dashboard/**")
					.authenticated()

				.requestMatchers("/carreras/**")
					.hasAnyRole("ADMINISTRADOR", "DIVISION_ESTUDIOS")

				.requestMatchers(
						"/residentes/index",
						"/residentes/search",
						"/residentes/create",
						"/residentes/save",
						"/residentes/edit/**",
						"/residentes/update"
				).hasAnyRole("ADMINISTRADOR", "DIVISION_ESTUDIOS")

				.requestMatchers(
						"/estudiantes/index",
						"/estudiantes/search",
						"/estudiantes/create",
						"/estudiantes/save",
						"/estudiantes/edit/**"
				).hasAnyRole("ADMINISTRADOR", "DIVISION_ESTUDIOS")

				.requestMatchers("/docentes/**")
					.hasAnyRole("ADMINISTRADOR", "DIVISION_ESTUDIOS")

				.requestMatchers("/directivos/**")
					.hasAnyRole("ADMINISTRADOR", "DIVISION_ESTUDIOS")

				.requestMatchers("/empresas/**")
					.hasAnyRole("ADMINISTRADOR", "DIVISION_ESTUDIOS", "VINCULACION")

				.requestMatchers(
						"/asesores-internos",
						"/asesores-internos/index",
						"/asesores-internos/search",
						"/asesores-internos/mis-proyectos",
						"/asesores-internos/proyectos/**",
						"/asesoresInternos/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"JEFE_DEPARTAMENTO",
						"ASESOR_INTERNO"
				)

				.requestMatchers(
						"/asesores-internos/create",
						"/asesores-internos/save",
						"/asesores-internos/edit/**",
						"/asesores-internos/update"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"JEFE_DEPARTAMENTO"
				)

				.requestMatchers(
						"/asesores-externos/index",
						"/asesores-externos/search",
						"/asesoresExternos/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"VINCULACION"
				)

				.requestMatchers(
						"/asesores-externos/proyectos/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"VINCULACION",
						"ASESOR_EXTERNO"
				)

				.requestMatchers(
						"/asesores-externos/create",
						"/asesores-externos/save",
						"/asesores-externos/edit/**",
						"/asesores-externos/update"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"VINCULACION"
				)

				.requestMatchers(
						"/residencias/dictamen/pdf"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"JEFE_DEPARTAMENTO"
				)
				.requestMatchers(
						"/residencias/dictamen/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"JEFE_DEPARTAMENTO"
				)

				.requestMatchers(
						"/residencias/create",
						"/residencias/save",
						"/residencias/create-desde-banco/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"ESTUDIANTE"
				)

				.requestMatchers(
						"/residencias/edit/**",
						"/residencias/update"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS"
				)

				.requestMatchers(
						"/residencias/index",
						"/residencias/searchPeriodoTexto",
						"/residencias/asesores-afines/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"JEFE_DEPARTAMENTO",
						"ASESOR_INTERNO",
						"ASESOR_EXTERNO",
						"SERVICIOS_ESCOLARES",
						"ESTUDIANTE"
				)

				.requestMatchers(
						"/banco-proyectos/create",
						"/banco-proyectos/save",
						"/banco-proyectos/edit/**",
						"/banco-proyectos/update"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"JEFE_DEPARTAMENTO"
				)

				.requestMatchers(
						"/banco-proyectos/pendientes",
						"/banco-proyectos/revisar/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"JEFE_DEPARTAMENTO"
				)

				.requestMatchers(
						"/banco-proyectos/index",
						"/banco-proyectos/search",
						"/bancoProyectos/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"JEFE_DEPARTAMENTO",
						"ESTUDIANTE"
				)

				.requestMatchers(
						"/documentos-residencia/reporte-preliminar/upload"
				).hasAnyRole(
						"ADMINISTRADOR",
						"ESTUDIANTE"
				)

				.requestMatchers(
						"/documentos-residencia/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"DIVISION_ESTUDIOS",
						"JEFE_DEPARTAMENTO",
						"ASESOR_INTERNO",
						"ASESOR_EXTERNO",
						"ESTUDIANTE",
						"SERVICIOS_ESCOLARES"
				)

				.requestMatchers(
						"/evaluaciones-residencia/externo/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"ASESOR_EXTERNO"
				)

				.requestMatchers(
						"/evaluaciones-residencia/interno/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"ASESOR_INTERNO"
				)

				.requestMatchers(
						"/evaluaciones-residencia/final/externo/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"ASESOR_EXTERNO"
				)

				.requestMatchers(
						"/evaluaciones-residencia/final/interno/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"ASESOR_INTERNO"
				)

				.requestMatchers(
						"/evaluaciones-residencia/guardar-criterios",
						"/calificaciones/**"
				).hasAnyRole(
						"ADMINISTRADOR",
						"ASESOR_INTERNO",
						"ASESOR_EXTERNO"
				)

				.requestMatchers("/cierre-expediente/**")
					.hasAnyRole(
							"ADMINISTRADOR",
							"DIVISION_ESTUDIOS",
							"SERVICIOS_ESCOLARES"
					)

				.requestMatchers("/api/estudiantes/**")
					.hasAnyRole(
							"ADMINISTRADOR",
							"DIVISION_ESTUDIOS"
					)

				.anyRequest().authenticated()
			)

			.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.defaultSuccessUrl("/dashboard", true)
				.failureUrl("/login?error")
				.permitAll()
			)

			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/?logout")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.permitAll()
			)

			.csrf(csrf -> csrf.disable());

		return http.build();
	}
}
