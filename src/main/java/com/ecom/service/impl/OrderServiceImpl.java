package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.model.Cart;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.service.OrderService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductOrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CommonUtil commonUtil;

	// =======================
	// GUARDAR PEDIDO
	// =======================
	@Transactional
	@Override
	public void saveOrder(Integer userId, OrderRequest orderRequest) {

		List<Cart> carts = cartRepository.findByUserId(userId);

		if (carts == null || carts.isEmpty()) {
			throw new RuntimeException("No se puede procesar un pedido con el carrito vacío");
		}

		UserDtls user = carts.get(0).getUser();

		for (Cart cart : carts) {

			Product product = cart.getProduct();

			// VALIDAR STOCK
			if (product.getStock() < cart.getQuantity()) {
				throw new RuntimeException(
					"Stock insuficiente para el producto: " + product.getTitle()
				);
			}

			// DESCONTAR STOCK
			product.setStock(product.getStock() - cart.getQuantity());
			productRepository.save(product);

			// CREAR ORDEN
			ProductOrder order = new ProductOrder();
			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());
			order.setProduct(product);
			order.setPrice(product.getDiscountPrice());
			order.setQuantity(cart.getQuantity());
			order.setUser(user);
			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());

			// DIRECCIÓN
			OrderAddress address = new OrderAddress();
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setDistrict(orderRequest.getDistrict());
			address.setDepartment(orderRequest.getDepartment());
			address.setPincode(orderRequest.getPincode());

			order.setOrderAddress(address);

			ProductOrder savedOrder = orderRepository.save(order);

			// EMAIL (NO rompe la compra si falla)
			try {
				commonUtil.sendMailForProductOrder(savedOrder, "success");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// LIMPIAR CARRITO (UNA SOLA VEZ)
		cartRepository.deleteByUser(user);
	}

	// =======================
	// ACTUALIZAR ESTADO
	// =======================
	@Transactional
@Override
public ProductOrder updateOrderStatus(Integer id, String status) {

    ProductOrder order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

    // 🚫 Validación fuerte de cancelación
    if (status.equals(OrderStatus.CANCEL.getName())) {

        if (
            order.getStatus().equals(OrderStatus.PRODUCT_PACKED.getName()) ||
            order.getStatus().equals(OrderStatus.OUT_FOR_DELIVERY.getName()) ||
            order.getStatus().equals(OrderStatus.DELIVERED.getName()) ||
            order.getStatus().equals(OrderStatus.SUCCESS.getName())
        ) {
            throw new RuntimeException(
                "No se puede cancelar el pedido en el estado: " + order.getStatus()
            );
        }

        // 🔁 Reponer stock
        Product product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        productRepository.save(product);
    }

    order.setStatus(status);
    return orderRepository.save(order);
}


	// =======================
	// OTROS MÉTODOS
	// =======================
	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
		return orderRepository.findByUserId(userId);
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(pageable);
	}

	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		return orderRepository.findByOrderId(orderId);
	}
}
