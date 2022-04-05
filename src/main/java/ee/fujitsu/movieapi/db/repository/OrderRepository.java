package ee.fujitsu.movieapi.db.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.fujitsu.movieapi.db.configuration.ApiConfiguration;
import ee.fujitsu.movieapi.db.model.order.Order;
import ee.fujitsu.movieapi.rest.api.exception.general.NotFoundException;
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
        });
        return orders;
    }

    @Override
    public List<Order> findAll() {
        return orders;
    }

    @Override
    public void saveToFile() throws IOException {
        if (apiConfiguration.getFileExtension().equals(".json")) {
            new ObjectMapper().findAndRegisterModules().writeValue(dataFile, orders);
        } else if (apiConfiguration.getFileExtension().equals(".yaml")) {
            mapper.writeValue(dataFile, orders);
        }
    }

    @Override
    public Order findById(String id) throws NotFoundException {
        return orders.stream().filter(order -> String.valueOf(order.getOrderId()).equals(id))
                .findFirst().orElseThrow(NotFoundException::new);
    }

    public Order add(Order order) throws IOException {
        orders.add(order);
        saveToFile();
        return order;
    }
}
