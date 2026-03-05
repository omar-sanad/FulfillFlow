package com.fulfillflow.inventory.controller;

import com.fulfillflow.inventory.domain.InventoryService;
import com.fulfillflow.inventory.model.CreateProductRequest;
import com.fulfillflow.inventory.model.ProductResponse;
import com.fulfillflow.inventory.model.RestockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalogue and stock management")
public class ProductController {

    private final InventoryService inventoryService;

    public ProductController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('administrator', 'warehouse')")
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = inventoryService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List products (paginated)")
    public Page<ProductResponse> listProducts(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly,
            Pageable pageable) {
        return inventoryService.listProducts(activeOnly, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    public ProductResponse getProduct(@PathVariable UUID id) {
        return inventoryService.getProduct(id);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get a product by SKU")
    public ProductResponse getProductBySku(@PathVariable String sku) {
        return inventoryService.getProductBySku(sku);
    }

    @PostMapping("/{id}/restock")
    @PreAuthorize("hasAnyRole('administrator', 'warehouse')")
    @Operation(summary = "Restock a product")
    public ProductResponse restock(@PathVariable UUID id, @Valid @RequestBody RestockRequest request) {
        return inventoryService.restock(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('administrator', 'warehouse')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate a product")
    public void deactivate(@PathVariable UUID id) {
        inventoryService.deactivate(id);
    }
}
