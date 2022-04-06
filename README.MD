# Fujitsu Java Programming Exercise

# 0. Prerequisites
 - Java 17
 - Maven 4.0.0

# 1. Running the application
 ## Run from command line (JSON database) - Default
      mvn spring-boot:run
  ## Run from command line (YAML database)
      mvn spring-boot:run -Dspring-boot.run.arguments="--db.fileExtension=.yaml"

 
# 2. Available endpoints
## Documentation is available at:
- /swagger-ui/index.html#/
- /v3/api-docs
## Movies
    /movies  // Get all movies
    /movies/add // Add a movie
    /movies/{category} // Get movies by category
    /movies/id/{movieId} // Get movies by imdbId. Also fetches data from omdbApi.
    /movies/delete?id={movieId} // Delete movie by imdbId
    /movies/update?id={movieId} // Update movie by imdbId
## Orders
    /orders // Get all orders
    /orders/{orderId} // Get order by orderId
    /orders/extend/{orderId} // Add new item to the order
    /orders/delete?id={orderId} // Delete order with provided orderId
    /orders/checkout?orderId={orderId} // Close order and checkout
