package com.ugungor.movie.application;

import com.ugungor.movie.domain.Movie;
import com.ugungor.movie.domain.MovieNotFoundException;
import com.ugungor.movie.domain.MovieService;
import com.ugungor.movie.infrastructure.JsonTransformer;
import com.ugungor.movie.interfaces.ApiError;
import com.ugungor.movie.interfaces.Health;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.exception;
import static spark.Spark.get;

public class MovieController {

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    private static final JsonTransformer JSON_TRANSFORMER = new JsonTransformer();

    private MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    public void handleRoutes() {

        get("/", (req, res) -> {

            final List<Movie> movies = movieService.findAll();

            logger.info("{} movies found", movies.size());

            final Map<String, Object> model = new HashMap<>();
            model.put("movies", movies);

            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/homepage.vtl"));
        });

        get("/movies/:id", (req, res) -> {

            final String id = req.params(":id");

            final Movie movie = movieService.findById(Integer.parseInt(id));

            final Map<String, Object> model = new HashMap<>();
            model.put("movie", movie);

            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/movie.vtl"));
        });

        get("/health", (request, response) -> Health.create("I'am alive!!"), JSON_TRANSFORMER);
    }

    public void handleExceptions() {

        exception(Exception.class, (exception, request, response) -> {
            response.status(500);
            response.body(ApiError.create(exception.getMessage()).toJson());
        });

        exception(MovieNotFoundException.class, (exception, request, response) -> {
            response.status(404);
            response.body(ApiError.create(exception.getMessage()).toJson());
        });
    }
}
