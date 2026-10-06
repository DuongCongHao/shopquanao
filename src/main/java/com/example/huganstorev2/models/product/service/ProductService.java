package com.example.huganstorev2.models.product.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import com.example.huganstorev2.exception.ResourceNotFoundException;

import com.example.huganstorev2.models.cart.entity.Cart;
import com.example.huganstorev2.models.cart.entity.CartItem;
import com.example.huganstorev2.models.cart.repository.CartRepository;

import com.example.huganstorev2.models.category.entity.Category;
import com.example.huganstorev2.models.category.repository.CategoryRepository;

import com.example.huganstorev2.models.order.entity.CustomerOrder;
import com.example.huganstorev2.models.order.entity.CustomerOrderItem;
import com.example.huganstorev2.models.order.repository.CustomerOrderRepository;

import com.example.huganstorev2.models.product.controller.Dtos.ProductPopularityResponse;
import com.example.huganstorev2.models.product.controller.Dtos.ProductRequest;
import com.example.huganstorev2.models.product.controller.Dtos.ProductResponse;

import com.example.huganstorev2.models.product.entity.Product;
import com.example.huganstorev2.models.product.entity.ProductVariant;

import com.example.huganstorev2.models.product.repository.ProductRepository;
import com.example.huganstorev2.models.product.repository.VariantRepository;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ProductService {

    private final VariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;
    private final CustomerOrderRepository orderRepository;
    private final Cloudinary cloudinary;


    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            VariantRepository variantRepository,
            CartRepository cartRepository,
            CustomerOrderRepository orderRepository,
            Cloudinary cloudinary) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.cloudinary = cloudinary;
    }


    // =========================================================
    // LẤY DANH SÁCH SẢN PHẨM
    // =========================================================

    public List<ProductResponse> getAllProduct() {

        return productRepository.findAll()
                .stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }


    // =========================================================
    // THỐNG KÊ ĐỘ PHỔ BIẾN SẢN PHẨM
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProductPopularityResponse> getProductPopularity() {

        Map<Long, long[]> quantitiesByProduct = new HashMap<>();


        // Số lượng sản phẩm đang nằm trong giỏ hàng
        for (Cart cart : cartRepository.findAll()) {

            for (CartItem item : cart.getItems()) {

                if (item.getVariant() == null
                        || item.getVariant().getProduct() == null) {
                    continue;
                }

                Long productId =
                        item.getVariant().getProduct().getId();

                quantitiesByProduct
                        .computeIfAbsent(
                                productId,
                                ignored -> new long[2]
                        )[0] += item.getQuantity();
            }
        }


        // Số lượng sản phẩm đã được đặt hàng
        for (CustomerOrder order : orderRepository.findAll()) {

            if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
                continue;
            }

            for (CustomerOrderItem item : order.getItems()) {

                if (item.getProductId() == null) {
                    continue;
                }

                quantitiesByProduct
                        .computeIfAbsent(
                                item.getProductId(),
                                ignored -> new long[2]
                        )[1] += item.getQuantity();
            }
        }


        List<Product> rankedProducts =
                productRepository.findAll()
                        .stream()
                        .sorted((first, second) -> {

                            long[] firstQuantities =
                                    quantitiesByProduct.getOrDefault(
                                            first.getId(),
                                            new long[2]
                                    );

                            long[] secondQuantities =
                                    quantitiesByProduct.getOrDefault(
                                            second.getId(),
                                            new long[2]
                                    );


                            long firstTotal =
                                    firstQuantities[0]
                                            + firstQuantities[1];

                            long secondTotal =
                                    secondQuantities[0]
                                            + secondQuantities[1];


                            int byTotal =
                                    Long.compare(
                                            secondTotal,
                                            firstTotal
                                    );

                            if (byTotal != 0) {
                                return byTotal;
                            }


                            int byOrders =
                                    Long.compare(
                                            secondQuantities[1],
                                            firstQuantities[1]
                                    );

                            if (byOrders != 0) {
                                return byOrders;
                            }


                            int byCarts =
                                    Long.compare(
                                            secondQuantities[0],
                                            firstQuantities[0]
                                    );

                            return byCarts != 0
                                    ? byCarts
                                    : first.getName()
                                        .compareToIgnoreCase(
                                                second.getName()
                                        );
                        })
                        .limit(10)
                        .toList();


        List<ProductPopularityResponse> topProducts =
                new ArrayList<>(rankedProducts.size());


        for (int index = 0;
             index < rankedProducts.size();
             index++) {

            Product product = rankedProducts.get(index);

            long[] quantities =
                    quantitiesByProduct.getOrDefault(
                            product.getId(),
                            new long[2]
                    );

            topProducts.add(
                    new ProductPopularityResponse(
                            product.getId(),
                            quantities[0],
                            quantities[1],
                            quantities[0] + quantities[1],
                            index + 1
                    )
            );
        }


        return topProducts;
    }


    // =========================================================
    // LẤY SẢN PHẨM THEO ID
    // =========================================================

    public ProductResponse getProductById(Long id) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy sản phẩm!"
                                )
                        );

        return new ProductResponse(product);
    }


    // =========================================================
    // TẠO SLUG
    // =========================================================

    public String generateSlug(String name) {

        String normalized =
                Normalizer.normalize(
                        name,
                        Normalizer.Form.NFD
                );

        return normalized
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-");
    }


    // =========================================================
    // TẠO SẢN PHẨM
    // =========================================================

    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsByName(request.getName())) {

            throw new ResourceNotFoundException(
                    "Sản phẩm đã tồn tại!"
            );
        }


        String slug =
                generateSlug(request.getName());


        if (productRepository.existsBySlug(slug)) {

            slug =
                    slug
                            + "-"
                            + System.currentTimeMillis();
        }


        List<Category> categories = resolveCategories(request);


        Product product = new Product();

        product.setName(request.getName());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImgUrl(request.getImgUrl());
        product.setImages(request.getImages());
        product.setCategories(categories);
        product.setCategory(categories.get(0));

        product.setIsPublished(
                request.getIsPublished() != null
                        ? request.getIsPublished()
                        : true
        );

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());


        Product savedProduct =
                productRepository.save(product);


        if (request.getVariants() != null
                && !request.getVariants().isEmpty()) {

            List<ProductVariant> variants =
                    request.getVariants()
                            .stream()
                            .map(v -> {

                                ProductVariant variant =
                                        new ProductVariant();

                                variant.setProduct(savedProduct);
                                variant.setSize(v.getSize());
                                variant.setColor(v.getColor());

                                variant.setPrice(
                                        v.getPrice() != null
                                                ? v.getPrice()
                                                : request.getPrice()
                                );

                                variant.setStock(ProductVariant.UNLIMITED_STOCK);
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


    // =========================================================
    // CẬP NHẬT SẢN PHẨM
    // =========================================================

    @Transactional
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request) {

        Product existingProduct =
                productRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy sản phẩm!"
                                )
                        );


        if (!existingProduct
                .getName()
                .equals(request.getName())
                && productRepository
                    .existsByName(request.getName())) {

            throw new RuntimeException(
                    "'Sản phẩm '"
                            + request.getName()
                            + "' đã tồn tại'"
            );
        }


        if (!existingProduct
                .getName()
                .equals(request.getName())) {

            String newSlug =
                    generateSlug(request.getName());


            if (productRepository.existsBySlug(newSlug)) {

                newSlug =
                        newSlug
                                + "-"
                                + System.currentTimeMillis();
            }


            existingProduct.setSlug(newSlug);
        }


        if (request.getCategoryIds() != null || request.getCategoryId() != null) {
            List<Category> categories = resolveCategories(request);
            existingProduct.setCategories(categories);
            existingProduct.setCategory(categories.get(0));
        }


        existingProduct.setName(request.getName());
        existingProduct.setDescription(
                request.getDescription()
        );
        existingProduct.setPrice(request.getPrice());
        existingProduct.setImgUrl(request.getImgUrl());


        if (request.getImages() != null) {

            existingProduct.setImages(
                    request.getImages()
            );
        }


        if (request.getIsPublished() != null) {

            existingProduct.setIsPublished(
                    request.getIsPublished()
            );
        }


        existingProduct.setUpdatedAt(
                LocalDateTime.now()
        );


        if (request.getVariants() != null
                && !request.getVariants().isEmpty()) {

            existingProduct
                    .getVariants()
                    .clear();


            List<ProductVariant> newVariants =
                    request.getVariants()
                            .stream()
                            .map(v -> {

                                ProductVariant variant =
                                        new ProductVariant();

                                variant.setProduct(
                                        existingProduct
                                );

                                variant.setSize(
                                        v.getSize()
                                );

                                variant.setColor(
                                        v.getColor()
                                );

                                variant.setPrice(
                                        v.getPrice() != null
                                                ? v.getPrice()
                                                : request.getPrice()
                                );

                                variant.setStock(ProductVariant.UNLIMITED_STOCK);

                                variant.setSku(
                                        v.getSku()
                                );

                                variant.setImgUrl(
                                        v.getImgUrl()
                                );

                                return variant;
                            })
                            .collect(
                                    Collectors.toList()
                            );


            existingProduct
                    .getVariants()
                    .addAll(newVariants);
        }


        Product savedProduct =
                productRepository.save(existingProduct);


        return new ProductResponse(savedProduct);
    }


    private List<Category> resolveCategories(ProductRequest request) {
        List<Long> categoryIds = request.getCategoryIds();
        if (categoryIds == null) {
            categoryIds = request.getCategoryId() == null
                    ? List.of()
                    : List.of(request.getCategoryId());
        }

        if (categoryIds.isEmpty()) {
            throw new ResourceNotFoundException("Vui lòng chọn ít nhất một danh mục!");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(categoryIds);
        List<Category> categories = new ArrayList<>();
        for (Long categoryId : uniqueIds) {
            if (categoryId == null) {
                throw new ResourceNotFoundException("Danh mục không hợp lệ!");
            }
            categories.add(categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục!")));
        }
        return categories;
    }


    // =========================================================
    // XÓA SẢN PHẨM
    // =========================================================

    @Transactional
    public void deleteProduct(Long id) {

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy sản phẩm!"
                                )
                        );


        /*
         * Lưu lại tất cả URL ảnh trước khi Product bị xóa.
         *
         * HashSet giúp tránh xóa cùng một URL nhiều lần.
         */
        Set<String> imageUrls = new HashSet<>();


        // Ảnh chính
        addImageUrl(
                imageUrls,
                product.getImgUrl()
        );


        // Danh sách ảnh phụ
        if (product.getImages() != null) {

            for (String imageUrl :
                    product.getImages()) {

                addImageUrl(
                        imageUrls,
                        imageUrl
                );
            }
        }


        // Ảnh của các variant
        if (product.getVariants() != null) {

            for (ProductVariant variant :
                    product.getVariants()) {

                addImageUrl(
                        imageUrls,
                        variant.getImgUrl()
                );
            }
        }


        /*
         * CartItem đang tham chiếu ProductVariant.
         *
         * Phải xóa CartItem chứa variant của sản phẩm
         * trước khi xóa Product.
         */
        List<Cart> carts =
                cartRepository.findAll();


        for (Cart cart : carts) {

            cart.getItems().removeIf(
                    item ->
                            item.getVariant() != null
                            && item.getVariant()
                                .getProduct() != null
                            && id.equals(
                                    item.getVariant()
                                        .getProduct()
                                        .getId()
                            )
            );
        }


        /*
         * Cart.items có:
         *
         * cascade = ALL
         * orphanRemoval = true
         *
         * nên các CartItem vừa remove sẽ bị DELETE.
         */
        cartRepository.flush();


        /*
         * Product.variants có:
         *
         * cascade = ALL
         * orphanRemoval = true
         *
         * nên Hibernate sẽ tự xóa variants.
         *
         * product_images là ElementCollection nên
         * Hibernate cũng xóa các record liên quan.
         */
        productRepository.delete(product);


        /*
         * Ép Hibernate thực hiện DELETE ngay.
         *
         * Nếu DB có lỗi foreign key thì lỗi sẽ xảy ra
         * tại đây, trước khi chúng ta đụng tới Cloudinary.
         */
        productRepository.flush();


        /*
         * DB đã xóa thành công.
         *
         * Bây giờ mới xóa file thật trên Cloudinary.
         */
        for (String imageUrl : imageUrls) {

            try {

                deleteImageFromCloudinary(
                        imageUrl
                );

            } catch (Exception e) {

                /*
                 * Nếu một ảnh Cloudinary xóa lỗi,
                 * không làm hỏng việc xóa Product.
                 *
                 * Ảnh đó có thể được dọn thủ công sau.
                 */
                System.err.println(
                        "Không thể xóa ảnh Cloudinary: "
                                + imageUrl
                                + " | "
                                + e.getMessage()
                );
            }
        }
    }


    // =========================================================
    // THÊM URL ẢNH VÀO DANH SÁCH CẦN XÓA
    // =========================================================

    private void addImageUrl(
            Set<String> imageUrls,
            String imageUrl) {

        if (imageUrl == null) {
            return;
        }

        String cleanedUrl =
                imageUrl.trim();


        if (cleanedUrl.isEmpty()) {
            return;
        }


        imageUrls.add(cleanedUrl);
    }


    // =========================================================
    // XÓA ẢNH TRÊN CLOUDINARY
    // =========================================================

    private void deleteImageFromCloudinary(
            String imageUrl) throws Exception {

        String publicId =
                extractCloudinaryPublicId(
                        imageUrl
                );


        /*
         * URL không phải Cloudinary hoặc không lấy
         * được public_id thì bỏ qua.
         */
        if (publicId == null
                || publicId.isBlank()) {

            return;
        }


        Map<?, ?> result =
                cloudinary
                        .uploader()
                        .destroy(
                                publicId,
                                ObjectUtils.asMap(
                                        "resource_type",
                                        "image",
                                        "invalidate",
                                        true
                                )
                        );


        System.out.println(
                "Cloudinary delete: "
                        + publicId
                        + " -> "
                        + result.get("result")
        );
    }


    // =========================================================
    // LẤY PUBLIC_ID TỪ CLOUDINARY URL
    // =========================================================

    private String extractCloudinaryPublicId(
            String imageUrl) {

        if (imageUrl == null
                || imageUrl.isBlank()) {

            return null;
        }


        /*
         * Chỉ xử lý URL Cloudinary.
         *
         * Nếu sau này sản phẩm sử dụng URL ảnh từ nơi khác,
         * code sẽ không cố xóa URL đó.
         */
        if (!imageUrl.contains(
                "res.cloudinary.com")) {

            return null;
        }


        try {

            int uploadIndex =
                    imageUrl.indexOf(
                            "/upload/"
                    );


            if (uploadIndex == -1) {
                return null;
            }


            /*
             * Ví dụ:
             *
             * https://res.cloudinary.com/xxx/image/upload/
             * v123456/hugan_uploads/abc.jpg
             *
             * Sau bước này:
             *
             * v123456/hugan_uploads/abc.jpg
             */
            String path =
                    imageUrl.substring(
                            uploadIndex
                                    + "/upload/"
                                    .length()
                    );


            /*
             * Bỏ query string nếu URL có dạng:
             *
             * abc.jpg?something=value
             */
            int queryIndex =
                    path.indexOf("?");


            if (queryIndex != -1) {

                path =
                        path.substring(
                                0,
                                queryIndex
                        );
            }


            /*
             * Bỏ fragment nếu có.
             */
            int fragmentIndex =
                    path.indexOf("#");


            if (fragmentIndex != -1) {

                path =
                        path.substring(
                                0,
                                fragmentIndex
                        );
            }


            /*
             * Bỏ Cloudinary version:
             *
             * v123456789/hugan_uploads/abc.jpg
             *
             * thành:
             *
             * hugan_uploads/abc.jpg
             */
            if (path.matches(
                    "^v\\d+/.*")) {

                path =
                        path.substring(
                                path.indexOf("/")
                                        + 1
                        );
            }


            /*
             * Bỏ phần mở rộng:
             *
             * hugan_uploads/abc.jpg
             *
             * thành:
             *
             * hugan_uploads/abc
             */
            int lastSlash =
                    path.lastIndexOf("/");

            int lastDot =
                    path.lastIndexOf(".");


            if (lastDot > lastSlash) {

                path =
                        path.substring(
                                0,
                                lastDot
                        );
            }


            return path;


        } catch (Exception e) {

            System.err.println(
                    "Không lấy được public_id từ URL: "
                            + imageUrl
            );

            return null;
        }
    }


    // =========================================================
    // NÚT BẬT / TẮT BÁN SẢN PHẨM
    // =========================================================

    /*
     * toggleIsPublished coming soon
     */
}