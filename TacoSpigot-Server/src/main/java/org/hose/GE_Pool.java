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
import net.minecraft.server.EntityLiving;
import net.minecraft.server.IEntitySelector;
import net.minecraft.server.World;


/**
 *
 * @author softpak
 */
public class GE_Pool extends RecursiveTask<List> {
    EntityLiving el;
    
    public GE_Pool(EntityLiving el){
        this.el = el;
    }
    
    @Override
    protected List compute() {
        return el.world.getEntities(el, el.getBoundingBox(), IEntitySelector.a(el));
    }
}
