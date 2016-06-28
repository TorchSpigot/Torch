/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hose;

import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import net.minecraft.server.Entity;
import net.minecraft.server.World;


/**
 *
 * @author softpak
 */
public class TE_Pool extends RecursiveTask<List> {
    Entity entity;
    double d0, d1, d2;
    
    public TE_Pool(Entity entity, double d0, double d1, double d2){
        this.entity = entity;
        this.d0 = d0;
        this.d1 = d1;
        this.d2 = d2;
    }
    
    @Override
    protected List compute() {
        return entity.world.getCubes(entity, entity.getBoundingBox().a(d0, d1, d2));
    }
}
