package com.fulfillflow.inventory.domain;

import com.fulfillflow.common.error.ConflictException;
import com.fulfillflow.common.error.NotFoundException;
import com.fulfillflow.inventory.model.CreateProductRequest;
import com.fulfillflow.inventory.model.ProductResponse;
import com.fulfillflow.inventory.model.RestockRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service orchestrating product and stock-level operations.
 */
@Service
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockReservationRepository reservationRepository;

    public InventoryService(ProductRepository productRepository,
                            StockLevelRepository stockLevelRepository,
                            StockReservationRepository reservationRepository) {
        this.productRepository = productRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.reservationRepository = reservationRepository;
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new ConflictException("SKU_EXISTS", "Product with SKU " + request.sku() + " already exists");
        }
        Product product = new Product(
                request.sku(), request.name(), request.description(),
                request.priceCents(), request.currency(), request.weightGrams());
        product = productRepository.save(product);

        StockLevel stock = new StockLevel(product, 0);
        stockLevelRepository.save(stock);

        return toResponse(product, stock);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(Boolean activeOnly, Pageable pageable) {
        return productRepository.findAll((root, query, cb) -> {
            if (Boolean.TRUE.equals(activeOnly)) {
                return cb.isTrue(root.get("active"));
            }
            return cb.conjunction();
        }, pageable).map(p -> {
            StockLevel sl = stockLevelRepository.findById(p.getId()).orElse(null);
            return toResponse(p, sl);
        });
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product " + id + " not found"));
        StockLevel stock = stockLevelRepository.findById(id).orElse(null);
        return toResponse(product, stock);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product with SKU " + sku + " not found"));
        StockLevel stock = stockLevelRepository.findById(product.getId()).orElse(null);
        return toResponse(product, stock);
    }

    public ProductResponse restock(UUID productId, RestockRequest request) {
        StockLevel stock = stockLevelRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("STOCK_NOT_FOUND",
                        "Stock level for product " + productId + " not found"));
        stock.restock(request.quantity());
        return toResponse(stock.getProduct(), stockLevelRepository.save(stock));
    }

    public void deactivate(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product " + productId + " not found"));
        product.setActive(Boolean.FALSE);
        productRepository.save(product);
    }

    private ProductResponse toResponse(Product product, StockLevel stock) {
        return new ProductResponse(
                product.getId(), product.getSku(), product.getName(), product.getDescription(),
                product.getPriceCents(), product.getCurrency(), product.getWeightGrams(),
                product.getActive(), product.getVersion(),
                product.getCreatedAt(), product.getUpdatedAt(),
                stock != null ? stock.getAvailableQuantity() : 0,
                stock != null ? stock.getReservedQuantity() : 0);
    }
}
