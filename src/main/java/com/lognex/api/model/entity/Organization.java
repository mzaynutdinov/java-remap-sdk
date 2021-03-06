package com.lognex.api.model.entity;

import com.lognex.api.util.ID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Organization extends Agent {
    public Organization(ID id) {
        super(id);
    }
}
