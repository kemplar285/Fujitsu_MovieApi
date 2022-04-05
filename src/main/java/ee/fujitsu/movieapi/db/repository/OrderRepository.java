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

    /**
     * Reads orders from file
     * @return orders from file
     * @throws IOException if unable to read
     */
    @Override
    public List<Order> findAllFromFile() throws IOException {
        Order[] objects = mapper.readValue(dataFile, Order[].class);
        List<Order> orders = new ArrayList<>(List.of(objects));
        orders.forEach(movie -> {
        });
        return orders;
    }

    /**
     * Returns array of orders
     * @return array of orders
     */
    @Override
    public List<Order> findAll() {
        return orders;
    }

    /**
     * Saves order to file
     * @throws IOException If unable to write
     */
    @Override
    public void saveToFile() throws IOException {
        if (apiConfiguration.getFileExtension().equals(".json")) {
            new ObjectMapper().findAndRegisterModules().writeValue(dataFile, orders);
        } else if (apiConfiguration.getFileExtension().equals(".yaml")) {
            mapper.writeValue(dataFile, orders);
        }
    }

    /**
     * Finds order by its orderId
     * @param id OrderId
     * @return First found order
     * @throws NotFoundException if order is not found
     */
    @Override
    public Order findById(String id) throws NotFoundException {
        return orders.stream().filter(order -> String.valueOf(order.getOrderId()).equals(id))
                .findFirst().orElseThrow(NotFoundException::new);
    }

    /**
     * Adds and saves orders
     * @param order order so add
     * @return  Saved order
     * @throws IOException unable to write into file
     */
    public Order add(Order order) throws IOException {
        orders.add(order);
        saveToFile();
        return order;
    }


    /**
     * Updates order data
     * @param order order to update
     * @throws NotFoundException if order is not found
     */
    public void update(Order order) throws NotFoundException {
        orders.stream().filter(ord -> ord.getOrderId().equals(order.getOrderId()))
                .findAny().orElseThrow(NotFoundException::new);
        orders.replaceAll(ord -> ord.getOrderId().equals(ord.getOrderId()) ? order : ord);
    }

    public void delete(String id) throws NotFoundException, IOException, NullPointerException {
        orders.stream().filter(order -> order.getOrderId().equals(id))
                .findAny().orElseThrow(NotFoundException::new);
        orders.removeIf(order -> order.getOrderId().equals(id));
        saveToFile();

    }
}
