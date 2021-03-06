package com.lognex.api.model.base;

import com.lognex.api.model.base.field.EmbeddedCollectionRef;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class OperationWithPositions<P extends Position> extends Operation implements IOperationWithPositions<P> {

    protected List<P> positions = new ArrayList<>();
    protected EmbeddedCollectionRef positionsRef;
    private double vatSum;

}
