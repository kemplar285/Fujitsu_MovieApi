package ee.fujitsu.movieapi.db.repository;

import java.util.List;

public interface IRepository<T>{
    List<T> findAllFromFile() throws Exception;
    List<T> findAll() throws Exception;
    void saveToFile() throws Exception;
    T findById(String id) throws Exception;
}
