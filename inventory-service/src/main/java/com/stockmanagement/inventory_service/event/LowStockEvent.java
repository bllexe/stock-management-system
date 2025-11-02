package com.stockmanagement.inventory_service.event;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LowStockEvent implements Serializable {

    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private Integer currentQuantity;
    private Integer minimumQuantity;

}

