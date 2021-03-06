package com.lognex.api.model.base;

import com.lognex.api.model.entity.good.Assortment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Position extends Entity {
    private int quantity;
    private double price;
    private double discount;
    private long vat;
    private Assortment assortment;
}
