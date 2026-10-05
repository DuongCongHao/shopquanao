package com.example.huganstorev2.models.product.service;

import com.example.huganstorev2.models.product.repository.VariantRepository;
import com.example.huganstorev2.models.cart.entity.Cart;
import com.example.huganstorev2.models.cart.entity.CartItem;
import com.example.huganstorev2.models.cart.repository.CartRepository;
import com.example.huganstorev2.models.order.entity.CustomerOrder;
import com.example.huganstorev2.models.order.entity.CustomerOrderItem;
import com.example.huganstorev2.models.order.repository.CustomerOrderRepository;
import com.example.huganstorev2.models.product.controller.Dtos.ProductPopularityResponse;
import java.util.HashMap;
import java.util.Map;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.huganstorev2.models.category.entity.Category;
import com.example.huganstorev2.exception.ResourceNotFoundException;
import com.example.huganstorev2.models.category.repository.CategoryRepository;
import com.example.huganstorev2.models.product.controller.Dtos.ProductRequest;
import com.example.huganstorev2.models.product.controller.Dtos.ProductResponse;
import com.example.huganstorev2.models.product.entity.Product;
import com.example.huganstorev2.models.product.entity.ProductVariant;
import com.example.huganstorev2.models.product.repository.ProductRepository;

@Service 
public class ProductService {
    private final VariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;
    private final CustomerOrderRepository orderRepository;
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, VariantRepository variantRepository,
            CartRepository cartRepository, CustomerOrderRepository orderRepository){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }
    // Lấy danh sách sản phẩm
    public List<ProductResponse> getAllProduct(){
        return productRepository.findAll().stream()
        .map(ProductResponse::new)
        .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductPopularityResponse> getProductPopularity(){
        Map<Long, long[]> quantitiesByProduct = new HashMap<>();

        for (Cart cart : cartRepository.findAll()) {
            for (CartItem item : cart.getItems()) {
                Long productId = item.getVariant().getProduct().getId();
                quantitiesByProduct.computeIfAbsent(productId, ignored -> new long[2])[0] += item.getQuantity();
            }
        }

        for (CustomerOrder order : orderRepository.findAll()) {
            if ("CANCELLED".equalsIgnoreCase(order.getStatus())) continue;
            for (CustomerOrderItem item : order.getItems()) {
                if (item.getProductId() == null) continue;
                quantitiesByProduct.computeIfAbsent(item.getProductId(), ignored -> new long[2])[1] += item.getQuantity();
            }
        }

        List<Product> rankedProducts = productRepository.findAll().stream()
            .sorted((first, second) -> {
                long[] firstQuantities = quantitiesByProduct.getOrDefault(first.getId(), new long[2]);
                long[] secondQuantities = quantitiesByProduct.getOrDefault(second.getId(), new long[2]);
                long firstTotal = firstQuantities[0] + firstQuantities[1];
                long secondTotal = secondQuantities[0] + secondQuantities[1];
                int byTotal = Long.compare(secondTotal, firstTotal);
                if (byTotal != 0) return byTotal;
                int byOrders = Long.compare(secondQuantities[1], firstQuantities[1]);
                if (byOrders != 0) return byOrders;
                int byCarts = Long.compare(secondQuantities[0], firstQuantities[0]);
                return byCarts != 0 ? byCarts : first.getName().compareToIgnoreCase(second.getName());
            })
            .limit(10)
            .toList();

        List<ProductPopularityResponse> topProducts = new ArrayList<>(rankedProducts.size());
        for (int index = 0; index < rankedProducts.size(); index++) {
            Product product = rankedProducts.get(index);
            long[] quantities = quantitiesByProduct.getOrDefault(product.getId(), new long[2]);
            topProducts.add(new ProductPopularityResponse(
                product.getId(), quantities[0], quantities[1], quantities[0] + quantities[1], index + 1));
        }
        return topProducts;
    }
    // Lấy danh sách sản phẩm theo id
    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));
        return new ProductResponse(product);
    }
    // Tạo slug
    public String generateSlug(String name){
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);

        return normalized.replaceAll("\\p{M}", "")
        .toLowerCase().trim()
        .replaceAll("\\s+", "-");
    }
    // Tạo sản phẩm
    public ProductResponse createProduct(ProductRequest request){
        if(productRepository.existsByName(request.getName())){
            throw new ResourceNotFoundException("Sản phẩm đã tồn tại!");
        }
        String slug = generateSlug(request.getName());

        if(productRepository.existsBySlug(slug)){
            slug = slug + "-" + System.currentTimeMillis();
        }

        Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục!"));

        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImgUrl(request.getImgUrl());
        product.setImages(request.getImages());
        product.setCategory(category);
        product.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        if(request.getVariants() != null && !request.getVariants().isEmpty()){
            List<ProductVariant> variants = request.getVariants().stream()
            .map(v -> {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setSize(v.getSize());
                variant.setColor(v.getColor());
                variant.setPrice(v.getPrice() != null ? v.getPrice() : request.getPrice());
                variant.setStock(v.getStock());
                variant.setSku(v.getSku());
                variant.setImgUrl(v.getImgUrl());
                return variant;
            })
            .collect(Collectors.toList());

            variantRepository.saveAll(variants);
            savedProduct.setVariants(variants);
        }
        return new ProductResponse(savedProduct);
    }
    
    // Cập nhật
    @Transactional 
    public ProductResponse updateProduct(Long id, ProductRequest request){
        Product existingProduct = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));
        if(!existingProduct.getName().equals(request.getName()) && productRepository.existsByName(request.getName())){
            throw new RuntimeException("'Sản phẩm '" + request.getName() + "' đã tồn tại'");
        }
        if(!existingProduct.getName().equals(request.getName())){
            String newSlug = generateSlug(request.getName());
            if(productRepository.existsBySlug(newSlug)){
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }
            existingProduct.setSlug(newSlug);
        }
        if(request.getCategoryId() != null && !existingProduct.getCategory().getId().equals(request.getCategoryId())){
            Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm sản danh mục!"));
            existingProduct.setCategory(category);
        }

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setImgUrl(request.getImgUrl());
        if(request.getImages() != null){
            existingProduct.setImages(request.getImages());
        }

        if(request.getIsPublished() != null){
            existingProduct.setIsPublished(request.getIsPublished());
        }

        existingProduct.setUpdatedAt(LocalDateTime.now());

        if(request.getVariants() != null && !request.getVariants().isEmpty()){
            existingProduct.getVariants().clear();

            List<ProductVariant> newVariants = request.getVariants().stream()
            .map(v -> {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(existingProduct);
                variant.setSize(v.getSize());
                variant.setColor(v.getColor());
                variant.setPrice(v.getPrice() != null ? v.getPrice() : request.getPrice());
                variant.setStock(v.getStock());
                variant.setSku(v.getSku());
                variant.setImgUrl(v.getImgUrl());
                return variant;
            })
            .collect(Collectors.toList());

            existingProduct.getVariants().addAll(newVariants);
        }

        Product savedProduct = productRepository.save(existingProduct);
        return new ProductResponse(savedProduct);
    }

    // Xóa
    @Transactional 
    public void deleteProduct(Long id){
        Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));
        productRepository.delete(product);
    }

    // Nút bặt/tắt bán sản phẩm
    /* toggleIsPublished commingsoon*/
    /*
    ... Code block
    */
}
