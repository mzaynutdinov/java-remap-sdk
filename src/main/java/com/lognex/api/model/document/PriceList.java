package com.lognex.api.model.document;

import com.lognex.api.model.base.Operation;
import com.lognex.api.model.base.IEntityWithAttributes;
import com.lognex.api.model.entity.attribute.Attribute;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class PriceList extends Operation implements IEntityWithAttributes {

    private String priceType;
    private List<PriceListColumn> columns = new ArrayList<>();
    private Set<Attribute<?>> attributes = new HashSet<>();
    private String documents;
}

