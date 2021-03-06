package com.lognex.api.model.entity;

import com.lognex.api.model.base.Entity;
import com.lognex.api.util.Type;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class Template extends Entity {
    private String name;
    private String type;
    private String content;
    private Type entityType;
}