package com.example.learnspringbatch.service.redis;

import com.example.learnspringbatch.entity.redis.CustomerRedis;
import com.example.learnspringbatch.repository.redis.CustomerRepositoryRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerServiceRedis {
    private final CustomerRepositoryRedis customerRepositoryRedis;

    private static Long id = 0L;

    public void save(CustomerRedis customer) {
        customerRepositoryRedis.save(customer);
    }

    public void createAndSave() {
        CustomerRedis customer = new CustomerRedis();
        customer.setId(id++);
        customer.setFirstName("Mark");
        customer.setLastName("Feathers");
        customerRepositoryRedis.save(customer);

    }

    public CustomerRedis findById(Long id) {
        return customerRepositoryRedis.findById(id).orElse(null);
    }

    public List<CustomerRedis> findAll() {
        return (List<CustomerRedis>) customerRepositoryRedis.findAll();
    }
}
