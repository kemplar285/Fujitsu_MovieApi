package ee.fujitsu.movieapi.db.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.fujitsu.movieapi.db.configuration.ApiConfiguration;
import ee.fujitsu.movieapi.db.model.order.Order;
import ee.fujitsu.movieapi.db.model.statistics.OrderStatistics;
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
    private File movieDataFile;
    private File statsDataFile;
    private List<Order> orders;
    private OrderStatistics statistics;

    @Autowired
    public void setApiConfiguration(ApiConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
    }

    @PostConstruct
    public void initializeFields() throws IOException {
        this.movieDataFile = new File(
                apiConfiguration.getFilePath() +
                        apiConfiguration.getOrderFileName() +
                        apiConfiguration.getFileExtension()
        );
        this.statsDataFile = new File(
                apiConfiguration.getFilePath() +
                        apiConfiguration.getOrderStatsFileName() +
                        apiConfiguration.getFileExtension()
        );
        this.orders = findAllFromFile();
        this.statistics = readStatisticsFromFile();
    }

    /**
     * Reads orders from file
     * @return orders from file
     * @throws IOException if unable to read
     */
    @Override
    public List<Order> findAllFromFile() throws IOException {
        Order[] objects = mapper.readValue(movieDataFile, Order[].class);
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
            new ObjectMapper().findAndRegisterModules().writeValue(movieDataFile, orders);
        } else if (apiConfiguration.getFileExtension().equals(".yaml")) {
            mapper.writeValue(movieDataFile, orders);
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
                .findFirst().orElseThrow(() -> new NotFoundException("Order not found"));
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

    /**
     * Deletes an order with provided id
     * @param id orderId
     * @throws NotFoundException
     * @throws IOException
     * @throws NullPointerException
     */
    public void delete(String id) throws NotFoundException, IOException, NullPointerException {
        orders.stream().filter(order -> order.getOrderId().equals(id))
                .findAny().orElseThrow(NotFoundException::new);
        orders.removeIf(order -> order.getOrderId().equals(id));
        saveToFile();
    }


    /**
     * Saves statistics to the db file.
     * The code here is duplicated from the top, but I haven't found a way to make it work in both cases
     * @param orderStatistics Statistics to save
     * @throws IOException Unable to write
     */
    public void recordStatistics(OrderStatistics orderStatistics) throws IOException {
        this.statistics = orderStatistics;
        if (apiConfiguration.getFileExtension().equals(".json")) {
            new ObjectMapper().findAndRegisterModules().writeValue(statsDataFile, statistics);
        } else if (apiConfiguration.getFileExtension().equals(".yaml")) {
            mapper.writeValue(statsDataFile, statsDataFile);
        }
    }

    /**
     * Returns statistics
     * @return statistics
     */
    public OrderStatistics getStatistics(){
        return statistics;
    }

    /**
     * Reads stats from file
     * @return Statistics array
     * @throws IOException Unable to read
     */
    private OrderStatistics readStatisticsFromFile() throws IOException {
        this.statistics = mapper.readValue(statsDataFile, OrderStatistics.class);
        if(this.statistics == null){
            this.statistics = new OrderStatistics();
            recordStatistics(this.statistics);
        }
        return statistics;
    }

    /**
     * Deletes from stats and saves
     * @param movieId movie id in stats
     */
    public void deleteFromStats(String movieId) throws IOException {
        getStatistics().removeFromRentedFor(movieId);
        getStatistics().removeFromOrderCount(movieId);
        recordStatistics(statistics);
    }
}
