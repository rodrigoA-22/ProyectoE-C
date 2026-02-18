package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;


	@GetMapping("/")
	public String home() {
		return "user/home";
	}

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {
		Cart saveCart = cartService.saveCart(pid, uid);

		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Error al añadir el producto al carrito");
		} else {
			session.setAttribute("succMsg", "Producto añadido al carrito");
		}
		return "redirect:/product/" + pid;
	}

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {

		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}

	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}


	//Este funcia :v
	/* 
	@GetMapping("/orders")
	public String orderPage(Principal p, Model m, HttpSession session) {
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());

		// VALIDACIÓN 
		if (carts == null || carts.isEmpty()) {
			session.setAttribute("errorMsg", "Tu carrito está vacío. Agrega productos antes de pagar.");
			return "redirect:/user/cart";
		}

		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice() + 250 + 100;
			m.addAttribute("orderPrice", orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/order";
	}
	*/
	@GetMapping("/orders")
	public String orderPage(Principal p, Model m, HttpSession session) {

		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());

		// 1️⃣ Carrito vacío
		if (carts == null || carts.isEmpty()) {
			session.setAttribute("errorMsg",
				"Tu carrito está vacío. Agrega productos antes de pagar.");
			return "redirect:/user/cart";
		}

		// 2️⃣ Validar stock por producto
		for (Cart cart : carts) {
			if (cart.getQuantity() > cart.getProduct().getStock()) {
				session.setAttribute("errorMsg",
					"Has superado el stock disponible del producto: "
					+ cart.getProduct().getTitle()
					);
				return "redirect:/user/cart";
			}
		}

		// Todo OK → mostrar pago
		m.addAttribute("carts", carts);

		Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
		Double totalOrderPrice = orderPrice + 250 + 100;

		m.addAttribute("orderPrice", orderPrice);
		m.addAttribute("totalOrderPrice", totalOrderPrice);

		return "/user/order";
	}



	/* 
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p) throws Exception {
		// System.out.println(request);
		UserDtls user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), request);

		return "redirect:/user/success";
	}
	*/
	
	//Nuevo con validacion para no madar pedidos fantasmas
	/* 
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request,
							Principal p,
							HttpSession session) throws Exception {

		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());

		if (carts == null || carts.isEmpty()) {
			session.setAttribute("errorMsg", "No puedes confirmar un pedido con el carrito vacío");
			return "redirect:/user/cart";
		}

		orderService.saveOrder(user.getId(), request);
		return "redirect:/user/success";
	}
	*/
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request,
							Principal p,
							HttpSession session) {

		try {
			UserDtls user = getLoggedInUserDetails(p);
			List<Cart> carts = cartService.getCartsByUser(user.getId());

			if (carts == null || carts.isEmpty()) {
				session.setAttribute("errorMsg", "No puedes confirmar un pedido con el carrito vacío");
				return "redirect:/user/cart";
			}

			orderService.saveOrder(user.getId(), request);
			return "redirect:/user/success";

		} catch (RuntimeException e) {
			// 👉 AQUÍ atrapamos el error de stock
			session.setAttribute("errorMsg", e.getMessage());
			return "redirect:/user/cart";
		}
	}


	@GetMapping("/success")
	public String loadSuccess() {
		return "/user/success";
	}

	@GetMapping("/user-orders")
	public String myOrder(Model m, Principal p) {
		UserDtls loginUser = getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
		m.addAttribute("orders", orders);
		return "/user/my_orders";
	}

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Estado actualizado");
		} else {
			session.setAttribute("errorMsg", "Estado no actualizado");
		}
		return "redirect:/user/user-orders";
	}

	@GetMapping("/profile")
	public String profile() {
		return "/user/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Perfil no actualizado");
		} else {
			session.setAttribute("succMsg", "Perfil actualizado");
		}
		return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "¡Contraseña no actualizada! Error en el servidor.");
			} else {
				session.setAttribute("succMsg", "Contraseña actualizada exitosamente");
			}
		} else {
			session.setAttribute("errorMsg", "Contraseña actual incorrecta");
		}

		return "redirect:/user/profile";
	}

}
