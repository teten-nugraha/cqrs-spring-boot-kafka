package id.ten.example.cqrspattern.service.impl;

import id.ten.example.cqrspattern.entity.Product;
import id.ten.example.cqrspattern.entity.PurchaseOrder;
import id.ten.example.cqrspattern.entity.User;
import id.ten.example.cqrspattern.repository.ProductRepository;
import id.ten.example.cqrspattern.repository.PurchaseOrderRepository;
import id.ten.example.cqrspattern.repository.UserRepository;
import id.ten.example.cqrspattern.service.OrderCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class OrderCommandServiceImpl implements OrderCommandService {

    private static final long ORDER_CANCELLATION_WINDOW = 30;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    private List<User> users;
    private List<Product> products;

    @PostConstruct
    private void init(){
        this.users = this.userRepository.findAll();
        this.products = this.productRepository.findAll();
    }

    @Override
    public void createOrder(int userIndex, int productIndex) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setProductId(this.products.get(productIndex).getId());
        purchaseOrder.setUserId(this.users.get(userIndex).getId());
        this.purchaseOrderRepository.save(purchaseOrder);
    }

    @Override
    public void cancelOrder(long orderId) {
        this.purchaseOrderRepository.findById(orderId)
                .ifPresent(purchaseOrder -> {
                    LocalDate orderDate = LocalDate.ofInstant(purchaseOrder.getOrderDate().toInstant(), ZoneId.systemDefault());
                    if(Duration.between(orderDate, LocalDate.now()).toDays() <= ORDER_CANCELLATION_WINDOW){
                        this.purchaseOrderRepository.deleteById(orderId);
                        //additional logic to issue refund
                    }
                });
    }
}
