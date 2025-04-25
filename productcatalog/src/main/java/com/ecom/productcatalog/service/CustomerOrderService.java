package com.ecom.productcatalog.service;

import com.ecom.productcatalog.model.CustomerOrder;
import com.ecom.productcatalog.repository.CustomerOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;

    public CustomerOrderService(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    public List<CustomerOrder> getAllOrders() {
        return customerOrderRepository.findAll();
    }

    public CustomerOrder saveOrder(CustomerOrder order) {
        return customerOrderRepository.save(order);
    }

    public CustomerOrder getOrderById(Long id) {
        return customerOrderRepository.findById(id).orElse(null);
    }

    public void deleteOrder(Long id) {
        customerOrderRepository.deleteById(id);
    }
}
