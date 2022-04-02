package ee.fujitsu.movieapi.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.fujitsu.movieapi.configuration.ApiConfiguration;
import ee.fujitsu.movieapi.model.movie.Movie;
import ee.fujitsu.movieapi.model.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepository implements IRepository<Order>{
    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
    private ApiConfiguration apiConfiguration;
    private File dataFile;
    private List<Order> orders;

    @Autowired
    public void setApiConfiguration(ApiConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
    }

    @PostConstruct
    public void initializeFields() throws IOException {
        this.dataFile = new File(
                apiConfiguration.getFilePath() +
                        apiConfiguration.getOrderFileName() +
                        apiConfiguration.getFileExtension()
        );
        this.orders = findAllFromFile();
    }

    @Override
    public List<Order> findAllFromFile() throws IOException {
        Order[] objects = mapper.readValue(dataFile, Order[].class);
        List<Order> orders = new ArrayList<>(List.of(objects));
        orders.forEach(movie -> {
            movie.setTotalPrice();
        });
        return orders;
    }

    @Override
    public List<Order> findAll() {
        return orders;
    }

    @Override
    public void saveToFile() {

    }

    @Override
    public Order findById(String id) {
        return null;
    }
}
