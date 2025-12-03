package com.project.service.impl;

import com.project.exception.ProductException;
import com.project.model.Category;
import com.project.model.Product;
import com.project.model.Seller;
import com.project.repository.CategoryRepository;
import com.project.repository.ProductRepository;
import com.project.request.CreateProductRequest;
import com.project.service.ProductService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Product createProduct(CreateProductRequest request, Seller seller) {
        Category category1 = categoryRepository.findByCategoryId(request.getCategory1());
        if (category1 == null) {
            Category category = new Category();
            category.setCategoryId(request.getCategory1());
            category.setLevel(1);
            category1 = categoryRepository.save(category);
        }

        Category category2 = categoryRepository.findByCategoryId(request.getCategory2());
        if (category2 == null) {
            Category category = new Category();
            category.setCategoryId(request.getCategory2());
            category.setParentCategory(category1);
            category.setLevel(2);
            category2 = categoryRepository.save(category);
        }

        Category category3 = categoryRepository.findByCategoryId(request.getCategory3());
        if (category3 == null) {
            Category category = new Category();
            category.setCategoryId(request.getCategory3());
            category.setParentCategory(category2);
            category.setLevel(3);
            category3 = categoryRepository.save(category);
        }

        int discountPercentage = calculateDiscountPercentage(request.getMrpPrice(), request.getSellingPrice());

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category3);
        product.setDescription(request.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setTitle(request.getTitle());
        product.setColor(request.getColor());
        product.setSellingPrice(request.getSellingPrice());
        product.setMrpPrice(request.getMrpPrice());
        product.setImages(request.getImages());
        product.setSizes(request.getSize());
        product.setDiscountPercentage(discountPercentage);

        return productRepository.save(product);
    }

    private int calculateDiscountPercentage(double mrpPrice, double sellingPrice) {
        if (mrpPrice <= 0) {
            throw new IllegalArgumentException("Actual price must be greater than zero");
        }
        double discount = mrpPrice - sellingPrice;
        return (int) (discount / mrpPrice) * 100;
    }

    @Override
    public void deleteProduct(Long productId) throws ProductException {
        Product product = this.findProductById(productId);
        productRepository.delete(product);
    }

    @Override
    public Product updateProduct(Long productId, Product product) throws ProductException {
        this.findProductById(productId);
        product.setId(productId);
        return productRepository.save(product);
    }

    @Override
    public Product findProductById(Long productId) throws ProductException {
        return productRepository.findById(productId).orElseThrow(() ->
                new ProductException("Product not found with id: " + productId));
    }

    @Override
    public List<Product> searchProducts(String query) {
        return productRepository.searchProduct(query);
    }

    @Override
    public Page<Product> getAllProducts(
            String category, String brand, String colors, String sizes, Integer minPrice, Integer maxPrice,
            Integer minDiscount,String sort, String stock, Integer pageNumber
    ) {
        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null) {
                Join<Product, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("categoryId"), category));
            }

            if (colors != null && !colors.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("color"), colors));
            }

            if (sizes != null && !sizes.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("size"), sizes));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }

            if (minDiscount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountPercentage"), minDiscount));
            }

            if (stock != null && !stock.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("stock"), stock));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            pageable = switch (sort) {
                case "price_low" -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
                        Sort.by("sellingPrice").ascending());
                case "price_high" -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
                        Sort.by("sellingPrice").descending());
                default -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10, Sort.unsorted());
            };
        } else {
            pageable = PageRequest.of(pageNumber != null ? pageNumber : 0, 10, Sort.unsorted());
        }
        return productRepository.findAll(specification, pageable);
    }

    @Override
    public List<Product> getProductsBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
}
