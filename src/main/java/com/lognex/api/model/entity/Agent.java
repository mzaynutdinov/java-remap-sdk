package com.lognex.api.model.entity;

import com.lognex.api.model.base.EntityLegendable;
import com.lognex.api.util.ID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class Agent extends EntityLegendable {
    private String legalAddress;
    private String inn;
    private String kpp;
    private boolean archived;

    public Agent(ID id) {
        setId(id);
    }
}
